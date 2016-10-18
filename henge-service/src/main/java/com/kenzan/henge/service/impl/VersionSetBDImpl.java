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

import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.kenzan.henge.config.CacheConfig;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.exception.HengeValidationException;
import com.kenzan.henge.repository.VersionSetRepository;
import com.kenzan.henge.service.VersionSetBD;

/**
 * @author kylebober
 *
 */
@Component
public class VersionSetBDImpl implements VersionSetBD {

    private VersionSetRepository versionSetRepository;
    
	private Cache versionSetCache;
    
    @Autowired
    public VersionSetBDImpl(VersionSetRepository versionSetRepository, @Qualifier(CacheConfig.VERSION_SET_CACHE) Cache versionSetCache) {
        this.versionSetRepository = versionSetRepository;
        this.versionSetCache = versionSetCache;
    }
    
	@Override
	public VersionSet create(final VersionSet entity) {
	    
	    final VersionSet newEntity = (VersionSet.builder(entity)).withCreatedDate(LocalDateTime.now()).build();
		VersionSet versionSet = versionSetRepository.create(newEntity);
		
		versionSetCache.put(entity.getName(), Optional.of(versionSet));
		versionSetCache.put(entity.getName() + entity.getVersion(), Optional.of(versionSet));
		
		return versionSet;
		
	}

	@Override
	public Optional<VersionSet> update(final String name, final VersionSet entity) {
		
		Optional<VersionSet> last = versionSetRepository.read(name);
		if (!last.isPresent()) {
			return Optional.empty();
		}
		if (entity.compareTo(last.get()) <= 0) {
			throw new HengeValidationException(Status.CONFLICT, "The VersionSet " + entity.getName()
                    + " object given for update has a version number [" + entity.getVersion()
                    + "] that is lesser than or equal to the current version [" + last.get().getVersion()
                    + "].");
		}
	    
	    final VersionSet newEntity = (VersionSet.builder(entity)).build();
	    Optional<VersionSet> versionSet = versionSetRepository.update(name, newEntity);
	    
	    versionSetCache.put(entity.getName(), versionSet);
		versionSetCache.put(entity.getName() + entity.getVersion(), versionSet);
	    
	    return versionSet;
	}

	@Override
	@CacheEvict(cacheNames=CacheConfig.VERSION_SET_CACHE, allEntries=true)
	public Optional<VersionSet> delete(final String name) {
	    
	    return versionSetRepository.delete(name);
	    
	}
	
	@Override
	@CacheEvict(cacheNames=CacheConfig.VERSION_SET_CACHE, allEntries=true)
    public Optional<VersionSet> delete(String name, String version) {
	    
	    return versionSetRepository.delete(name, version);
	    
    }

	@Override
	@Cacheable(value = CacheConfig.VERSION_SET_CACHE, key = "#name")
	public Optional<VersionSet> read(final String name) {
	    
	    return versionSetRepository.read(name);
	    
	}

	@Override
	@Cacheable(value = CacheConfig.VERSION_SET_CACHE, key= "#name + #version")
    public Optional<VersionSet> read(String name, String version) {

	    return versionSetRepository.read(name, version);
	    
    }
	
	@Override
    public Optional<Set<String>> versions(String name) {
        return versionSetRepository.versions(name);
    }

}
