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
package com.kenzan.henge.service;

import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.model.type.PropertyGroupType;

import java.util.Set;

/**
 * 
 * @author kylebober
 *
 */
public interface PropertyGroupBD  extends BaseCrudBD<PropertyGroup> {
	
	/**
	 * Retrieve the list of the property group that the version set contains by {@link PropertyGroupType}
	 * @param versionSet
	 * @param type
	 * @return Set of {@link PropertyGroup}
	 */
	Set<PropertyGroup> getPropertyGroup(final VersionSet versionSet, final PropertyGroupType type);
	
	/**
	 * Retrieve the list of the property group that the version set contains by {@link PropertyGroupType}, filtering by libraries
	 * @param versionSet
	 * @param type
	 * @return Set of {@link PropertyGroup}
	 */
	Set<PropertyGroup> getPropertyGroup(final VersionSet versionSet, final PropertyGroupType type, final java.util.Optional<Set<String>> libs);

}
