package com.kenzan.henge.repository.impl.flatfile;

import static org.junit.Assert.assertTrue;

import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.model.FileVersion;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.VersionSet;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 *
 *
 * @author wmatsushita
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@ActiveProfiles({ "dev", "flatfile_local", "setmapping" })
public class FlatFileRepositoryCreatorTest {

    @Autowired
    private FlatFileRepositoryCreator flatFileRepositoryCreator;
    
    @Value("${user.home}/${repository.location}")
    private String repositoryLocation;
    
    /**
     * Test method for {@link com.kenzan.henge.repository.impl.flatfile.FlatFileRepositoryCreator#run(org.springframework.boot.ApplicationArguments)}.
     */
    @Test
    public void testRun() throws Exception {

        flatFileRepositoryCreator.run(null);
        
        Path repositoryDir = FileSystems.getDefault().getPath(repositoryLocation);
        Path propertyGroupDir = FileSystems.getDefault().getPath(repositoryLocation, PropertyGroup.class.getSimpleName());
        Path versionSetDir = FileSystems.getDefault().getPath(repositoryLocation, VersionSet.class.getSimpleName());
        Path fileVersionDir = FileSystems.getDefault().getPath(repositoryLocation, FileVersion.class.getSimpleName());
        Path fileVersionDataDir = FileSystems.getDefault().getPath(repositoryLocation, FileVersion.class.getSimpleName() + "/" + FileVersionFlatFileRepository.DATA_SUB_FOLDER_NAME );
        
        assertTrue(Files.exists(repositoryDir));
        assertTrue(Files.exists(propertyGroupDir));
        assertTrue(Files.exists(versionSetDir));
        assertTrue(Files.exists(fileVersionDir));
        assertTrue(Files.exists(fileVersionDataDir));
    }

}
