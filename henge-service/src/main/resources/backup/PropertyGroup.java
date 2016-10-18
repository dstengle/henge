package com.kenzanmedia.propertiesserver.model;

import com.google.auto.value.AutoValue;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * PropertyGroup holds a related collection of properties together by name, type and version.
 *
 * Created by dstengle on 3/12/15.
 */

@AutoValue
public abstract class PropertyGroup {

  public static PropertyGroup create(String propertyGroupName, PropertyGroupType propertyGroupType, ZonedDateTime propertyGroupUpdateDate,
                              String propertyGroupVersion, List<ConfigProperty> configProperties) {

    return new AutoValue_PropertyGroup(propertyGroupName, propertyGroupType, propertyGroupUpdateDate, propertyGroupVersion,
        configProperties);

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
   * Get date PropertyGroup was updated.
   *
   * @return
   */
  public abstract ZonedDateTime propertyGroupUpdateDate();

  /**
   * Get version of PropertyGroup.
   *
   * @return
   */
  public abstract String propertyGroupVersion();

  /**
   * Get list of properties in PropertyGroup.
   *
   * @return
   */
  public abstract List<ConfigProperty> configProperties();

}
