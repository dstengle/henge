package com.kenzan.henge.repository.impl.cassandra;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.kenzan.henge.domain.model.FileVersionReference;
import com.kenzan.henge.domain.model.PropertyGroupReference;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.model.VersionSet.Builder;
import com.kenzan.henge.domain.utils.JsonUtils;
import com.kenzan.henge.exception.HengeParseException;
import com.kenzan.henge.repository.VersionSetRepository;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author Igor K. Shiohara
 */
@Component
@Profile("cassandra")
public class VersionSetCassandraRepositoryImpl extends BaseCassandraRepository<VersionSet> implements VersionSetRepository{
	
	private static final String VERSION_SET_COLUMN_FAMILY = "version_set";

	private static final Logger LOGGER = LoggerFactory.getLogger(VersionSetCassandraRepositoryImpl.class);

	private JsonUtils jsonUtils;
	
    @Autowired
	public VersionSetCassandraRepositoryImpl(Session session,  JsonUtils jsonUtils) {
        super(session);
	    this.jsonUtils = jsonUtils;
	}

	@Override
	protected Insert insertStatement(VersionSet versionSet) {
		try {
			Insert insert = QueryBuilder.insertInto(VERSION_SET_COLUMN_FAMILY)
					.value(VersionSetColumnFamily.NAME.toString(), versionSet.getName())
					.value(VersionSetColumnFamily.DESCRIPTION.toString(), versionSet.getDescription())
					.value(VersionSetColumnFamily.VERSION.toString(), versionSet.getVersion())
					.value(VersionSetColumnFamily.PROPERTY_GROUP_REFERENCES.toString(), jsonUtils.toJson(versionSet.getPropertyGroupReferences()))
					.value(VersionSetColumnFamily.FILE_VERSION_REFERENCES.toString(), jsonUtils.toJson(versionSet.getFileVersionReferences()))
					.value(VersionSetColumnFamily.CREATED_BY.toString(), versionSet.getCreatedBy());
					if (versionSet.getCreatedDate() != null) {
						insert.value(VersionSetColumnFamily.CREATED_DATE.toString(), Date.from(versionSet.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant()));
					}
			return insert;
		} catch (JsonProcessingException e) {
			LOGGER.info("Error to parse PropertyGroupReferences: " + versionSet.getPropertyGroupReferences() + ". Exception:" + e);
			throw new HengeParseException("Error to parse PropertyGroupReferences: " + versionSet.getPropertyGroupReferences());
		}	
	}
	
	@Override
	protected VersionSet buildEntity(Row row) {
		try {
			Builder builder = VersionSet.builder(
			    row.getString(VersionSetColumnFamily.NAME.toString()), 
			    row.getString(VersionSetColumnFamily.VERSION.toString()))
					.withDescription(row.getString(VersionSetColumnFamily.DESCRIPTION.toString()));
					if (StringUtils.isNotBlank(row.getString(VersionSetColumnFamily.PROPERTY_GROUP_REFERENCES.toString()))) {
						builder.withPropertyGroupReferences(jsonUtils.fromJson(row.getString(VersionSetColumnFamily.PROPERTY_GROUP_REFERENCES.toString()), PropertyGroupReference[].class));
					}
					if (StringUtils.isNotBlank(row.getString(VersionSetColumnFamily.FILE_VERSION_REFERENCES.toString()))) {
						builder.withFileVersionReferences(jsonUtils.fromJson(row.getString(VersionSetColumnFamily.FILE_VERSION_REFERENCES.toString()), FileVersionReference[].class));
					}
					builder.withCreatedBy(row.getString(VersionSetColumnFamily.CREATED_BY.toString()));
			if (row.getTimestamp(VersionSetColumnFamily.CREATED_DATE.toString()) != null) {
				builder.withCreatedDate(row.getTimestamp(VersionSetColumnFamily.CREATED_DATE.toString()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
			}
			return builder.build();
		} catch (IOException e) {
			LOGGER.error("Error to parse PropertyGroupReferences: " + row.getString(VersionSetColumnFamily.PROPERTY_GROUP_REFERENCES.toString()) + ". Exception:" + e);
			throw new HengeParseException("Error to parse PropertyGroupReferences: " + row.getString(VersionSetColumnFamily.PROPERTY_GROUP_REFERENCES.toString()));
		}	
	}
	
	@Override
	protected String getColumnFamily() {
		return VERSION_SET_COLUMN_FAMILY;
	}

	private enum VersionSetColumnFamily {
		NAME,
		DESCRIPTION,
		VERSION,
		PROPERTY_GROUP_REFERENCES,
		SCOPED_PROPERTY_VALUE_KEYS,
		FILE_VERSION_REFERENCES,
		IS_TYPE_HIERARCHY_ENABLED,
		CREATED_BY,
		CREATED_DATE;
	}

}
