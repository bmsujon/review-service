package com.incognito.reviewservice.config; // Or any appropriate package

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // You can make allowedOrigins configurable via application.properties
    // For example: app.cors.allowed-origins=http://localhost:3000,https://your-frontend.com
    @Value("${app.cors.allowed-origins:http://localhost:5173}") // Default to localhost:3000
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Configure CORS for all paths under /api
                .allowedOrigins(allowedOrigins) // Origins that are allowed to access the resources
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Allowed HTTP methods
                .allowedHeaders("*") // Allow all headers in the request
                .allowCredentials(true) // Allow cookies and authorization headers
                .maxAge(3600); // Cache pre-flight response for 1 hour
    }
}