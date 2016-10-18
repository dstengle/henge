package com.kenzan.henge.domain.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

/**
 * This interface provides mapping functionality, allowing to fetch model
 * instances, given a key and to insert new mappings.
 *
 * @author wmatsushita
 */
public interface Mapping<T extends NamedVersionedModelReference> extends Serializable {

    /**
     * Gets a previously mapped instance of a subclass of
     * {@link NamedVersionedModel} from the mapping by the given
     * {@link MappingKey}.
     * 
     * @param key an instance of a {@link MappingKey} containing the parameters
     *        to search for in the mapping.
     * @return an {@link Optional} of a subclass of NamedVersionedModel. It will
     *         be absent if the given key does not match to an existing key.
     */
    public Optional<T> get(MappingKey key);

    /**
     * Inserts a new entry in the mapping, linking the {@link MappingKey} to an
     * instance of a subclass of {@link NamedVersionedModel}.
     * 
     * @param key an instance of a {@link MappingKey} containing the parameters
     *        to link to the model object.
     * @param value a model object to be mapped to the given key.
     */
    public void put(MappingKey key, T value);
    
    /**
     * Remove the entry by the given {@link MappingKey}.
     * @param key the {@link MappingKey} to be removed.
     */
    public void remove(MappingKey key);
    
    /**
     * @return the inner map for further inspection of the current mapping state.
     */
    public Map<MappingKey, T> getInnerRepresentation();

    /**
     * This is used to update the Mapping data once the repository has deserialized the data.
     * @param map
     */
    public void setInnerRepresentation(Map<MappingKey, T> map);
}
