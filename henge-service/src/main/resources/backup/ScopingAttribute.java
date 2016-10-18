package com.kenzanmedia.propertiesserver.model;

import com.google.auto.value.AutoValue;


public abstract class ScopingAttribute {

  protected ScopingAttribute() {
  }

  public static ScopingAttribute create(String name, String value) {
    return new AutoValue_ScopingAttribute(name, value);
  }

  public abstract String name();

  public abstract String value();

  @Override
  public String toString() {
    return name() + "=" + value();
  }

}
