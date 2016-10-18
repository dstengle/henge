package com.kenzan.henge.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.kenzan.henge.config.TestContextConfig;
import com.kenzan.henge.domain.model.MappingKey;
import com.kenzan.henge.domain.model.Scope;
import com.kenzan.henge.domain.model.ScopePrecedenceConfiguration;
import com.kenzan.henge.domain.model.VersionSetMapping;
import com.kenzan.henge.domain.model.VersionSetReference;
import com.kenzan.henge.domain.utils.JsonUtils;
import com.kenzan.henge.domain.utils.ScopeUtils;


/**
 * Tests the VersionSetMappingBD implementation
 *
 * @author wmatsushita
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(TestContextConfig.class)
@ActiveProfiles({ "dev", "flatfile_local", "setmapping"})
@TestPropertySource(properties={"versionset.mapping.file.name=test_version_set_mapping"})
public class VersionSetMappingTest {
    
    private static final int NUMBER_OF_VERSION_SETS = 22;
    
    private static final Scope[] ENVS = {Scope.builder("env", "env-0").build(), Scope.builder("env", "env-1").build(), Scope.builder("env", "env-2").build()};
    private static final Scope[] REGIONS = {Scope.builder("region", "region-0").build(), Scope.builder("region", "region-1").build(), Scope.builder("region", "region-2").build()};
    private static final Scope[] STACKS = {Scope.builder("stack", "stack-0").build(), Scope.builder("stack", "stack-1").build(), Scope.builder("stack", "stack-2").build()};
    private static final Scope[] HOSTNAMES = {Scope.builder("hostname", "hostname-0").build(), Scope.builder("hostname", "hostname-1").build()};

    private static final String SCOPE_PRECEDENCE_CONFIGURATION = "env;env+region;env+region+stack;hostname;application";
    private static final String SCOPE_APPLICATION_NAME_KEY = "application";
    
    private VersionSetReference[] vsReferences = new VersionSetReference[NUMBER_OF_VERSION_SETS];
    
    private VersionSetMapping mapping;
    
    @Autowired
    private JsonUtils jsonUtils;
    
    @Autowired
    private ObjectMapper mapper;
    
    @Before
    public void setUp() {

        for(int i=0; i<NUMBER_OF_VERSION_SETS; i++) {
            
            vsReferences[i] = VersionSetReference.builder("test-version-set-"+i, "1.0.0").build();
            
        }
        
        final ScopePrecedenceConfiguration scopePrecedenceConfiguration = new ScopePrecedenceConfiguration(SCOPE_PRECEDENCE_CONFIGURATION);
        
        mapping = new VersionSetMapping(scopePrecedenceConfiguration);
        
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[0]))),                        vsReferences[0]);
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[0], REGIONS[0]))),            vsReferences[1]);
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[0], REGIONS[0], STACKS[0]))), vsReferences[2]);
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[0], REGIONS[0], STACKS[1]))), vsReferences[3]);
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[0], REGIONS[0], STACKS[2]))), vsReferences[4]);
        
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[0], REGIONS[1]))),            vsReferences[5]);
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[0], REGIONS[1], STACKS[0]))), vsReferences[6]);
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[0], REGIONS[1], STACKS[1]))), vsReferences[7]);
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[0], REGIONS[1], STACKS[2]))), vsReferences[8]);
        
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[1]))),                        vsReferences[9]);
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[1], REGIONS[0]))),            vsReferences[10]);
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[1], REGIONS[0], STACKS[0]))), vsReferences[11]);
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[1], REGIONS[0], STACKS[1]))), vsReferences[12]);
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[1], REGIONS[0], STACKS[2]))), vsReferences[13]);

        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[1], REGIONS[1]))),            vsReferences[14]);
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[1], REGIONS[1], STACKS[0]))), vsReferences[15]);
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[1], REGIONS[1], STACKS[1]))), vsReferences[16]);
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[1], REGIONS[1], STACKS[2]))), vsReferences[17]);
        
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(HOSTNAMES[0]))),                   vsReferences[18]);
        mapping.put( new MappingKey(ImmutableSet.copyOf( Sets.newHashSet(ENVS[0], HOSTNAMES[1]))),          vsReferences[19]);

        ImmutableSet<Scope> scopeSet = new ImmutableSet.Builder<Scope>()
                        .add(Scope.builder(SCOPE_APPLICATION_NAME_KEY, "application-0").build()).build();
        // Two direct mapping to specific applications
        mapping.put( new MappingKey(scopeSet), vsReferences[20]);
        scopeSet = new ImmutableSet.Builder<Scope>()
                        .add(Scope.builder(SCOPE_APPLICATION_NAME_KEY, "application-1").build()).build();
        mapping.put( new MappingKey(scopeSet), vsReferences[21]);
        
    }
    
    
    /**
     * ScopePrecedenceConfiguration is as follows:
     * "env;env+region;env+region+stack;hostname;application"
     * 
     * Mappings are as follows: 
     * ("env=env-0") => test-version-set-0
     * ("env=env-0,region=region-0") => test-version-set-1
     * ("env=env-0,region=region-0,stack=stack-0") => test-version-set-2
     * ("env=env-0,region=region-0,stack=stack-1") => test-version-set-3
     * ("env=env-0,region=region-0,stack=stack-2") => test-version-set-4
     * ("env=env-0,region=region-1") => test-version-set-5
     * ("env=env-0,region=region-1,stack=stack-0") => test-version-set-6
     * ("env=env-0,region=region-1,stack=stack-1") => test-version-set-7
     * ("env=env-0,region=region-1,stack=stack-2") => test-version-set-8
     * ("env=env-1") => test-version-set-9
     * ("env=env-1,region=region-0") => test-version-set-10
     * ("env=env-1,region=region-0,stack=stack-0") => test-version-set-11
     * ("env=env-1,region=region-0,stack=stack-1") => test-version-set-12
     * ("env=env-1,region=region-0,stack=stack-2") => test-version-set-13
     * ("env=env-1,region=region-1") => test-version-set-14
     * ("env=env-1,region=region-1,stack=stack-0") => test-version-set-15
     * ("env=env-1,region=region-1,stack=stack-1") => test-version-set-16
     * ("env=env-1,region=region-1,stack=stack-2") => test-version-set-17
     * ("hostname=hostname-0") => test-version-set-18
     * ("env=env-0,hostname=hostname-1") => test-version-set-19 
     * This mapping becomes unfetchable because it mixes sepparate ScopePrecedence configuration key groups. 
     * Adding the fact that we append the application name when doing the queries (application name is mandatory for queries), makes exact matching impossible.
     * TODO: Should we validate cases like the one above so it's not possible to add them? It seams bad to allow adding mappings that can never be fetched.
     * ("application=application-0") => test-version-set-20
     * ("application=application-1") => test-version-set-21
     * 
     * Query keys and expected version set are as follows: 
     * ("env=env-2") => absent (no match) 
     * ("env=env-0,region=region-1") => test-version-set-5 (exact match) 
     * ("env=env-0,region=region-2") => test-version-set-0 (fall back to ("env=env-0")) 
     * ("env=env-1,region=region-1,stack=stack-3") => test-version-set-14 (fall back to ("env=env-1,region=region-1"))
     * ("env=env-1,region=region-1,stack=stack-2") => test-version-set-17 (exact match) 
     * ("env=env-0,hostname=hostname-1") => test-version-set-0 (unfetchable entry case, so fall back to ("env=env-0"))
     * ("env=env-0,hostname=hostname-0") => test-version-set-18 (overwrite by most specific match)
     * ("hostname=hostname-1") => absent (no match, because there is no hostname=hostname-1 by itself)
     * ("application=application-0") => test-version-set-20 (exact match)
     * ("env=env-0,region=region-1,application=application-1") => test-version-set-21 (overwrite by most specificmatch)
     */
    @Test
    public void test() {
        
        Optional<VersionSetReference> vs;
        
        // ("env=env-2") => absent (no match)
        vs = mapping.get(new MappingKey(addApplicationToScope("unmatchableAppName", ScopeUtils.parseScopeString("env=env-2"))));
        assertFalse(vs.isPresent());
        
        // ("env=env-0,region=region-1") => test-version-set-5 (exact match)
        vs = mapping.get(new MappingKey(addApplicationToScope("unmatchableAppName", ScopeUtils.parseScopeString("env=env-0,region=region-1"))));
        assertEquals("test-version-set-5", vs.get().getName());
        
        // ("env=env-0,region=region-2") => test-version-set-0 (fall back to ("env=env-0")) 
        vs = mapping.get(new MappingKey(addApplicationToScope("unmatchableAppName", ScopeUtils.parseScopeString("env=env-0,region=region-2"))));
        assertEquals("test-version-set-0", vs.get().getName());

        // ("env=env-1,region=region-1,stack=stack-3") => test-version-set-14 (fall back to ("env=env-1,region=region-1"))
        vs = mapping.get(new MappingKey(addApplicationToScope("unmatchableAppName", ScopeUtils.parseScopeString("env=env-1,region=region-1,stack=stack-3"))));
        assertEquals("test-version-set-14", vs.get().getName());

        // ("env=env-1,region=region-1,stack=stack-2") => test-version-set-17 (exact match) 
        vs = mapping.get(new MappingKey(addApplicationToScope("unmatchableAppName", ScopeUtils.parseScopeString("env=env-1,region=region-1,stack=stack-2"))));
        assertEquals("test-version-set-17", vs.get().getName());
        
        // ("env=env-0,hostname=hostname-1") => test-version-set-0 (unfetchable entry case, so fall back to ("env=env-0"))
        vs = mapping.get(new MappingKey(addApplicationToScope("unmatchableAppName", ScopeUtils.parseScopeString("env=env-0,hostname=hostname-1"))));
        assertEquals("test-version-set-0", vs.get().getName());
        
        // ("env=env-0,hostname=hostname-0") => test-version-set-18 (overwrite by most specific match)
        vs = mapping.get(new MappingKey(addApplicationToScope("unmatchableAppName", ScopeUtils.parseScopeString("env=env-0,hostname=hostname-0"))));
        assertEquals("test-version-set-18", vs.get().getName());
        
        // ("hostname=hostname-1") => absent (no match, because there is no hostname=hostname-1 by itself)
        vs = mapping.get(new MappingKey(addApplicationToScope("unmatchableAppName", ScopeUtils.parseScopeString("hostname=hostname-1"))));
        assertFalse(vs.isPresent());
        
        // ("application=application-0") => test-version-set-20 (exact match)
        vs = mapping.get(new MappingKey(addApplicationToScope("application-0", null)));
        assertEquals("test-version-set-20", vs.get().getName());
        
        // ("env=env-0,region=region-1,application=application-1") => test-version-set-21 (overwrite by most specificmatch)
        vs = mapping.get(new MappingKey(addApplicationToScope("application-1", ScopeUtils.parseScopeString("env=env-0,region=region-1"))));
        assertEquals("test-version-set-21", vs.get().getName());

    }
    
    @Test
    public void testJsonSerialization() throws Exception {
        
        String mappingJson = jsonUtils.toJson(mapping.getInnerRepresentation());
        System.out.println("json=" + mappingJson);
        
        final JavaType type = mapper.getTypeFactory().constructMapType(mapping.getInnerRepresentation().getClass(), MappingKey.class, VersionSetReference.class);
        Map<MappingKey, VersionSetReference> map = mapper.readValue(mappingJson, type);
        for(Map.Entry<MappingKey, VersionSetReference> entry : map.entrySet()) {
        	System.out.println(entry.getKey().getScopeSet() + " =====> " + entry.getValue().getName());
        }
        
    }
    
    
    private ImmutableSet<Scope> addApplicationToScope(String application, Set<Scope> scopeSet) {
        Set<Scope> myScopeSet = (scopeSet == null)? new HashSet<>() : new HashSet<>(scopeSet);
        myScopeSet.add(Scope.builder(SCOPE_APPLICATION_NAME_KEY, application).build());
        
        return ImmutableSet.copyOf(myScopeSet);
    }    
    
}
