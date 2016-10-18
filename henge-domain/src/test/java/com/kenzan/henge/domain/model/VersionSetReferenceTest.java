package com.kenzan.henge.domain.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import org.junit.Before;
import org.junit.Test;


/**
 * Unit tests for {@link VersionSetReference}.
 *
 * @author wmatsushita
 */
public class VersionSetReferenceTest {

    private static final String NAME = "name";
    private static final String VERSION = "version";
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

    }

    /**
     * Test method for {@link com.kenzan.henge.domain.model.VersionSetReference#builder(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testBuilderStringString() {
        
        final VersionSetReference fileVersionReference = VersionSetReference.builder(NAME, VERSION).build();
        assertThat(fileVersionReference.getName(), equalTo(NAME));
        assertThat(fileVersionReference.getVersion(), equalTo(VERSION));
        
    }

    /**
     * Test method for {@link com.kenzan.henge.domain.model.VersionSetReference#builder(com.kenzan.henge.domain.model.VersionSetReference)}.
     */
    @Test
    public void testBuilderVersionSetReference() {

        final VersionSetReference fileVersionReference = VersionSetReference.builder(NAME, VERSION).build();
        final VersionSetReference copy = VersionSetReference.builder(fileVersionReference).build();
        
        assertThat(copy.getName(), equalTo(fileVersionReference.getName()));
        assertThat(copy.getVersion(), equalTo(fileVersionReference.getVersion()));

    }

    /**
     * Test method for {@link com.kenzan.henge.domain.model.VersionSetReference#builder(com.kenzan.henge.domain.model.VersionSet)}.
     */
    @Test
    public void testBuilderVersionSet() {
        final VersionSet fileVersion = VersionSet.builder(NAME, VERSION).build();
        
        VersionSetReference fileVersionReference = VersionSetReference.builder(fileVersion).build();

        assertThat(fileVersionReference.getName(), equalTo(fileVersion.getName()));
        assertThat(fileVersionReference.getVersion(), equalTo(fileVersion.getVersion()));

    }
    
    @Test
    public void testEquals() {

        final VersionSetReference fileVersionReference = VersionSetReference.builder(NAME, VERSION).build();
        final VersionSetReference copy1 = VersionSetReference.builder(fileVersionReference).build();
        
        final VersionSetReference copy2 = VersionSetReference.builder(fileVersionReference).withVersion("1.0.1").build();
        
        assertThat(copy1, equalTo(fileVersionReference));
        assertThat(copy2, not(fileVersionReference));
        
    }

}
