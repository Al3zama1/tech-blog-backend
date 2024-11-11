package com.selflearntech.tech_blog_backend.exception;

public class RoleAssignmentException extends RuntimeException{
    public RoleAssignmentException(String message, String role) {
        super(String.format("%s: %s", message, role));
    }
}
