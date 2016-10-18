package com.kenzan.henge.service;

import java.util.Optional;

import com.kenzan.henge.domain.model.FileVersion;

/**
 * 
 * Provides de File services
 * 
 * @author Igor K. Shiohara
 *
 */
public interface FileBD extends BaseCrudBD<FileVersion>{

	/**
	 * Set the current version of the binary file
	 * @param fileVersionName
	 * @param fileVersionVersion
	 * @return {@link FileVersion}
	 */
	FileVersion setCurrentVersion(final String fileVersionName, final String fileVersionVersion);

	/**
	 * Get the current version of the binary file
	 * @param fileVersionName
	 * @return {@link FileVersion}
	 */
	Optional<FileVersion> getCurrentVersion(String fileVersionName);

}
