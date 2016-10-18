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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This test class tests not only the VersionSetMapping class 
 * but also the VersionSetMappingFileRepository.
 *
 * @author wmatsushita
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@ActiveProfiles({"dev","flatfile_local","setmapping"})
@TestPropertySource(properties={"versionset.mapping.file.name=test_version_set_mapping"})
public class VersionSetMappingTest {

    private HashMap<MappingKey, VersionSet> map;
    private MappingKey[] keys;

    @Value("${versionset.mapping.file.name}")
    private String versionSetMappingName;
    
    @Autowired
    private VersionSetRepository repository;

    @Autowired
    private MappingRepository<VersionSetReference> mappingRepository;
    
    @Autowired
    private Mapping<VersionSetReference> mapping;

    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private FileNamingService fileNamingService;
    
    /**
     * This setup does the following things: 
     * - Creates 100 VersionSets with 10 PropertyGroupReferences each. 
     * - Creates the keys for each VersionSet and inserts them into an array. 
     * - Inserts the VersionSetReferences in the mapping. 
     * - Inserts the VersionSets in a HashMap for later comparison. 
     * - Saves the mapping.
     * 
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        PropertyGroupReference[] pgReferences = new PropertyGroupReference[1000];
        PropertyGroupReference[] dummy;
        VersionSet vs;
        VersionSetReference vsReference;
        MappingKey key;

        map = new HashMap<>();

        // Creates the PropertyGroupReferences
        for (int i = 0; i < 1000; i++) {
            PropertyGroupReference pgr =
                PropertyGroupReference.builder("property-group-test-" + i, "1.0.0").build();
            pgReferences[i] = pgr;
        }

        keys = new MappingKey[100];

        for (int i = 0; i < 100; i++) {
            

            // Splits the main PropertyGroupReference array into subarrays.
            dummy = new PropertyGroupReference[10];
            for (int j = 0; j < 10; j++) {
                dummy[j] = pgReferences[i + j];
            }

            // Instantiates the VersionSet with the PropertyGroupReferences in
            // the subarray
            vs =
                VersionSet.builder("test-version-set-" + i, "1.0.0")
                    .withDescription("Version set for testing the mapping").withCreatedBy("Wagner Yukio Matsushita")
                    .withCreatedDate(LocalDateTime.now()).withPropertyGroupReferences(dummy).build();

            // Instantiates the Key
            key = new MappingKey(ScopeUtils.parseScopeString("application="+dummy[0].getName()));
            keys[i] = key;

            // Persists the VersionSet
            repository.create(vs);

            // Maps the key to the VersionSetReference
            vsReference = VersionSetReference.builder(vs.getName(), vs.getVersion()).build();
            mapping.put(key, vsReference);
            map.put(key, vs);

        }

        // Persists the mapping. 
        mappingRepository.save(mapping);

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {

        fileStorageService.deleteBeginningWith(fileNamingService.getPath(VersionSet.class), "test-version-set");

        fileStorageService.delete(StringUtils.EMPTY, versionSetMappingName);

        keys = null;
        map = null;
    }

    /**
     * This test takes the keys array and shuffles it in random order. Then, for
     * each key, it retrieves the VersionSetReference, reads the VersionSet from
     * the repository and compares to the VersionSet stored in the HashMap.
     */
    @Test
    public void testVersionSetMapping() {

        // rearrange the keys in random order
        Arrays.sort(keys, new Comparator<MappingKey>() {

            public int compare(MappingKey o1, MappingKey o2) {

                Random rand = new Random();
                return (rand.nextInt(1) - 1);
            }

        });

        // loads the mapping
        Mapping<VersionSetReference> mapping = mappingRepository.load();
        VersionSet expectedVs, vs;
        VersionSetReference vsReference;

        System.out.println("map="+mapping.getInnerRepresentation());
        
        for (int i = 0; i < keys.length; i++) {
            vsReference = mapping.get(keys[i]).get();
            expectedVs = map.get(keys[i]);
            vs = repository.read(vsReference.getName(), vsReference.getVersion()).get();
            assertThat(vs).isEqualTo(expectedVs);
        }

    }

}
