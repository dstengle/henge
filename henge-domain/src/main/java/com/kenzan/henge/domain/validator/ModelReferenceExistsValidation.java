package com.kenzan.henge.domain.validator;

import com.kenzan.henge.domain.model.NamedVersionedModelReference;

import javax.validation.ConstraintValidatorContext;

/**
 * Assists the validation of {@link NamedVersionedModelReference}s by checking
 * for the existence of them in the repository. Implementations should be
 * created for each specific type of repository that exists.
 *
 * @author wmatsushita
 */
public interface ModelReferenceExistsValidation {

    public boolean checkForExistence(final Class<?> modelType, final String name, final String version, final ConstraintValidatorContext constraintContext);

}
