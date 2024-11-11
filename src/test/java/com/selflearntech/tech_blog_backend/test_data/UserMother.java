package com.selflearntech.tech_blog_backend.test_data;

import com.selflearntech.tech_blog_backend.model.User;

public class UserMother {

    public static User.UserBuilder complete() {
        return User.builder()
                .userId(1)
                .profileImg("")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@gmail.com");
    }
}
