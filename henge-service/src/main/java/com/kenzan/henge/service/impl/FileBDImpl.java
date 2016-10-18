package com.kenzan.henge.service.impl;

import com.kenzan.henge.config.CacheConfig;
import com.kenzan.henge.domain.model.FileVersion;
import com.kenzan.henge.exception.HengeValidationException;
import com.kenzan.henge.repository.CurrentFileVersionRepository;
import com.kenzan.henge.repository.FileVersionRepository;
import com.kenzan.henge.service.FileBD;

import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * @author Igor K. Shiohara
 */
@Component
public class FileBDImpl implements FileBD {

	private FileVersionRepository repository;
	private CurrentFileVersionRepository currentRepository;
	private Cache fileApiCache;

	@Autowired
	public FileBDImpl(FileVersionRepository repository, CurrentFileVersionRepository currentRepository, @Qualifier(CacheConfig.FILE_API_CACHE) Cache fileApiCache) {
		this.repository = repository;
		this.currentRepository = currentRepository;
		this.fileApiCache = fileApiCache;
	}

	@Override
	public FileVersion create(FileVersion entity) {
		final FileVersion fileVersion = repository.create(entity);
		
		fileApiCache.put(entity.getName(), Optional.of(fileVersion));
		fileApiCache.put(entity.getName() + entity.getVersion(), Optional.of(fileVersion));
		
		return fileVersion; 
	}

	@Override
	public Optional<FileVersion> update(String name, FileVersion fileVersion) {
		Optional<FileVersion> last = repository.read(name);
		if (!last.isPresent()) {
			return Optional.empty();
		}
		if (fileVersion.compareTo(last.get()) <= 0) {
			throw new HengeValidationException(Status.CONFLICT, "The FileVersion " + fileVersion.getName()
                    + " object given for update has a version number [" + fileVersion.getVersion()
                    + "] that is lesser than or equal to the current version [" + last.get().getVersion()
                    + "].");
		}
		
	    final FileVersion newEntity = (FileVersion.builder(fileVersion)).build();
	    final Optional<FileVersion> updated = repository.update(name, newEntity); 
	    
	    fileApiCache.put(fileVersion.getName(), updated);
	    fileApiCache.put(fileVersion.getName() + fileVersion.getVersion(), updated);
	    
	    
		return updated;
		
	}

	@Override
	@CacheEvict(cacheNames=CacheConfig.FILE_API_CACHE, key = "#name")
	public Optional<FileVersion> delete(String name) {
		return repository.delete(name);
	}

	@Override
	@CacheEvict(cacheNames=CacheConfig.FILE_API_CACHE, key = "#name + #version")
	public Optional<FileVersion> delete(String name, String version) {
		return repository.delete(name, version);
	}

	@Override
	@Cacheable(value = CacheConfig.FILE_API_CACHE, key = "#name")
	public Optional<FileVersion> read(String name) {
		return repository.read(name);
	}

	@Override
	@Cacheable(value = CacheConfig.FILE_API_CACHE, key = "#name + #version")
	public Optional<FileVersion> read(String name, String version) {
		return repository.read(name, version);
	}

	@Override
	public Optional<Set<String>> versions(String name) {
		return repository.versions(name);
	}

	@Override
	public FileVersion setCurrentVersion(final String fileVersionName, final String fileVersionVersion) {
		Optional<FileVersion> existent = repository.read(fileVersionName, fileVersionVersion);
		if (!existent.isPresent()) {
			throw new HengeValidationException(Status.NO_CONTENT, "This FileVersion with name "+ fileVersionName +" and version " + fileVersionVersion + " doesn't exists.");
		}
		currentRepository.setCurrentVersion(fileVersionName, fileVersionVersion);
		return existent.get();
	}

	@Override
	public Optional<FileVersion> getCurrentVersion(String fileVersionName) {
		Optional<String> version = currentRepository.getCurrentVersion(fileVersionName);
		if (!version.isPresent()) {
			throw new HengeValidationException(Status.NO_CONTENT, "This FileVersion with name "+ fileVersionName +" and version " + version.get() + " doesn't exists.");
		}
		return repository.read(fileVersionName, version.get());
	}

}
