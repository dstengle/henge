package com.kenzan.henge.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.base.MoreObjects;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import jersey.repackaged.com.google.common.base.Objects;

/**
 * This class represents a reference to a {@link PropertyGroup}, containing it's
 * name and version, which is the information required to identify it.
 *
 * @author wmatsushita
 */
@JsonDeserialize(builder = Scope.Builder.class, as = Scope.class)
@JsonPropertyOrder({ "key", "value" })
@JsonRootName("")
@ApiModel(
          description = "A scope is one key -> value pair used to determine a set of scopes for a property. Ex: the query string env=dev is equivalent to a scope instance with key = env and value = dev. A query string env=dev&region=US-east-1 would be equivalent to a Set<Scope> with one entry having key=env and value=dev and another having key=region and value = US-east-1.")
public class Scope implements Serializable {
	
    @NotNull
    @Size(min=3, max=50)
    @ApiModelProperty(required = true, value="A single scope key.", example="env")
    private final String key;

    @NotNull
    @Size(min=3, max=50)
    @ApiModelProperty(required = true, value="A single scope value.", example="dev")
    private final String value;
    
    private Scope(final Builder builder) {
        
        this.key = builder.key;
        this.value = builder.value;
    }

    /**
     * @return the name
     */
    public String getKey() {

        return key;
    }

    /**
     * @return the version
     */
    public String getValue() {

        return value;
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(this.key, this.value);

    }

    @Override
    public boolean equals(Object obj) {

        if (null == obj || !(obj instanceof Scope)) {
            return false;
        }

        Scope that = (Scope) obj;

        return Objects.equal(this.key, that.getKey()) && Objects.equal(this.value, that.getValue());

    }

    @Override
    public String toString() {

        return MoreObjects.toStringHelper(this)
                        .add("key", this.key)
                        .add("value", this.value)
                        .toString();
        
    }
    
    public static Builder builder(final String key, final String value) {

        return new Builder(key, value);
    }
    
    public static Builder builder(final Scope original) {

        return new Builder(original);
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static final class Builder {
        
        private String key;
        private String value;

        private Builder(final Scope original) {

            this.key = original.getKey();
            this.value = original.getValue();
        }

        @JsonCreator
        /* Required fields should be present in the Builder's Constructor */
        private Builder(@JsonProperty("key") final String key, @JsonProperty("value") final String value) {

            this.key = key;
            this.value = value;
        }

        public Builder withKey(String key) {

            this.key = key;
            return this;
        }

        public Builder withValue(String value) {

            this.value = value;
            return this;
        }

        public Scope build() {

            return new Scope(this);
        }

        public Scope build(final Validator validator) {
            
            final Scope object = build();

            final Set<ConstraintViolation<Scope>> violations = validator.validate(object);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(new HashSet<ConstraintViolation<Scope>>(
                    violations));
            }

            return object;
        }

    }

}
