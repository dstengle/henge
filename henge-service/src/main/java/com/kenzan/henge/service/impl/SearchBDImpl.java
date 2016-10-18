package com.kenzan.henge.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kenzan.henge.domain.model.Property;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.PropertyScopedValue;
import com.kenzan.henge.domain.model.Scope;
import com.kenzan.henge.domain.model.ScopePrecedenceConfiguration;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.model.type.PropertyGroupType;
import com.kenzan.henge.domain.utils.ScopeUtils;
import com.kenzan.henge.exception.HengeException;
import com.kenzan.henge.service.PropertyGroupBD;
import com.kenzan.henge.service.SearchBD;
import com.kenzan.henge.service.VersionSetMappingBD;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides the search service
 *
 * @author wmatsushita
 */
@Component
public class SearchBDImpl implements SearchBD {

    private VersionSetMappingBD versionSetMappingBD;

    private PropertyGroupBD propertyGroupBD;

    private ScopePrecedenceConfiguration scopePrecedenceConfig;
    
    
    @Autowired
    public SearchBDImpl(VersionSetMappingBD versionSetMappingBD, PropertyGroupBD propertyGroupBD, ScopePrecedenceConfiguration scopePrecedenceConfig) {
        this.versionSetMappingBD = versionSetMappingBD;
        this.propertyGroupBD = propertyGroupBD;
        this.scopePrecedenceConfig = scopePrecedenceConfig;
    }

    /**
     * Searches the {@link VersionSetMappingBD} for the {@VersionSetReference} 
     * and if found, reads the referenced {@link VersionSet}. 
     * Then it takes all the {@PropertyGroupReference}s and reads the corresponding
     * {@link PropertyGroup}s evaluating each {@link Property} by using the
     * {@link ScopePrecedenceConfiguration} to match the possible
     * {@link PropertyScopedValue}s and the {@Scope}s given in the search.
     * 
     * @param application the name of the application
     * @param scopeString the set of {@link Scope}s in string format
     * @param libs an optional set of libraries in string format. If present, it will 
     *      restrict the library {@link Property} listed to only the ones present in 
     *      {@link PropertyGroup}s that are in the list. 
     */
    @Override
    public Optional<String> findProperties(final String application, final String scopeString, final Optional<String> libs) {

        final Set<String> libraries =
            libs.isPresent() ? Sets.newHashSet(Splitter.on(',').split(libs.get())) : Sets.newHashSet();
        final Set<Scope> scopeSet =
            StringUtils.isNotBlank(scopeString) ? ScopeUtils.parseScopeString(scopeString) : Sets.newHashSet();

        Optional<VersionSet> versionSet = versionSetMappingBD.findMatch(application, scopeSet);
        if (!versionSet.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(convertToProperties(application, scopeSet, versionSet.get(), libraries));

    }
    
    /**
     * Iterate a VersionSet and convert the properties into a property format
     * String
     * 
     * @param application Application name
     * @param scopes Scopes to find
     * @param versionSet A version set to convert
     * @param libs Libraries to convert
     * @return Property file format
     * 
     * @throws HengeException
     */
    private String convertToProperties(final String application, final Set<Scope> scopeSet,
                                       VersionSet versionSet, final Set<String> libs) {

        Set<PropertyGroup> apps = propertyGroupBD.getPropertyGroup(versionSet, PropertyGroupType.APP);
        Set<PropertyGroup> libraries =
            propertyGroupBD.getPropertyGroup(versionSet, PropertyGroupType.LIB, java.util.Optional.of(libs));

        Set<PropertyGroup> propertyGroups = Sets.newLinkedHashSet();
        propertyGroups.addAll(apps);
        propertyGroups.addAll(libraries);

        Set<String> properties = new LinkedHashSet<>();
        properties.add("# version-set name: " + versionSet.getName());
        properties.add("# version:" + versionSet.getVersion());
        properties.add("# name:" + (StringUtils.isNotBlank(versionSet.getName())? versionSet.getName() : StringUtils.EMPTY));
        properties.add("# description:" + (StringUtils.isNotBlank(versionSet.getDescription())? versionSet.getDescription() : StringUtils.EMPTY));
        properties.add(StringUtils.EMPTY);
        properties.add(parseProperties(propertyGroups, scopeSet));

        StringBuilder sb = new StringBuilder();
        properties.stream().forEach(prop -> sb.append(prop).append(System.lineSeparator()));
        return sb.toString();
        
    }

    /**
     * This method iterates over each PropertyGroup, retrieves each property and
     * values converting to a property format
     * 
     * @param propertyGroups
     * @param scopes
     * @return
     */
    private String parseProperties(final Set<PropertyGroup> propertyGroups, final Set<Scope> scopes) {

        StringBuilder builder = new StringBuilder();
        Map<String, Property> properties = Maps.newLinkedHashMap();

        for (PropertyGroup pg : propertyGroups) {
            final String propertyGroupName = pg.getName();
            final String propertyGroupDescription = pg.getDescription();
            final String propertyGroupType = pg.getType();

            for (Property p : pg.getProperties()) {

                if (!properties.containsKey(p.getName())) {
                    builder.append("# property group name: ").append(propertyGroupName);
                    builder.append(System.lineSeparator());
                    builder.append("# property group description: ").append(propertyGroupDescription);
                    builder.append(System.lineSeparator());
                    String[] scope = ScopeUtils.getScopeValue(p, scopes,
                            scopePrecedenceConfig.getInnerRepresentation());
                    builder.append("# scope: " + scope[0]);
                    builder.append(System.lineSeparator());
                    builder.append("# type: ").append(propertyGroupType);
                    builder.append(System.lineSeparator());
                    builder.append(p.getName()).append("=")
                        .append(StringUtils.isBlank(scope[1]) ? p.getDefaultValue() : scope[1]);
                    builder.append(System.lineSeparator()).append(System.lineSeparator());

                    properties.put(p.getName(), p);
                }
            }

        }

        return builder.toString();
        
    }

}
