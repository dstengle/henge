package com.kenzan.henge.util;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.kenzan.henge.domain.model.FileVersion;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.repository.impl.flatfile.FileNamingService;
import com.kenzan.henge.repository.impl.flatfile.storage.FileStorageService;
import com.kenzan.henge.service.FileBD;
import com.kenzan.henge.service.PropertyGroupBD;
import com.kenzan.henge.service.VersionSetBD;

/**
 * This is used in the teardown of integration tests, to erase the entities created in them.
 * 
 * @author wmatsushita
 *
 */
@Component
@Profile({"flatfile_local", "flatfile_s3"})
public class FlatFileLocalCleaner implements CleanerUtils {

	@Autowired
	protected FileStorageService fileStorageService;
	
	@Autowired
	protected PropertyGroupBD propertyGroupBD;
	
	@Autowired
	protected VersionSetBD versionSetBD;

	@Autowired
	protected FileBD fileVersionBD;

	@Autowired
	protected FileNamingService fileNamingService;
	
	@Override
	public void execute() {
		//VersionSet
		deleteVersionSetsByNamePrefix("VersionSet-Test");
		
		//PropertyGroup
		deletePropertyGroupsByNamePrefix("PropertyGroup-Test");
		
		//File
		deleteFileVersionsByNamePrefix("FileVersion-Test");
		
	}

	/*
	 * The methods below will erase the files using the business delegates so the cache is also evicted.
	 * That's why we don't use the FileStorageService directly anymore.
	 */
	
	private void deleteVersionSetsByNamePrefix(String prefix) {
		Optional<Set<String>> fileNames = fileStorageService.getFileNamesStartingWith(fileNamingService.getPath(VersionSet.class), prefix);

		if(fileNames.isPresent()) {
			fileNames.get().forEach(fileName -> {
				final String name = fileNamingService.extractEntityNameFromFileName(fileName);
				final String version = fileNamingService.extractEntityVersionFromFileName(fileName);
				
				versionSetBD.delete(name, version);	
			});
		}
	}

	private void deletePropertyGroupsByNamePrefix(String prefix) {
		Optional<Set<String>> fileNames = fileStorageService.getFileNamesStartingWith(fileNamingService.getPath(PropertyGroup.class), prefix);

		if(fileNames.isPresent()) {
			fileNames.get().forEach(fileName -> {
				final String name = fileNamingService.extractEntityNameFromFileName(fileName);
				final String version = fileNamingService.extractEntityVersionFromFileName(fileName);
				
				propertyGroupBD.delete(name, version);	
			});
		}
	}

	private void deleteFileVersionsByNamePrefix(String prefix) {
		Optional<Set<String>> fileNames = fileStorageService.getFileNamesStartingWith(fileNamingService.getPath(FileVersion.class), prefix);

		if(fileNames.isPresent()) {
			fileNames.get().forEach(fileName -> {
				final String name = fileNamingService.extractEntityNameFromFileName(fileName);
				final String version = fileNamingService.extractEntityVersionFromFileName(fileName);
				
				fileVersionBD.delete(name, version);	
			});
		}
	}
}
