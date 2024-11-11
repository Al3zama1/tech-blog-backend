package com.selflearntech.tech_blog_backend.mapper;

import com.selflearntech.tech_blog_backend.dto.UserDTO;
import com.selflearntech.tech_blog_backend.model.User;
import com.selflearntech.tech_blog_backend.test_data.RoleMother;
import com.selflearntech.tech_blog_backend.test_data.UserMother;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private static UserMapper userMapper;

    @BeforeAll
    static void beforeAll() {
        userMapper = Mappers.getMapper(UserMapper.class);
    }

    @Test
    void toUserDTO_ShouldReturnUserDTO() {
        // Given
        User user = UserMother.complete()
                .authorities(Set.of(RoleMother.ADMIN().build()))
                .build();
        String accessToken = "accessToken";

        // When
        UserDTO userDTO = userMapper.toUserDTO(user, accessToken);

        // Then
        assertThat(userDTO.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(userDTO.getLastName()).isEqualTo(user.getLastName());
        assertThat(userDTO.getEmail()).isEqualTo(user.getEmail());
        assertThat(userDTO.getProfileImg()).isEqualTo(user.getProfileImg());
        assertThat(userDTO.getAccessToken()).isEqualTo(accessToken);
        assertThat(userDTO.getRoles()).isEqualTo(Set.of("ADMIN"));
    }

}
