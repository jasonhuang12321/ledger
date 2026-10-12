package com.learning;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/403")
    public String denied() {
        return "denied";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid RegisterForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("message", firstError(bindingResult));
            return "register";
        }
        try {
            userService.register(form.getUsername(), form.getPassword());
        } catch (BusinessException ex) {
            model.addAttribute("message", ex.getMessage());
            return "register";
        }
        model.addAttribute("message", "注册成功，请登录");
        return "login";
    }

    private String firstError(BindingResult bindingResult) {
        FieldError error = bindingResult.getFieldError();
        return error == null ? "填写不正确" : error.getDefaultMessage();
    }
}
