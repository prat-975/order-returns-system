package com.orderrreturns.controller;

import com.orderrreturns.dto.RegisterDto;
import com.orderrreturns.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "registered", required = false) String registered,
                        Model model) {
        if (error != null) {
            model.addAttribute("loginError", true);
        }
        if (logout != null) {
            model.addAttribute("logoutSuccess", true);
        }
        if (registered != null) {
            model.addAttribute("registerSuccess", true);
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerForm", new RegisterDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterDto registerForm,
                           BindingResult bindingResult) {
        String normalizedUsername = userService.normalizeUsername(registerForm.getUsername());

        if (!registerForm.getPassword().equals(registerForm.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (userService.isReservedUsername(normalizedUsername)) {
            bindingResult.rejectValue("username", "reserved", "This username is reserved");
        }
        if (userService.usernameExists(normalizedUsername)) {
            bindingResult.rejectValue("username", "duplicate", "Username is already taken");
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.registerUser(registerForm);
        return "redirect:/login?registered";
    }
}
