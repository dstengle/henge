package com.kenzan.henge.repository.impl.flatfile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kenzan.henge.domain.model.FileVersion;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.utils.JsonUtils;
import com.kenzan.henge.exception.HengeIOException;
import com.kenzan.henge.exception.HengeValidationException;
import com.kenzan.henge.exception.RuntimeHengeException;
import com.kenzan.henge.repository.FileVersionRepository;
import com.kenzan.henge.repository.impl.flatfile.storage.FileStorageService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 *
 * @author wmatusushita
 * @author Igor K. Shiohara
 */
@Component
@Profile({ "flatfile_local", "flatfile_s3" })
public class FileVersionFlatFileRepository extends BaseFlatFileRepository<FileVersion> implements FileVersionRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileVersionFlatFileRepository.class);

    private static final Class<FileVersion> GENERIC_TYPE = FileVersion.class;

    public static final String DATA_SUB_FOLDER_NAME = "data";

    @Autowired
    public FileVersionFlatFileRepository(FileStorageService fileStorageService, FileNamingService fileNamingService,
        JsonUtils jsonUtils) {

        super(fileStorageService, fileNamingService, jsonUtils);

    }

    @Override
    protected Class<FileVersion> getGenericType() {

        return GENERIC_TYPE;
    }

    @Override
    public FileVersion create(final FileVersion entity) {

        checkContentHasData(entity);

        // By creating the model object first we benefit from the validation
        // checks that it makes, which are the sameones that need to be done for
        // the data object.
        final FileVersion created = super.create(entity);
        final String dataPath = fileNamingService.getPath(GENERIC_TYPE, DATA_SUB_FOLDER_NAME);
        final String fileName = fileNamingService.getCompleteFileName(entity.getName(), entity.getVersion());

        try {

            final byte[] data = entity.getContent();
            fileStorageService.writeBytes(dataPath, fileName, data, false);

            return created;
        } catch (RuntimeHengeException e) {
            throw e;
        } catch (Exception e) {
            // erase model file that was created
            final String modelPath = fileNamingService.getPath(GENERIC_TYPE);
            fileStorageService.delete(modelPath, fileName);

            throw new HengeIOException(Status.INTERNAL_SERVER_ERROR,
                "Problem trying to create entity [" + getGenericType().getSimpleName()
                + "], fileName [" + fileName + "]", e);
        }

    }

    @Override
    public Optional<FileVersion> update(final String name, final FileVersion entity) {

        checkContentHasData(entity);

        final Optional<FileVersion> updated = super.update(name, entity);
        if(!updated.isPresent()) {
            return Optional.empty();
        }

        final String dataPath = fileNamingService.getPath(GENERIC_TYPE, DATA_SUB_FOLDER_NAME);
        final String fileName = fileNamingService.getCompleteFileName(entity.getName(), entity.getVersion());

        try {

            final byte[] data = entity.getContent();
            fileStorageService.writeBytes(dataPath, fileName, data, false);

            return updated;
        } catch (RuntimeHengeException e) {
            throw e;
        } catch (Exception e) {
            // erase model file that was created
            final String modelPath = fileNamingService.getPath(GENERIC_TYPE);
            fileStorageService.delete(modelPath, fileName);

            throw new HengeIOException(Status.INTERNAL_SERVER_ERROR,
                "Problem trying to update entity [" + getGenericType().getSimpleName()
                + "] by the name [" + name + "]", e);
        }

    }

    @Override
    public Optional<FileVersion> delete(final String name) {

        final String modelPath = fileNamingService.getPath(getGenericType());
        final String dataPath = fileNamingService.getPath(getGenericType(), DATA_SUB_FOLDER_NAME);
        
        final Map<String, byte[]> erasedModels = new HashMap<>();
        final Map<String, byte[]> erasedData = new HashMap<>();

        // deleteValidation(name, StringUtils.EMPTY);
        
        final Optional<Set<String>> allModelFileNames = fileStorageService.getFileNamesStartingWith(modelPath, name);
        if (!allModelFileNames.isPresent()) {
            return Optional.empty();
        }

        // reads the current entity as return value before erasing all of the versions.
        FileVersion currentEntity = read(name).get();

        try {

            for(String fileName : allModelFileNames.get()) {

                final byte[] modelData = fileStorageService.readBytes(modelPath, fileName).get();
                fileStorageService.delete(modelPath, fileName);
                
                erasedModels.put(fileName, modelData);
            
            }
        
            final Optional<Set<String>> allDataFileNames = fileStorageService.getFileNamesStartingWith(dataPath, name);
            
            for(String fileName : allDataFileNames.get()) {
            
                final byte[] data = fileStorageService.readBytes(dataPath, fileName).get();
                LOGGER.info("Will Delete {} from {}.", fileName, dataPath);
                fileStorageService.delete(dataPath, fileName);
                
                erasedData.put(fileName, data);
                        
            }
        } catch (Exception e) {
            LOGGER.error("Problem trying to delete one or more {} versions by the name [{}]. Will rollback and rewrite the ones that were deleted.", getGenericType().getSimpleName(), name);
            
            rollbackDeletions(erasedModels, modelPath);
            rollbackDeletions(erasedData, dataPath);
            
            throw new HengeIOException(Status.INTERNAL_SERVER_ERROR,
                "Problem trying to delete one or more " + getGenericType().getSimpleName()
                    + " versions by the name [" + name + "]", e);
        }

        return Optional.ofNullable(currentEntity);

    }

    @Override
    public Optional<FileVersion> delete(final String name, final String version) {

        // deleteValidation(name, version);
        
        Optional<FileVersion> deleted = super.delete(name, version);
        if(!deleted.isPresent()) {
            return Optional.empty();
        }
        
        try {
            
            final String dataPath = fileNamingService.getPath(getGenericType(), DATA_SUB_FOLDER_NAME);
            final String fileName = fileNamingService.getCompleteFileName(name, version);

            fileStorageService.delete(dataPath, fileName);
            
        } catch(RuntimeHengeException e) {
            throw e;
        } catch(Exception e) {
            //rewrites the erased file
            final String path = fileNamingService.getPath(getGenericType());
            final String fileName = fileNamingService.getCompleteFileName(name, version);
            try {
                fileStorageService.write(path, fileName, jsonUtils.toJson(deleted.get()), false);
                throw new HengeIOException(Status.INTERNAL_SERVER_ERROR,
                    "Problem trying to delete entity [" + getGenericType().getSimpleName()
                    + "] by the name [" + name + "]", e);
                
            } catch (JsonProcessingException e1) {                
                throw new HengeIOException(Status.INTERNAL_SERVER_ERROR,
                    "Problem trying to delete entity [" + getGenericType().getSimpleName()
                    + "] by the name [" + name + "]", e1);
            }
            
        }
        
        return deleted;
        
    }
    
    @Override
    public Optional<FileVersion> read(final String name) {

        Optional<FileVersion> optRead = super.read(name);
        
        if(optRead.isPresent()) {
            final FileVersion fileVersion = optRead.get();
            
            final String path = fileNamingService.getPath(getGenericType(), DATA_SUB_FOLDER_NAME);
            final String fileName = fileNamingService.getCompleteFileName(fileVersion.getName(), fileVersion.getVersion());
            final byte[] content = fileStorageService.readBytes(path, fileName).get();
            
            optRead = Optional.of(FileVersion.builder(fileVersion).withContent(content).build());
        }
        
        return optRead;

    }

    @Override
    public Optional<FileVersion> read(final String name, final String version) {

        Optional<FileVersion> optRead = super.read(name, version);
        
        
        if(optRead.isPresent()) {
            final FileVersion fileVersion = optRead.get();  

            final String path = fileNamingService.getPath(getGenericType(), DATA_SUB_FOLDER_NAME);
            final String fileName = fileNamingService.getCompleteFileName(fileVersion.getName(), fileVersion.getVersion());
            final byte[] content = fileStorageService.readBytes(path, fileName).get();
            
            optRead = Optional.of(FileVersion.builder(fileVersion).withContent(content).build());
        }
        
        return optRead;

    }
    
    /**
     * This method verifies that the given {@link FileVersion} is not referenced
     * by any existing {@link VersionSet}
     * 
     * @param name {@link FileVersion} name
     */
    /*
     * private void deleteValidation(String name, String version) { final
     * Set<String> referencedVersionSets = Sets.newHashSet(); try { final String
     * path = fileNamingService.getPath(VersionSet.class); Optional<Set<String>>
     * versionSetFileNames = fileStorageService.getFileNamesStartingWith(path,
     * StringUtils.EMPTY); if (versionSetFileNames.isPresent()) { for (String
     * versionSetPath : versionSetFileNames.get()) { String content =
     * fileStorageService.read(path, versionSetPath).get(); VersionSet
     * versionSet = jsonUtils.fromJson(content, VersionSet.class);
     * versionSet.getFileVersionReferences().parallelStream().forEach(new
     * Consumer<FileVersionReference>() {
     * @Override public void accept(FileVersionReference ref) { if
     * (!version.isEmpty()) { if (version.equalsIgnoreCase(ref.getVersion()) &&
     * name.equalsIgnoreCase(ref.getName())) {
     * referencedVersionSets.add(versionSet.getName()); } } if
     * (name.equalsIgnoreCase(ref.getName())) {
     * referencedVersionSets.add(versionSet.getName()); } } }); } } if
     * (!referencedVersionSets.isEmpty()) { throw new
     * HengeValidationException(Status.CONFLICT,
     * "You can not delete this property group " +
     * "because there are referenced VersionSets : " +
     * referencedVersionSets.toString()); } } catch (IOException e) { throw new
     * RuntimeHengeException(Status.INTERNAL_SERVER_ERROR,
     * "IO error while trying to delete " + getGenericType().getSimpleName() +
     * " file [" + name + "]", e); } }
     */

    private void checkContentHasData(FileVersion entity) {

        if (entity.getContent() == null) {
            throw new HengeValidationException(Status.BAD_REQUEST, "File object contains no data");
        }

    }
    
    private void rollbackDeletions(Map<String, byte[]> deletedData, String path) {

        deletedData.entrySet().forEach(erasedEntry -> {
            fileStorageService.writeBytes(path, erasedEntry.getKey(), erasedEntry.getValue(), false);
        });

    }

}
