package com.kenzan.henge.repository.impl.cassandra;

import static com.datastax.driver.core.querybuilder.QueryBuilder.desc;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.kenzan.henge.domain.model.NamedVersionedModel;
import com.kenzan.henge.exception.HengeResourceNotFoundException;
import com.kenzan.henge.repository.BaseCrudRepository;

/**
 * Cassandra base repository
 * 
 * @author Igor K. Shiohara
 *
 * @param <T> entity
 */
public abstract class BaseCassandraRepository<T extends NamedVersionedModel> implements BaseCrudRepository<T> {

	private Session session;
	
	private static final String NAME_COLUMN = "name";
	private static final String VERSION_COLUMN = "version";
	
    
	@Autowired
	public BaseCassandraRepository(Session session) {
        this.session = session;
    }
	
	@Override
	public T create(T entity) {
		Statement insert = insertStatement(entity);
		session.execute(insert);
		return entity;
	}

	@Override
	public Optional<T> update(String name, T entity) {
		if (!findFirst(Optional.of(name), Optional.empty()).isPresent()) {
			return Optional.empty();
		}
		session.execute(insertStatement(entity));
		return Optional.of(entity);
	}

	@Override
	public Optional<T> delete(String name) {
		Statement delete = QueryBuilder.delete().from(getColumnFamily())
				.where(eq(NAME_COLUMN, name));
		Optional<T> entity = findFirst(Optional.of(name), Optional.empty());
		if (!entity.isPresent()) {
			throw new HengeResourceNotFoundException("No data were found with name: " + name + " to delete.");
		}
		session.execute(delete);
		return entity;
	}

	@Override
	public Optional<T> delete(String name, String version) {
		Statement delete = QueryBuilder.delete().from(getColumnFamily())
				.where(eq(NAME_COLUMN, name))
				.and(eq(VERSION_COLUMN, version));
		Optional<T> entity = findFirst(Optional.of(name), Optional.of(version));
		if (!entity.isPresent()) {
			throw new HengeResourceNotFoundException("No data were found with name: " + name + " and version " + version + " to delete.");
		}
		session.execute(delete);
		return entity;
	}

	@Override
	public Optional<T> read(String name) {
		java.util.Optional<String> latestVersion = getVersions(name).findFirst();
		return findFirst(Optional.of(name), Optional.of(latestVersion.get()));
	}

	@Override
	public Optional<T> read(String name, String version) {
		return findFirst(Optional.of(name), Optional.of(version));
	}

	@Override
	public Optional<Set<String>> versions(String name) {
		Set<String> versions = getVersions(name).collect(Collectors.toSet());
		return Optional.of(versions);
	}
	
	/**
     * Checks if an entity by the given name exists in the repository.
     * @param name the name of the entity to check for existence.
     * @return true if the entity exists, false otherwise.
     */
	public boolean exists(final String name, final String version) {
		return read(name, version).isPresent();
	}
	
	/**
	 * Create an insert statement
	 * @param propertyGroup
	 * @return a PropertyGroup
	 */
	protected abstract Insert insertStatement(T entity);
	
	/**
	 * Builds the entity with the Cassandra rows
	 * @param row
	 * @return a filled entity
	 */
	protected abstract T buildEntity(Row row);
	
	/**
     * Provides the column family name
     * 
     * @return Column family name
     */
    protected abstract String getColumnFamily();
	
	/**
	 * Retrieves all versions
	 * @return Stream of versions
	 */
	private Stream<String> getVersions(String name) {
		Select select = select()
				  .column(VERSION_COLUMN)
				  .from(getColumnFamily())
				  .where(eq(NAME_COLUMN, name))
				  .orderBy(desc(VERSION_COLUMN));
		return session.execute(select)
				.all()
				.stream()
				.map(row -> row.getString(VERSION_COLUMN));
	}

	/**
	 * Finds the first T by name and version
	 * @param name T name
	 * @param version T version
	 * @return T
	 */
	private Optional<T> findFirst(final Optional<String> name, final Optional<String> version) {
		Select select = select().from(getColumnFamily());
		select.where(eq(NAME_COLUMN, name.get()));
		
		if (version.isPresent()) {
			select.where().and(eq(VERSION_COLUMN, version.get()));
		}
		
		java.util.Optional<Row> row = session.execute(select)
			.all()
			.stream()
			.findFirst();
		
		if (row.isPresent()) {
			return Optional.of(buildEntity(row.get()));
		}
		return Optional.empty();
	}
	
	protected Session getSession() {
		return session;
	}

}
