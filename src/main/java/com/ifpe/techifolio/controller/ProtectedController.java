package com.ifpe.techifolio.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProtectedController {

    @GetMapping("/protected")
    public String protectedEndpoint(Authentication authentication) {
        String name;
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            name = oAuth2User.getAttribute("name");
        } else if (authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            name = user.getUsername();
        } else {
            name = "Unknown";
        }
        return "Authenticated user: " + name;
    }
}