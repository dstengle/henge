package com.kenzan.henge.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kenzan.henge.domain.validator.CheckModelReference;
import com.kenzan.henge.domain.validator.CheckReferenceValidator;
import com.kenzan.henge.domain.validator.CheckScopeString;

import javax.validation.constraints.NotNull;

public class MappingGroup {
	
	@NotNull
    private final String application;
	
	@CheckScopeString
	private final String scopeString;

	@CheckModelReference(value = VersionSet.class, groups = {CheckReferenceValidator.class})
	private final VersionSetReference vsReference;

	@JsonCreator(mode = Mode.PROPERTIES)
	public MappingGroup(@JsonProperty("application") final String application, 
						 @JsonProperty("scopeString") final String scopeString, 
						 @JsonProperty("vsReference") final VersionSetReference vsReference) {
		this.application = application;
		this.scopeString = scopeString;
		this.vsReference = vsReference;
	}
	
	public String getApplication() {
		return application;
	}
	
	public String getScopeString() {
		return scopeString;
	}
	
	public VersionSetReference getVsReference() {
		return vsReference;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((application == null) ? 0 : application.hashCode());
		result = prime * result + ((scopeString == null) ? 0 : scopeString.hashCode());
		result = prime * result + ((vsReference == null) ? 0 : vsReference.hashCode());
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
		MappingGroup other = (MappingGroup) obj;
		if (application == null) {
			if (other.application != null)
				return false;
		} else if (!application.equals(other.application))
			return false;
		if (scopeString == null) {
			if (other.scopeString != null)
				return false;
		} else if (!scopeString.equals(other.scopeString))
			return false;
		if (vsReference == null) {
			if (other.vsReference != null)
				return false;
		} else if (!vsReference.equals(other.vsReference))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "MappingGroup [application=" + application + ", scopeString=" + scopeString + ", vsReference="
				+ vsReference + "]";
	}
	
}
