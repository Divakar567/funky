package com.diva.funky.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class WebViewConfiguration {

    /**
     * Renders index.html view for all non rest and static resource paths
     *
     * @return index.html
     */
    @Bean
    RouterFunction<ServerResponse> getDefaultView() {
        return RouterFunctions.route()
                .GET(request -> !request.path().startsWith("/api/")
                                && !request.path().contains("."),
                        request -> ServerResponse.ok().render("index", Collections.emptyMap()))
                .build();
    }
}
