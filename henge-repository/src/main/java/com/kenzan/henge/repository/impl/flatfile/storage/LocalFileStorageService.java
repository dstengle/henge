package com.kenzan.henge.repository.impl.flatfile.storage;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.kenzan.henge.exception.HengeIOException;
import com.kenzan.henge.exception.HengeValidationException;
import com.kenzan.henge.exception.RuntimeHengeException;

/**
 * 
 *
 * @author wmatsushita
 */
@Profile("flatfile_local")
@Component
public class LocalFileStorageService implements FileStorageService {

    @Value("${user.home}/${repository.location}")
    private String repositoryLocation;

    @Value("${text.encoding}")
    private String textEncoding;

    @Override
    public void write(final String path, final String fileName, final String text, boolean overwrite) {

        final byte[] bytes = text.getBytes(Charset.forName(textEncoding));

        writeBytes(path, fileName, bytes, overwrite);

    }

    @Override
    public void writeBytes(final String path, final String fileName, byte[] data, boolean overwrite) {

        final Path filePath = FileSystems.getDefault().getPath(repositoryLocation, path, fileName);
        final OpenOption option = overwrite ? StandardOpenOption.CREATE : StandardOpenOption.CREATE_NEW;
        try {
            Files.write(filePath, data, option);
        } catch (IOException e) {
            if (Files.exists(filePath)) {
                throw new HengeValidationException(Status.CONFLICT,
                    "The file cannot be created because it already exists.", e);
            }
            throw new HengeIOException(Status.INTERNAL_SERVER_ERROR,
                "An IO error occured while trying to write the file", e);
        }
    }

    @Override
    public void write(String path, String fileName, String text, boolean overwrite, boolean lock) {

        final Path filePath = FileSystems.getDefault().getPath(repositoryLocation, path, fileName);

        if (lock) {

            FileChannel channel = null;
            FileLock fileLock = null;
            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile(filePath.toFile(), "rw");
                channel = randomAccessFile.getChannel();
                fileLock = channel.tryLock(0, Long.MAX_VALUE, true);
                if (fileLock != null) {
                    writeBytes(path, fileName, text.getBytes(Charset.forName(textEncoding)), overwrite);
                }
            } catch (IOException e) {
                if (Files.exists(filePath)) {
                    throw new HengeValidationException(Status.CONFLICT,
                        "The file cannot be created because it already exists.", e);
                }
                throw new HengeIOException(Status.INTERNAL_SERVER_ERROR,
                    "An IO error occured while trying to lock the file", e);
            } finally {
                try {
                    if (fileLock != null) {
                        fileLock.release();
                    }
                    channel.close();
                    randomAccessFile.close();
                } catch (IOException e) {
                    throw new RuntimeHengeException(Status.INTERNAL_SERVER_ERROR,
                        "Failed to release the lock file.", e);
                }
            }
        } else {
            write(path, fileName, text, overwrite);
        }

    }

    @Override
    public Optional<String> read(final String path, final String fileName) {

        final Optional<byte[]> bytes = readBytes(path, fileName);

        return bytes.isPresent() ? Optional.of(new String(bytes.get(), Charset.forName(textEncoding))) : Optional
            .empty();

    }

    @Override
    public Optional<byte[]> readBytes(final String path, final String fileName) {

        final Path file = FileSystems.getDefault().getPath(repositoryLocation, path, fileName);

        try {
            return Files.exists(file) ? Optional.of(Files.readAllBytes(file)) : Optional.empty();
        } catch (IOException e) {
            throw new HengeIOException(Status.INTERNAL_SERVER_ERROR,
                "An IO error occured while trying to read the file", e);
        }
    }

    @Override
    public boolean delete(final String path, final String fileName) {

        final Path file = FileSystems.getDefault().getPath(repositoryLocation, path, fileName);

        try {
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new HengeIOException(Status.INTERNAL_SERVER_ERROR,
                "An IO error occured while trying to delete the file", e);
        }

    }

    @Override
    public List<String> deleteBeginningWith(final String path, final String nameStart) {

        final List<String> result = new ArrayList<>();

        Optional<Set<String>> fileNames = getFileNamesStartingWith(path, nameStart);
        if (fileNames.isPresent()) {
            fileNames.get().stream().forEach(fileName -> {
                delete(path, fileName);
                result.add(fileName);
            });
        }

        return result;
    }

    @Override
    public boolean exists(final String path, final String fileName) {

        final Path file = FileSystems.getDefault().getPath(repositoryLocation, path, fileName);

        return Files.exists(file);

    }

    @Override
    public boolean existsBeginningWith(final String path, final String nameStart) {

        final Path dir = FileSystems.getDefault().getPath(repositoryLocation, path);

        DirectoryStream<Path> pathList;
        try {
            pathList = Files.newDirectoryStream(dir, nameStart + "*");
        } catch (IOException e) {
            throw new HengeIOException(Status.INTERNAL_SERVER_ERROR,
                "An IO error occured while trying list files", e);
        }

        return pathList.iterator().hasNext();

    }

    @Override
    public Optional<Set<String>> getFileNamesStartingWith(final String path, final String nameStart) {

        final Path dir = FileSystems.getDefault().getPath(repositoryLocation, path);
        DirectoryStream<Path> pathList;
        try {
            pathList = Files.newDirectoryStream(dir, nameStart + "*");
        } catch (IOException e) {
            throw new HengeIOException(Status.INTERNAL_SERVER_ERROR,
                "An IO error occured while trying list files", e);
        }

        final Iterator<Path> it = pathList.iterator();

        if (!it.hasNext()) {
            return Optional.empty();
        }

        final Set<String> fileNames = new HashSet<>();

        it.forEachRemaining(p -> fileNames.add(p.getFileName().toString()));

        return Optional.ofNullable(fileNames);
    }

}
