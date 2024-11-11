package com.selflearntech.tech_blog_backend.mapper;

import com.selflearntech.tech_blog_backend.dto.UserDTO;
import com.selflearntech.tech_blog_backend.model.RoleType;
import com.selflearntech.tech_blog_backend.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(getRoles(authorities))")
    UserDTO toUserDTO(User user, Set<GrantedAuthority> authorities, String accessToken);

    default Set<String> getRoles(Set<GrantedAuthority> authorities) {
        return authorities.stream().map(role -> RoleType.valueOf(role.getAuthority()).name())
                .collect(Collectors.toSet());
    }


}
