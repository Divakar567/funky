package com.diva.funky.config;

import static com.azure.spring.cloud.autoconfigure.aad.AadWebApplicationHttpSecurityConfigurer.aadWebApplication;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.DeferredSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.DefaultRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class AadOAuth2LoginSecurityConfiguration {

    private final OAuth2AuthorizedClientRepository authorizedClientRepository;

    private final OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    private final Duration expirySkew = Duration.ofSeconds(120);

    private final JwtDecoderFactory<ClientRegistration> jwtDecoderFactory = new OidcIdTokenDecoderFactory();

    private final OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> accessTokenResponseClient = new DefaultRefreshTokenTokenResponseClient();

    private static final String INVALID_ID_TOKEN_ERROR_CODE = "INVALID_ID_TOKEN";

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain defaultAadWebApplicationFilterChain(HttpSecurity http) throws Exception {
        SecurityContextRepository securityContextRepository = securityContextRepository();
        http.apply(aadWebApplication())
            .and()
                .addFilterBefore(securityContextRefreshFilter(securityContextRepository), SecurityContextHolderFilter.class)
                .securityContext()
                .securityContextRepository(securityContextRepository)
            .and()
                .authorizeHttpRequests()
                .requestMatchers("/login", "/actuator/health", "/swagger-ui/index.html")
                .permitAll()
            .anyRequest().authenticated();
        return http.build();
    }

    @Bean
    OncePerRequestFilter securityContextRefreshFilter(SecurityContextRepository securityContextRepository) {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                            @NonNull FilterChain filterChain) throws ServletException, IOException {
                DeferredSecurityContext deferredContext = securityContextRepository.loadDeferredContext(request);
                try {
                    SecurityContext securityContext = deferredContext.get();
                    if (Objects.nonNull(securityContext.getAuthentication())
                            && BooleanUtils.isTrue(securityContext.getAuthentication().isAuthenticated())) {
                        Authentication authentication = securityContext.getAuthentication();
                        OAuth2AuthorizedClient authorizedClient = authorizedClientRepository.loadAuthorizedClient(
                                ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId(), authentication, request);
                        if (Objects.nonNull(authorizedClient) && Objects.nonNull(authorizedClient.getRefreshToken())
                                && hasTokenExpired(((OidcUser)authentication.getPrincipal()).getIdToken())) {
                            log.info("OIDC token expired: {}", ((OidcUser)authentication.getPrincipal()).getIdToken());
                            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                                    .withClientRegistrationId(authorizedClient.getClientRegistration().getRegistrationId())
                                    .principal(authentication)
                                    .attribute(HttpServletRequest.class.getName(), request)
                                    .attribute(HttpServletResponse.class.getName(), response)
                                    .build();
                            authorizedClient = authorizedClientManager.authorize(authorizeRequest);
                            authorizedClientRepository.saveAuthorizedClient(authorizedClient, authentication, request, response);

                            securityContext = buildNewSecurityContext(authorizedClient);
                            securityContextRepository.saveContext(securityContext, request, response);
                        }
                    }
                } finally {
                    filterChain.doFilter(request, response);
                }
            }
        };
    }

    private OidcIdToken createOidcToken(ClientRegistration clientRegistration,
                                        OAuth2AccessTokenResponse accessTokenResponse) {
        JwtDecoder jwtDecoder = this.jwtDecoderFactory.createDecoder(clientRegistration);
        Jwt jwt = getJwt(accessTokenResponse, jwtDecoder);
        return new OidcIdToken(jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), jwt.getClaims());
    }

    private Jwt getJwt(OAuth2AccessTokenResponse accessTokenResponse, JwtDecoder jwtDecoder) {
        try {
            Map<String, Object> parameters = accessTokenResponse.getAdditionalParameters();
            return jwtDecoder.decode((String) parameters.get(OidcParameterNames.ID_TOKEN));
        } catch (JwtException ex) {
            OAuth2Error invalidIdTokenError = new OAuth2Error(INVALID_ID_TOKEN_ERROR_CODE, ex.getMessage(), null);
            throw new OAuth2AuthenticationException(invalidIdTokenError, invalidIdTokenError.toString(), ex);
        }
    }

    private boolean hasTokenExpired(OAuth2Token token) {
        return Objects.nonNull(token)
                && Objects.nonNull(token.getExpiresAt())
                && Instant.now().isAfter(Objects.requireNonNull(token.getExpiresAt()).minus(expirySkew));
    }

    private SecurityContext buildNewSecurityContext(OAuth2AuthorizedClient authorizedClient) {
        if (Objects.isNull(authorizedClient) || Objects.isNull(authorizedClient.getRefreshToken())) {
            return SecurityContextHolder.getContext();
        }

        ClientRegistration clientRegistration = authorizedClient.getClientRegistration();
        OAuth2RefreshTokenGrantRequest refreshTokenGrantRequest = new OAuth2RefreshTokenGrantRequest(
                authorizedClient.getClientRegistration(), authorizedClient.getAccessToken(),
                authorizedClient.getRefreshToken(), Collections.emptySet());
        OAuth2AccessTokenResponse accessTokenResponse = accessTokenResponseClient.getTokenResponse(refreshTokenGrantRequest);
        OidcIdToken oidcIdToken = createOidcToken(clientRegistration, accessTokenResponse);
        OidcUserRequest userRequest = new OidcUserRequest(clientRegistration, authorizedClient.getAccessToken(), oidcIdToken);
        OidcUser oidcUser = oidcUserService.loadUser(userRequest);

        SecurityContext newSecurityContext = SecurityContextHolder.createEmptyContext();
        newSecurityContext.setAuthentication(new OAuth2AuthenticationToken(oidcUser, Collections.emptyList(), clientRegistration.getRegistrationId()));
        return newSecurityContext;
    }
}
