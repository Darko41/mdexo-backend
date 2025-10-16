package com.doublez.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve AdminLTE resources from static/
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
    }
    
    @Controller
    public static class ReactRoutingController {
        
        @RequestMapping(value = { 
            "/", "/property/**", "/create-listing", "/login", 
            "/signup", "/buy", "/rent", "/sell", "/help", "/search" 
        })
        public String forwardToReact() {
            return "forward:/index.html";
        }
    }
}