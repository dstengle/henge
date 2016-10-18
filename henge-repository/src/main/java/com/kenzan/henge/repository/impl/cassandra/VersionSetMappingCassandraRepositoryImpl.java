package com.kenzan.henge.repository.impl.cassandra;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.kenzan.henge.domain.model.Mapping;
import com.kenzan.henge.domain.model.MappingFactory;
import com.kenzan.henge.domain.model.MappingKey;
import com.kenzan.henge.domain.model.VersionSetReference;
import com.kenzan.henge.domain.utils.JsonUtils;
import com.kenzan.henge.exception.HengeException;
import com.kenzan.henge.exception.HengeParseException;
import com.kenzan.henge.repository.MappingRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("cassandra")
public class VersionSetMappingCassandraRepositoryImpl implements MappingRepository<VersionSetReference> {

	private static final Logger LOGGER = LoggerFactory.getLogger(VersionSetMappingCassandraRepositoryImpl.class);
	
	private static final String VALUE_COLUMN = "value";
	private static final String KEY_COLUMN = "key";

	private static final String VERSION_SET_MAPPING_COLUMN_FAMILY = "version_set_mapping";

	private Session session;
	
    private MappingFactory<VersionSetReference> mappingFactory;
	
	private JsonUtils jsonUtils;
	
    @Autowired
    public VersionSetMappingCassandraRepositoryImpl(Session session, MappingFactory<VersionSetReference> mappingFactory, JsonUtils jsonUtils) {
        this.session = session;
        this.mappingFactory = mappingFactory;
        this.jsonUtils = jsonUtils;
    }
	
	@Override
	public Mapping<VersionSetReference> save(Mapping<VersionSetReference> mapping) {
		insert(mapping);
		return mapping;
	}

	@Override
	public Mapping<VersionSetReference> load() {
		List<Row> rows = select();
		return transform(rows);
	}
	
	private void insert(Mapping<VersionSetReference> mapping) {
		Map<MappingKey, VersionSetReference> map = mapping.getInnerRepresentation();
		BatchStatement batch = new BatchStatement();
		for (Map.Entry<MappingKey, VersionSetReference> key : map.entrySet()) {
			try {
				Insert insert = QueryBuilder.insertInto(VERSION_SET_MAPPING_COLUMN_FAMILY)
						.value(KEY_COLUMN, jsonUtils.toJson(key.getKey()))
						.value(VALUE_COLUMN, jsonUtils.toJson(key.getValue()));
				batch.add(insert);
			} catch (IOException e) {
				LOGGER.error("Error to parse Key "+key.getKey()+ " and value " + key.getValue() + ". Exception:" + e);
				throw new HengeException("Error to parse Key "+key.getKey()+ " and value " + key.getValue());
			}
		}
		session.execute(batch);
	}
	
	private List<Row> select() {
		Select select = QueryBuilder.select()
						.from(VERSION_SET_MAPPING_COLUMN_FAMILY);
		return session.execute(select).all();
	}
	
	private Mapping<VersionSetReference> transform(final List<Row> rows) {
		Mapping<VersionSetReference> mapping = mappingFactory.create();
			for (Row row : rows) {
				String key = row.getString(KEY_COLUMN);
				String value = row.getString(VALUE_COLUMN);
				MappingKey mappingKey = null;
				VersionSetReference versionSetReference = null;
				try {
					if (StringUtils.isNotBlank(key)) {
						mappingKey = jsonUtils.fromJson(key, MappingKey.class);
					}
					if (StringUtils.isNotBlank(value)) {
						versionSetReference = jsonUtils.fromJson(value, VersionSetReference.class);
					}
				} catch (IOException e) {
					throw new HengeParseException("Parse error during transform key = "+key+ " and value = "+ value + ". Exception:" + e);
				}	
				mapping.put(mappingKey, versionSetReference);
			}
		return mapping;
			
	}

}
