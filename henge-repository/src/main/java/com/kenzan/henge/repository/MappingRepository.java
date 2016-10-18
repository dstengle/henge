package com.kenzan.henge.repository;

import com.kenzan.henge.domain.model.Mapping;
import com.kenzan.henge.domain.model.NamedVersionedModelReference;

/**
 * Repository interface for the {@link Mapping}. It should be used whenever a
 * mapping instance is needed, or new entries were writen into it.
 * Implementations should use caching of the mapping to improve performance.
 *
 * @author wmatsushita
 */
public interface MappingRepository<T extends NamedVersionedModelReference> {

    /**
     * Persists a mapping with the optional given name. If the name is not
     * present, then it will use the value of versionset.mapping.file.name
     * configured in application.yml. New mapping entries will only be visible
     * to others after this method is called.
     * 
     * @param mapping the mapping instance to be persisted
     * @return the persisted mapping. This is so, instead of void, so that the
     *         cache can be updated with the returned value.
     */
    public Mapping<T> save(Mapping<T> mapping);

    /**
     * Loads the mapping. The name of the mapping file comes from the 
     * versionset.mapping.file.name configuration variable defined in 
     * application-flatfile_local.yml or application-flatfile_s3.yml.
     *  
     * @return Retrieves the stored {@link Mapping}. This method should be used
     *         whenever a reference to a mapping is needed
     */
    public Mapping<T> load();
    
}
