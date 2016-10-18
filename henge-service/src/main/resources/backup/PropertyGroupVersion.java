package com.kenzanmedia.propertiesserver.model;

import com.google.auto.value.AutoValue;

import java.time.ZonedDateTime;
import java.util.Date;

/**
 * PropertyGroupVersion is a metadata class that allows information about a PropertyGroup to be pulled from the
 * PropertyGroupStore without requiring the transfer of the full PropertyGroup.
 *
 *
 * Created by dstengle on 3/8/15.
 */
@AutoValue
public abstract class PropertyGroupVersion {

  protected PropertyGroupVersion() {}

  public static PropertyGroupVersion create(String propertyGroupName, PropertyGroupType propertyGroupType, ZonedDateTime updateDate, String propertyGroupVersion) {
    return new AutoValue_PropertyGroupVersion(propertyGroupName, propertyGroupType, updateDate, propertyGroupVersion);
  }

  /**
   * Get PropertyGroup name.
   *
   * @return
   */
  public abstract String propertyGroupName();

  /**
   * Get PropertyGroup type.
   *
   * @return
   */
  public abstract PropertyGroupType propertyGroupType();

  /**
   * Get PropertyGroup update time.
   *
   * @return
   */
  public abstract ZonedDateTime updateDate();

  /**
   * Get PropertyGroup version.
   *
   * @return
   */
  public abstract String propertyGroupVersion();

}
