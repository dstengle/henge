package com.kenzan.henge.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;

/**
 * @author Igor K. Shiohara
 */
@JsonDeserialize(builder = Group.Builder.class, as = Group.class)
@JsonPropertyOrder({"versionSetList", "propertyGroupList", "mappingList"})
public class Group implements Serializable {
	
	private static final long serialVersionUID = -1328010682066722894L;

	private Group(final Set<VersionSet> versionSetList, final Set<PropertyGroup> propertyGroupList, final Set<MappingGroup> mappingList) {
		this.versionSetList = versionSetList;
		this.propertyGroupList = propertyGroupList;
		this.mappingList = mappingList;
	}

	@Valid
	private final Set<VersionSet> versionSetList;
	
    @Valid
	private final Set<PropertyGroup> propertyGroupList;
	
    @Valid
	private final Set<MappingGroup> mappingList;

	public Set<VersionSet> getVersionSetList() {
		return versionSetList;
	}

	public Set<PropertyGroup> getPropertyGroupList() {
		return propertyGroupList;
	}
	
	public Set<MappingGroup> getMappingList() {
		return mappingList;
	}
	
	public static Builder builder(final Set<VersionSet> versionSetList, final Set<PropertyGroup> propertyGroupList, final Set<MappingGroup> mappingList) {
		return new Builder(versionSetList, propertyGroupList, mappingList);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mappingList == null) ? 0 : mappingList.hashCode());
		result = prime * result + ((propertyGroupList == null) ? 0 : propertyGroupList.hashCode());
		result = prime * result + ((versionSetList == null) ? 0 : versionSetList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Group other = (Group) obj;
		if (mappingList == null) {
			if (other.mappingList != null)
				return false;
		} else if (!mappingList.equals(other.mappingList))
			return false;
		if (propertyGroupList == null) {
			if (other.propertyGroupList != null)
				return false;
		} else if (!propertyGroupList.equals(other.propertyGroupList))
			return false;
		if (versionSetList == null) {
			if (other.versionSetList != null)
				return false;
		} else if (!versionSetList.equals(other.versionSetList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Group [versionSetList=" + versionSetList + ", propertyGroupList=" + propertyGroupList + ", mappingList="
				+ mappingList + "]";
	}

	@JsonPOJOBuilder(buildMethodName = "build")
	public static final class Builder {
		
		private Set<VersionSet> versionSetList;
		
		private Set<PropertyGroup> propertyGroupList;
		
		private Set<MappingGroup> mappingList;
		
		@JsonCreator
		public Builder(final @JsonProperty(value = "versionSetList") Set<VersionSet> versionSetList, 
					   final @JsonProperty(value = "propertyGroupList") Set<PropertyGroup> propertyGroupList,
					   final @JsonProperty(value = "mappingList") Set<MappingGroup> mappingList) {
			this.versionSetList = versionSetList == null? new HashSet<>() : versionSetList;
			this.propertyGroupList = propertyGroupList == null? new HashSet<>() : propertyGroupList;
			this.mappingList = mappingList == null? new HashSet<>() : mappingList;
		}
		
		public Group build() {
			return new Group(versionSetList, propertyGroupList, mappingList);
		}
		
	}
	
}
