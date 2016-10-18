package com.kenzan.henge.repository.impl.cassandra;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.kenzan.henge.repository.CurrentFileVersionRepository;

/**
 * @author Igor K. Shiohara
 */
@Profile("cassandra")
@Component
public class CurrentFileVersionCassandraRepositoryImpl implements CurrentFileVersionRepository {

	private Session session;

	private static final String FILE_VERSION_CURRENT_COLUMN_FAMILY = "file_version_current";
    
	@Autowired
	public CurrentFileVersionCassandraRepositoryImpl(Session session) {
        this.session = session;
    }
	
	@Override
	public void setCurrentVersion(final String fileVersionName, final String fileVersionVersion) {
		if (getCurrentVersion(fileVersionName).isPresent()) {
			Statement updateCurrentVersion = QueryBuilder.update(FILE_VERSION_CURRENT_COLUMN_FAMILY)
												.with(QueryBuilder.set("version", fileVersionVersion))
												.where(eq("name", fileVersionName));
			session.execute(updateCurrentVersion);
		}
		Insert insertCurrentVersion = QueryBuilder.insertInto(FILE_VERSION_CURRENT_COLUMN_FAMILY)
				.value("name", fileVersionName)
				.value("version", fileVersionVersion);
		session.execute(insertCurrentVersion);
	}

	@Override
	public Optional<String> getCurrentVersion(final String fileVersionName) {
		Select select = select().from(FILE_VERSION_CURRENT_COLUMN_FAMILY);
		select.where(eq("name", fileVersionName));
		
		Optional<Row> rows = session.execute(select).all().stream().findFirst();
		if (rows.isPresent()) {
			return Optional.of(rows.get().getString("version"));
		}
		return Optional.empty();
	}

}
