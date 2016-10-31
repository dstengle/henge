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
import com.google.common.collect.Sets;
import com.kenzan.henge.domain.model.type.PropertyType;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * <ul>
 * <li>Properties must have a name</li>
 * <li>Properties must be typed</li>
 * <li>Properties must have descriptions</li>
 * <li>Properties must support different values in different scopes or
 * combinations of scopes</li>
 * <li>Scopes are defined as key-value mappings that describe attributes of an
 * environment where an application might run</li>
 * <li>Properties must be defined separately from how they are consumed</li>
 * <li>Properties must support default values, but do not require them</li>
 * <li>Properties must have all of their values stored in one place</li>
 * </ul>
 * </p>
 * 
 * A property may only belong to one property-group. 0
 */
@JsonDeserialize(builder = Property.Builder.class, as = Property.class)
@JsonPropertyOrder({ "id", "name", "description", "defaultValue", "type", "scopeValues", "version", "createdBy", "createdDate" })
@ApiModel(description = "A property contains a name, description and a set of property-scoped-values.")
public class Property implements Serializable {

	private static final long serialVersionUID = 7333797780700266773L;

	public enum SortOrder {
		ASCENDING, DESCENDING
	}

	@NotNull
	@Size(min = 3, max = 50)
	// TODO :: Add regex
	// @Pattern(regexp="", message="")
	@ApiModelProperty(required = true, value = "Unique property name.", example = "pet-store-application-name")
	private final String name;

	@Size(min = 3, max = 500)
	// TODO :: Add regex
	// @Pattern(regexp="", message="")
	@ApiModelProperty(required = false, value = "property description.", example = "The name of the pet-store application.")
	private final String description;

	// TODO : Shouldn't this just be PropertyScopedValue ??? Change this!
	@Size(min = 1, max = 500)
	// TODO :: Add regex
	// @Pattern(regexp="", message="")
	@ApiModelProperty(required = false, value = "property's default value.", example = "Default-PetStore application.")
	private final String defaultValue;
	
	@ApiModelProperty(required = false, value = "property's type.", example="String")
	private final PropertyType type;

	@Valid
	@ApiModelProperty(required = false, value = "Set of unique property-scoped-values associated with this property.")
	private final Set<PropertyScopedValue> propertyScopedValues;

	private Property(final Builder builder) {

		this.name = builder.name;
		this.description = builder.description;
		this.defaultValue = builder.defaultValue;
		this.propertyScopedValues = builder.propertyScopedValues;
		this.type = builder.type;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * 
	 * @return the type
	 */
	public PropertyType getType() {
		return type;
	}

	/**
	 * @return the scopePropertyValues
	 */
	public Set<PropertyScopedValue> getPropertyScopedValues() {
		return this.propertyScopedValues;
	}

	@Override
	public boolean equals(final Object obj) {
		if (null == obj || !(obj instanceof Property)) {
			return false;
		}

		Property that = (Property) obj;
		return Objects.equal(this.name, that.name)
				&& Objects.equal(this.description, that.description)
				&& Objects.equal(this.defaultValue, that.defaultValue)
				&& Objects.equal(this.type, that.getType())
				&& Objects.equal(this.propertyScopedValues,
						that.getPropertyScopedValues());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.name, this.description, this.defaultValue,
				this.type, this.propertyScopedValues);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("name", this.name)
				.add("defaultValue", this.defaultValue)
				.add("type", this.type)
				.add("propertyScopedValues", this.propertyScopedValues)
				.toString();
	}

	public static Builder builder(final String name) {

		return new Builder(name);
	}

	public static Builder builder(final Property original) {

		return new Builder(original);
	}

	@JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
	public static final class Builder {

		private String name;
		private String description;
		private String defaultValue;
		private PropertyType type;
		private Set<PropertyScopedValue> propertyScopedValues;

		private Builder(final Property original) {

			this.name = original.getName();
			this.description = original.getDescription();
			this.defaultValue = original.getDefaultValue();
			this.type = original.getType();
			this.propertyScopedValues = original.getPropertyScopedValues();
		}

		/* Required fields make up the constructor parameters */
		@JsonCreator
		public Builder(@JsonProperty("name") final String name) {

			this.name = name;
			this.propertyScopedValues = new HashSet<>();
		}

		public Builder withDescription(final String description) {

			this.description = description;
			return this;
		}

		public Builder withDefaultValue(final String defaultValue) {

			this.defaultValue = defaultValue;
			return this;
		}
		
		public Builder withType(final PropertyType type) {
			this.type = type;
			return this;
		}

		@JsonProperty(value = "propertyScopedValues")
		public Builder withScopedValues(
				PropertyScopedValue... propertyScopeValues) {

			if (this.propertyScopedValues == null) {
				this.propertyScopedValues = new HashSet<>();
			}
			this.propertyScopedValues.addAll(Sets
					.newHashSet(propertyScopeValues));
			return this;
		}

		public Property build() {

			return new Property(this);
		}

	}

}