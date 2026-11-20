package com.smartcampus.web;

import com.smartcampus.dto.ApiResponse;
import com.smartcampus.dto.LoginRequest;
import com.smartcampus.model.UserAccount;
import com.smartcampus.security.AuthInterceptor;
import com.smartcampus.security.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest req) {
        String token = auth.login(req.getUsername(), req.getPassword());
        UserAccount user = auth.verify(token).orElseThrow();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("user", user);
        return ApiResponse.ok("Login successful", data);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        auth.logout(AuthInterceptor.extractToken(request));
        return ApiResponse.ok("Logged out", null);
    }

    @GetMapping("/me")
    public ApiResponse<UserAccount> me(HttpServletRequest request) {
        return ApiResponse.ok((UserAccount) request.getAttribute(AuthInterceptor.ATTR_USER));
    }
}
