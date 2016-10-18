package com.kenzan.henge.repository.impl.cassandra;

import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.kenzan.henge.domain.validator.ModelReferenceExistsValidation;

@Profile("cassandra")
@Component
public class CassandraModelReferenceExistsValidation implements ModelReferenceExistsValidation{

	private CassandraRepositoryFactory factory;
	
    @Autowired
    public CassandraModelReferenceExistsValidation(CassandraRepositoryFactory factory) {
        this.factory = factory;
    }
	
	@Override
	public boolean checkForExistence(Class<?> modelType, String name, String version,
			ConstraintValidatorContext constraintContext) {
		constraintContext.disableDefaultConstraintViolation();
		final boolean isValid = factory.get(modelType).exists(name, version);
		if (!isValid) {
            String message = new StringBuilder("The model reference for model type: ").append(modelType)
                .append(", name: ").append(name).append(" and version: ").append(version)
                .append(" points to a non existing object.").toString();
            ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder =
                constraintContext.buildConstraintViolationWithTemplate(message);
            violationBuilder.addConstraintViolation();
        }
		return isValid;
	}

}
