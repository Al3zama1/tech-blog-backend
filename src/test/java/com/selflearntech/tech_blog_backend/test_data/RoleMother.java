package com.selflearntech.tech_blog_backend.test_data;

import com.selflearntech.tech_blog_backend.model.Role;
import com.selflearntech.tech_blog_backend.model.RoleType;

public class RoleMother {

    public static Role.RoleBuilder USER() {
        return Role.builder()
                .roleId(1)
                .authority(RoleType.USER);
    }

    public static Role.RoleBuilder ADMIN() {
        return Role.builder()
                .roleId(2)
                .authority(RoleType.ADMIN);
    }
}
