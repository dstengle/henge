package com.kenzan.henge.repository;

import java.util.Optional;

public interface CurrentFileVersionRepository {
	
	/**
	 * Set the current version of the binary file
	 */
	void setCurrentVersion(final String fileVersionName, final String fileVersionVersion);

	/**
	 * Get the current version of the binary file
	 * @return
	 */
	Optional<String> getCurrentVersion(final String fileVersionName);

}
