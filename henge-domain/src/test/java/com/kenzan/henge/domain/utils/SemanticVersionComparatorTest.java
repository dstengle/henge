package com.kenzan.henge.domain.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;


/**
 * Unit test for {@link SemanticVersionComparator} class
 *
 * @author wmatsushita
 */
public class SemanticVersionComparatorTest {

    /**
     * Test method for {@link com.kenzan.henge.domain.utils.SemanticVersionComparator#compare(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCompare() {
        
        final List<String> expectedVersionList = new ArrayList<>();
        expectedVersionList.add("0.0.1");
        expectedVersionList.add("0.0.2");
        expectedVersionList.add("0.1.0");
        expectedVersionList.add("0.1.1");
        expectedVersionList.add("1.0.0");
        expectedVersionList.add("1.0.1");
        expectedVersionList.add("5.0.0");
        expectedVersionList.add("6.1.2");
        
        final List<String> versionList = new ArrayList<>(expectedVersionList);
        Collections.shuffle(versionList);
        
        assertFalse(expectedVersionList.equals(versionList));
    
        Collections.sort(versionList, new SemanticVersionComparator());
        
        assertEquals(expectedVersionList, versionList);
        
    }

}
