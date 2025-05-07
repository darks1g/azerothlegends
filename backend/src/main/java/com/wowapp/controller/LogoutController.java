package com.wowapp.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LogoutController {

    @PostMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate(); // Cierra la sesión
    }
}
