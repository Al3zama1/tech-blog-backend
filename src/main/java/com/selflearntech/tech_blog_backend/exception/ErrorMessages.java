package com.selflearntech.tech_blog_backend.exception;

public class ErrorMessages {
    public static final String UNKNOWN_ERROR = "Unknown error occurred";
    public static final String VALIDATION_ERROR = "Validation error. Check 'errors' field for details";
    public static final String PASSWORD_CONSTRAINT = "Password must be 8 to 15 characters long, include uppercase and lowercase letters, a number, and a special character (! @ # $ %)";
    public static final String PASSWORDS_MUST_MATCH = "Registration passwords must match";
    public static final String USER_EXISTS = "Email is taken";
    public static final String ROLE_ASSIGNMENT_FAILURE = "Role assignment failure";
    public static final String INVALID_CREDENTIALS = "Check your credentials and try again";
    public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    public static final String INVALIDATED_REFRESH_TOKEN = "Invalidated refresh token";
    public static final String COOKIE_REFRESH_TOKEN_AND_DB_TOKEN_UNMATCH = "Cookie and db refresh tokens do not match";
    public static final String EXPIRED_REFRESH_TOKEN = "Refresh token expired";
    public static final String FAIL_TOKEN_DECODE  = "Failed to decode token";
}
