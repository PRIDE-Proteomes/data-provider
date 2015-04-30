package uk.ac.ebi.pride.proteomes.pipeline.validator;

import org.apache.commons.lang.StringUtils;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * User: ntoro
 * Date: 02/10/2013
 * Time: 16:25
 */

/**
 * The BeanValidationValidator allows to use BeanValidation (JSR 303: Bean Validation standard) in which the rules for
 * the validation are contained in the object. With this way of validate the same rules are used in all the different
 * levels in the application. The rules are defined by means of annotations in the db-core
 */
public class BeanValidationValidator<T> implements Validator<T> {

    private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    private javax.validation.Validator validator = factory.getValidator();

    public void validate(T value) throws ValidationException {
        Set<ConstraintViolation<T>> violations = validator.validate(value);
        if(!violations.isEmpty()) {
            throw new ValidationException("Validation failed for " + value + ": " + violationsToString(violations));
        }
    }

    private String violationsToString(Set<ConstraintViolation<T>> violations) {
        String delimiter = ", ";
        StringBuilder builder = new StringBuilder();
        for(ConstraintViolation<T> violation : violations) {
            builder.append(violation.getPropertyPath())
                    .append(" ")
                    .append(violation.getMessage())
                    .append(delimiter);
        }
        return StringUtils.removeEnd(builder.toString(), delimiter);
    }

}

