package com.example.demo.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @SuppressWarnings("null")
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:C:/demo/uploads/");

        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:C:/demo/avatars/");
        registry.addResourceHandler("/inspirations/**")
                .addResourceLocations("file:C:/demo/inspirations/");
    }
}