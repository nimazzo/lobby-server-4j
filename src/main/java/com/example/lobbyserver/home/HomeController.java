package com.example.lobbyserver.home;

import com.example.lobbyserver.mail.MailVerificationService;
import com.example.lobbyserver.user.UserDao;
import com.example.lobbyserver.user.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {
    private final UserService userService;
    private final MailVerificationService mailVerificationService;

    public HomeController(UserService userService, MailVerificationService mailVerificationService) {
        this.userService = userService;
        this.mailVerificationService = mailVerificationService;
    }

    @GetMapping
    public String home(Model model) {
        model.addAttribute("name", "Spring User");
        return "home";
    }

    @GetMapping("/register")
    public String registration() {
        return "register";
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute UserDao user, RedirectAttributes redirectAttributes) {
        if (userService.usernameExists(user.username())) {
            redirectAttributes.addFlashAttribute("error", "Username already exists.");
            return "redirect:/register";
        }

        if (userService.emailExists(user.email())) {
            redirectAttributes.addFlashAttribute("error", "Email already exists.");
            return "redirect:/register";
        }

        userService.createUser(user.username(), user.password(), user.email(), "USER");
        mailVerificationService.sendVerificationMail(user.email());

        return "redirect:/login?registered";
    }
}
