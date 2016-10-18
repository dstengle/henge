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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * TODO : Think about renaming this domain object to ScopedPropertyValue
 * @author kylebober
 *
 */
@JsonDeserialize(builder = PropertyScopedValue.Builder.class, as=PropertyScopedValue.class)
@JsonPropertyOrder({"id", "key", "value", "version", "createdBy", "createdDate" })
@ApiModel(description = "A property-scoped-value contains a key(scope)/value(string) pair.")
public class PropertyScopedValue implements Serializable {	

	private static final long serialVersionUID = 8395336999156132010L;
	
	public static final String DEFAULT_SCOPE_KEY = "default";
	public static final String SCOPE_CONFIGURATION_DELIMITER = ";";
	public static final String SCOPE_CONFIGURATION_AGGREGATOR = "+";
	
    /**
     * Creates the Scope Precedence Configuration from the configuration string. 
     * Example:
     * Configuration String: dev;dev+stack;dev+stack+region;hostname;application
     * Result:
     * [
     *   ["dev"], ["dev","stack"], ["dev","stack","region"], ["hostname"], ["application"]
     * ]
     * 
     * @param configString
     * @return the Scope Precedence Configuration as an {@link ImmutableList} of {@link ImmutableSet}.
     */
	public static ImmutableList<ImmutableSet<String>> createScopePrecedenceConfiguration(String configString) {

        List<ImmutableSet<String>> result = new ArrayList<>();

        String[] configEntries = configString.split(SCOPE_CONFIGURATION_DELIMITER);
        for(String configEntry : configEntries) {
            ImmutableSet<String> scopeSet = ImmutableSet.copyOf(configEntry.split(SCOPE_CONFIGURATION_AGGREGATOR));
            result.add(scopeSet);
        }

        return ImmutableList.copyOf(result);
        
    }	
	
	
	/**
	 * Transform a Set<Scope> to a Scope precedence config format: env+stack+region
	 * @param scopeKeys
	 * @return
	 */
	public static String transformToConfigFormat(Set<String> scopeKeys) {
		String format = "";
		
		for (String s : scopeKeys) { 
			format += s + SCOPE_CONFIGURATION_AGGREGATOR;
		}	
		
		return format.substring(0, format.length()-1);
	}
	
    @NotNull
	@Valid
    @ApiModelProperty(required = true, value = "Set of unique scopes that identify when this value should be used to evaluate the property.")
	private final Set<Scope> scopeSet;
	
	@NotNull
	@Size(min=3, max=50)
	// TODO :: Add regex
	//@Pattern(regexp="", message="")	
	@ApiModelProperty(required = true, value="A property-scoped-value's value.", example="Development-PetStore application.")
	private final String value;
	
    private PropertyScopedValue(final Builder builder) {

        this.scopeSet = builder.scopeSet;
        this.value = builder.value;
    }

    /**
	 * @return the scopeSet associated with this value
	 */
	public Set<Scope> getScopeSet() {
		return scopeSet;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(final Object obj) {
		if (null == obj || !(obj instanceof PropertyScopedValue)) {
            return false;
        }

		PropertyScopedValue that = (PropertyScopedValue) obj;
        return Objects.equal(this.scopeSet, that.scopeSet)
        		&& Objects.equal(this.value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(
                this.scopeSet, 
                this.value);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
                .add("scopeSet", this.scopeSet)
                .add("value", this.value)
                .toString();
	}
	
	public static Builder builder(final Set<Scope> scopeSet, final String value) {

        return new Builder(scopeSet, value);
    }
	
	@JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
	public static final class Builder {
	    
	    private Set<Scope> scopeSet;
	    private String value;
	    
		/* Required fields make up the constructor parameters */
		@JsonCreator
		private Builder(@JsonProperty("scopeSet") final Set<Scope> scopeSet, @JsonProperty("value") final String value) {

		    this.scopeSet = scopeSet;
			this.value = value;
	    }
		
		public PropertyScopedValue build() {
			
		    return new PropertyScopedValue(this);
		}        
		
        public PropertyScopedValue build(final Validator validator) {
            
            final PropertyScopedValue object = build();
            
            final Set<ConstraintViolation<PropertyScopedValue>> violations = validator.validate(object);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(
                    new HashSet<ConstraintViolation<?>>(violations));
            }
            
            return object;
        }
        
        
	}	
}
