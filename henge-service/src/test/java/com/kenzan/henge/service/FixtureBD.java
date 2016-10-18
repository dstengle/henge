package com.kenzan.henge.service;

import com.google.common.collect.Sets;
import com.kenzan.henge.domain.model.Property;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.PropertyGroupReference;
import com.kenzan.henge.domain.model.PropertyScopedValue;
import com.kenzan.henge.domain.model.Scope;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.model.type.PropertyGroupType;

public abstract class FixtureBD {

    protected static final String VERSION_1 = "1.0.0";
    protected static final String VERSION_1_0_1 = "1.0.1";

    protected final Scope env_dev = Scope.builder("env", "dev").build();
    protected final Scope env_prod = Scope.builder("env", "prod").build();
    protected final Scope stack_sbnet = Scope.builder("stack", "sbnet").build();
    protected final Scope stack_cnet = Scope.builder("stack", "cnet").build();
    protected final Scope region_west = Scope.builder("region", "dev-us-west-2").build();

    protected final PropertyScopedValue scopesCombination1 = PropertyScopedValue.builder(Sets.newHashSet(env_dev), "end-dev-value").build();
    protected final PropertyScopedValue scopesCombination2 = PropertyScopedValue.builder(Sets.newHashSet(env_dev, stack_sbnet), "end-dev-stack-cbnet-value").build();
    protected final PropertyScopedValue scopesCombination3 = PropertyScopedValue.builder(Sets.newHashSet(env_dev, stack_cnet), "end-dev-stack-cnet-value").build();
    protected final PropertyScopedValue scopesCombination4 = PropertyScopedValue.builder(Sets.newHashSet(env_dev, stack_cnet, region_west), "end-dev-stack-cnet-region-west-value").build();
    protected final PropertyScopedValue scopesCombination5 = PropertyScopedValue.builder(Sets.newHashSet(env_prod, stack_cnet, region_west), "end-prod-stack-cnet-region-west-value").build();
    protected final PropertyScopedValue scopesCombination6 = PropertyScopedValue.builder(Sets.newHashSet(env_prod), "end-prod-value").build();

    protected final Property property1 = new Property.Builder("property-1").withDefaultValue("DefaultValue1").withDescription("Property-Description1").withScopedValues(scopesCombination1).build();
    protected final Property property2 = new Property.Builder("property-2").withDefaultValue("DefaultValue2").withDescription("Property-Description2").withScopedValues(scopesCombination2, scopesCombination4).build();
    protected final Property property2conflictedName = new Property.Builder("property-2").withDefaultValue("ConflictedProperty").withDescription("Conflicted Property with Lib1").withScopedValues(scopesCombination1).build();
    protected final Property property3 = new Property.Builder("property-3").withDefaultValue("DefaultValue3").withDescription("Property-Description3").withScopedValues(scopesCombination3).build();
    protected final Property property4 = new Property.Builder("property-4").withDefaultValue("DefaultValue4").withDescription("Property-Description4").withScopedValues(scopesCombination4).build();
    protected final Property property5 = new Property.Builder("property-5").withDefaultValue("DefaultValue5").withDescription("Property-Description5").withScopedValues(scopesCombination5).build();
    protected final Property property6 = new Property.Builder("property-6").withDefaultValue("DefaultValue6").withDescription("Property-Description6").withScopedValues(scopesCombination6).build();
    protected final Property property7 = new Property.Builder("property-7").withDefaultValue("DefaultValue7").withDescription("Property-Description7").withScopedValues(scopesCombination6).build();
    protected final Property property8 = new Property.Builder("property-8").withDefaultValue("DefaultValue8").withDescription("Property-Description8").withScopedValues(scopesCombination6).build();

    protected final PropertyGroup app = PropertyGroup.builder("PropertyGroup-App", VERSION_1).withDescription("App description").withType(PropertyGroupType.APP.toString()).withIsActive(true).withProperties(property1,property2conflictedName).build();
    protected final PropertyGroup lib1 = PropertyGroup.builder("PropertyGroup-Lib-1", VERSION_1).withDescription("PropertyGroup-Lib-1 description").withType(PropertyGroupType.LIB.toString()).withIsActive(true).withProperties(property2,property3).build();
    protected final PropertyGroup lib2 = PropertyGroup.builder("PropertyGroup-Lib-2", VERSION_1).withDescription("PropertyGroup-Lib-2 description").withType(PropertyGroupType.LIB.toString()).withIsActive(true).withProperties(property4,property5).build();
    protected final PropertyGroup lib3 = PropertyGroup.builder("PropertyGroup-Lib-3", VERSION_1).withDescription("PropertyGroup-Lib-3 description").withType(PropertyGroupType.LIB.toString()).withIsActive(true).withProperties(property6,property7,property8).build();
    protected final PropertyGroup lib4 = PropertyGroup.builder("PropertyGroup-Lib-4", VERSION_1_0_1).withDescription("PropertyGroup-Lib-4 description").withType(PropertyGroupType.LIB.toString()).withIsActive(true).withProperties(property6,property7,property8).build();
    protected final PropertyGroup lib4_new_version = PropertyGroup.builder("PropertyGroup-Lib-4", VERSION_1_0_1).withDescription("PropertyGroup-Lib-4 new description").withType(PropertyGroupType.LIB.toString()).withIsActive(true).withProperties(property6,property7,property8).build();
    protected final VersionSet versionSet = VersionSet.builder("VersionSet1", VERSION_1).withPropertyGroupReferences(PropertyGroupReference.builder("PropertyGroup-App",VERSION_1).build(),PropertyGroupReference.builder("PropertyGroup-Lib-1",VERSION_1).build(),PropertyGroupReference.builder("PropertyGroup-Lib-2",VERSION_1).build(),PropertyGroupReference.builder("PropertyGroup-Lib-3",VERSION_1).build()).build();

}
