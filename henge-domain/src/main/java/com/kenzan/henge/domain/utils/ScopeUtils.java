package com.kenzan.henge.domain.utils;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kenzan.henge.domain.model.Property;
import com.kenzan.henge.domain.model.PropertyScopedValue;
import com.kenzan.henge.domain.model.Scope;
import com.kenzan.henge.exception.HengeParseException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

/**
 * This class provides data structure transformations of mapping model objects
 * in order to assist algorithms that use them.
 *
 * @author wmatsushita
 */
public class ScopeUtils {
    
    // This constants are used for parsing a scope string into a Set<Scope>
    public static final String SCOPE_DELIMITER = ",";
    public static final String SCOPE_EQUAL = "=";
    private static final String SCOPE_STRING_PATTERN = "([a-zA-Z0-9_\\-.]+=[a-zA-Z0-9_\\-.]+,)*([a-zA-Z0-9_\\-.]+=[a-zA-Z0-9_\\-.]+)+";
    
    private ScopeUtils() {
    	
    }
    
    /**
     * Extracts the keys of a set of {@link Scope}. This is useful for applying
     * the scope precedence configuration.
     * 
     * @param scopeSet
     * @return a set of the keys of the {@link Scope} as an {@link ImmutableSet}
     *         .
     */
    public static ImmutableSet<String> extractScopeKeys(Set<Scope> scopeSet) {

        Set<String> keys = Sets.newLinkedHashSetWithExpectedSize(scopeSet.size());

        scopeSet.stream().forEach(scope -> keys.add(scope.getKey()));

        return ImmutableSet.copyOf(keys);
    }
    
    /**
     * Utility method for transforming a Set<Scope> into a Map.
     * 
     * @param scopeSet
     * @return map where the keys are the {@link Scope} keys and values are the {@link Scope} values
     */
    public static ImmutableMap<String, String> toMap(Set<Scope> scopeSet) {
        
        Map<String, String> map = new HashMap<>();
        for(Scope scope : scopeSet) {
            map.put(scope.getKey(), scope.getValue());
        }
        
        return ImmutableMap.copyOf(map);
    }
    
    /**
     * Returns a subset of the given set of scopes that match the given set of keys. 
     * @param scopeSet
     * @param keys the keys that must be present in the returned set of {@link Scope}
     * @return a subset of {@link Scope} that has the given set of keys
     */
    public static ImmutableSet<Scope> subScopeSet(Set<Scope> scopeSet, Set<String> keys) {
        Set<Scope> subScopeSet = new HashSet<>();
        
        for(Scope scope : scopeSet) {
            if(keys.contains(scope.getKey())) 
                subScopeSet.add(scope);
        }
        
        return ImmutableSet.copyOf(subScopeSet);
    }
    
	/**
	 * Generates the Scope Set from a Scope String.
	 * Example: 
	 * Given: env=env-1,stack=stack-1,subenv=subenv-1
	 * Returns a Set like this (remember that Scope is a key -> value class):
	 *   env -> env-1
	 *   stack -> stack-1
	 *   subenv -> subenv-1
	 *   
	 * @param scopeString
	 * @param scopeDelimiter an Optional delimiter. If not provided, the default "," is used
	 * @return a set of {@link Scope}.
	 */
	public static ImmutableSet<Scope> parseScopeString(final String scopeString, final Optional<String> scopeDelimiter) {
	    final String delimiter = scopeDelimiter.isPresent() ? scopeDelimiter.get() : SCOPE_DELIMITER;
	    
	    final Set<String> scopeStrings = StringUtils.isNotBlank(scopeString) ? Sets.newHashSet(Splitter.on(delimiter).split(scopeString)) : Sets.newHashSet();
	    final Set<Scope> scopeSet = scopeStrings.parallelStream().map(scopeString1 -> {
		    List<String> parsed = Lists.newArrayList(Splitter.on(SCOPE_EQUAL).split(scopeString1));
		    if(parsed.size() != 2) {
		        throw new HengeParseException("Incorrect format of scope string " + scopeString1 + ". Example: env=env-1,stack=stack-1,region=region-1");
		    }
		    return Scope.builder(parsed.get(0), parsed.get(1)).build();
		}).collect(Collectors.toSet());
        
        return ImmutableSet.copyOf(scopeSet);
	}
	
    /**
     * Convenience method when using default scope delimiter.
     * 
     * @param scopeString
     * @return a set of {@link Scope}
     */
	public static ImmutableSet<Scope> parseScopeString(final String scopeString) {
        
       return parseScopeString(scopeString, Optional.empty());
        
    }
	
	/**
	 * Retrieve the correct value by scope
	 * 
	 * @param scopes
	 * @return Property value by scope
	 */
	public static String[] getScopeValue(final Property p, final Set<Scope> scopes, final ImmutableList<ImmutableSet<String>> scopePrecedenceConfig) {
        String[] result = {"default", p.getDefaultValue()};
        
	    Set<Set<Scope>> matches = new HashSet<>();
        for (ImmutableSet<String> configScopeKeys : scopePrecedenceConfig) {
            // if the set of keys of scopes do not contain all of the current configScopeKeys, skip this iteration
            if(!ScopeUtils.extractScopeKeys(scopes).containsAll(configScopeKeys)) {
                continue;
            }
            // gets the intersection between the scope precedence key set and
            // the mapping key scope key set
            ImmutableSet<Scope> intersection = ScopeUtils.subScopeSet(scopes, configScopeKeys);
            
            Optional<PropertyScopedValue> propertyScopedValue = p.getPropertyScopedValues().parallelStream()
                            .filter(psv -> psv.getScopeSet().equals(intersection))
                            .findFirst();
            
            if (propertyScopedValue.isPresent() && !matches.contains(propertyScopedValue.get().getScopeSet())) {
                matches.add(propertyScopedValue.get().getScopeSet());
                result[0] = propertyScopedValue.get().getScopeSet().stream().map(scope -> scope.getKey() + "=" +scope.getValue()).collect(Collectors.joining("&"));
                result[1] = propertyScopedValue.get().getValue(); 
            }
            
        }
        
        return result;

	}
	
	public static boolean validateScopeString( final String scopeString) {
	    final Pattern pattern = Pattern.compile(SCOPE_STRING_PATTERN);
	    final Matcher matcher = pattern.matcher(scopeString);
	    
        return matcher.matches();
	}
	
}