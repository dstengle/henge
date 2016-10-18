package com.kenzan.henge.domain.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import org.junit.Before;
import org.junit.Test;


/**
 * Unit tests for {@link FileVersionReference}.
 *
 * @author wmatsushita
 */
public class FileVersionReferenceTest {

    private static final String NAME = "name";
    private static final String VERSION = "version";
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

    }

    /**
     * Test method for {@link com.kenzan.henge.domain.model.FileVersionReference#builder(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testBuilderStringString() {
        
        final FileVersionReference fileVersionReference = FileVersionReference.builder(NAME, VERSION).build();
        assertThat(fileVersionReference.getName(), equalTo(NAME));
        assertThat(fileVersionReference.getVersion(), equalTo(VERSION));
        
    }

    /**
     * Test method for {@link com.kenzan.henge.domain.model.FileVersionReference#builder(com.kenzan.henge.domain.model.FileVersionReference)}.
     */
    @Test
    public void testBuilderFileVersionReference() {

        final FileVersionReference fileVersionReference = FileVersionReference.builder(NAME, VERSION).build();
        final FileVersionReference copy = FileVersionReference.builder(fileVersionReference).build();
        
        assertThat(copy.getName(), equalTo(fileVersionReference.getName()));
        assertThat(copy.getVersion(), equalTo(fileVersionReference.getVersion()));

    }

    /**
     * Test method for {@link com.kenzan.henge.domain.model.FileVersionReference#builder(com.kenzan.henge.domain.model.FileVersion)}.
     */
    @Test
    public void testBuilderFileVersion() {
        final FileVersion fileVersion = FileVersion.builder(NAME, VERSION, "content".getBytes(), "testfile.txt").build();
        
        FileVersionReference fileVersionReference = FileVersionReference.builder(fileVersion).build();

        assertThat(fileVersionReference.getName(), equalTo(fileVersion.getName()));
        assertThat(fileVersionReference.getVersion(), equalTo(fileVersion.getVersion()));

    }
    
    @Test
    public void testEquals() {

        final FileVersionReference fileVersionReference = FileVersionReference.builder(NAME, VERSION).build();
        final FileVersionReference copy1 = FileVersionReference.builder(fileVersionReference).build();
        
        final FileVersionReference copy2 = FileVersionReference.builder(fileVersionReference).withVersion("1.0.1").build();
        
        assertThat(copy1, equalTo(fileVersionReference));
        assertThat(copy2, not(fileVersionReference));
        
    }

}
