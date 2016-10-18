package com.kenzan.henge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.cache.Cache;
import org.springframework.cache.guava.GuavaCache;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.type.PropertyGroupType;
import com.kenzan.henge.exception.HengeValidationException;
import com.kenzan.henge.repository.PropertyGroupRepository;
import com.kenzan.henge.service.impl.PropertyGroupBDImpl;

@RunWith(MockitoJUnitRunner.class)
public class PropertyGroupBDTest extends FixtureBD {
    
	@InjectMocks
	private PropertyGroupBDImpl propertyGroupBD;
	
	@Mock
	private PropertyGroupRepository propertyGroupRepository;
	
	@Rule
    public final ExpectedException exception = ExpectedException.none();
	
	@Mock
	private Cache propertyGroupCache;
	
	@Test
	public void getOnlyLibsTest() {
	    
        when(propertyGroupRepository.read("PropertyGroup-App", VERSION_1)).thenReturn(Optional.of(app));
        when(propertyGroupRepository.read("PropertyGroup-Lib-1", VERSION_1)).thenReturn(Optional.of(lib1));
        when(propertyGroupRepository.read("PropertyGroup-Lib-2", VERSION_1)).thenReturn(Optional.of(lib2));
        when(propertyGroupRepository.read("PropertyGroup-Lib-3", VERSION_1)).thenReturn(Optional.of(lib3));
		
	    final Set<PropertyGroup> propertyGroup = propertyGroupBD.getPropertyGroup(versionSet, PropertyGroupType.LIB, java.util.Optional.of(Sets.newHashSet(lib1.getName(), lib2.getName(), lib3.getName())));
		assertEquals(3, propertyGroup.size());
		
		final Set<PropertyGroup> propertyGroup2 = propertyGroupBD.getPropertyGroup(versionSet, PropertyGroupType.LIB, java.util.Optional.of(Sets.newHashSet(lib1.getName(), lib2.getName())));
		assertEquals(2, propertyGroup2.size());
	}
	
    @Test
    public void testReadPropertyGroup() {
        
        when(propertyGroupRepository.read(app.getName(), app.getVersion())).thenReturn(Optional.of(app));
        
        final PropertyGroup propertyGroup = propertyGroupBD.read(app.getName(), app.getVersion()).get();
        assertThat(propertyGroup).isEqualTo(app);
    }
    
    @Test
    public void testCreatePropertyGroup() {
        
        when(propertyGroupRepository.create(app)).thenReturn(app);
        doNothing().when(propertyGroupCache).put(Mockito.any(), Mockito.any());

        final PropertyGroup newApp = propertyGroupBD.create(app);

        assertThat(newApp).isEqualTo(app);
    }
    
    @Test
    public void testReadNonExistingFile() {

        when(propertyGroupRepository.read("thisPropertyDoesNotExist", "thisVersionDoesNotExist")).thenReturn(Optional.empty());
        
        Optional<PropertyGroup> entity = propertyGroupBD.read("thisPropertyDoesNotExist", "thisVersionDoesNotExist");
        assertThat(entity.isPresent()).isFalse();
        
    }
    
    @Test
    public void testUpdateNonExistingFile() {
        
        when(propertyGroupRepository.read("test-property-group-update")).thenReturn(Optional.empty());

        final Optional<PropertyGroup> entity = propertyGroupBD.update("test-property-group-update", app);
        assertThat(entity.isPresent()).isFalse();
            
    }
    
	@Test
	public void testUpdateLesserOrEqualVersion() throws Exception {
		when(propertyGroupRepository.read(Mockito.anyString())).thenReturn(Optional.of(app));		

		exception.expect(HengeValidationException.class);
		exception.expectMessage("The PropertyGroup-App object given for update has a version number [1.0.0] that is lesser than or equal to the current version [1.0.0].");
		PropertyGroup newPG = PropertyGroup.builder(app).withVersion(VERSION_1).build();

		propertyGroupBD.update("test-property-group-update", newPG);
	}
    
    @Test
    public void testDeleteNonExistentFile() {
        
        when(propertyGroupRepository.delete("This name does not exist", "This version does not exist")).thenReturn(Optional.empty());
        
        final Optional<PropertyGroup> entity = propertyGroupBD.delete("This name does not exist", "This version does not exist");
        assertThat(entity.isPresent()).isFalse();
        
    }
    
    @Test
    public void testDelete() {
        
        when(propertyGroupRepository.delete(app.getName(), app.getVersion())).thenReturn(Optional.of(app));
        
        final Optional<PropertyGroup> deleted = propertyGroupBD.delete(app.getName(), app.getVersion());
        
        assertThat(deleted.isPresent()).isTrue();
    }
    
    @Test
    public void testVersions() {
        
        final Set<String> expectedVersions = Sets.newHashSet("1.0.0", "1.0.1");
        
        when(propertyGroupRepository.versions("test-property-group-versions")).thenReturn(Optional.of(expectedVersions));
        
        final Set<String> versions = propertyGroupBD.versions("test-property-group-versions").get();
        
        assertThat(versions).isEqualTo(expectedVersions);
    }
    
}
