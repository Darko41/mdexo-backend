package com.doublez.backend.controller.admindashboard;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


// For Admin dashboard (Thymeleaf server side) authentication


@Controller
@RequestMapping("/auth")
public class AuthController {
    
	@GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               @RequestParam(value = "admin", required = false) Boolean adminRedirect,
                               HttpServletRequest request,
                               Model model) {
        
        // If already authenticated, redirect appropriately
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return determineRedirectUrl(auth, request);
        }
        
        // Add model attributes for template
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        if (adminRedirect != null && adminRedirect) {
            model.addAttribute("adminLogin", true);
        }
        
        return "auth/login";
    }
	
	@GetMapping("/success")
    public String handleLoginSuccess(Authentication authentication, HttpServletRequest request) {
        return determineRedirectUrl(authentication, request);
    }
	
	private String determineRedirectUrl(Authentication auth, HttpServletRequest request) {
        // Check if user has admin role
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            // Check if there's a saved request (interrupted admin access)
            SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, null);
            if (savedRequest != null) {
                String redirectUrl = savedRequest.getRedirectUrl();
                if (redirectUrl != null && redirectUrl.contains("/admin")) {
                    return "redirect:" + redirectUrl;
                }
            }
            // Default admin destination
            return "redirect:/admin/dashboard";
        }
        
        // Check for regular user's saved request
        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, null);
        if (savedRequest != null) {
            return "redirect:" + savedRequest.getRedirectUrl();
        }
        
        // Default for regular users
        return "redirect:/";
    }
	
	@PostMapping("/logout")
    public String handleLogout(HttpServletRequest request, HttpServletResponse response) {
        // Clear Spring Security context
        SecurityContextHolder.clearContext();
        
        // Invalidate session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // This will log out ALL tabs/windows
        }
        
        // Clear JSESSIONID cookie
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        
        return "redirect:/auth/login?logout=true";
    }
    
}
