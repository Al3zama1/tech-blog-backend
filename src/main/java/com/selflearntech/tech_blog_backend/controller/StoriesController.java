package com.selflearntech.tech_blog_backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stories")
@RequiredArgsConstructor
@Slf4j
public class StoriesController {

    @GetMapping("/drafts")
    public List<String> getUserDraftStories(Authentication authentication) {
        log.info("getUserDraftStories");
        return List.of("draft one", "draft two", "draft three");
    }

    @GetMapping("/published")
    public void getUserPublishedStories(Authentication authentication) {

    }

}
