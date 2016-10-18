/**
 * Copyright (C) ${project.inceptionYear} Kenzan - Kyle S. Bober
 * (kbober@kenzan.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kenzan.henge.domain.validator;

import static com.google.common.base.Preconditions.checkNotNull;

import com.kenzan.henge.domain.model.Group;
import com.kenzan.henge.domain.model.MappingGroup;
import com.kenzan.henge.domain.model.NamedVersionedModelReference;
import com.kenzan.henge.domain.model.PropertyGroup;
import com.kenzan.henge.domain.model.PropertyGroupReference;
import com.kenzan.henge.domain.model.VersionSet;
import com.kenzan.henge.domain.model.VersionSetReference;
import com.kenzan.henge.domain.utils.ScopeUtils;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Validates that a {@link NamedVersionedModelReference} points to an existing
 * object in the respository.
 */
public class GroupValidator implements ConstraintValidator<CheckGroup, Group> {

    private ModelReferenceExistsValidation validation;

    @Autowired
    public GroupValidator(ModelReferenceExistsValidation validation) {

        super();
        this.validation = validation;
    }

    @Override
    public void initialize(final CheckGroup validationAnnotation) {

        checkNotNull(validationAnnotation);
    }

    @Override
    public boolean isValid(final Group model, final ConstraintValidatorContext constraintContext) {

        constraintContext.disableDefaultConstraintViolation();
        boolean isValid = true;

        isValid = validateVersionSets(model, constraintContext, isValid);

        isValid = validateMappingGroups(model, constraintContext, isValid);

        return isValid;
    }

    /**
     * @param model
     * @param constraintContext
     * @param isValid
     * @return
     */
    private boolean validateVersionSets(final Group model, final ConstraintValidatorContext constraintContext, 
                                        boolean isValid) {

        Set<PropertyGroupReference> failedPreReferences = validatePropertyGroupInnerReferences(model);

        isValid = validatePropertyGroupReferences(constraintContext, isValid, failedPreReferences);
        return isValid;
    }

    /**
     * @param model
     * @param constraintContext
     * @param isValid
     * @return
     */
    private boolean validateMappingGroups(final Group model, final ConstraintValidatorContext constraintContext,
                                          boolean isValid) {

        Set<VersionSetReference> failedVSPreReferences = new HashSet<>();
        for (MappingGroup mg : model.getMappingList()) {

            boolean found = false;
            for (VersionSet vs : model.getVersionSetList()) {
                if (vs.getName().equals(mg.getVsReference().getName())
                    && vs.getVersion().equals(mg.getVsReference().getVersion())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                failedVSPreReferences.add(mg.getVsReference());
            }
            
        }

        // validate Mapping's VersionSetReferences
        for (VersionSetReference vsRef : failedVSPreReferences) {
            boolean validationResult = 
                validation.checkForExistence(VersionSet.class, vsRef.getName(), vsRef.getVersion(), constraintContext);
            isValid = !validationResult ? false : isValid;
        }

        return isValid;
    }

    /**
     * @param constraintContext
     * @param isValid
     * @param failedPGPreReferences
     * @return
     */
    private boolean validatePropertyGroupReferences(final ConstraintValidatorContext constraintContext,
                                                    boolean isValid, Set<PropertyGroupReference> failedPGPreReferences) {

        // validate VersionSet's PropertyGroupReferences
        for (PropertyGroupReference pgRef : failedPGPreReferences) {
            boolean validationResult =
                validation.checkForExistence(PropertyGroup.class, pgRef.getName(), pgRef.getVersion(), constraintContext);
            isValid = !validationResult ? false : isValid;
        }

        return isValid;
    }

    /**
     * Validates that the VersionSet
     * @param model
     * @return
     */
    private Set<PropertyGroupReference> validatePropertyGroupInnerReferences(final Group model) {

        // validates references to PropertyGroups inside
        Set<PropertyGroupReference> failedPreReferences = new HashSet<>();
        
        for (VersionSet vs : model.getVersionSetList()) {
            for (PropertyGroupReference ref : vs.getPropertyGroupReferences()) {
                boolean found = false;
                for (PropertyGroup pg : model.getPropertyGroupList()) {
                    if (pg.getName().equals(ref.getName()) && pg.getVersion().equals(ref.getVersion())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    failedPreReferences.add(ref);
                }
            }
        }
        return failedPreReferences;
    }

}
