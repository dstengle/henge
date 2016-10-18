package com.kenzan.henge.repository.impl.flatfile;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.kenzan.henge.domain.model.NamedVersionedModel;
import com.kenzan.henge.domain.utils.JsonUtils;
import com.kenzan.henge.domain.utils.SemanticVersionComparator;
import com.kenzan.henge.exception.HengeIOException;
import com.kenzan.henge.exception.HengeValidationException;
import com.kenzan.henge.exception.RuntimeHengeException;
import com.kenzan.henge.repository.BaseCrudRepository;
import com.kenzan.henge.repository.impl.flatfile.storage.FileStorageService;

/**
 * This class provides basic flat file repository funcionality to subclasses.
 * Most subclasses will not need to overide them since the details of how to
 * serialize each model class is taken care by the Jackson object mapper.
 *
 * @author wmatsushita
 */
public abstract class BaseFlatFileRepository<T extends NamedVersionedModel> implements BaseCrudRepository<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseFlatFileRepository.class);

    protected FileStorageService fileStorageService;
    
    protected JsonUtils jsonUtils;

    protected FileNamingService fileNamingService;
    
    
    public BaseFlatFileRepository(FileStorageService fileStorageService, FileNamingService fileNamingService, JsonUtils jsonUtils) {
        this.fileStorageService = fileStorageService;
        this.jsonUtils = jsonUtils;
        this.fileNamingService = fileNamingService;
    }
    
    
    /**
     * Provides the type that parameterize the concrete subclass.
     * 
     * @return a subclass of {@link NamedVersionedModel}
     */
    protected abstract Class<T> getGenericType();

    /**
     * Creates a new file representation of a {@link NamedVersionedModel}. If
     * there is already a file for a version of the {@link NamedVersionedModel}
     * with the same name, then a {@link HengeValidationException} is
     * thrown to indicate that the update method should be used instead.
     * 
     * @param entity the {@link NamedVersionedModel} to be persisted.
     * @return the persisted {@link NamedVersionedModel}.
     * @throws HengeValidationException if the a
     *         {@link NamedVersionedModel} already exists by the given name.
     * @throws RuntimeHengeException if there is a serialization or IO
     *         problem.
     */
    @Override
    public T create(T entity) {

        try {

            final String path = fileNamingService.getPath(entity.getClass());
            
            if (fileStorageService.existsBeginningWith(path,entity.getName())) {
                throw new HengeValidationException(Status.CONFLICT, "The " + getGenericType().getSimpleName()
                    + " being created already exists. Consider using the update method." + "property:" + entity.getName());
            }

            final String fileName =
                fileNamingService.getCompleteFileName(entity.getName(), entity.getVersion());

            fileStorageService.write(path, fileName, jsonUtils.toJson(entity), false);
            LOGGER.info(getGenericType().getSimpleName() + " file {} recorded succesfuly.", fileName);

        } catch (JsonProcessingException e) {
            throw new RuntimeHengeException(Status.INTERNAL_SERVER_ERROR, "The "
                + getGenericType().getSimpleName() + " file cannot be created due to Json Generation problem.", e);
        } catch (HengeIOException e) {
            throw new RuntimeHengeException(Status.INTERNAL_SERVER_ERROR, "The "
                + getGenericType().getSimpleName() + " file cannot be created due to an IO problem.", e);
        }

        return entity;
    }

    /**
     * Updates the {@link NamedVersionedModel} by creating a new file for it.
     * 
     * @param name the name of {@link NamedVersionedModel}.
     * @param entity the {@link NamedVersionedModel} instance to be persisted as
     *        the new current version of this {@link NamedVersionedModel}. The
     *        name of the file for this {@link NamedVersionedModel} will be
     *        composed from the name and version of this instance.
     * @returns an {@link Optional} containing the persisted instance or absent
     *          if there were no other versions of {@link NamedVersionedModel}
     *          by the given name.
     * @throws HengeValidationException if the given
     *         {@link NamedVersionedModel} instance has a version number that is
     *         lesser than the current version number for the given name.
     * @throws RuntimeHengeException if there is a serialization or IO
     *         problem.
     */
    @Override
    public Optional<T> update(String name, T entity) {

        try {
        	Optional<T> currentEntity = readLatestVersion(name);

            if (!currentEntity.isPresent()) {
                return Optional.empty();
            }
        	
            final String fileName =
                fileNamingService.getCompleteFileName(entity.getName(), entity.getVersion());

            fileStorageService.write(fileNamingService.getPath(entity.getClass()),fileName, jsonUtils.toJson(entity), false);
            LOGGER.info(getGenericType().getSimpleName() + " file {} created succesfuly as the result of an update.", fileName);

        } catch (JsonProcessingException e) {
            throw new RuntimeHengeException(Status.INTERNAL_SERVER_ERROR, "The " + getGenericType().getSimpleName()
                + " file cannot be created as result of an update due to Json Generation problem.", e);
        } catch (HengeIOException e) {
            throw new RuntimeHengeException(Status.INTERNAL_SERVER_ERROR, "The " + getGenericType().getSimpleName()
                + " file cannot be created as result of an update due to an IO problem.", e);
        }

        return Optional.ofNullable(entity);

    }

    /**
     * Deletes all the versions of a given {@link NamedVersionedModel}.
     * 
     * @param name the name of the {@link NamedVersionedModel}
     * @return an {@link Optional} with the current version of the
     *         {@link NamedVersionedModel} by the given name or absent if no
     *         versions were found.
     * @throws RuntimeHengeException if an IO error occurs.
     */
    @Override
    public Optional<T> delete(String name) {

        final String path = fileNamingService.getPath(getGenericType());
        
        Optional<Set<String>> allFileNames;
        try {
            allFileNames = fileStorageService.getFileNamesStartingWith(path, name);
            if (!allFileNames.isPresent()) {
                return Optional.empty();
            }

            // reads the current entity as return value before erasing all of
            // them.
            T currentEntity = read(name).get();

            allFileNames
                .get()
                .stream()
                .forEach(
                    (fileName) -> {

                        try {
                            fileStorageService.delete(path, fileName);
                        } catch (HengeIOException e) {
                            LOGGER.error("IO problem trying to delete file {}.", fileName, e);
                            throw new HengeIOException(Status.INTERNAL_SERVER_ERROR,
                                "Problem trying to delete one or more " + getGenericType().getSimpleName()
                                    + " versions by the name [" + name + "]", e);
                        }
                    });

            return Optional.ofNullable(currentEntity);
        } catch (HengeIOException ioe) {
            LOGGER.error("IO problem trying to delete files starting with {}.", name, ioe);
            throw new HengeIOException(Status.INTERNAL_SERVER_ERROR, "Problem trying to delete one or more "
                + getGenericType().getSimpleName() + " versions by the name [" + name + "]", ioe);
        }

    }

    /**
     * Deletes a given {@link NamedVersionedModel} version.
     * 
     * @param name the name of the {@link NamedVersionedModel}
     * @param version the version of the {@link NamedVersionedModel}
     * @return an {@link Optional} with the current version of the
     *         {@link NamedVersionedModel} by the given name and version or
     *         absent if it was not found.
     * @throws RuntimeHengeException if there is and IO error trying to
     *         delete the {@link NamedVersionedModel} by the given name and
     *         version
     */
    @Override
    public Optional<T> delete(String name, String version) {

        final String path = fileNamingService.getPath(getGenericType());
        final String fileName = fileNamingService.getCompleteFileName(name, version);
        final Optional<T> entityOptional = read(name, version);
        if (!entityOptional.isPresent()) {
            return Optional.empty();
        }

        try {
            if (!fileStorageService.delete(path, fileName)) {
                throw new RuntimeHengeException(Status.INTERNAL_SERVER_ERROR, "Inconsistent state. The "
                    + getGenericType().getSimpleName() + " file [" + fileName
                    + "] was read but not found for deletion.");
            }
        } catch (HengeIOException e) {
            throw new RuntimeHengeException(Status.INTERNAL_SERVER_ERROR, "IO error while trying to delete "
                + getGenericType().getSimpleName() + " file [" + fileName + "]", e);
        }

        return entityOptional;
    }
    
    /**
     * Reads the contents of the latest version of the file and instantiates and
     * populates a {@link NamedVersionedModel} with it.
     * 
     * @param name the name of the {@link NamedVersionedModel}. The version will
     *        default to the last one.
     * @return an {@link Optional} of {@link NamedVersionedModel}
     * @throws RuntimeHengeException if and IO error occurs.
     */
    @Override
    public Optional<T> read(String name) {

        return readLatestVersion(name);

    }

    /**
     * Reads a {@link NamedVersionedModel} version from the file system.
     * 
     * @param name the {@link NamedVersionedModel} name.
     * @param version the {@link NamedVersionedModel} version.
     * @return an {@link Optional} of {@link NamedVersionedModel}
     * @throws RuntimeHengeException if an IO error occurs.
     */
    @Override
    public Optional<T> read(String name, String version) {

        final String fileName = fileNamingService.getCompleteFileName(name, version);

        try {
            
            final Optional<String> json =
                fileStorageService.read(
                    fileNamingService.getPath(getGenericType()),
                    fileName);
            if (!json.isPresent()) {
                return Optional.empty();
            }

            return Optional.ofNullable(jsonUtils.fromJson(json.get(), getGenericType()));

        } catch (IOException e) {
            LOGGER.error(
                "There was an IO error while trying to read the " + fileName + " file", e);
            throw new RuntimeHengeException(Status.INTERNAL_SERVER_ERROR,
                "There was an IO error while trying to read the " + fileName + " file", e);
        }

    }

    /**
     * Lists all the available versions o the the {@link NamedVersionedModel}
     * with given name. This implementation lists the files that begin with the
     * given name and reads the version from inside each file.
     * 
     * @param name the name of the {@link NamedVersionedModel}.
     * @return an Optional containing a set of {@link String} with the available
     *         versions or absent if no versions were found.
     * @throws RuntimeHengeException if an IO error occurs.
     */
    @Override
    public Optional<Set<String>> versions(String name) {

        try {
            final Optional<Set<String>> fileNames;
            final Set<String> versions;

            fileNames =
                fileStorageService.getFileNamesStartingWith(
                    fileNamingService.getPath(getGenericType()), 
                    name);
            if (!fileNames.isPresent()) {
                return Optional.empty();
            }

            versions = new HashSet<>();
            for (String fileName : fileNames.get()) {
                //no need to open the file, just extract the version from the filename
                versions.add(fileNamingService.extractEntityVersionFromFileName(fileName));
            }

            return Optional.of(versions);
        } catch (HengeIOException e) {
            LOGGER.error("Error trying to fetch versions of the " + getGenericType().getSimpleName()
                + " with name [{}]", name, e);
            throw new RuntimeHengeException(Status.INTERNAL_SERVER_ERROR,
                "Error trying to fetch versions of a " + getGenericType().getSimpleName(), e);
        }

    }

    /**
     * Retrieve the latest version of the {@link NamedVersionedModel} with the
     * given name. Implementations must return an absent {@link Optional} if
     * there are no latest version of the entity by the given name.
     * 
     * @param name the name of the PropertyGroup
     * @return an {@link Optional} with the latest version of a persisted
     *         PropertyGroup or absent.
     */
   Optional<T> readLatestVersion(final String name) {

        Optional<Set<String>> versions = versions(name);

        if (versions.isPresent()) {
            List<String> versionList = Lists.newArrayList(versions.get());
            Collections.sort(versionList, new SemanticVersionComparator());
            String versionNumber = versionList.get(versionList.size() - 1);
            return read(name, versionNumber);
        }

        return Optional.empty();
    }

}
