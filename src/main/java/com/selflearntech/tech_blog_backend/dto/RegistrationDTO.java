package com.selflearntech.tech_blog_backend.dto;

import com.selflearntech.tech_blog_backend.exception.ErrorMessages;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationDTO {

    @NotBlank
    String firstName;

    @NotBlank
    String lastName;

    @NotBlank
    @Email
    private String email;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%]).{8,15}$",
            message = ErrorMessages.PASSWORD_CONSTRAINT)
    private String password;

    @NotNull
    @Size(min = 8, max = 15)
    private String verifyPassword;

}
