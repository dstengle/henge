package com.kenzan.henge.domain.model;

/**
 * Provides new instances of mappings. Each implementation of this interface
 * should be for a specific implementation of the {@link Mapping} interface.
 *
 * @author wmatsushita
 */
public interface MappingFactory<T extends NamedVersionedModelReference> {

    public Mapping<T> create();

}
