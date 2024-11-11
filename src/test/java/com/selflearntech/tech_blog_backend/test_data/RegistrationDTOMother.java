package com.selflearntech.tech_blog_backend.test_data;

import com.selflearntech.tech_blog_backend.dto.RegistrationDTO;

public class RegistrationDTOMother {

    public static RegistrationDTO.RegistrationDTOBuilder complete() {
        return RegistrationDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@gmail.com")
                .password("C11l08a#05")
                .verifyPassword("C11l08a#05");
    }
}
