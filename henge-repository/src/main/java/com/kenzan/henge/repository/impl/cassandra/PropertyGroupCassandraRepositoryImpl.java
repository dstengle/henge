package com.kenzan.henge.repository.impl.cassandra;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.kenzan.henge.domain.model.Property;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.PropertyGroup.Builder;
import com.kenzan.henge.domain.utils.JsonUtils;
import com.kenzan.henge.exception.HengeParseException;
import com.kenzan.henge.repository.PropertyGroupRepository;

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
 * 
 * @author Igor K. Shiohara
 *
 */
@Profile("cassandra")
@Component
public class PropertyGroupCassandraRepositoryImpl extends BaseCassandraRepository<PropertyGroup> implements PropertyGroupRepository {

	private JsonUtils jsonUtils;
	
	private static final String PROPERTY_GROUP_COLUMN_FAMILY = "property_group";

	private static final Logger LOGGER = LoggerFactory.getLogger(PropertyGroupCassandraRepositoryImpl.class);
	
	
    @Autowired
	public PropertyGroupCassandraRepositoryImpl(Session session, JsonUtils jsonUtils) {
	    super(session);
	    this.jsonUtils = jsonUtils;
	}
	
	@Override
    protected Insert insertStatement(PropertyGroup propertyGroup) {
		try {
			Insert statement = QueryBuilder.insertInto(PROPERTY_GROUP_COLUMN_FAMILY)
						.value(PropertyGroupColumnFamily.NAME.toString(), propertyGroup.getName())
						.value(PropertyGroupColumnFamily.DESCRIPTION.toString(), propertyGroup.getDescription())
						.value(PropertyGroupColumnFamily.VERSION.toString(), propertyGroup.getVersion())
						.value(PropertyGroupColumnFamily.TYPE.toString(), propertyGroup.getType())
						.value(PropertyGroupColumnFamily.IS_ACTIVE.toString(), propertyGroup.isActive())
						.value(PropertyGroupColumnFamily.PROPERTIES.toString(),jsonUtils.toJson(propertyGroup.getProperties()))
						.value(PropertyGroupColumnFamily.CREATED_BY.toString(), propertyGroup.getCreatedBy());
						if (propertyGroup.getCreatedDate() != null) {
							statement.value(PropertyGroupColumnFamily.CREATED_DATE.toString(), Date.from(propertyGroup.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant()));
						}
			return statement;
		} catch (JsonProcessingException e) {
			LOGGER.info("Error to parse Properties: " + propertyGroup.getProperties() + ". Exception:" + e);
			throw new HengeParseException("Error to parse Properties: " + propertyGroup.getProperties());
		}
	}
	
	@Override
	protected PropertyGroup buildEntity(Row row) {
		try {
			Builder builder = PropertyGroup.builder(
			    row.getString(PropertyGroupColumnFamily.NAME.toString()), 
			    row.getString(PropertyGroupColumnFamily.VERSION.toString()))
					.withDescription(row.getString(PropertyGroupColumnFamily.DESCRIPTION.toString()))
					.withType(row.getString(PropertyGroupColumnFamily.TYPE.toString()))
					.withIsActive(row.getBool(PropertyGroupColumnFamily.IS_ACTIVE.toString()));
					if (StringUtils.isNotBlank(row.getString(PropertyGroupColumnFamily.PROPERTIES.toString()))) {
						builder.withProperties(jsonUtils.fromJson(row.getString(PropertyGroupColumnFamily.PROPERTIES.toString()), Property[].class));
					}
					builder.withCreatedBy(row.getString(PropertyGroupColumnFamily.CREATED_BY.toString()));
					if (row.getTimestamp(PropertyGroupColumnFamily.CREATED_DATE.toString()) != null) {
						builder.withCreatedDate(row.getTimestamp(PropertyGroupColumnFamily.CREATED_DATE.toString()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
					}
					return builder.build();
		} catch (IOException e) {
			LOGGER.error("Error to parse Properties: " + row.getString(PropertyGroupColumnFamily.PROPERTIES.toString()) + ". Exception:" + e);
			throw new HengeParseException("Error to parse Properties: " + row.getString(PropertyGroupColumnFamily.PROPERTIES.toString()));
		}	
	}

	@Override
	protected String getColumnFamily() {
		return PROPERTY_GROUP_COLUMN_FAMILY;
	}
	
	private enum PropertyGroupColumnFamily {
		NAME,
		DESCRIPTION,
		VERSION,
		TYPE,
		IS_ACTIVE,
		PROPERTIES,
		CREATED_BY,
		CREATED_DATE;
	}

}
