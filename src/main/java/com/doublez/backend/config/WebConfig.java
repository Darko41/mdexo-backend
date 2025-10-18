package com.doublez.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
            "/dist/**",
            "/plugins/**", 
            "/pages/**"
        ).addResourceLocations(
            "classpath:/static/dist/",
            "classpath:/static/plugins/", 
            "classpath:/static/pages/"
        );
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/admin/login").setViewName("admin/login");
        
        // Map ALL your React routes explicitly - no warnings!
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
    
    // Optional: Catch any remaining routes
    @Controller
    public static class CatchAllController {
        @GetMapping("/**")
        public String catchAll(HttpServletRequest request) {
            String path = request.getServletPath();
            
            // Only forward non-API, non-admin, non-static routes
            if (!path.startsWith("/api") && 
                !path.startsWith("/admin") && 
                !path.startsWith("/dist") && 
                !path.startsWith("/plugins") && 
                !path.startsWith("/pages") &&
                !path.contains(".")) { // Exclude files with extensions
                return "forward:/index.html";
            }
            return null;
        }
    }
}