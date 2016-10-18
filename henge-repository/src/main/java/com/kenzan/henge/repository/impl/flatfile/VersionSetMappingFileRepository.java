package com.kenzan.henge.repository.impl.flatfile;

import java.io.IOException;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.kenzan.henge.domain.model.Mapping;
import com.kenzan.henge.domain.model.MappingFactory;
import com.kenzan.henge.domain.model.MappingKey;
import com.kenzan.henge.domain.model.VersionSetReference;
import com.kenzan.henge.exception.HengeIOException;
import com.kenzan.henge.exception.RuntimeHengeException;
import com.kenzan.henge.repository.MappingRepository;
import com.kenzan.henge.repository.impl.flatfile.storage.FileStorageService;

/**
 * File implementation of the {@link MappingRepository}. It stores the mapping
 * in binary format. It caches the mapping to improve performance.
 *
 * @author wmatsushita
 */
@Component
@Profile({ "flatfile_local", "flatfile_s3" })
public class VersionSetMappingFileRepository implements MappingRepository<VersionSetReference> {

    @Value("${versionset.mapping.file.name}")
    private String mappingFileName;
    
    private FileStorageService fileStorageService;

    private MappingFactory<VersionSetReference> mappingFactory;

    private ObjectMapper mapper;

    @Autowired
    public VersionSetMappingFileRepository(FileStorageService fileStorageService,
        MappingFactory<VersionSetReference> mappingFactory, ObjectMapper mapper) {

        this.fileStorageService = fileStorageService;
        this.mappingFactory = mappingFactory;
        this.mapper = mapper;

    }

    /**
     * Saves the mapping to a file by the given name
     * 
     * @param mapping an instance of {@link Mapping} to be
     *        persisted. It overwrites any previously stored mapping.
     * @param name the name of the mapping file
     * @throws RuntimeHengeException if there is a JSON serialization
     *         or IO problem while trying to write the file.
     */
    public Mapping<VersionSetReference> save(Mapping<VersionSetReference> mapping, Optional<String> name) {

        final String mappingName = name.isPresent() ? name.get() : mappingFileName;

        try {
            fileStorageService.write(StringUtils.EMPTY, mappingName, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapping.getInnerRepresentation()), true, true);
            
            return mapping;
        } catch (HengeIOException | JsonProcessingException e) {
            throw new RuntimeHengeException(Status.INTERNAL_SERVER_ERROR,
                "The Mapping could not be serialized into a file.", e);
        }

    }

    public Mapping<VersionSetReference> save(Mapping<VersionSetReference> mapping) {

        return save(mapping, Optional.absent());

    }

    /**
     * Loads a previously saved instance of {@link Mapping}. If
     * there is none, it creates a new mapping instance and returns it. The
     * exact implementation of the Mapping is set at runtime by the profile
     * defined in application.yml
     * 
     * @param name the name of the mapping file
     * @returns a previously saved {@link Mapping} or a new
     *          instance if none is found.
     * @throws RuntimeHengeException if there is an IO problem while
     *         trying to read the file.
     */
    public Mapping<VersionSetReference> load(Optional<String> name) {

        final String mappingName = name.isPresent() ? name.get() : mappingFileName;
        Mapping<VersionSetReference> mapping = mappingFactory.create();
        
        if (!fileStorageService.exists(StringUtils.EMPTY, mappingName)) {
            return mapping;
        }
        try {

            final String mappingJson = fileStorageService.read(StringUtils.EMPTY, mappingName).get();
            
            final JavaType type = mapper.getTypeFactory().constructMapType(mapping.getInnerRepresentation().getClass(), MappingKey.class, VersionSetReference.class);
            mapping.setInnerRepresentation(mapper.readValue(mappingJson, type));
            
            return mapping;

        } catch (HengeIOException | IOException e) {
            throw new RuntimeHengeException(Status.INTERNAL_SERVER_ERROR,
                "The Mapping file cannot be deserialized due to an IO problem.", e);
        }

    }

    public Mapping<VersionSetReference> load() {

        return load(Optional.absent());

    }

}
