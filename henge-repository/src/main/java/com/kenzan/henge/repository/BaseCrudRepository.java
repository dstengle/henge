/**
 * Copyright (C) ${project.inceptionYear} Kenzan - Kyle S. Bober (kbober@kenzan.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kenzan.henge.repository;

import java.util.Optional;
import java.util.Set;

import com.kenzan.henge.domain.model.NamedVersionedModel;

public interface BaseCrudRepository<E extends NamedVersionedModel> {

	/**
	 * Persists a new entity. If a version is not provided by the given entity, the default version is filled in.
	 * 
	 * @param entity the entity with the data to be persisted.
	 * @return the entity with the version filled in.
	 */
    public E create(final E entity);
	
	    /**
     * Updates the entity by persisting a new versioned instance of it. The
     * version number for the new instance must be passed in from the caller.
     * Implementations should validate that the given version is larger than the
     * current version.
     * 
     * @param name the name of the entity.
     * @param entity the entity with data that updates the current version.
     * @return an {@link Optional} with the entity or absent, in case the given name
     *         points to a non existent entity.
     */
    public Optional<E> update(final String name, final E entity);
	
	/**
	 * Deletes all the versions of a given entity.
	 * 
	 * @param name the name of the entity. All the versions of it must be deleted
     * @return an {@link Optional} with the current version of a persisted entity or absent, 
     * in case the given name points to a non existent entity. 
	 */
    public Optional<E> delete(final String name);
	
    /**
     * Deletes a given version of an entity.
     * 
     * @param name the name of the entity.
     * @param version a specific version of the entity.
     * @return an {@link Optional} with the given version of a persisted entity or absent, 
     * in case the given name points to a non existent entity. 
     */
	public Optional<E> delete(final String name, final String version);
	
	/**
	 * Reads and returns the latest version of an entity by the given name.
	 * 
	 * @param name the name of the entity. The current version should be retrieved.
     * @return an {@link Optional} with the current version of a persisted entity or absent, 
     * in case the given name points to a non existent entity. 
	 */
	public Optional<E> read(final String name);
	
    /**
     * Reads and returns a specific version of an entity.
     * 
     * @param name the name of the entity.
     * @param version the version of the entity.
     * @return an {@link Optional} with the specified version of a persisted entity or absent, 
     * in case the given name points to a non existent entity. 
     */
	public Optional<E> read(final String name, final String version);

    /**
     * Lists all the available versions o the the entity with
     * given name. Implementations must return an absent {@link Optional} if there are no versions
     * of the entity by the given name.
     * 
     * @param name the name of the entity.
     * @return an {@link Optional} containing a set of {@link String} with the available versions.
     */
    public Optional<Set<String>> versions(final String name);
    
}
