package com.selflearntech.tech_blog_backend.mapper;

import com.selflearntech.tech_blog_backend.dto.UserDTO;
import com.selflearntech.tech_blog_backend.model.Role;
import com.selflearntech.tech_blog_backend.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(getRoles(user))")
    UserDTO toUserDTO(User user, String accessToken);

    default Set<String> getRoles(User user) {
        return user.getAuthorities().stream().map(Role::getAuthority).collect(Collectors.toSet());
    }


}
