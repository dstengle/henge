package com.kenzan.henge.repository.impl.flatfile;

import com.google.common.collect.Sets;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.utils.JsonUtils;
import com.kenzan.henge.exception.HengeValidationException;
import com.kenzan.henge.exception.RuntimeHengeException;
import com.kenzan.henge.repository.PropertyGroupRepository;
import com.kenzan.henge.repository.impl.flatfile.storage.FileStorageService;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 *
 * @author wmatusushita
 * @author Igor K. Shiohara
 */
@Component
@Profile({"flatfile_local","flatfile_s3"})
public class PropertyGroupFlatFileRepository extends BaseFlatFileRepository<PropertyGroup> implements PropertyGroupRepository {

    private static final Class<PropertyGroup> GENERIC_TYPE = PropertyGroup.class;
    

    @Autowired
    public PropertyGroupFlatFileRepository(FileStorageService fileStorageService, FileNamingService fileNamingService, JsonUtils jsonUtils) {

        super(fileStorageService, fileNamingService, jsonUtils);
        
    }
    
    @Override
    protected Class<PropertyGroup> getGenericType() {
        return GENERIC_TYPE;
    }
    
	@Override
	public Optional<PropertyGroup> delete(String name) {
		deleteValidation(name, StringUtils.EMPTY);
		return super.delete(name);
	}
	
	@Override
	public Optional<PropertyGroup> delete(String name, String version) {
		deleteValidation(name, version);
		return super.delete(name, version);
	}
	
	/**
	 * This method verifies that the given {@link PropertyGroup} is not referenced by any existing {@link VersionSet} 
	 * @param name {@link PropertyGroup} name
	 */
	private void deleteValidation(String name, String version) {
		final Set<String> referencedVersionSets = Sets.newHashSet();
		try {
			final String path = fileNamingService.getPath(VersionSet.class); 
		    Optional<Set<String>> versionSetFileNames = fileStorageService.getFileNamesStartingWith(path, StringUtils.EMPTY);
			if (versionSetFileNames.isPresent()) {
				
				for (String versionSetPath : versionSetFileNames.get()) {
					String content = fileStorageService.read(path, versionSetPath).get();
					VersionSet versionSet = jsonUtils.fromJson(content, VersionSet.class);
					versionSet.getPropertyGroupReferences().parallelStream().forEach(ref -> {
						if (!version.isEmpty() && version.equalsIgnoreCase(ref.getVersion()) && name.equalsIgnoreCase(ref.getName())) {
							referencedVersionSets.add(versionSet.getName());
						}
					});
				}
				
			}	
			if (!referencedVersionSets.isEmpty()) {
				throw new HengeValidationException(Status.CONFLICT, "You can not delete this property group "
						+ "because there are referenced VersionSets : " + referencedVersionSets.toString());
			}
			
		} catch (IOException e) {
			throw new RuntimeHengeException(Status.INTERNAL_SERVER_ERROR, "IO error while trying to delete "
	                + getGenericType().getSimpleName() + " file [" + name + "]", e);
		}
	}
    
}
