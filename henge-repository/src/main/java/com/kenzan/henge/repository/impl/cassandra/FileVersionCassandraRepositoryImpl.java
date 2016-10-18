package com.kenzan.henge.repository.impl.cassandra;

import java.nio.ByteBuffer;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.kenzan.henge.domain.model.FileVersion;
import com.kenzan.henge.domain.model.FileVersion.Builder;
import com.kenzan.henge.repository.FileVersionRepository;

/**
 * 
 * @author Igor K. Shiohara
 *
 */
@Profile("cassandra")
@Component
public class FileVersionCassandraRepositoryImpl extends BaseCassandraRepository<FileVersion> implements FileVersionRepository {

	private static final String FILE_VERSION_COLUMN_FAMILY = "file_version";
	
	@Autowired
	public FileVersionCassandraRepositoryImpl(Session session) {
		super(session);
	}

	@Override
	protected Insert insertStatement(FileVersion fileVersion) {
		Insert insertStatement = QueryBuilder.insertInto(FILE_VERSION_COLUMN_FAMILY)
									.value(FileVersionColumnFamily.NAME.toString(), fileVersion.getName())
									.value(FileVersionColumnFamily.VERSION.toString(), fileVersion.getVersion())
									.value(FileVersionColumnFamily.DESCRIPTION.toString(), fileVersion.getDescription())
									.value(FileVersionColumnFamily.CONTENT.toString(), ByteBuffer.wrap(fileVersion.getContent()))
									.value(FileVersionColumnFamily.FILENAME.toString(), fileVersion.getFilename())
									.value(FileVersionColumnFamily.CREATED_BY.toString(), fileVersion.getCreatedBy());
		if (fileVersion.getCreatedDate() != null) {
			insertStatement.value(FileVersionColumnFamily.CREATED_DATE.toString(), Date.from(fileVersion.getCreatedDate().atZone(ZoneId.systemDefault()).toInstant()));
		}

		return insertStatement;
	}

	@Override
	protected FileVersion buildEntity(Row row) {
		Builder builder = FileVersion.builder(
								row.getString(FileVersionColumnFamily.NAME.toString()),
								row.getString(FileVersionColumnFamily.VERSION.toString()), 
								row.getBytes(FileVersionColumnFamily.CONTENT.toString()).array(), 
								row.getString(FileVersionColumnFamily.FILENAME.toString()))
							.withDescription(row.getString(FileVersionColumnFamily.DESCRIPTION.toString()))
							.withCreatedBy(row.getString(FileVersionColumnFamily.CREATED_BY.toString()));
		if (row.getTimestamp(FileVersionColumnFamily.CREATED_DATE.toString()) != null) {
			builder.withCreatedDate(row.getTimestamp(FileVersionColumnFamily.CREATED_DATE.toString()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
		}

		return builder.build();
	}
	
	@Override
	protected String getColumnFamily() {
		return FILE_VERSION_COLUMN_FAMILY;
	}
	
	private enum FileVersionColumnFamily {
		NAME,
		DESCRIPTION,
		VERSION,
		CONTENT,
		FILENAME,
		CREATED_BY,
		CREATED_DATE,
	}


}
