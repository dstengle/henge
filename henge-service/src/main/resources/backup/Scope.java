package com.kenzanmedia.propertiesserver.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Created by dstengle on 12/13/14.
 */

@AutoValue
public abstract class Scope {

  protected Scope() {
  }

  public static Scope create(Set<ScopingAttribute> scopingAttributes) {
    return new AutoValue_Scope(ImmutableSet.copyOf(scopingAttributes));
  }
  
  public static Scope create(ScopingAttribute... scopingAttributes){
      return new AutoValue_Scope(ImmutableSet.copyOf(scopingAttributes));
  }

  public abstract Set<ScopingAttribute> scopes();

}
