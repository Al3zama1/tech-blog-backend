package com.selflearntech.tech_blog_backend.validation;

import com.selflearntech.tech_blog_backend.validation.annotation.PasswordMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {
    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        try {
            String password = (String) getFieldValue(object, "password");
            String verifyPassword = (String) getFieldValue(object, "verifyPassword");
            return password != null && password.equals(verifyPassword);
        } catch (Exception e) {
           return false;
        }
    }

    private Object getFieldValue(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);

        return field.get(object);
    }
}
