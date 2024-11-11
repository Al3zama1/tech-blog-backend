package com.selflearntech.tech_blog_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selflearntech.tech_blog_backend.config.SecurityConfig;
import com.selflearntech.tech_blog_backend.dto.RegistrationDTO;
import com.selflearntech.tech_blog_backend.exception.BadRequestException;
import com.selflearntech.tech_blog_backend.exception.ErrorMessages;
import com.selflearntech.tech_blog_backend.exception.UserExistsException;
import com.selflearntech.tech_blog_backend.service.impl.AuthenticationService;
import com.selflearntech.tech_blog_backend.test_data.RegistrationDTOMother;
import com.selflearntech.tech_blog_backend.test_utils.ResponseBodyMatchers;
import com.selflearntech.tech_blog_backend.utils.RSAKeyProperties;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@Import({SecurityConfig.class, RSAKeyProperties.class})
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;
    @MockBean
    private UserDetailsService userDetailsService;

    @Nested
    public class UserRegistration {

        @Test
        void registerUser_WithValidData_ShouldReturn201Status() throws Exception {
            // Given
            RegistrationDTO registrationDTO = RegistrationDTOMother.complete().build();

            // When
            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registrationDTO)))
                    .andExpect(status().isCreated());

            // Then
            then(authenticationService).should().registerUser(registrationDTO);
        }

        @Test
        void registerUser_WithInvalidEmailFormat_ShouldReturn400StatusWithViolationMessage() throws Exception {
            // Given
            RegistrationDTO registrationDTO = RegistrationDTOMother.complete().email("john.doe.com").build();

            // When
            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registrationDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(ResponseBodyMatchers.responseBody().containsError("email", "must be a well-formed email address"));

            // Then
            then(authenticationService).shouldHaveNoInteractions();
        }

        @Test
        void registerUser_WithExistingEmail_ShouldReturn409Status() throws Exception {
            // Given
            RegistrationDTO registrationDTO = RegistrationDTOMother.complete().build();

            doThrow(new UserExistsException(ErrorMessages.USER_EXISTS)).when(authenticationService).registerUser(registrationDTO);

            // When, Then
            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registrationDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(ErrorMessages.USER_EXISTS));
        }

        @Test
        void registerUser_WithNonMatchingPasswords_ShouldReturn400StatusWithViolationMessage() throws Exception {
            // Given
            RegistrationDTO registrationDTO = RegistrationDTOMother.complete()
                    .password("C11l08a#0522")
                    .verifyPassword("C11l08a#0523")
                    .build();

            doThrow(new BadRequestException(ErrorMessages.PASSWORDS_MUST_MATCH)).when(authenticationService).registerUser(registrationDTO);

            // When, Then
            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registrationDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ErrorMessages.PASSWORDS_MUST_MATCH));
        }
    }

}
