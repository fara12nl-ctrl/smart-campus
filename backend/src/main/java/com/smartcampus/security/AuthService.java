package com.smartcampus.security;

import com.smartcampus.dao.UserRepository;
import com.smartcampus.exception.ApiException;
import com.smartcampus.model.UserAccount;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Simple in-memory token auth: SHA-256 password compare + token map. */
@Service
public class AuthService {

    private static final long TOKEN_TTL_MS = 8L * 60 * 60 * 1000; // 8 hours

    private final UserRepository users;
    private final Map<String, Session> tokens = new ConcurrentHashMap<>();

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public String login(String username, String password) {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            throw new ApiException(400, "Username and password are required");
        }
        Object[] creds = users.findCredentials(username.trim())
                .orElseThrow(() -> new ApiException(401, "Invalid username or password"));
        UserAccount account = (UserAccount) creds[0];
        String storedHash = (String) creds[1];
        if (!sha256(password).equalsIgnoreCase(storedHash)) {
            throw new ApiException(401, "Invalid username or password");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        tokens.put(token, new Session(account, System.currentTimeMillis() + TOKEN_TTL_MS));
        return token;
    }

    public Optional<UserAccount> verify(String token) {
        if (token == null) return Optional.empty();
        Session s = tokens.get(token);
        if (s == null) return Optional.empty();
        if (s.expiresAt < System.currentTimeMillis()) {
            tokens.remove(token);
            return Optional.empty();
        }
        return Optional.of(s.account);
    }

    public void logout(String token) {
        if (token != null) tokens.remove(token);
    }

    public static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private record Session(UserAccount account, long expiresAt) { }
}
