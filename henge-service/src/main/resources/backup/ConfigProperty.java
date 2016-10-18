package com.kenzanmedia.propertiesserver.model;

import com.google.auto.value.AutoValue;

/**
 * Created by dstengle on 12/12/14.
 */
@AutoValue
public abstract class ConfigProperty {

  protected ConfigProperty() {
  }

  public static ConfigProperty create(String name, String value, String description, Scope scope, PropertyGroupType propertyGroupType, String propertyGroupName, String propertyGroupVersion) {
    return new AutoValue_ConfigProperty(name, value, description, scope, propertyGroupType, propertyGroupName, propertyGroupVersion);
  }

  public abstract String name();

  public abstract String value();

  public abstract String description();

  public abstract Scope scope();

  public abstract PropertyGroupType propertyGroupType();

  public abstract String propertyGroupName();

  public abstract String propertyGroupVersion();


}
