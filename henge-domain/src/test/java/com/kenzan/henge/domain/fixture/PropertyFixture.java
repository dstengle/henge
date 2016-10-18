package com.kenzan.henge.domain.fixture;

import com.google.common.collect.Sets;
import com.kenzan.henge.domain.model.Property;
import com.kenzan.henge.domain.model.PropertyScopedValue;
import com.kenzan.henge.domain.model.Scope;

public class PropertyFixture {
	
	private static Property property1;
	private static Property property2WithConflict;
	private static Property property2;
	private static Property property3;
	private static Property property4;
	private static Property property5;
	private static Property property6;
	private static Property property7;
	private static Property property8;

	public PropertyFixture() {

	    final Scope env_dev = Scope.builder("env", "dev").build();
	    final Scope env_prod = Scope.builder("env", "prod").build();
	    final Scope stack1 = Scope.builder("stack", "stack1").build();
	    final Scope stack2 = Scope.builder("stack", "stack2").build();
	    final Scope region1 = Scope.builder("region", "region1").build();
	    final Scope region2 = Scope.builder("region", "region2").build();
		
	    final PropertyScopedValue scopesCombination1 = PropertyScopedValue.builder(Sets.newHashSet(env_dev), "end-dev-value").build();
	    final PropertyScopedValue scopesCombination2 = PropertyScopedValue.builder(Sets.newHashSet(env_dev, region1), "end-dev-region-1-value").build();
	    final PropertyScopedValue scopesCombination3 = PropertyScopedValue.builder(Sets.newHashSet(env_dev, region2), "end-dev-region-2-value").build();
	    final PropertyScopedValue scopesCombination4 = PropertyScopedValue.builder(Sets.newHashSet(env_dev, stack1, region1), "end-dev-stack-1-region-1").build();
	    final PropertyScopedValue scopesCombination5 = PropertyScopedValue.builder(Sets.newHashSet(env_prod, stack2, region1), "end-prod-stack-2-region-1").build();
	    final PropertyScopedValue scopesCombination6 = PropertyScopedValue.builder(Sets.newHashSet(env_prod, region1), "end-prod-region1-value").build();
		
		property1 = new Property.Builder("property-1").withDefaultValue("DefaultValue1").withDescription("Property-Description1").withScopedValues(scopesCombination1, scopesCombination2, scopesCombination3, scopesCombination4, scopesCombination5, scopesCombination6).build();
		property2WithConflict = new Property.Builder("property-2").withDefaultValue("ConflictedProperty").withDescription("Conflicted Property with Lib1").withScopedValues(scopesCombination2, scopesCombination4).build();
		property2 = new Property.Builder("property-2").withDefaultValue("DefaultValue2").withDescription("Property-Description2").withScopedValues(scopesCombination2, scopesCombination4).build();
		property3 = new Property.Builder("property-3").withDefaultValue("DefaultValue3").withDescription("Property-Description3").withScopedValues(scopesCombination3).build();
		property4 = new Property.Builder("property-4").withDefaultValue("DefaultValue4").withDescription("Property-Description4").withScopedValues(scopesCombination4).build();
		property5 = new Property.Builder("property-5").withDefaultValue("DefaultValue5").withDescription("Property-Description5").withScopedValues(scopesCombination5).build();
		property6 = new Property.Builder("property-6").withDefaultValue("DefaultValue6").withDescription("Property-Description6").withScopedValues(scopesCombination6).build();
		property7 = new Property.Builder("property-7").withDefaultValue("DefaultValue7").withDescription("Property-Description7").withScopedValues(scopesCombination6).build();
		property8 = new Property.Builder("property-8").withDefaultValue("DefaultValue8").withDescription("Property-Description8").withScopedValues(scopesCombination6).build();
	}

	public Property getProperty1() {
		return property1;
	}

	public Property getProperty2WithConflict() {
		return property2WithConflict;
	}

	public Property getProperty2() {
		return property2;
	}

	public Property getProperty3() {
		return property3;
	}

	public Property getProperty4() {
		return property4;
	}

	public Property getProperty5() {
		return property5;
	}

	public Property getProperty6() {
		return property6;
	}

	public Property getProperty7() {
		return property7;
	}

	public Property getProperty8() {
		return property8;
	}

}
