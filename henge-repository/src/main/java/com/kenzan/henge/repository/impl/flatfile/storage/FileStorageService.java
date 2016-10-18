package com.kenzan.henge.repository.impl.flatfile.storage;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.kenzan.henge.exception.HengeValidationException;

/**
 * Interface to define the contract between repositories and the storage of data. 
 * All operations defined here happens in a repository location configured inside each implementation.
 * Only the implementations should know about it's location. 
 *
 * @author wmatsushita
 */
public interface FileStorageService {

    /**
     * Persists the given text into a file. If the overwrite flag is set to
     * false, implementations should throw an a
     * {@link HengeValidationException} if the file being written has a
     * conflict with a already existing file.
     * @param path the path to the file, analogous to a folder in the flatfile system, relative to the root of the repository
     * @param fileName the name of the file to be written
     * @param text the content of the file
     * @param overwrite whether or not the an existing file by the same name can be overwritten.
     */
    public abstract void write(String path, String fileName, String text, boolean overwrite);

    /**
     * Persists the given byte array into a file by the given name. If the overwrite flag is set to
     * false, implementations should throw an a
     * {@link HengeValidationException} if the file being written has a
     * conflict with a already existing file. 
     * @param path the path to the file, analogous to a folder in the flatfile system, relative to the root of the repository
     * @param fileName the name of the file to be written
     * @param data the byte array
     * @param overwrite whether or not the an existing file by the same name can be overwritten.
     */
    public abstract void writeBytes(String path, String fileName, byte[] data, boolean overwrite);
    
    /**
     * Persists the given text into a file with lock
     * @param path the path to the file, analogous to a folder in the flatfile system, relative to the root of the repository
     * @param fileName the name of the file to be written
     * @param text the content of the file
     * @param overwrite whether or not the an existing file by the same name can be overwritten.
     * @param lock whether or not uses lock file
     */
    public abstract void write(String path, String fileName, String text, boolean overwrite, boolean lock);

    /**
     * Reads the content of the file in text form. Implementations should use the text.encoding property set in the .yml files while converting to text. 
     * @param fileName the name of the file to be read
     * @return an Optional containing the text content of the file or absent if the file was not found.
     */
    public abstract Optional<String> read(String path, String fileName);

    /**
     * Reads all the bytes present in the file. 
     * @param path the path to the file, analogous to a folder in the flatfile system, relative to the root of the repository
     * @param fileName the name of the file to be read
     * @return an Optional containing the all the bytes contained in the file or absent if the file was not found.
     */
    public abstract Optional<byte[]> readBytes(String path, String fileName);

    /**
     * Deletes a file by the give name.
     * @return true if the file was deleted or false if it was not found for deletion.
     */
    public abstract boolean delete(String path, String fileName);

    /**
     * Deletes all the files that have name staring with the given string.
     * Currently this is used in cleaning up after unit and integration tests.
     * @param path the path to the file, analogous to a folder in the flatfile system, relative to the root of the repository
     * @param nameStart the part of the filename that must be present for the file to be deleted
     * @return a list containing the names of the deleted files
     */
    public abstract List<String> deleteBeginningWith(String path, String nameStart);

    /**
     * Checks if a file by the given name exists in the repository.
     * @param path the path to the file, analogous to a folder in the flatfile system, relative to the root of the repository
     * @param fileName the name of the file to check for existence.
     * @return true if the file exists, false otherwise.
     */
    public abstract boolean exists(String path, String fileName);

    /**
     * Checks if a file having a name that begins with the given token exists.
     * @param path the path to the file, analogous to a folder in the flatfile system, relative to the root of the repository
     * @param nameStart the token
     * @return true if one or more files exist beginning with the given token.
     */
    public abstract boolean existsBeginningWith(String path, String nameStart);

    /**
     * Gets a list of file names that exist in the repository staring with the given token  
     * @param path the path to the file, analogous to a folder in the flatfile system, relative to the root of the repository
     * @param nameStart the token
     * @return an Optional containing the list of names or absent if none were found
     */
    public abstract Optional<Set<String>> getFileNamesStartingWith(String path, String nameStart);

}
