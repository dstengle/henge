/**
 * Copyright (C) ${project.inceptionYear} Kenzan - Kyle S. Bober
 * (kbober@kenzan.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.Size;

/**
 * TODO :: Should a version-set have it's own version? I do not see why it
 * shouldn't atm.
 *
 */
@JsonDeserialize(builder = VersionSet.Builder.class, as = VersionSet.class)
@JsonPropertyOrder({ "name", "version", "description", "scope", "createdBy", "createdDate", "propertyGroupReferences", "fileVersionReferences"})
@ApiModel(
          description = "A version-set contains one or more uniquely versioned property-groups. A version-set is associated with a scope.")
public class VersionSet extends NamedVersionedModel {

    private static final long serialVersionUID = 2899673741651544480L;

    @Size(min = 3, max = 500)
    // TODO :: Add regex
    // @Pattern(regexp="", message="")
    @ApiModelProperty(
                      required = false,
                      value = "version-set description.",
                      example = "This version-set contains all the versioned property-groups for the pet-store application.")
    private final String description;

    @Valid
    @ApiModelProperty(required = true, value = "The property-group references that are associated with this version-set.")
    private final Set<PropertyGroupReference> propertyGroupReferences;

    @Valid
    @ApiModelProperty(required = false, value = "The file version references that are associated with this version-set.")
    private final Set<FileVersionReference> fileVersionReferences;

    private VersionSet(final Builder builder) {
        
        super(builder);
        this.description = builder.description;
        this.propertyGroupReferences = builder.propertyGroupReferences != null? builder.propertyGroupReferences : Sets.newHashSet();
        this.fileVersionReferences = builder.fileVersionReferences != null? builder.fileVersionReferences : Sets.newHashSet();
    }

    // TODO :: Determine if we should include an ordered set of the property
    // group hierarchies, make this static for now.

    /**
     * @return the description
     */
    public String getDescription() {

        return description;
    }

    /**
     * @return the propertyGroups
     */
    public Set<PropertyGroupReference> getPropertyGroupReferences() {

        return propertyGroupReferences;
    }

    /**
     * @return the Set of FileVersionReference 
     */
    public Set<FileVersionReference> getFileVersionReferences() {
		return fileVersionReferences;
	}

    @Override
    public boolean equals(final Object obj) {

        if (null == obj || !(obj instanceof VersionSet)) {
            return false;
        }

        VersionSet that = (VersionSet) obj;

        return Objects.equal(this.getName(), that.getName()) && Objects.equal(this.description, that.description)
            && Objects.equal(this.propertyGroupReferences, that.propertyGroupReferences)
            && Objects.equal(this.fileVersionReferences, that.fileVersionReferences);
        
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(this.getName(), this.description, this.propertyGroupReferences, 
            this.fileVersionReferences);
    }

    @Override
    public String toString() {

        return MoreObjects.toStringHelper(this).add("name", this.getName()).add("description", this.description)
            .add("propertyGroupReferences", this.propertyGroupReferences)
            .add("fileVersionReferences", this.fileVersionReferences).toString();
    }

    public static Builder builder(final String name, final String version) {

        return new Builder(name, version);
    }
    
    public static Builder builder(final VersionSet original) {

        return new Builder(original);
    }
    
    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static final class Builder extends NamedVersionedModel.Builder<VersionSet, Builder> {
        
        private String description;
        private Set<PropertyGroupReference> propertyGroupReferences;
        private Set<FileVersionReference> fileVersionReferences;

        /* Copy constructor */
        private Builder(VersionSet original) {

            super(original);
            this.description = original.getDescription();
            this.propertyGroupReferences = original.getPropertyGroupReferences();
            this.fileVersionReferences = original.getFileVersionReferences();
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

        public Builder withPropertyGroupReferences(final PropertyGroupReference... propertyGroupReferences) {
            if (propertyGroupReferences != null){
                if (this.propertyGroupReferences == null) {
                    this.propertyGroupReferences = new HashSet<>();
                }
                this.propertyGroupReferences.addAll(Sets.newHashSet(propertyGroupReferences));
            }
                return this;            
        }

        public Builder withFileVersionReferences(final FileVersionReference... fileVersionReferences) {
        	if (fileVersionReferences != null) {
	            if (this.fileVersionReferences == null) {
	                this.fileVersionReferences = new HashSet<>();
	            }
	            	this.fileVersionReferences.addAll(Sets.newHashSet(fileVersionReferences));
            }
            return this;
        }
        
        @Override
        protected Builder getBuilder() {

            return this;
        }
        
        @Override
        public VersionSet build() {
            
            return new VersionSet(this);
        }

        @Override
        public VersionSet build(Validator validator) {

            final VersionSet object = build();
            
            final Set<ConstraintViolation<VersionSet>> violations = validator.validate(object);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(
                    new HashSet<ConstraintViolation<?>>(violations));
            }
            
            return object;
        }
    }

}
