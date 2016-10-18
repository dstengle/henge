package com.kenzan.henge.repository.impl.flatfile;

import com.kenzan.henge.domain.validator.ModelReferenceExistsValidation;
import com.kenzan.henge.repository.impl.flatfile.storage.FileStorageService;

import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author wmatsushita
 */
@Profile({ "flatfile_local", "flatfile_s3" })
@Component
public class FlatFileModelReferenceExistsValidation implements ModelReferenceExistsValidation {

    private FileStorageService fileStorageService;

    private FileNamingService fileNamingService;

    
    @Autowired
    public FlatFileModelReferenceExistsValidation(FileStorageService fileStorageService, FileNamingService fileNamingService) {
        this.fileStorageService = fileStorageService;
        this.fileNamingService = fileNamingService;
    }

    @Override
    public boolean checkForExistence(final Class<?> modelType, final String name, final String version,
                                     final ConstraintValidatorContext constraintContext) {

        constraintContext.disableDefaultConstraintViolation();

        final boolean isValid = fileStorageService.exists(
            fileNamingService.getPath(modelType),
            fileNamingService.getCompleteFileName(name, version));
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
