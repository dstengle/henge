package com.kenzan.henge.repository.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.model.Mapping;
import com.kenzan.henge.domain.model.MappingKey;
import com.kenzan.henge.domain.model.PropertyGroupReference;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.model.VersionSetReference;
import com.kenzan.henge.domain.utils.ScopeUtils;
import com.kenzan.henge.repository.MappingRepository;
import com.kenzan.henge.repository.VersionSetRepository;
import com.kenzan.henge.repository.impl.flatfile.FileNamingService;
import com.kenzan.henge.repository.impl.flatfile.storage.FileStorageService;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This test class tests if the mapping class implementation specified by the
 * ActiveProfiles annotation works as expected in regards to returning the best
 * generic mapping when an exact match is not found.
 *
 * @author wmatsushita
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@ActiveProfiles({ "dev", "flatfile_local", "setmapping"})
@TestPropertySource(properties={"scope.precedence.configuration=env;env+region;env+region+stack;hostname;application", "versionset.mapping.file.name=test_version_set_mapping"})
public class VersionSetMappingGenericFallbackTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionSetMappingGenericFallbackTest.class);
    private static final int NUMBER_OF_VERSION_SETS = 5;

    
    private MappingKey[] keys;
    private VersionSetReference[] vsReferences = new VersionSetReference[NUMBER_OF_VERSION_SETS];
    
    @Value("${versionset.mapping.file.name}")
    private String mappingName;

    @Autowired
    private VersionSetRepository repository;

    @Autowired
    private Mapping<VersionSetReference> mapping;
    
    @Autowired
    private MappingRepository<VersionSetReference> mappingRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private FileNamingService fileNamingService;

    /**
     * This setup does the following things: - Creates 4 VersionSets with 10
     * PropertyGroupReferences each. - Creates the keys for each VersionSet and
     * inserts them into an array. - Inserts the VersionSetReferences in the
     * mapping. - Saves the mapping.
     * 
     * @throws java.lang.Exception if any exception occurs
     */
    @Before
    public void setUp() throws Exception {

        PropertyGroupReference[] dummy;
        VersionSet[] versionSets = new VersionSet[NUMBER_OF_VERSION_SETS];

        // Creates the PropertyGroupReferences
        PropertyGroupReference[] pgReferences = new PropertyGroupReference[10 * NUMBER_OF_VERSION_SETS];
        for (int i = 0; i < (10 * NUMBER_OF_VERSION_SETS); i++) {
            PropertyGroupReference pgr =
                PropertyGroupReference.builder("property-group-test-" + i, "1.0.0").build();
            pgReferences[i] = pgr;
        }

        keys = new MappingKey[NUMBER_OF_VERSION_SETS];

        for (int i = 0; i < NUMBER_OF_VERSION_SETS; i++) {

            // Splits the main PropertyGroupReference array into subarrays.
            dummy = new PropertyGroupReference[10];
            for (int j = 0; j < 10; j++) {
                dummy[j] = pgReferences[i + j];
            }

            // Instantiates the VersionSet with the PropertyGroupReferences in
            // the subarray
            versionSets[i] =
                VersionSet.builder("test-version-set-" + i, "1.0.0")
                    .withDescription("Version set for testing the mapping").withCreatedBy("Wagner Yukio Matsushita")
                    .withCreatedDate(LocalDateTime.now()).withPropertyGroupReferences(dummy).build();

            // Persists the VersionSet
            repository.create(versionSets[i]);

            // Maps the key to the VersionSetReference
            vsReferences[i] =
                VersionSetReference.builder(versionSets[i].getName(), versionSets[i].getVersion()).build();

        }

        // Instantiates the Keys
        keys[0] = new MappingKey(ScopeUtils.parseScopeString("env=dev"));
        keys[1] = new MappingKey(ScopeUtils.parseScopeString("env=dev,region=region-1"));
        keys[2] = new MappingKey(ScopeUtils.parseScopeString("env=dev,region=region-2"));
        keys[3] = new MappingKey(ScopeUtils.parseScopeString("env=prod,region=region-3,stack=stack-1"));
        keys[4] = new MappingKey(ScopeUtils.parseScopeString("hostname=hostname-1"));

        // Loads the mapping from the repository. This is inside the loop on
        // purpose, to test the performance of the caching.

        for (int i = 0; i < NUMBER_OF_VERSION_SETS; i++) {
            mapping.put(keys[i], vsReferences[i]);
        }

        // Persists the mapping. This is inside the loop on purpose, to test the
        // performance of the caching.
        mappingRepository.save(mapping);
        
        LOGGER.debug("Mapping after setup: " + mapping.toString());

    }

    /**
     * Deletes files created and removes all the mapping keys inserted during
     * the test.
     * 
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {

        fileStorageService.deleteBeginningWith(fileNamingService.getPath(VersionSet.class), "test-version-set");

        //Erases the mapping file
        fileStorageService.delete(StringUtils.EMPTY, mappingName);

    }

    /**
     * ScopePrecedenceConfiguration is as follows:
     * "env;env+region;env+region+stack;hostname;application"
     * 
     * Mappings are as follows: 
     * ("env=dev") => test-version-set-0
     * ("env=dev&region=region-1") => test-version-set-1
     * ("env=dev&region=region-2") => test-version-set-2
     * ("env=prod&region=region-3&stack=stack-1") => test-version-set-3
     * ("hostname=hostname-1") => test-version-set-4
     * 
     * Query keys and expected version set are as follows: 
     * ("env=prod") => absent (no match) 
     * ("env=dev&region=region-2") => test-version-set-2 (exact match) 
     * ("env=dev&region=region-3") => test-version-set-0 (fall back to ("env=dev")) 
     * ("env=dev&region=region-3&stack=stack-1") => test-version-set-0 (fall back to ("env=dev"))
     * ("env=prod&region=region-3&stack=stack-1") => test-version-set-3 (exact match) 
     * ("env=dev&region=region-1&stack=stack-1") => test-version-set-1 (fall back to ("env=dev&region=region-1"))
     * ("env=dev&region=region-1&hostname=hostname=hostname-1") => test-version-set-4 (overwrite by most specific match)
     */
    @Test
    public void testVersionSetMapping() {

        MappingKey key;

        LOGGER.debug("Mapping before first call: " + mapping.toString());
        
        key = new MappingKey(ScopeUtils.parseScopeString("env=prod"));
        assertThat(mapping.get(key)).isEqualTo(Optional.empty());

        key = new MappingKey(ScopeUtils.parseScopeString("env=dev,region=region-2"));
        assertThat(mapping.get(key).get()).isEqualTo(vsReferences[2]);

        key = new MappingKey(ScopeUtils.parseScopeString("env=dev,region=region-3"));
        assertThat(mapping.get(key).get()).isEqualTo(vsReferences[0]);

        key = new MappingKey(ScopeUtils.parseScopeString("env=dev,region=region-3,stack=stack-1"));
        assertThat(mapping.get(key).get()).isEqualTo(vsReferences[0]);

        key =
            new MappingKey(ScopeUtils.parseScopeString("env=prod,region=region-3,stack=stack-1"));
        assertThat(mapping.get(key).get()).isEqualTo(vsReferences[3]);

        key = new MappingKey(ScopeUtils.parseScopeString("env=dev,region=region-1,stack=stack-1"));
        assertThat(mapping.get(key).get()).isEqualTo(vsReferences[1]);

        key = new MappingKey(ScopeUtils.parseScopeString("env=dev,region=region-1,hostname=hostname-1"));
        assertThat(mapping.get(key).get()).isEqualTo(vsReferences[4]);

    }

}
