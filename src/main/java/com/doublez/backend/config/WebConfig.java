package com.doublez.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
            "/dist/**",
            "/plugins/**", 
            "/pages/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/static/**",
            "/assets/**",
            "/favicon.ico"
        ).addResourceLocations(
            "classpath:/static/dist/",
            "classpath:/static/plugins/", 
            "classpath:/static/pages/",
            "classpath:/static/css/",
            "classpath:/static/js/",
            "classpath:/static/images/",
            "classpath:/static/static/",
            "classpath:/static/assets/",
            "classpath:/static/"
        );
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/admin/login").setViewName("redirect:/auth/login?admin=true");
        registry.addViewController("/admin").setViewName("redirect:/auth/login?admin=true");
        
        // React routes
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/buy").setViewName("forward:/index.html");
        registry.addViewController("/rent").setViewName("forward:/index.html");
        registry.addViewController("/sell").setViewName("forward:/index.html");
        registry.addViewController("/help").setViewName("forward:/index.html");
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/signup").setViewName("forward:/index.html");
        registry.addViewController("/create-listing").setViewName("forward:/index.html");
        registry.addViewController("/search").setViewName("forward:/index.html");
        registry.addViewController("/real-estates").setViewName("forward:/index.html");
        registry.addViewController("/property/{id}").setViewName("forward:/index.html");
    }
}