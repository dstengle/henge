CREATE KEYSPACE henge WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3};

use henge;

CREATE TABLE property_group (
  name text,
  description text,
  version text,
  type text,
  is_active boolean,
  properties text,
  created_by text,
  modified_by text,
  created_date timestamp,
  modified_date timestamp,
  PRIMARY KEY (name, version)
);

CREATE TABLE version_set (
  name text,
  description text,
  version text,
  property_group_references text,
  scoped_property_value_keys text,
  file_version_references text,
  is_type_hierarchy_enabled boolean,
  created_by text,
  modified_by text,
  created_date timestamp,
  modified_date timestamp,
  PRIMARY KEY (name, version)
);

CREATE TABLE version_set_mapping (
	key text,
	value text,
	PRIMARY KEY (key)
);

CREATE TABLE file_version (
  name text,
  description text,
  version text,
  content blob,
  filename text,
  created_by text,
  modified_by text,
  created_date timestamp,
  modified_date timestamp,
  PRIMARY KEY (name, version)
);

CREATE TABLE file_version_current (
  name text,
  version text,
  PRIMARY KEY (name)
);
