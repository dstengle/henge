package com.kenzan.henge.domain.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;

import java.io.Serializable;

import javax.validation.Validator;

import io.swagger.annotations.ApiModel;
import jersey.repackaged.com.google.common.base.Objects;

/**
 * This class represents a reference to a {@link NamedVersionedModel}, containing it's
 * name and version, which is the information required to identify it.
 *
 * @author wmatsushita
 */
@JsonDeserialize(builder = NamedVersionedModelReference.Builder.class, as = NamedVersionedModelReference.class)
@JsonPropertyOrder({ "name", "version" })
@ApiModel(
          description = "A NamedVersionedModel reference contains a reference to a NamedVersionedModel (name and version), so that the full object can be later retrieved.")
public abstract class NamedVersionedModelReference implements Serializable {

    private final String name;

    private final String version;
    
    protected NamedVersionedModelReference(final Builder builder) {
        
        this.name = builder.name;
        this.version = builder.version;
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

    @Override
    public int hashCode() {

        return Objects.hashCode(name, version);
        
    }

    @Override
    public boolean equals(Object obj) {
        
        if (null == obj || !(obj instanceof NamedVersionedModelReference)) {
            return false;
        }

        NamedVersionedModelReference that = (NamedVersionedModelReference) obj;

        return Objects.equal(this.name, that.getName()) && Objects.equal(this.version, that.getVersion());

    }

    @Override
    public String toString() {

        return MoreObjects.toStringHelper(this)
                        .add("name", this.name)
                        .add("version", this.version)
                        .toString();
        
    }

    protected static abstract class Builder<T extends NamedVersionedModelReference, M extends NamedVersionedModel, B extends Builder<T, M, B>> {

        protected B builder;
        protected String name;
        protected String version;

        protected Builder(final T original) {

            this.name = original.getName();
            this.version = original.getVersion();
            this.builder = getBuilder();
        }
        
        protected Builder(final M modelObject) {

            this.name = modelObject.getName();
            this.version = modelObject.getVersion();
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
        
        protected abstract B getBuilder();

        public abstract T build();

        public abstract T build(final Validator validator);

    }

}
