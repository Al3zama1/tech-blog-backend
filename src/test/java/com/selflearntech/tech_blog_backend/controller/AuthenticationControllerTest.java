package com.selflearntech.tech_blog_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selflearntech.tech_blog_backend.config.SecurityConfig;
import com.selflearntech.tech_blog_backend.dto.AuthenticationDTO;
import com.selflearntech.tech_blog_backend.dto.RegistrationDTO;
import com.selflearntech.tech_blog_backend.dto.UserDTO;
import com.selflearntech.tech_blog_backend.dto.UserWithRefreshAndAccessTokenDTO;
import com.selflearntech.tech_blog_backend.exception.*;
import com.selflearntech.tech_blog_backend.mapper.UserMapper;
import com.selflearntech.tech_blog_backend.model.RoleType;
import com.selflearntech.tech_blog_backend.service.impl.AuthenticationService;
import com.selflearntech.tech_blog_backend.test_data.AuthenticationDTOMother;
import com.selflearntech.tech_blog_backend.test_data.RegistrationDTOMother;
import com.selflearntech.tech_blog_backend.utils.RSAKeyProperties;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static com.selflearntech.tech_blog_backend.test_utils.ResponseBodyMatchers.responseBody;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @MockBean
    private UserMapper userMapper;


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
        void registerUser_WithValidDataButFailToAssignUserRole_ShouldReturn500Status() throws Exception {
            // Given
            RegistrationDTO registrationDTO = RegistrationDTOMother.complete().build();

            doThrow(new RoleAssignmentException(ErrorMessages.ROLE_ASSIGNMENT_FAILURE, RoleType.USER.name()))
                    .when(authenticationService).registerUser(registrationDTO);

            // When
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDTO)))
                    .andExpect(status().isInternalServerError());

            // Then
            then(authenticationService).should().registerUser(registrationDTO);
        }

        @Test
        void registerUser_WithInvalidEmailFormat_ShouldReturn400StatusWithValidationError() throws Exception {
            // Given
            RegistrationDTO registrationDTO = RegistrationDTOMother.complete().email("john.doe.com").build();

            // When
            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registrationDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(responseBody().containsError("email", "must be a well-formed email address"));

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
        void registerUser_WithNonMatchingPasswords_ShouldReturn400StatusWithValidationError() throws Exception {
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

    @Nested
    public class UserAuthentication {
        @Test
        void authenticateUser_WithValidData_ShouldReturn200StatusWithUserDTO() throws Exception {
            // Given
            AuthenticationDTO authenticationDTO = AuthenticationDTOMother.complete().build();
            UserWithRefreshAndAccessTokenDTO userWithRefreshAndAccessTokenDTO = UserWithRefreshAndAccessTokenDTO.builder()
                    .refreshToken("refreshToken")
                    .build();

            given(authenticationService.authenticateUser(authenticationDTO.getEmail(), authenticationDTO.getPassword()))
                    .willReturn(userWithRefreshAndAccessTokenDTO);

            // When
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authenticationDTO)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().value("refresh-token", userWithRefreshAndAccessTokenDTO.getRefreshToken()))
                    .andExpect(cookie().httpOnly("refresh-token", true))
                    .andExpect(cookie().domain("refresh-token", "localhost"))
                    .andExpect(cookie().path("refresh-token", "/auth/refresh"));

            // Then
            then(authenticationService).should().authenticateUser(authenticationDTO.getEmail(), authenticationDTO.getPassword());
            then(userMapper).should().toUserDTO(userWithRefreshAndAccessTokenDTO);
        }

        @Test
        void authenticateUser_WithIncorrectEmail_ShouldReturn401Status() throws Exception {
            // Given
            AuthenticationDTO authenticationDTO = AuthenticationDTOMother.complete().build();

            given(authenticationService.authenticateUser(authenticationDTO.getEmail(), authenticationDTO.getPassword()))
                    .willThrow(new UsernameNotFoundException(ErrorMessages.INVALID_CREDENTIALS));

            // When, Then
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authenticationDTO)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(ErrorMessages.INVALID_CREDENTIALS));
        }

        @Test
        void authenticateUser_WithIncorrectPassword_ShouldReturn401Status() throws Exception {
            // Given
            AuthenticationDTO authenticationDTO = AuthenticationDTOMother.complete().build();

            given(authenticationService.authenticateUser(authenticationDTO.getEmail(), authenticationDTO.getPassword()))
                    .willThrow(new BadCredentialsException(ErrorMessages.INVALID_CREDENTIALS));


            // When, Then
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authenticationDTO)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(ErrorMessages.INVALID_CREDENTIALS));
        }

        @Test
        void authenticateUser_WithInvalidEmailFormat_ShouldReturn400StatusWithValidationError() throws Exception {
            // Given
            AuthenticationDTO authenticationDTO = AuthenticationDTOMother.complete()
                    .email("john.com")
                    .build();

            // When
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authenticationDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(responseBody().containsError("email", "must be a well-formed email address"));

            // Then
            then(authenticationService).shouldHaveNoInteractions();
        }
    }

    @Nested
    public class RefreshToken {
        @Test
        void refreshAccessToken_WithValidCookie_ShouldReturnNewAccessToken() throws Exception {
            // Given
            String refreshToken = "refreshToken";
            UserDTO userDTO = new UserDTO("John", "Doe", "john.doe@gmail.com", "", Set.of("ADMIN", "USER"), "accessToken");

            MockCookie refreshTokenCookie = new MockCookie("refresh-token", refreshToken);

            given(authenticationService.refreshAccessToken(refreshToken)).willReturn(userDTO);;

            // When
            mockMvc.perform(post("/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .cookie(refreshTokenCookie))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(responseBody().containsObjectAsJson(userDTO, UserDTO.class));

            // Then
            then(authenticationService).should().refreshAccessToken(refreshToken);
        }

        @Test
        void refreshAccessToken_WithMissingRefreshTokenCookie_ShouldReturn400Status() throws Exception {
            // Given


            // When
            mockMvc.perform(post("/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            // Then
            then(authenticationService).shouldHaveNoInteractions();
        }

        @Test
        void refreshAccessToken_WithInvalidRefreshToken_ShouldReturn401Status() throws Exception {
            // Given
            String refreshToken = "refreshToken";
            MockCookie refreshTokenCookie = new MockCookie("refresh-token", refreshToken);

            given(authenticationService.refreshAccessToken(refreshToken)).willThrow(new RefreshTokenException(ErrorMessages.INVALID_REFRESH_TOKEN + ": " + ErrorMessages.FAIL_TOKEN_DECODE));

            // When
            mockMvc.perform(post("/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .cookie(refreshTokenCookie))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(header().string("Set-Cookie", "refresh-token="))
                    .andExpect(jsonPath("$.message").value(ErrorMessages.INVALID_REFRESH_TOKEN));

            // Then
            then(authenticationService).should().refreshAccessToken(refreshToken);
        }
    }

}
