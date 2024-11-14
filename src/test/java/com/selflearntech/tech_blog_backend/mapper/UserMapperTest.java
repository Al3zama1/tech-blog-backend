package com.selflearntech.tech_blog_backend.mapper;

import com.selflearntech.tech_blog_backend.dto.UserDTO;
import com.selflearntech.tech_blog_backend.dto.UserWithRefreshAndAccessTokenDTO;
import com.selflearntech.tech_blog_backend.model.User;
import com.selflearntech.tech_blog_backend.test_data.RoleMother;
import com.selflearntech.tech_blog_backend.test_data.TokenMother;
import com.selflearntech.tech_blog_backend.test_data.UserMother;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private static UserMapper cut;

    @BeforeAll
    static void beforeAll() {
        cut = Mappers.getMapper(UserMapper.class);
    }

    @Test
    void toUserWithRefreshAndAccessTokenDTO_FromUser() {
        // Given
        User user = UserMother.complete()
                .authorities(Set.of(RoleMother.ADMIN().build()))
                .token(TokenMother.complete().build())
                .build();
        String accessToken = "accessToken";

        // When
        UserWithRefreshAndAccessTokenDTO userDTO = cut.toUserWithAccessAndRefreshTokenDTO(user, accessToken);

        // Then
        assertThat(userDTO.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(userDTO.getLastName()).isEqualTo(user.getLastName());
        assertThat(userDTO.getEmail()).isEqualTo(user.getEmail());
        assertThat(userDTO.getProfileImg()).isEqualTo(user.getProfileImg());
        assertThat(userDTO.getAccessToken()).isEqualTo(accessToken);
        assertThat(userDTO.getRoles()).isEqualTo(Set.of("ADMIN"));
        assertThat(userDTO.getRefreshToken()).isEqualTo(user.getToken().getRefreshToken());
    }

    @Test
    void toUserDTO_FromUserWithRefreshAndAccessTokenDTO() {
        // Given
        UserWithRefreshAndAccessTokenDTO userWithRefreshAndAccessTokenDTO = UserWithRefreshAndAccessTokenDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@gmail.com")
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .profileImg("")
                .roles(Set.of("ADMIN"))
                .build();

        // When
        UserDTO userDTO = cut.toUserDTO(userWithRefreshAndAccessTokenDTO);

        // Then
        assertThat(userDTO.getFirstName()).isEqualTo(userWithRefreshAndAccessTokenDTO.getFirstName());
        assertThat(userDTO.getLastName()).isEqualTo(userWithRefreshAndAccessTokenDTO.getLastName());
        assertThat(userDTO.getEmail()).isEqualTo(userWithRefreshAndAccessTokenDTO.getEmail());
        assertThat(userDTO.getProfileImg()).isEqualTo(userWithRefreshAndAccessTokenDTO.getProfileImg());
        assertThat(userDTO.getRoles()).isEqualTo(userWithRefreshAndAccessTokenDTO.getRoles());
        assertThat(userDTO.getAccessToken()).isEqualTo(userWithRefreshAndAccessTokenDTO.getAccessToken());
    }

    @Test
    void toUserDTO_FromUserAndRefreshToken() {
        // Given
        User user = UserMother.complete()
                .authorities(Set.of(RoleMother.ADMIN().build(), RoleMother.USER().build()))
                .build();
        String accessToken = "accessToken";

        // When
        UserDTO userDTO = cut.toUserDTOFromUserAndAccessToken(user, accessToken);

        // Then
        assertThat(userDTO.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(userDTO.getLastName()).isEqualTo(user.getLastName());
        assertThat(userDTO.getEmail()).isEqualTo(user.getEmail());
        assertThat(userDTO.getProfileImg()).isEqualTo(user.getProfileImg());
        assertThat(userDTO.getRoles()).isEqualTo(Set.of("USER", "ADMIN"));
        assertThat(userDTO.getAccessToken()).isEqualTo(accessToken);
    }

}
