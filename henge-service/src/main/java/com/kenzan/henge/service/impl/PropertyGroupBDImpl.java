/**
 * Copyright (C) ${project.inceptionYear} Kenzan - Kyle S. Bober (kbober@kenzan.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kenzan.henge.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.kenzan.henge.config.CacheConfig;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.model.type.PropertyGroupType;
import com.kenzan.henge.exception.HengeValidationException;
import com.kenzan.henge.repository.PropertyGroupRepository;
import com.kenzan.henge.service.PropertyGroupBD;

/**
 * @author kylebober
 * @author Igor K. Shiohara
 *
 */
@Component
public class PropertyGroupBDImpl implements PropertyGroupBD {

	private PropertyGroupRepository propertyGroupRepository;
	
	private Cache propertyGroupCache;

	@Autowired
    public PropertyGroupBDImpl(PropertyGroupRepository propertyGroupRepository, @Qualifier(CacheConfig.PROPERTY_GROUP_CACHE) Cache propertyGroupCache) {
        this.propertyGroupRepository = propertyGroupRepository;
        this.propertyGroupCache = propertyGroupCache;
    }

	@Override
	public PropertyGroup create(final PropertyGroup entity) {
		final PropertyGroup newEntity = (PropertyGroup.builder(entity)).withCreatedDate(LocalDateTime.now()).build();
		final PropertyGroup propertyGroup = propertyGroupRepository.create(newEntity);
		
		propertyGroupCache.put(entity.getName(), Optional.of(propertyGroup));
		propertyGroupCache.put(entity.getName() + entity.getVersion(), Optional.of(propertyGroup));
		
		return propertyGroup;
		
	}

	@Override
	public Optional<PropertyGroup> update(final String propertyGroupName, final PropertyGroup entity) {
		Optional<PropertyGroup> last = propertyGroupRepository.read(propertyGroupName);
		if (!last.isPresent()) {
			return Optional.empty();
		}
		if (entity.compareTo(last.get()) <= 0) {
			throw new HengeValidationException(Status.CONFLICT, "The " + entity.getName()
                    + " object given for update has a version number [" + entity.getVersion()
                    + "] that is lesser than or equal to the current version [" + last.get().getVersion()
                    + "].");
		}
		
	    final PropertyGroup newEntity = (PropertyGroup.builder(entity)).build();
	    final Optional<PropertyGroup> propertyGroup = propertyGroupRepository.update(propertyGroupName, newEntity); 
	    
	    propertyGroupCache.put(entity.getName(), propertyGroup);
	    propertyGroupCache.put(entity.getName() + entity.getVersion(), propertyGroup);
	    
	    
		return propertyGroup;		
	}

	@Override
	@CacheEvict(cacheNames=CacheConfig.PROPERTY_GROUP_CACHE, key = "#propertyGroupName")
	public Optional<PropertyGroup> delete(final String propertyGroupName) {
		
		return propertyGroupRepository.delete(propertyGroupName);
		
	}
	
	@Override
	@CacheEvict(cacheNames=CacheConfig.PROPERTY_GROUP_CACHE, key = "#name + #version")
	public Optional<PropertyGroup> delete(String name, String version) {
		
		return propertyGroupRepository.delete(name, version);
		
	}
	
	@Override
	@Cacheable(value = CacheConfig.PROPERTY_GROUP_CACHE, key = "#propertyGroupName")
	public Optional<PropertyGroup> read(final String propertyGroupName) {
		
		return propertyGroupRepository.read(propertyGroupName);
		
	}

	@Override
	@Cacheable(value = CacheConfig.PROPERTY_GROUP_CACHE, key = "#propertyGroupName + #propertyGroupVersion")
	public Optional<PropertyGroup> read(final String propertyGroupName, final String propertyGroupVersion) {
		
		return propertyGroupRepository.read(propertyGroupName, propertyGroupVersion);
		
	}
	
    @Override
	public Optional<Set<String>> versions(String propertyGroupName) {

        return propertyGroupRepository.versions(propertyGroupName);
    }

	/**
	 * Retrieve all PropertyGroup filtering by {@link PropertyGroupType} and libraries
	 */
	@Override
	public Set<PropertyGroup> getPropertyGroup(final VersionSet versionSet, final PropertyGroupType type, final java.util.Optional<Set<String>> libs) {
		return versionSet.getPropertyGroupReferences().parallelStream()
				.map(reference -> propertyGroupRepository.read(reference.getName(), reference.getVersion()).get())
				.filter(PropertyGroupType.LIB.equals(type) ? libsPredicate(libs.get()) : appPredicate())
				.collect(Collectors.toSet());
	}

	/**
	 * Retrieve all PropertyGroup filtering by {@link PropertyGroupType}
	 */
	@Override
	public Set<PropertyGroup> getPropertyGroup(final VersionSet versionSet, final PropertyGroupType type) {
		return getPropertyGroup(versionSet, type, java.util.Optional.empty());
	}
	
	private Predicate<PropertyGroup> appPredicate() {
		return pg -> PropertyGroupType.APP.toString().equalsIgnoreCase(pg.getType()) && 
				pg.isActive();
	}
	
	private Predicate<PropertyGroup> libsPredicate(final Set<String> libs) {
		if (libs.isEmpty()) {
			return pg -> PropertyGroupType.LIB.toString().equalsIgnoreCase(pg.getType()) &&
					pg.isActive();
		}
		final Set<String> trimLibs = libs.parallelStream().map(lib -> StringUtils.trim(lib)).collect(Collectors.toSet());
		return pg -> PropertyGroupType.LIB.toString().equalsIgnoreCase(pg.getType()) && trimLibs.contains(pg.getName()) && 
				pg.isActive();
	}

}
