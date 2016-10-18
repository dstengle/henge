package com.kenzan.henge.service.impl;

import com.google.common.collect.ImmutableSet;
import com.kenzan.henge.domain.model.Mapping;
import com.kenzan.henge.domain.model.MappingKey;
import com.kenzan.henge.domain.model.Scope;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.model.VersionSetReference;
import com.kenzan.henge.exception.HengeException;
import com.kenzan.henge.repository.MappingRepository;
import com.kenzan.henge.service.VersionSetBD;
import com.kenzan.henge.service.VersionSetMappingBD;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class provides methods for finding matching {@link VersionSet} given the
 * search parameters.
 * 
 * @author wmatsushita
 */
@Component
public class VersionSetMappingBDImpl implements VersionSetMappingBD {

    public static final String SCOPE_APPLICATION_NAME_KEY = "application";
    
    private MappingRepository<VersionSetReference> mappingRepository;

    private VersionSetBD versionSetBD;
    
    private Mapping<VersionSetReference> mapping;


    @Autowired
    public VersionSetMappingBDImpl(MappingRepository<VersionSetReference> mappingRepository, VersionSetBD versionSetBD, Mapping<VersionSetReference> mapping) {
        this.versionSetBD = versionSetBD;
        this.mappingRepository = mappingRepository;
        this.mapping = mapping;
    }
    
    
    /**
     * Returns an {@link Optional} of a {@link VersionSet}, given the
     * application name and version.
     * 
     * @param application name of the application
     * @param scopeSet set of scopes 
     * @return an {@link Optional} of a {@link VersionSet} mapped the the given
     *         application name and version.
     */
    @Override
    public Optional<VersionSet> findMatch(final String application, final Set<Scope> scopeSet) {
        
        // Adds the application name as part of the scope
        ImmutableSet<Scope> scopeWithApplication = addApplicationToScope(scopeSet, application);
        
        Optional<VersionSetReference> vsReference = mapping.get(new MappingKey(scopeWithApplication));
        if (!vsReference.isPresent())
            return Optional.empty();

        return versionSetBD.read(vsReference.get().getName(), vsReference.get().getVersion());

    }
    
    /**
     * Adds a new entry for a {@link VersionSetReference} in the {@link Mapping} by the given name, with the application and scope set as keys.
     * 
     * @param application the {@link Optional} application name to bind the {@link VersionSet} to.
     * @param scopeSet the set of {@link Scope}s to bind the {@link VersionSet} to.
     */
    @Override
    public void setMapping(final Optional<String> application, Set<Scope> scopeSet, final VersionSetReference versionSetReference) {
        
        if(application.isPresent()) {
            scopeSet = addApplicationToScope(scopeSet, application.get());
        }
        
        MappingKey key = new MappingKey(ImmutableSet.copyOf(scopeSet));
        mapping.put(key, versionSetReference);
        
        mappingRepository.save(mapping);
        
    }
    
	@Override
	public Map<MappingKey, VersionSetReference> getAllMappings() {
		
		return mapping.getInnerRepresentation();
		
	}    
	
	@Override
	public void deleteMapping(Optional<String> application, Set<Scope> scopeSet) {
		
        if(application.isPresent()) {
            scopeSet = addApplicationToScope(scopeSet, application.get());
        }
        
        MappingKey key = new MappingKey(ImmutableSet.copyOf(scopeSet));
        
        if (!mapping.get(key).isPresent()){
            throw new HengeException(Status.NOT_FOUND, "No VersionSet Mapping found for application: " + application.get());
        }
        
        mapping.remove(key);
        
        mappingRepository.save(mapping);
		
	}


	private ImmutableSet<Scope> addApplicationToScope(Set<Scope> scopeSet, String application) {
        Set<Scope> myScopeSet = (scopeSet == null)? new HashSet<>() : new HashSet<>(scopeSet);
        myScopeSet.add(Scope.builder(SCOPE_APPLICATION_NAME_KEY, application).build());
        
        return ImmutableSet.copyOf(myScopeSet);
    }

    
}
