package com.kenzan.henge.repository.impl.flatfile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.utils.JsonUtils;
import com.kenzan.henge.repository.VersionSetRepository;
import com.kenzan.henge.repository.impl.flatfile.storage.FileStorageService;

/**
 *
 *
 * @author wmatsushita
 */
@Component
@Profile({"flatfile_local","flatfile_s3"})
public class VersionSetFlatFileRepository extends BaseFlatFileRepository<VersionSet> implements VersionSetRepository {

    private static final Class<VersionSet> GENERIC_TYPE = VersionSet.class;

    @Autowired
    public VersionSetFlatFileRepository(FileStorageService fileStorageService, FileNamingService fileNamingService, JsonUtils jsonUtils) {

        super(fileStorageService, fileNamingService, jsonUtils);
        
    }
    
    @Override
    protected Class<VersionSet> getGenericType() {

        return GENERIC_TYPE;
    }

}
