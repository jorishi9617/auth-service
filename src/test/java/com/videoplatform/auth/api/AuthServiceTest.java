package com.videoplatform.auth.api;

import com.videoplatform.auth.user.UserAccount;
import com.videoplatform.auth.user.UserRepository;
import com.videoplatform.common.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final JwtService jwt = mock(JwtService.class);
    private final AuthService service = new AuthService(users, encoder, jwt, 900);

    @Test
    void registrationNormalizesEmailAndReturnsBearerToken() {
        when(users.existsByEmailIgnoreCase("person@example.com")).thenReturn(false);
        when(encoder.encode("long-password")).thenReturn("hashed");
        when(users.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwt.issue(any(UUID.class), any(String.class))).thenReturn("signed-token");

        AuthDtos.TokenResponse result = service.register(new AuthDtos.Credentials(" Person@Example.com ", "long-password"));

        assertEquals("person@example.com", result.email());
        assertEquals("Bearer", result.tokenType());
        assertEquals("signed-token", result.accessToken());
    }

    @Test
    void loginDoesNotRevealWhetherEmailExists() {
        when(users.findByEmailIgnoreCase("person@example.com")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.login(new AuthDtos.Credentials("person@example.com", "incorrect-password")));
    }
}
