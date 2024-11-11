package com.selflearntech.tech_blog_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationDTO {

    @NotBlank
    @Email
    private String email;

    @Size(min = 8, max = 15)
    private String password;
}
