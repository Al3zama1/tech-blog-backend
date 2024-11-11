package com.selflearntech.tech_blog_backend.service.impl;

import com.selflearntech.tech_blog_backend.service.IUserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.selflearntech.tech_blog_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService, UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("In the user details service");

        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User is not valid"));
    }

}
