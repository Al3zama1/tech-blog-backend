package com.selflearntech.tech_blog_backend.service.impl;

import com.selflearntech.tech_blog_backend.dto.RegistrationDTO;
import com.selflearntech.tech_blog_backend.exception.*;
import com.selflearntech.tech_blog_backend.mapper.UserMapper;
import com.selflearntech.tech_blog_backend.model.RoleType;
import com.selflearntech.tech_blog_backend.model.Token;
import com.selflearntech.tech_blog_backend.model.User;
import com.selflearntech.tech_blog_backend.repository.RoleRepository;
import com.selflearntech.tech_blog_backend.repository.UserRepository;
import com.selflearntech.tech_blog_backend.service.ITokenService;
import com.selflearntech.tech_blog_backend.test_data.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserMapper userMapper;

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
            User user = UserMother.complete().build();
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user, password);
            String refreshToken = "refreshToken";
            Jwt jwt = Jwt.withTokenValue(refreshToken)
                    .expiresAt(Instant.now())
                    .header("alg", "SH252")
                    .build();

            given(authenticationManager.authenticate(any(Authentication.class))).willReturn(authenticationToken);
            given(tokenService.validateJWT(refreshToken)).willReturn(jwt);
            given(tokenService.createRefreshToken(email)).willReturn(refreshToken);

            // When
            cut.authenticateUser(email, password);

            // Then
            ArgumentCaptor<User> userArgumentMatcher = ArgumentCaptor.forClass(User.class);
            then(userRepository).should().save(userArgumentMatcher.capture());

            assertThat(userArgumentMatcher.getValue().getToken()).isNotNull();
            assertThat(userArgumentMatcher.getValue().getToken().isValid()).isTrue();
            assertThat(userArgumentMatcher.getValue().getToken().getExpireTime()).isEqualTo(jwt.getExpiresAt());
        }

        @Test
        void authenticateUser_WithValidCredentialsButFailToValidateRecentlyCreatedJwt_ShouldThrowRuntimeException() {
            // Given
            String email = AuthenticationDTOMother.complete().build().getEmail();
            String password = AuthenticationDTOMother.complete().build().getPassword();
            User user = UserMother.complete().build();
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user, password);
            String refreshToken = "refreshToken";

            given(authenticationManager.authenticate(any(Authentication.class))).willReturn(authenticationToken);
            given(tokenService.validateJWT(refreshToken)).willThrow(new RuntimeException(ErrorMessages.INVALID_REFRESH_TOKEN));
            given(tokenService.createRefreshToken(email)).willReturn(refreshToken);

            // When
            assertThatThrownBy(() -> cut.authenticateUser(email, password))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage(ErrorMessages.INVALID_REFRESH_TOKEN);

            // Then
            then(userRepository).shouldHaveNoInteractions();
            then(userMapper).shouldHaveNoInteractions();
        }
    }

    @Nested
    class RefreshAccessToken {

        @Test
        void refreshAccessToken_WithValidToken_ShouldReturnNewAccessToken() {
            // Given
            LocalDateTime defaultLocalDateTime = LocalDateTime.of(2024, 12, 13, 12, 15);
            Clock fixedClock = Clock.fixed(defaultLocalDateTime.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
            Instant tokenExpirationTime = fixedClock.instant();
            Instant currentTime = fixedClock.instant().minusSeconds(100);

            String refreshToken = "refreshToken";
            Token token = TokenMother.complete().refreshToken(refreshToken).expireTime(tokenExpirationTime).build();
            User user = UserMother.complete().token(token).build();
            Jwt jwt = Jwt.withTokenValue(refreshToken)
                    .expiresAt(tokenExpirationTime)
                    .subject(user.getEmail())
                    .header("alg", "SH252")
                    .build();

            given(tokenService.validateJWT(refreshToken)).willReturn(jwt);
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(clock.instant()).willReturn(currentTime);

            // When
            cut.refreshAccessToken(refreshToken);

            // Then
            then(tokenService).should().createAccessToken(user);
        }

        @Test
        void refreshAccessToken_WithTokenValidationException_ShouldThrowRefreshTokenException() {
            // Given
            String refreshToken = "refreshToken";

            given(tokenService.validateJWT(refreshToken)).willThrow(new RefreshTokenException(ErrorMessages.INVALID_REFRESH_TOKEN));

            // When
            assertThatThrownBy(() -> cut.refreshAccessToken(refreshToken))
                    .isInstanceOf(RefreshTokenException.class)
                    .hasMessage(ErrorMessages.INVALID_REFRESH_TOKEN);

            // Then
            then(userRepository).shouldHaveNoInteractions();
            then(tokenService).shouldHaveNoMoreInteractions();
        }

        @Test
        void refreshAccessToken_WithNonExistingSubject_ShouldThrowRefreshTokenException() {
            // Given
            LocalDateTime defaultLocalDateTime = LocalDateTime.of(2024, 12, 13, 12, 15);
            Clock fixedClock = Clock.fixed(defaultLocalDateTime.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
            Instant tokenExpirationTime = fixedClock.instant();

            String refreshToken = "refreshToken";
            Token token = TokenMother.complete().refreshToken(refreshToken).expireTime(tokenExpirationTime).build();
            User user = UserMother.complete().token(token).build();
            Jwt jwt = Jwt.withTokenValue(refreshToken)
                    .expiresAt(tokenExpirationTime)
                    .subject(user.getEmail())
                    .header("alg", "SH252")
                    .build();

            given(tokenService.validateJWT(refreshToken)).willReturn(jwt);
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> cut.refreshAccessToken(refreshToken))
                    .isInstanceOf(RefreshTokenException.class)
                    .hasMessage(ErrorMessages.INVALID_REFRESH_TOKEN);

            // Then
            then(tokenService).shouldHaveNoMoreInteractions();
        }

        @Test
        void refreshAccessToken_WithInvalidatedToken_ShouldThrowRefreshTokenException() {
            // Given
            LocalDateTime defaultLocalDateTime = LocalDateTime.of(2024, 12, 13, 12, 15);
            Clock fixedClock = Clock.fixed(defaultLocalDateTime.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
            Instant tokenExpirationTime = fixedClock.instant();
            Instant currentTime = fixedClock.instant().minusSeconds(100);

            String refreshToken = "refreshToken";
            Token token = TokenMother.complete().refreshToken(refreshToken).isValid(false).expireTime(tokenExpirationTime).build();
            User user = UserMother.complete().token(token).build();
            Jwt jwt = Jwt.withTokenValue(refreshToken)
                    .expiresAt(tokenExpirationTime)
                    .subject(user.getEmail())
                    .header("alg", "SH252")
                    .build();

            given(tokenService.validateJWT(refreshToken)).willReturn(jwt);
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(clock.instant()).willReturn(currentTime);

            // When
            assertThatThrownBy(() -> cut.refreshAccessToken(refreshToken))
                    .isInstanceOf(RefreshTokenException.class)
                    .hasMessage(ErrorMessages.INVALID_REFRESH_TOKEN + ": " + ErrorMessages.INVALIDATED_REFRESH_TOKEN);

            // Then
            then(tokenService).shouldHaveNoMoreInteractions();
        }

        @Test
        void refreshAccessToken_WithCookieTokenAndDatabaseTokenNotMatching_ShouldThrowRefreshTokenException() {
            // Given
            LocalDateTime defaultLocalDateTime = LocalDateTime.of(2024, 12, 13, 12, 15);
            Clock fixedClock = Clock.fixed(defaultLocalDateTime.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
            Instant tokenExpirationTime = fixedClock.instant();
            Instant currentTime = fixedClock.instant().minusSeconds(100);

            String refreshToken = "refreshToken";
            Token token = TokenMother.complete().refreshToken("different Token").expireTime(tokenExpirationTime).build();
            User user = UserMother.complete().token(token).build();
            Jwt jwt = Jwt.withTokenValue(refreshToken)
                    .expiresAt(tokenExpirationTime)
                    .subject(user.getEmail())
                    .header("alg", "SH252")
                    .build();

            given(tokenService.validateJWT(refreshToken)).willReturn(jwt);
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(clock.instant()).willReturn(currentTime);

            // When
            assertThatThrownBy(() -> cut.refreshAccessToken(refreshToken))
                    .isInstanceOf(RefreshTokenException.class)
                    .hasMessage(ErrorMessages.INVALID_REFRESH_TOKEN + ": " + ErrorMessages.COOKIE_REFRESH_TOKEN_AND_DB_TOKEN_UNMATCH);

            // Then
            then(tokenService).shouldHaveNoMoreInteractions();
        }

        @Test
        void refreshAccessToken_WithExpiredToken_ShouldThrowRefreshTokenException() {
            // Given
            LocalDateTime defaultLocalDateTime = LocalDateTime.of(2024, 12, 13, 12, 15);
            Clock fixedClock = Clock.fixed(defaultLocalDateTime.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
            Instant tokenExpirationTime = fixedClock.instant();
            Instant currentTime = fixedClock.instant().plusSeconds(100);

            String refreshToken = "refreshToken";
            Token token = TokenMother.complete().refreshToken(refreshToken).expireTime(tokenExpirationTime).build();
            User user = UserMother.complete().token(token).build();
            Jwt jwt = Jwt.withTokenValue(refreshToken)
                    .expiresAt(tokenExpirationTime)
                    .subject(user.getEmail())
                    .header("alg", "SH252")
                    .build();

            given(tokenService.validateJWT(refreshToken)).willReturn(jwt);
            given(userRepository.findByEmail(user.getEmail())).willReturn(Optional.of(user));
            given(clock.instant()).willReturn(currentTime);

            // When
            assertThatThrownBy(() -> cut.refreshAccessToken(refreshToken))
                    .isInstanceOf(RefreshTokenException.class)
                    .hasMessage(ErrorMessages.INVALID_REFRESH_TOKEN + ": " + ErrorMessages.EXPIRED_REFRESH_TOKEN);

            // Then
            then(tokenService).shouldHaveNoMoreInteractions();
        }
    }
}
