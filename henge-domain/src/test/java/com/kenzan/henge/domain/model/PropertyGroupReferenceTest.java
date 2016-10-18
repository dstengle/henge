package com.kenzan.henge.domain.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import org.junit.Before;
import org.junit.Test;


/**
 * Unit tests for {@link PropertyGroupReference}.
 *
 * @author wmatsushita
 */
public class PropertyGroupReferenceTest {

    private static final String NAME = "name";
    private static final String VERSION = "version";
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

    }

    /**
     * Test method for {@link com.kenzan.henge.domain.model.PropertyGroupReference#builder(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testBuilderStringString() {
        
        final PropertyGroupReference fileVersionReference = PropertyGroupReference.builder(NAME, VERSION).build();
        assertThat(fileVersionReference.getName(), equalTo(NAME));
        assertThat(fileVersionReference.getVersion(), equalTo(VERSION));
        
    }

    /**
     * Test method for {@link com.kenzan.henge.domain.model.PropertyGroupReference#builder(com.kenzan.henge.domain.model.PropertyGroupReference)}.
     */
    @Test
    public void testBuilderPropertyGroupReference() {

        final PropertyGroupReference fileVersionReference = PropertyGroupReference.builder(NAME, VERSION).build();
        final PropertyGroupReference copy = PropertyGroupReference.builder(fileVersionReference).build();
        
        assertThat(copy.getName(), equalTo(fileVersionReference.getName()));
        assertThat(copy.getVersion(), equalTo(fileVersionReference.getVersion()));

    }

    /**
     * Test method for {@link com.kenzan.henge.domain.model.PropertyGroupReference#builder(com.kenzan.henge.domain.model.PropertyGroup)}.
     */
    @Test
    public void testBuilderPropertyGroup() {
        final PropertyGroup fileVersion = PropertyGroup.builder(NAME, VERSION).build();
        
        PropertyGroupReference fileVersionReference = PropertyGroupReference.builder(fileVersion).build();

        assertThat(fileVersionReference.getName(), equalTo(fileVersion.getName()));
        assertThat(fileVersionReference.getVersion(), equalTo(fileVersion.getVersion()));

    }
    
    @Test
    public void testEquals() {

        final PropertyGroupReference fileVersionReference = PropertyGroupReference.builder(NAME, VERSION).build();
        final PropertyGroupReference copy1 = PropertyGroupReference.builder(fileVersionReference).build();
        
        final PropertyGroupReference copy2 = PropertyGroupReference.builder(fileVersionReference).withVersion("1.0.1").build();
        
        assertThat(copy1, equalTo(fileVersionReference));
        assertThat(copy2, not(fileVersionReference));
        
    }

}
