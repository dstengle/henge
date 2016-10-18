package com.kenzan.henge.repository.impl.flatfile;

import com.kenzan.henge.domain.model.FileVersion;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.VersionSet;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * This class is executed during application startup. It creates the repository
 * folder if it doesn't exist yet.
 *
 * @author wmatsushita
 */
@Profile("flatfile_local")
@Component
public class FlatFileRepositoryCreator implements ApplicationRunner {

    @Value("${user.home}/${repository.location}")
    private String repositoryLocation;

    /**
     * Creates the repository folder in the configured location, inside the user
     * home directory. This class is called by Spring once the SpringApplication 
     * has started to make sure the flat file repository exists.
     * 
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {

        Path repositoryDir = FileSystems.getDefault().getPath(repositoryLocation);
        Path propertyGroupDir = FileSystems.getDefault().getPath(repositoryLocation, PropertyGroup.class.getSimpleName());
        Path versionSetDir = FileSystems.getDefault().getPath(repositoryLocation, VersionSet.class.getSimpleName());
        Path fileVersionDir = FileSystems.getDefault().getPath(repositoryLocation, FileVersion.class.getSimpleName());
        Path fileVersionDataDir = FileSystems.getDefault().getPath(repositoryLocation, FileVersion.class.getSimpleName() + "/" + FileVersionFlatFileRepository.DATA_SUB_FOLDER_NAME );

        Files.createDirectories(repositoryDir);
        Files.createDirectories(propertyGroupDir);
        Files.createDirectories(versionSetDir);
        Files.createDirectories(fileVersionDir);
        Files.createDirectories(fileVersionDataDir);
        
    }

}
