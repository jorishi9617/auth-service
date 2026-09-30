package com.videoplatform.auth.api;

import com.videoplatform.auth.user.UserAccount;
import com.videoplatform.auth.user.UserRepository;
import com.videoplatform.common.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long expirationSeconds;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService,
                       @Value("${security.jwt.expiration-seconds:900}") long expirationSeconds) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.expirationSeconds = expirationSeconds;
    }

    @Transactional
    public AuthDtos.TokenResponse register(AuthDtos.Credentials credentials) {
        String email = normalize(credentials.email());
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        return token(users.save(new UserAccount(email, passwordEncoder.encode(credentials.password()))));
    }

    @Transactional(readOnly = true)
    public AuthDtos.TokenResponse login(AuthDtos.Credentials credentials) {
        UserAccount user = users.findByEmailIgnoreCase(normalize(credentials.email()))
                .filter(account -> passwordEncoder.matches(credentials.password(), account.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return token(user);
    }

    @Transactional
    public AuthDtos.TokenResponse localTestSession(UUID browserId) {
        String email = "local-" + browserId + "@local.test";
        users.createLocalTestUserIfAbsent(UUID.randomUUID(), email,
                passwordEncoder.encode(UUID.randomUUID().toString()));
        UserAccount user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Local test account was not created"));
        return token(user);
    }

    private AuthDtos.TokenResponse token(UserAccount user) {
        return new AuthDtos.TokenResponse(jwtService.issue(user.getId(), user.getEmail()), "Bearer",
                expirationSeconds, user.getId(), user.getEmail());
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
