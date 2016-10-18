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
 * This class represents a reference to a {@link PropertyGroup}, containing it's
 * name and version, which is the information required to identify it.
 *
 * @author wmatsushita
 */
@JsonDeserialize(builder = PropertyGroupReference.Builder.class, as=PropertyGroupReference.class)
@JsonPropertyOrder({"name", "version"})
@ApiModel(description = "A property-group reference contains a reference to a property-group (name and version), so that the full object can be later retrieved.")
public class PropertyGroupReference extends NamedVersionedModelReference {
    
    private PropertyGroupReference(final Builder builder) {
        
        super(builder);
    }

    public static Builder builder(final String name, final String version) {

        return new Builder(name, version);
    }
    
    public static Builder builder(final PropertyGroupReference original) {

        return new Builder(original);
    }
    
    public static Builder builder(final PropertyGroup original) {

        return new Builder(original);
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static final class Builder extends NamedVersionedModelReference.Builder<PropertyGroupReference, PropertyGroup, Builder>{

        private Builder(final PropertyGroupReference original) {

            super(original);
        }
        
        private Builder(final PropertyGroup propertyGroup) {
            
            super(propertyGroup);
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
        public PropertyGroupReference build() {

            return new PropertyGroupReference(this);
        }

        @Override
        public PropertyGroupReference build(final Validator validator) {

            final PropertyGroupReference object = build();
            
            final Set<ConstraintViolation<PropertyGroupReference>> violations = validator.validate(object);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(
                    new HashSet<ConstraintViolation<?>>(violations));
            }
            
            return object;
        }


    }
    
}
