package com.kenzan.henge.domain.model;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import io.swagger.annotations.ApiModel;

/**
 * @author Igor K. Shiohara
 */
@JsonDeserialize(builder = FileVersionCurrent.Builder.class, as = FileVersionCurrent.class)
@JsonPropertyOrder({ "name", "version" })
@ApiModel(description = "This class handle the current version of the FileVersion.")
public class FileVersionCurrent extends NamedVersionedModelReference{

	private FileVersionCurrent(Builder builder) {
		super(builder);
	}

	private static final long serialVersionUID = -3810360826781190461L;
	
	public static Builder builder(final String name, final String version) {

        return new Builder(name, version);
    }
    
    public static Builder builder(final FileVersionCurrent original) {

        return new Builder(original);
    }
    
    public static Builder builder(final FileVersion original) {

        return new Builder(original);
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static class Builder extends NamedVersionedModelReference.Builder<FileVersionCurrent, FileVersion, Builder> {

        private Builder(final FileVersionCurrent FileVersionCurrent) {
            
            super(FileVersionCurrent);
        }
        
        private Builder(final FileVersion FileVersion) {
            
            super(FileVersion);
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
        public FileVersionCurrent build() {

            return new FileVersionCurrent(this);
        }

        @Override
        public FileVersionCurrent build(final Validator validator) {

            final FileVersionCurrent object = build();
            
            final Set<ConstraintViolation<FileVersionCurrent>> violations = validator.validate(object);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(
                    new HashSet<ConstraintViolation<?>>(violations));
            }
            
            return object;
        }
    }
}
