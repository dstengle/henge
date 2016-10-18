package com.kenzan.henge.domain.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.kenzan.henge.domain.utils.ScopeUtils;

/**
 * Implementation of the mapping index as a HashMap. When getting a
 * {@link VersionSetReference}, it searches the map for a match as follows: 
 * 1 - iterates through all the entries looking for a mapping with the same
 * application name and version and returns that, if found. 
 * 2 - iterates a second time, doing a set difference operation on the scope 
 * sets (currentMappingKey.getScopeSet() - givenMappingKey.getScopeSet()) and
 * considers a match if the resulting set is empty. This allows the matching of
 * a more generic mapping in case the given key is not matched exactly.
 *
 * @author wmatsushita
 */
public class VersionSetMapping implements Mapping<VersionSetReference> {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4850431686242113298L;

	private static final Logger LOGGER = LoggerFactory.getLogger(VersionSetMapping.class);
    
    private ScopePrecedenceConfiguration scopePrecedenceConfiguration;

    private Map<MappingKey, VersionSetReference> map;

    public VersionSetMapping(ScopePrecedenceConfiguration scopePrecedenceConfiguration) {

        map = new ConcurrentHashMap<>();
        this.scopePrecedenceConfiguration = scopePrecedenceConfiguration;
        
    }

    /**
     * Gets the {@link VersionSetReference} mapped to the given key. If not
     * found, this implementation returns the best, more generic mapping, as
     * defined in the {@link ScopePrecedenceConfiguration}.
     * 
     * @param queryKey the {@link MappingKey} containing the desired mapped
     *        value.
     * @return the {@link VersionSetReference} mapped to the given key, if it
     *         exists.
     */
    @Override
    public Optional<VersionSetReference> get(MappingKey queryKey) {

        VersionSetReference result = null;
        
        // in cases like application direct mapping, if we don't try the exact match first, the algorithm will waste time.
        if(map.containsKey(queryKey)) {
            return Optional.ofNullable(map.get(queryKey));
        }
        
        Set<Scope> queryScopeSet = queryKey.getScopeSet();
        List<Set<Scope>> matches = new LinkedList<>();
        
        LOGGER.debug("");
        LOGGER.debug("queryScopeSet: " + queryScopeSet.toString());
        
        if (!queryScopeSet.isEmpty()) {
            for (ImmutableSet<String> configScopeKeys : scopePrecedenceConfiguration.getInnerRepresentation()) {
    
                // if the set of keys of queryScopeSet do not contain all of the current configScopeKeys, skip this iteration
                if(!ScopeUtils.extractScopeKeys(queryScopeSet).containsAll(configScopeKeys)) {
                    continue;
                }
                
                // gets the intersection between the scope precedence key set and
                // the mapping key scope key set
                ImmutableSet<Scope> intersection = ScopeUtils.subScopeSet(queryScopeSet, configScopeKeys);
    
                for (Map.Entry<MappingKey, VersionSetReference> currentMappingEntry : map.entrySet()) {
                    Set<Scope> scopeSet = currentMappingEntry.getKey().getScopeSet();
    
                    // skip this iteration if the scope set is null
                    if (scopeSet == null)
                        continue;
    
                    LOGGER.debug("-----------------------------------------------");
                    LOGGER.debug("mappingScopeSet: " + scopeSet.toString());
                    LOGGER.debug("intersection: " + intersection.toString());
                    LOGGER.debug("-----------------------------------------------");
                    
                    // here we are doing the set difference operation: {mappingSet -
                    // querySet = emptySet} -> found match
                    if (intersection.containsAll(scopeSet) && !matches.contains(scopeSet)) {
                        matches.add(scopeSet);
                        result = currentMappingEntry.getValue();
                        LOGGER.debug("Match!!! ---> " + result.getName());
                    }
                }
            }
        }

        return Optional.ofNullable(result);
    }

    /**
     * Inserts a new mapped {@link VersionSetReference}.
     * 
     * @param key the {@link MappingKey} to index the entry by.
     * @param value the {@link VersionSetReference} to bind to the given key.
     */
    @Override
    public void put(MappingKey key, VersionSetReference value) {

        map.put(key, value);
    }

    /**
     * @see com.kenzan.henge.domain.model.Mapping#remove(com.kenzan.henge.domain.model.MappingKey)
     */
    @Override
    public void remove(MappingKey key) {

        map.remove(key);

    }

    /**
     * @see com.kenzan.henge.domain.model.Mapping#getInnerRepresentation()
     */
    @Override
    public Map<MappingKey, VersionSetReference> getInnerRepresentation() {

        return map;

    }
    
    @Override
    public void setInnerRepresentation(Map<MappingKey, VersionSetReference> map) {
        
        this.map = new ConcurrentHashMap<>(map);
        
    }
    
    @Override
    public int hashCode() {
        
        return Objects.hashCode(map);

    }

    @Override
    public boolean equals(Object obj) {

        if (null == obj || !(obj instanceof VersionSetMapping)) {
            return false;
        }

        VersionSetMapping that = (VersionSetMapping) obj;

        return Objects.equal(map, that.getInnerRepresentation());

    }

    @Override
    public String toString() {

        return MoreObjects.toStringHelper(this).add("map", map).toString();
        
    }
    
}
