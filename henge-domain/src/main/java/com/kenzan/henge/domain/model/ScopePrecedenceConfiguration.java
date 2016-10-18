package com.kenzan.henge.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kenzan.henge.domain.utils.ScopeUtils;

/**
 * This class is used to provide a means of ordering the scope key
 * combination from most generic to most specific. It is used in the
 * {@link Property} evaluation process and in matching a query to a
 * {@link VersionSet}.   
 * Example: 
 *
 * @author wmatsushita
 */
@Component
public class ScopePrecedenceConfiguration implements Serializable {

    public static final String SCOPE_CONFIGURATION_DELIMITER = ";";
    public static final String SCOPE_CONFIGURATION_AGGREGATOR = "+";

    private ImmutableList<ImmutableSet<String>> scopePrecedence;

    /**
     * Creates the Scope Precedence Configuration from the configuration string.
     * Example: 
     * Configuration String: dev;dev+stack;dev+stack+region;hostname;application 
     * Result: 
     * [ 
     *      ["dev"], 
     *      ["dev","stack"], 
     *      ["dev","stack","region"], 
     *      ["hostname"], 
     *      ["application"]
     * ]
     */
    public ScopePrecedenceConfiguration(String configString) {

        List<ImmutableSet<String>> result = new ArrayList<>();

        String[] configEntries = configString.split(SCOPE_CONFIGURATION_DELIMITER);

        for (String configEntry : configEntries) {
            ImmutableSet<String> scopeSet = ImmutableSet.copyOf(configEntry.split("\\" + SCOPE_CONFIGURATION_AGGREGATOR));
            result.add(scopeSet);
        }

        this.scopePrecedence = ImmutableList.copyOf(result);

    }

    /**
     * Returns the position where the set of keys is found in the ScopePrecedenceConfiguration, or an absent
     * Optional if it is not found at all.
     * 
     * @param scopeSet the set of scopes
     * @return the {@link Optional} containing the index of occurrence or absent
     * @throws NullPointerException if the given scopeSet is null
     */
    public Optional<Integer> indexOfScopeKeys(Set<Scope> scopeSet) {
        
        Preconditions.checkNotNull(scopeSet, "The scopeSet must not be null");
        
        Set<String> keys = ScopeUtils.extractScopeKeys(scopeSet);
        int i = scopePrecedence.indexOf(keys);
        
        return (i == -1)? Optional.absent() : Optional.of(i); 
    }
    
    /**
     * @return Returns the inner representation of the ScopePrecedenceConfiguration.
     */
    public ImmutableList<ImmutableSet<String>> getInnerRepresentation() {
        return scopePrecedence;
    }
   
    

}
