package com.kenzan.henge.repository.impl.cassandra;

import com.kenzan.henge.domain.model.FileVersion;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.exception.HengeException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("cassandra")
@Component
public class CassandraRepositoryFactory {
	
	private PropertyGroupCassandraRepositoryImpl propertyGroupRepository;
	
	private VersionSetCassandraRepositoryImpl versionSetRepository;
	
	private FileVersionCassandraRepositoryImpl fileVersionRepository;
	
    @Autowired
	public CassandraRepositoryFactory(PropertyGroupCassandraRepositoryImpl propertyGroupRepository, VersionSetCassandraRepositoryImpl versionSetRepository, FileVersionCassandraRepositoryImpl fileVersionRepository) {
	    this.propertyGroupRepository = propertyGroupRepository;
	    this.versionSetRepository = versionSetRepository;
	    this.fileVersionRepository = fileVersionRepository;
	}
	
	public BaseCassandraRepository<?> get(final Class<?> modelType) {
		if (modelType.equals(PropertyGroup.class)) {
			return propertyGroupRepository;
		}
		if (modelType.equals(VersionSet.class)) {
			return versionSetRepository;
		}
		if (modelType.equals(FileVersion.class)) {
			return fileVersionRepository;
		}
		throw new HengeException("Wrong paremeter passed. Expected values are: PropertyGroup and VersionSet classes.");
	}

}
