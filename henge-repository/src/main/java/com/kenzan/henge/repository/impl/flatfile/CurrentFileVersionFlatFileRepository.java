package com.kenzan.henge.repository.impl.flatfile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.kenzan.henge.domain.model.FileVersion;
import com.kenzan.henge.domain.utils.JsonUtils;
import com.kenzan.henge.exception.HengeParseException;
import com.kenzan.henge.repository.CurrentFileVersionRepository;
import com.kenzan.henge.repository.impl.flatfile.storage.FileStorageService;

/**
 * @author Igor K. Shiohara
 */
@Component
@Profile({"flatfile_local","flatfile_s3"})
public class CurrentFileVersionFlatFileRepository implements CurrentFileVersionRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(CurrentFileVersionFlatFileRepository.class);
	
	private static final Class<FileVersion> GENERIC_TYPE = FileVersion.class;
	private static final String DATA_SUB_FOLDER_NAME = "data";
	private static final String FILENAME = "CurrentFileVersionsMap";
	
	private FileStorageService fileStorageService;
    private FileNamingService fileNamingService;
    private JsonUtils jsonUtils;
    
    @Autowired
    public CurrentFileVersionFlatFileRepository(final FileStorageService fileStorageService, 
    											final FileNamingService fileNamingService, 
    											final JsonUtils jsonUtils) {
    	this.fileStorageService = fileStorageService;
    	this.fileNamingService = fileNamingService;
    	this.jsonUtils = jsonUtils;
    }
	
	@Override
	public void setCurrentVersion(final String fileVersionName, final String fileVersionVersion) {
		final String dataPath = fileNamingService.getPath(GENERIC_TYPE, DATA_SUB_FOLDER_NAME);
        String json;
		try {
			Map<String, String> data = getAllCurrentVersionsInAMap();
			data.put(fileVersionName, fileVersionVersion);
			json = jsonUtils.toJson(data);
		} catch (JsonProcessingException e) {
			String message = "Failed to convert the FileVersionCurrent to JSON.";
			LOGGER.error(message);
			throw new HengeParseException(message);
		} 
		
        fileStorageService.writeBytes(dataPath, FILENAME, json.getBytes(), true);
	}

	@Override
	public Optional<String> getCurrentVersion(String fileVersionName) {
		return Optional.of(getAllCurrentVersionsInAMap().get(fileVersionName));
	}

	private Map<String, String> getAllCurrentVersionsInAMap() {
		final String path = fileNamingService.getPath(GENERIC_TYPE, DATA_SUB_FOLDER_NAME);
		Optional<String> json = Optional.empty();
		try {
			json = fileStorageService.read(path, FILENAME);
			if (json.isPresent()) {
				return jsonUtils.fromJson(json.get(), new TypeReference<Map<String, String>>(){});
			}
			return Maps.newHashMap();
		} catch (IOException e) {
			String message = "Failed to convert the JSON (" + json.get() + ") to FileVersionCurrent.";
			LOGGER.error(message);
			throw new HengeParseException(message);
		}
	}

}
