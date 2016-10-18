package com.kenzan.henge.domain.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 
 * @author Igor K. Shiohara
 *
 */
@JsonDeserialize(builder = FileVersion.Builder.class, as = FileVersion.class)
@JsonPropertyOrder({ "name", "version", "description", "content", "filename", "createdBy", "createdDate"})
@ApiModel(description = "A file contains a name, version, description and file content.")
public class FileVersion extends NamedVersionedModel {

    private static final long serialVersionUID = -2306731570034735322L;

    @Size(min = 3, max = 500)
    @ApiModelProperty(required = false, value = "File description.",
                      example = "Attached csv format file on property group.")
    private final String description;

    @ApiModelProperty(required = true, value = "The content file in bytes.")
    private final byte[] content;

    @ApiModelProperty(required = true, value = "The filename.")
    private String fileName;

    private FileVersion(Builder builder) {
        super(builder);
        this.description = builder.description;
        this.content = builder.content;
        this.fileName = builder.fileName;
    }

    public String getDescription() {

        return description;
    }

    public byte[] getContent() {

        return content;
    }

    public String getFilename() {

        return fileName;
    }

    @Override
    public String toString() {

        return MoreObjects.toStringHelper(this)
                        .add("name", this.getName())
                        .add("version", this.getVersion())
                        .add("description", this.description)
                        .add("content", this.content)
                        .add("filename", this.fileName)
                        .toString();
    }

    @Override
    public boolean equals(Object object) {

        if (null == object || !(object instanceof FileVersion)) {
            return false;
        }

        FileVersion that = (FileVersion) object;
        return Objects.equal(this.getName(), that.getName())
                        && Objects.equal(this.getVersion(), that.getVersion())
                        && Objects.equal(this.description, that.getDescription()) 
                        && Arrays.equals(this.content, that.getContent())
                        && Objects.equal(this.fileName, that.getFilename());
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(
            this.getName(), 
            this.getVersion(), 
            this.getDescription(), 
            this.getContent(),
            this.getFilename());
    }

    public static Builder builder(final String name, final String version, final byte[] content, final String filename) {

        return new Builder(name, version, content, filename);
    }

    public static Builder builder(final FileVersion original) {

        return new Builder(original);
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static final class Builder extends NamedVersionedModel.Builder<FileVersion, Builder> {

        private String description;
        private byte[] content;
        private String fileName;

        private Builder(final FileVersion original) {

            super(original);
            this.description = original.getDescription();
            this.content = original.getContent();
            this.fileName = original.fileName;
        }

        /* Required fields make up the constructor parameters */
        @JsonCreator
        private Builder(
                        @JsonProperty("name") final String name,
                        @JsonProperty("version") final String version, 
                        @JsonProperty("content") final byte[] content,
                        @JsonProperty("filename") final String fileName) {

            super(name, version);
            this.content = content;
            this.fileName = fileName;
        }

        public Builder withContent(final byte[] content) {

            this.content = content;
            return this;
        }

        public Builder withDescription(final String description) {

            this.description = description;
            return this;
        }

        public Builder withFileName(final String fileName) {

            this.fileName = fileName;
            return this;
        }

        @Override
        protected Builder getBuilder() {

            return this;
        }

        @Override
        public FileVersion build() {

            return new FileVersion(this);
        }

        @Override
        public FileVersion build(Validator validator) {

            final FileVersion object = build();

            final Set<ConstraintViolation<FileVersion>> violations = validator.validate(object);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
            }

            return object;
        }

    }

}
