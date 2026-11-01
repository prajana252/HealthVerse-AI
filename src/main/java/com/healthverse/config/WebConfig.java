package com.healthverse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/** Which websites may call the API. Set CORS_ORIGINS (comma separated) in production, e.g. https://yourname.github.io */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${healthverse.cors.allowed-origins:*}")
    private String origins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowed = Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
        registry.addMapping("/api/**").allowedOrigins(allowed)
                .allowedMethods("GET", "POST", "DELETE", "OPTIONS").allowedHeaders("*");
    }
}
