package com.kenzan.henge.repository.impl.flatfile.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.kenzan.henge.exception.HengeIOException;
import com.kenzan.henge.exception.HengeValidationException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


/**
 * S3 implementation of the FileStorageService
 *
 * @author wmatsushita
 */
@Profile("flatfile_s3")
@Component
public class S3FileStorageService implements FileStorageService {

    private AmazonS3 amazonS3;
    
    private static final String PREFIX_GLUE = "/";
    
    @Value("${repository.bucket.name}")
    private String bucketName;
    
    @Value("${text.encoding}")
    private String textEncoding;
    

    @Autowired
    public S3FileStorageService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }
    
    @Override
    public void write(final String path, final String fileName, final String text, boolean overwrite) {

        writeBytes(path, fileName, text.getBytes(Charset.forName(textEncoding)), overwrite);

    }

    @Override
    public void writeBytes(final String path, final String fileName, byte[] data, boolean overwrite) {

        final String fileKey = getFileKey(path, fileName);
        
        if(overwrite || !amazonS3.doesObjectExist(bucketName, fileKey)) {
            try {
                InputStream inputStream = new ByteArrayInputStream(data);
                
                final ObjectMetadata objectMetaData = new ObjectMetadata();
                objectMetaData.setContentLength(data.length);
                
                final PutObjectRequest putReq = new PutObjectRequest(bucketName, fileKey, inputStream, objectMetaData);
        
                amazonS3.putObject(putReq);
            } catch(Exception e) {
                throw new HengeIOException(Status.INTERNAL_SERVER_ERROR, 
                    "An IO error occured while trying to write the file", e);
            }
        } else {
            throw new HengeValidationException(Status.CONFLICT,
                "The file cannot be written because it already exists.");
        }
        
    }

    @Override
    public Optional<String> read(final String path, final String fileName) {

        final Optional<byte[]> bytes = readBytes(path, fileName);
        
        return bytes.isPresent()? Optional.of(new String(bytes.get(), Charset.forName(textEncoding))) : Optional.empty();
        
    }

    @Override
    public Optional<byte[]> readBytes(final String path, final String fileName) {

        final String fileKey = getFileKey(path, fileName);
        
        try {
            if(! amazonS3.doesObjectExist(bucketName, fileKey)) {
                return Optional.empty();
            }
            
            final S3Object s3Object = amazonS3.getObject(bucketName, fileKey);
            
            try (InputStream in = s3Object.getObjectContent()){
                
                return Optional.of(IOUtils.toByteArray(in));
                
            } 
        } catch (Exception e) {
            throw new HengeIOException(Status.INTERNAL_SERVER_ERROR, 
                "An IO error occured while trying to read the file", e);
            
        }
        
    }

    @Override
    public boolean delete(final String path, final String fileName) {
        
        final String fileKey = getFileKey(path, fileName);
        
        try {
            boolean result = amazonS3.doesObjectExist(bucketName, fileKey);
            
            amazonS3.deleteObject(bucketName, fileKey);
                
            return result;
        } catch (Exception e) {
            throw new HengeIOException(Status.INTERNAL_SERVER_ERROR, 
                "An IO error occured while trying to delete the file", e);            
        }
    }

    @Override
    public List<String> deleteBeginningWith(final String path, final String nameStart) {

        final List<String> result = new ArrayList<>();
        
        Optional<Set<String>> fileNames = getFileNamesStartingWith(path, nameStart);
        if(fileNames.isPresent()) {
            fileNames.get().stream().forEach(fileName -> {
                delete(path, fileName);
                result.add(fileName);
            });
        }
        
        return result;
    }

    @Override
    public boolean exists(final String path, final String fileName) {

        final String fileKey = getFileKey(path, fileName);
        
        try {
            return amazonS3.doesObjectExist(bucketName, fileKey);
        } catch (Exception e) {
            throw new HengeIOException(Status.INTERNAL_SERVER_ERROR, 
                "An IO error occured while trying to check if a file exists", e);                        
        }
        
    }

    @Override
    public boolean existsBeginningWith(final String path, final String nameStart) {

        final String fileKeyStart = getFileKey(path, nameStart);
        
        try {
            final ObjectListing objectListing = amazonS3.listObjects(bucketName, fileKeyStart);
            
            return !objectListing.getObjectSummaries().isEmpty();
        } catch (Exception e) {
            throw new HengeIOException(Status.INTERNAL_SERVER_ERROR, 
                "An IO error occured while trying to check if a file exists", e);                        
        }
        
    }

    @Override
    public Optional<Set<String>> getFileNamesStartingWith(final String path, final String nameStart) {

        final String fileKeyStart = getFileKey(path, nameStart);

        try {
            final ObjectListing objectListing = amazonS3.listObjects(bucketName, fileKeyStart);

            if (objectListing.getObjectSummaries().isEmpty()
                || (objectListing.getObjectSummaries().size() == 1 && objectListing.getObjectSummaries().get(0)
                    .getKey().equals(path + PREFIX_GLUE))) {
                return Optional.empty();
            }

            Predicate<S3ObjectSummary> onlyFilesNoFolders =
                summary -> !summary.getKey().trim().equals(path + PREFIX_GLUE);
            Function<S3ObjectSummary, String> fromSummaryToKeyWithoutFolder =
                summary -> summary.getKey().replaceAll(path + PREFIX_GLUE, StringUtils.EMPTY);
                
            return Optional.ofNullable(objectListing.getObjectSummaries().stream().filter(onlyFilesNoFolders)
                .map(fromSummaryToKeyWithoutFolder).collect(Collectors.toSet()));
        } catch (Exception e) {
            throw new HengeIOException(Status.INTERNAL_SERVER_ERROR,
                "An IO error occured while trying to list files", e);
        }

    }
    
    private String getFileKey(final String path, final String fileName) {
        
        return StringUtils.isNotBlank(path) ? new StringBuilder(path).append(PREFIX_GLUE).append(fileName).toString() : fileName;
        
    }

	@Override
	public void write(String path, String fileName, String text, boolean overwrite, boolean lock) {
		write(path, fileName, text, overwrite);
		
	}

}
