package com.diva.funky.api.rest.controller;

import java.security.Principal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.diva.funky.api.model.UserModel;
import com.diva.funky.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User Rest Controller")
public class UserController {

    private final UserService userService;

    @GetMapping("/_me")
    @PreAuthorize("isAuthenticated()")
    public UserModel getMyDetails(@RegisteredOAuth2AuthorizedClient("azure") OAuth2AuthorizedClient authorizedClient, Principal principal) {
        log.info("Client id-token: {}", ((OidcUser) ((OAuth2AuthenticationToken)principal).getPrincipal()).getIdToken().getTokenValue());
        log.info("Client access-token: {}", authorizedClient.getAccessToken().getTokenValue());
        log.info("Client refresh-token: {}", authorizedClient.getRefreshToken().getTokenValue());
        return userService.getUserByUsername(principal.getName());
    }
}
