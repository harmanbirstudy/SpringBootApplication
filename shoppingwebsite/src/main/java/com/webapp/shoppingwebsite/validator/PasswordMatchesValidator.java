package com.webapp.shoppingwebsite.validator;

import com.webapp.shoppingwebsite.payload.SignUpRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;


public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, SignUpRequest> {

    @Override
    public boolean isValid(final SignUpRequest user, final ConstraintValidatorContext context) {
        return Objects.equals(user.getPassword(), user.getMatchingPassword());
    }
}
