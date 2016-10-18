package com.kenzan.henge.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import io.swagger.annotations.ApiModel;

/**
 * This class represents a reference to a {@link VersionSet}, containing it's
 * name and version, which is the information required to identify it.
 *
 * @author wmatsushita
 */
@JsonDeserialize(builder = VersionSetReference.Builder.class, as = VersionSetReference.class)
@JsonPropertyOrder({ "name", "version" })
@ApiModel(
          description = "A version-set reference contains a reference to a version-set (name and version), so that the full object can be later retrieved.")
public class VersionSetReference extends NamedVersionedModelReference {
    
    private VersionSetReference(final Builder builder) {
        
        super(builder);
    }
    
    public static Builder builder(final String name, final String version) {

        return new Builder(name, version);
    }
    
    public static Builder builder(final VersionSetReference original) {

        return new Builder(original);
    }
    
    public static Builder builder(final VersionSet original) {

        return new Builder(original);
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static class Builder extends NamedVersionedModelReference.Builder<VersionSetReference, VersionSet, Builder> {

        private Builder(final VersionSetReference versionSetReference) {
            
            super(versionSetReference);
        }
        
        private Builder(final VersionSet versionSet) {
            
            super(versionSet);
        }
        
        @JsonCreator
        /* Required fields should be present in the Builder's Constructor */
        private Builder(@JsonProperty("name") final String name, @JsonProperty("version") final String version) {
               
            super(name, version);
        }

        @Override
        protected Builder getBuilder() {

            return this;
        }

        @Override
        public VersionSetReference build() {

            return new VersionSetReference(this);
        }

        @Override
        public VersionSetReference build(final Validator validator) {

            final VersionSetReference object = build();
            
            final Set<ConstraintViolation<VersionSetReference>> violations = validator.validate(object);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(
                    new HashSet<ConstraintViolation<?>>(violations));
            }
            
            return object;
        }
    }

}
