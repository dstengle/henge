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
 * This class represents a reference to a {@link FileVersion}, containing it's
 * name and version, which is the information required to identify it.
 *
 * @author Igor K. Shiohara
 */
@JsonDeserialize(builder = FileVersionReference.Builder.class, as=FileVersionReference.class)
@JsonPropertyOrder({"name", "version"})
@ApiModel(description = "A file version reference contains a reference to a file version (name and version), so that the full object can be later retrieved.")
public class FileVersionReference extends NamedVersionedModelReference {
    
	private static final long serialVersionUID = 4520758446021477790L;

	private FileVersionReference(final Builder builder) {
        
        super(builder);
    }

    public static Builder builder(final String name, final String version) {

        return new Builder(name, version);
    }
    
    public static Builder builder(final FileVersionReference original) {

        return new Builder(original);
    }
    
    public static Builder builder(final FileVersion fileVersion) {

        return new Builder(fileVersion);
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static final class Builder extends NamedVersionedModelReference.Builder<FileVersionReference, FileVersion, Builder>{

        private Builder(final FileVersionReference original) {

            super(original);
        }
        
        private Builder(final FileVersion fileVersion) {
            
            super(fileVersion);
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
        public FileVersionReference build() {

            return new FileVersionReference(this);
        }

        @Override
        public FileVersionReference build(final Validator validator) {

            final FileVersionReference object = build();
            
            final Set<ConstraintViolation<FileVersionReference>> violations = validator.validate(object);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(
                    new HashSet<ConstraintViolation<?>>(violations));
            }
            
            return object;
        }


    }
    
}
