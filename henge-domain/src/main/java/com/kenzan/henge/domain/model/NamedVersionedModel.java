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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.google.common.base.Preconditions;
import com.kenzan.henge.domain.utils.SemanticVersionComparator;
import com.kenzan.henge.exception.RuntimeHengeException;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.core.Response.Status;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public abstract class NamedVersionedModel implements Serializable, Comparable<NamedVersionedModel> {

    private static final long serialVersionUID = -3810698589228819621L;

    @NotNull
    @Size(min = 3, max = 50)
    @ApiModelProperty(required = true, value = "Unique property-group name.", example = "pet-store-edge")
    private final String name;

    @NotNull
    @Pattern(regexp = "[0-9]+\\.[0-9]+\\.[0-9]+")
    @ApiModelProperty(required = true, value = "Revision number of this entity.", example = "1.1.0")
    private final String version;

    @ApiModelProperty(required = false, hidden = true, value = "Identifier of the user who created this entity.",
                      example = "adminstrator-james")
    private final String createdBy;

    @DateTimeFormat(iso = ISO.DATE_TIME)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @ApiModelProperty(required = false, hidden = true, value = "Timestamp of when this entity was created.",
                      example = "2012-04-23T18:25:43.511")
    private final LocalDateTime createdDate;

    protected NamedVersionedModel(final Builder builder) {

        this.name = builder.name;
        this.version = builder.version;
        this.createdBy = builder.createdBy;
        if (builder.createdDate != null) {
        	this.createdDate = builder.createdDate;
        } else {
        	this.createdDate = LocalDateTime.now();
        }
    }

    /**
     * @return the name
     */
    public String getName() {

        return name;
    }

    /**
     * @return the version
     */
    public String getVersion() {

        return version;
    }

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {

        return createdBy;
    }

    /**
     * @return the createdDate
     */
    public LocalDateTime getCreatedDate() {

        return createdDate;
    }

    @Override
    public abstract String toString();

    @Override
    public abstract boolean equals(Object object);

    @Override
    public abstract int hashCode();

    /**
     * Compares this model instance with another by the version number. 
     * 
     * @param other the other model instance to compare to.
     * @return a negative integer, zero, or a positive integer as this object is
     *         less than, equal to, or greater than the specified object.
     *
     * @throws NullPointerException if the specified object is null
     * @throws RuntimeHengeException if the two objects are unrelated
     *         due to the specified object's name not matching this instance's
     *         name.
     */
    public int compareTo(NamedVersionedModel other) {

        Preconditions.checkNotNull(other, "Can't compare to a null object.");

        if (!this.name.equals(other.getName())) {
            throw new RuntimeHengeException(Status.INTERNAL_SERVER_ERROR,
                "Trying to compare versions of unrelated model instances: [" + this.getName() + "] and ["
                    + other.getName() + "]");
        }

        final SemanticVersionComparator comparator = new SemanticVersionComparator();
        
        return comparator.compare(this.getVersion(), other.getVersion());

    }
    
    protected static abstract class Builder<T extends NamedVersionedModel, B extends Builder<T, B>> {
        
        protected B builder;
        protected String name;
        protected String version;
        protected String createdBy;
        protected LocalDateTime createdDate;

        protected Builder(final NamedVersionedModel original) {

            this.name = original.name;
            this.createdBy = original.createdBy;
            this.createdDate = original.createdDate;
            this.version = original.version;
            this.builder = getBuilder();
        }

        /* Required fields should be present in the Builder's Constructor */
        protected Builder(final String name, final String version) {

            this.name = name;
            this.version = version;
            this.builder = getBuilder();
        }

        public B withName(final String name) {
            
            this.name = name;
            return builder;
        }

        public B withVersion(final String version) {
            
            this.version = version;
            return builder;
        }

        public B withCreatedBy(final String createdBy) {
            
            this.createdBy = createdBy;
            return builder;
        }

        public B withCreatedDate(final LocalDateTime createdDate) {
            
            this.createdDate = createdDate;
            return builder;
        }

        protected abstract B getBuilder();

        public abstract T build();

        public abstract T build(final Validator validator);
    }

}
