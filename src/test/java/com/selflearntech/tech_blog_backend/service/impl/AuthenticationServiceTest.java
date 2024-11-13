package com.selflearntech.tech_blog_backend.service.impl;

import com.selflearntech.tech_blog_backend.dto.RegistrationDTO;
import com.selflearntech.tech_blog_backend.exception.BadRequestException;
import com.selflearntech.tech_blog_backend.exception.ErrorMessages;
import com.selflearntech.tech_blog_backend.exception.RoleAssignmentException;
import com.selflearntech.tech_blog_backend.exception.UserExistsException;
import com.selflearntech.tech_blog_backend.model.RoleType;
import com.selflearntech.tech_blog_backend.model.User;
import com.selflearntech.tech_blog_backend.repository.RoleRepository;
import com.selflearntech.tech_blog_backend.repository.UserRepository;
import com.selflearntech.tech_blog_backend.service.ITokenService;
import com.selflearntech.tech_blog_backend.test_data.AuthenticationDTOMother;
import com.selflearntech.tech_blog_backend.test_data.RegistrationDTOMother;
import com.selflearntech.tech_blog_backend.test_data.RoleMother;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private Clock clock;
    @Mock
    private ITokenService tokenService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AuthenticationService cut;

    @Nested
    class UserRegistration {

        @Test
        void registerUser_WithValidData_ShouldCreateNewUser() {
            // Given
            RegistrationDTO registrationDTO = RegistrationDTOMother.complete().build();

            given(userRepository.existsUserByEmail(registrationDTO.getEmail())).willReturn(false);
            given(roleRepository.findByAuthority(RoleType.USER)).willReturn(Optional.of(RoleMother.USER().build()));
            given(passwordEncoder.encode(registrationDTO.getPassword())).willReturn("encoded-password");

            // When
            cut.registerUser(registrationDTO);

            // Then
            ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
            then(userRepository).should().save(userArgumentCaptor.capture());
        }

        @Test
        void registerUser_WithNonMatchingPasswords_ShouldThrowBadRequestException() {
            // Given
            RegistrationDTO registrationDTO = RegistrationDTO.builder().password("C11").build();

            // When
            assertThatThrownBy(() -> cut.registerUser(registrationDTO)).isInstanceOf(BadRequestException.class)
                    .hasMessage(ErrorMessages.PASSWORDS_MUST_MATCH);

            // Then
            then(userRepository).shouldHaveNoInteractions();
            then(roleRepository).shouldHaveNoInteractions();
            then(passwordEncoder).shouldHaveNoInteractions();
        }

        @Test
        void registerUser_WithEmailAlreadyTaken_ShouldThrowUserExistsException() {
            // Given
            RegistrationDTO registrationDTO = RegistrationDTOMother.complete().build();

            given(userRepository.existsUserByEmail(registrationDTO.getEmail())).willReturn(true);

            // When
            assertThatThrownBy(() -> cut.registerUser(registrationDTO)).isInstanceOf(UserExistsException.class)
                    .hasMessage(ErrorMessages.USER_EXISTS);

            // Then
            then(roleRepository).shouldHaveNoInteractions();
            then(passwordEncoder).shouldHaveNoInteractions();
        }

        @Test
        void registerUser_WithValidDataButFailToAssignUserRole_ShouldThrowRoleAssignmentException() {
            // Given
            RegistrationDTO registrationDTO = RegistrationDTOMother.complete().build();
            RoleType userRoleType = RoleType.USER;

            given(userRepository.existsUserByEmail(registrationDTO.getEmail())).willReturn(false);
            given(roleRepository.findByAuthority(userRoleType)).willReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> cut.registerUser(registrationDTO))
                    .isInstanceOf(RoleAssignmentException.class)
                    .hasMessage(ErrorMessages.ROLE_ASSIGNMENT_FAILURE + ": " + userRoleType.name());

            // Then
            then(passwordEncoder).shouldHaveNoInteractions();
            then(userRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    class UserAuthentication {

        @Test
        void authenticateUser_WithValidCredentials_ShouldReturnUserWithRefreshAndAccessTokenDTO() {
            // Given
            String email = AuthenticationDTOMother.complete().build().getEmail();
            String password = AuthenticationDTOMother.complete().build().getPassword();

            // When
            cut.authenticateUser(email, password);


        }
    }

}
