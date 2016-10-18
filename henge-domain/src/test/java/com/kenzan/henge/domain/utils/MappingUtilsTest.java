package com.kenzan.henge.domain.utils;

import static org.junit.Assert.assertEquals;

import com.kenzan.henge.domain.model.Scope;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;


/**
 * Tests for the MappingUtils class
 *
 * @author wmatsushita
 */
public class MappingUtilsTest {

    @Test
    public void testExtractScopeKeys() {
        
        final Set<Scope> scopeSet = ScopeUtils.parseScopeString("dev=env,stack=stack-1,region=US-east-1");
        final Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("dev", "env");
        expectedMap.put("stack", "stack-1");
        expectedMap.put("region", "US-east-1");
        
        assertEquals(expectedMap, ScopeUtils.toMap(scopeSet));
    }

    @Test
    public void testToMap() {
        
        final Set<Scope> scopeSet = ScopeUtils.parseScopeString("dev=env,stack=stack-1,region=US-east-1");
        final Set<String> expectedKeys = new HashSet<>();
        expectedKeys.add("dev");
        expectedKeys.add("stack");
        expectedKeys.add("region");
        
        assertEquals(expectedKeys, ScopeUtils.extractScopeKeys(scopeSet));
    }
    
    @Test
    public void testSubScopeSet() {
        
        final Set<Scope> scopeSet = ScopeUtils.parseScopeString("dev=env,stack=stack-1,region=US-east-1");
        final Set<String> keys = new HashSet<>();
        keys.add("dev"); keys.add("stack");
        
        final Set<Scope> expectedScopeSet = ScopeUtils.parseScopeString("dev=env,stack=stack-1");
        
        assertEquals(expectedScopeSet, ScopeUtils.subScopeSet(scopeSet, keys));
    }

}
