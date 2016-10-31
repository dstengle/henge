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
package com.kenzan.henge.domain.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kenzan.henge.domain.model.type.PropertyGroupType;
import com.kenzan.henge.domain.validator.CheckEnumeration;
import com.kenzan.henge.domain.validator.CheckPropertiesType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * PropertyGroup holds a related collection of properties together by name, type and version.
 *	<p>
 * 		<ul>
 * 			<li>Properties must be grouped in Property Groups either by application or library</li>
 * 			<li>Property Groups must be identified by name and type</li>
 * 			<li>Types must be either application or library</li>
 * 			<li>Property Groups must be kept together in a single artifact</li>
 * 			<li>The artifact containing a Property Group must be versionable</li>
 * 			<li>Property Groups will use semantic versioning</li>
 * 			<li>Property Group names will not contain special characters or spaces</li>
 * 		</ul>
 * 	</p>
 * 
 * TODO :: Create the Builder Inner Class
 * TODO :: Add Swagger ApiModelProperty annotations
 * TODO :: Add JSR-303 Bean Validation Annotations
 * TODO :: Create Validator for Enums
 *
 */
@JsonDeserialize(builder = PropertyGroup.Builder.class, as=PropertyGroup.class)
@JsonPropertyOrder({"id", "name", "version", "description", "type", "active", "createdBy", "createdDate", "properties"})
@ApiModel(description = "A property-group contains a name, description, type, scope-schema and a set of associated properties.")
public class PropertyGroup extends NamedVersionedModel {
	
	private static final long serialVersionUID = -1880872177360087793L;
	
	@Size(min=3, max=500)
	// TODO :: Add regex
	//@Pattern(regexp="", message="")
	@ApiModelProperty(required = false, value="property-group description.", example="Property group for the pet-store-edge's service properties.")
	private final String description;
	
	@CheckEnumeration(PropertyGroupType.class)
	@ApiModelProperty(required = true, allowableValues="APP,LIB", value="property-group type.", example="APP")
	private final String type;
	
	@ApiModelProperty(required = false, value="Denotes this property-group, as the active property-group version.", example="true")
	private final boolean isActive;
	
	@CheckPropertiesType
	@ApiModelProperty(required = false, value="The property-group's associated properties.")
	private final Set<Property> properties;
	
	
	
	/**
     * @param description
     * @param type
     * @param isActive
     * @param properties
     */
    private PropertyGroup(final Builder builder) {
        
        super(builder);
        this.description = builder.description;
        this.type = builder.type;
        this.isActive = builder.isActive;
        this.properties = builder.properties;
    }

    /**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the isActive
	 */
	public boolean isActive() {
		return isActive;
	}
	
	/**
	 * @return the properties
	 */
	public Set<Property> getProperties() {
		return properties;
	}
	
	/**
	 * TODO: Review this method, considering changes in the scope model and scope precedence configuration
	 * Map of the property-groups scopes and associated Properties that are in that scope
	 * key :: scope.name 
	 * value:: Map of key:: property.name value :: property
	 * 
	 * @return
	 */
	@JsonIgnore
	public Map<Set<Scope>, Map<String, PropertyScopedValue>> getMapOfPropertyScopeValuesByScope() {

		// key :: scope.name value:: Map of key :: property.name value :: property
		Map<Set<Scope>, Map<String, PropertyScopedValue>> mapOfScopeAndPropertyScopedValues = new HashMap<>();
		
		// Iterate over all of the properties in this property-group
		if(this.properties != null) {
			for(Property property : this.properties) {
				
				// Iterate over each property's set of property-scoped-values
				for(PropertyScopedValue propertyScopedValue : property.getPropertyScopedValues()) {
					
					// Check if the map contains the scope already
					if(mapOfScopeAndPropertyScopedValues.containsKey(propertyScopedValue.getScopeSet())) {
						// key :: property.name, value :: PropertyScopedValue
						Map<String, PropertyScopedValue> propertyNamePropertyScopeValueMap = mapOfScopeAndPropertyScopedValues.get(propertyScopedValue.getScopeSet());
						propertyNamePropertyScopeValueMap.put(property.getName(), propertyScopedValue);
						mapOfScopeAndPropertyScopedValues.put(propertyScopedValue.getScopeSet(), propertyNamePropertyScopeValueMap);
					} else {
						Map<String, PropertyScopedValue> propertyNamePropertyScopeValueMap = Maps.newHashMap();
						propertyNamePropertyScopeValueMap.put(property.getName(), propertyScopedValue);
						mapOfScopeAndPropertyScopedValues.put(propertyScopedValue.getScopeSet(), propertyNamePropertyScopeValueMap);
					}				
				}
				
			}
		}

		return mapOfScopeAndPropertyScopedValues;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (null == obj || !(obj instanceof PropertyGroup)) {
            return false;
        }

		PropertyGroup that = (PropertyGroup) obj;
        return Objects.equal(this.getName(), that.getName()) 
        		&& Objects.equal(this.getVersion(), that.getVersion())
        		&& Objects.equal(this.description, that.getDescription())
                && Objects.equal(this.type, that.getType())
                && Objects.equal(this.isActive, that.isActive())
                && Objects.equal(this.properties, that.getProperties());
                
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(
                this.getName(),
                this.getVersion(),
                this.description, 
                this.type,
                this.isActive,
                this.properties);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
                .add("name", this.getName())
				.add("version", this.getVersion())
                .add("description", this.description)
                .add("type", this.type)
                .add("isActive", this.isActive)
                .add("properties", this.properties)
                .toString();
	}
	
	public static Builder builder(final String name, final String version) {

        return new Builder(name, version);
    }
    
    public static Builder builder(final PropertyGroup original) {

        return new Builder(original);
    }
	
	@JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
	public static final class Builder extends NamedVersionedModel.Builder<PropertyGroup, Builder> {
	    
	    private String description;
	    private String type;
	    private boolean isActive;
	    private Set<Property> properties;
		
		/* Copy constructor */
		private Builder(final PropertyGroup original) {

		    super(original);
		    this.description  = original.getDescription();
		    this.type = original.getType();
		    this.isActive = original.isActive();
		    this.properties = original.getProperties();
		}
		
		/* Required fields make up the constructor parameters */
		@JsonCreator
		private Builder(@JsonProperty("name") final String name, 
		                @JsonProperty("version") final String version) {
			
		    super(name, version);
	    }		
		
		public Builder withDescription(final String description) {			
			
		    this.description = description;
            return this;
        }
		
		public Builder withType(final String type) {			
		    
		    this.type = type;
            return this;
        }
		
		@JsonProperty("active")
		public Builder withIsActive(final boolean isActive) {			
			
		    this.isActive = isActive;
            return this;
        }
		
		public Builder withProperties(final Property... property) {
			
		    if(property != null) {
				if(this.properties == null) {
				    this.properties = new HashSet<>();	
				}			
				this.properties.addAll(Sets.newHashSet(property));
			}
			return this;
		}
		
        @Override
        protected Builder getBuilder() {

            return this;
        }
		
		@Override
        public PropertyGroup build() {
			
		    return new PropertyGroup(this);
		}

        @Override
        public PropertyGroup build(Validator validator) {

            final PropertyGroup object = build();
            
            final Set<ConstraintViolation<PropertyGroup>> violations = validator.validate(object);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(
                    new HashSet<ConstraintViolation<?>>(violations));
            }
            
            return object;
        }

	} 

}
