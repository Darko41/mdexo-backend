package com.doublez.backend.controller.admindashboard;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.doublez.backend.exception.IllegalOperationException;
import com.doublez.backend.exception.UserNotFoundException;
import com.doublez.backend.response.ApiResponse;
import com.doublez.backend.service.user.UserService;

@Controller
@RequestMapping("/admin/user-management")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserService userService;

    public UserAdminController(UserService userService) {
        this.userService = userService;
    }

    // User list page
//    @GetMapping
//    public String userList(Model model,
//                          @RequestParam(defaultValue = "0") int page,
//                          @RequestParam(defaultValue = "10") int size) {
//        List<UserDTO> users = userService.getAllUsers(); // Changed to UserDTO
//        model.addAttribute("users", users);
//        model.addAttribute("currentPage", page);
//        return "admin/users/list";
//    }

    // User details page
//    @GetMapping("/{id}")
//    public String userDetails(@PathVariable Long id, Model model) {
//        try {
//            UserDTO user = userService.getUserById(id); // Changed to UserDTO
//            model.addAttribute("user", user);
//            return "admin/users/details";
//        } catch (Exception e) {
//            return "redirect:/admin/users?error=User not found";
//        }
//    }

    // Create user form
//    @GetMapping("/create")
//    public String createUserForm(Model model) {
//        // Create empty UserDTO.Create instead of AdminUserCreateDTO
//        UserDTO.Create userCreate = new UserDTO.Create();
//        userCreate.setRoles(List.of("ROLE_USER")); // Default role
//        model.addAttribute("userCreate", userCreate);
//        return "admin/users/create";
//    }

    // Create user (form submission)
//    @PostMapping("/create")
//    public String createUser(@ModelAttribute("userCreate") UserDTO.Create createDTO,
//                           RedirectAttributes redirectAttributes) {
//        try {
//            userService.registerUser(createDTO, true); // Use registerUser with admin flag
//            redirectAttributes.addFlashAttribute("success", "User created successfully");
//            return "redirect:/admin/user-management";
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Error creating user: " + e.getMessage());
//            return "redirect:/admin/user-management/create";
//        }
//    }

    // Edit user form
//    @GetMapping("/{id}/edit")
//    public String editUserForm(@PathVariable Long id, Model model) {
//        try {
//            UserDTO user = userService.getUserById(id); // Changed to UserDTO
//            
//            // Create update DTO from existing user data
//            UserDTO.Update updateDTO = new UserDTO.Update();
//            updateDTO.setEmail(user.getEmail());
//            updateDTO.setRoles(user.getRoles());
//            updateDTO.setProfile(user.getProfile());
//            
//            model.addAttribute("user", user);
//            model.addAttribute("updateDTO", updateDTO);
//            return "admin/users/edit";
//        } catch (Exception e) {
//            return "redirect:/admin/user-management?error=User not found";
//        }
//    }

    // Update user (form submission)
//    @PostMapping("/{id}/edit")
//    public String updateUser(@PathVariable Long id,
//                           @ModelAttribute("updateDTO") UserDTO.Update updateDTO,
//                           RedirectAttributes redirectAttributes) {
//        try {
//            userService.updateUser(id, updateDTO); // Changed to updateUser method
//            redirectAttributes.addFlashAttribute("success", "User updated successfully");
//            return "redirect:/admin/user-management/" + id;
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Error updating user: " + e.getMessage());
//            return "redirect:/admin/user-management/" + id + "/edit";
//        }
//    }

    // Delete user
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            // For admin operations, we need to simulate the authentication
            // Since admin can delete any user, we'll create a simple approach
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            userService.deleteUser(id, authentication);
            
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
        } catch (IllegalOperationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete user: " + e.getMessage()));
        }
    }
}