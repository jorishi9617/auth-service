package com.videoplatform.auth.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@ConditionalOnProperty(name = "security.local-test.enabled", havingValue = "true")
public class LocalTestSessionController {
    private final AuthService authService;

    public LocalTestSessionController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/local-test-session")
    public AuthDtos.TokenResponse createSession(@Valid @RequestBody LocalTestSessionRequest request,
                                                HttpServletRequest httpRequest) {
        try {
            if (!InetAddress.getByName(httpRequest.getRemoteAddr()).isLoopbackAddress()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Local test sign-in is only available from this machine");
            }
        } catch (UnknownHostException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Local test sign-in is only available from this machine");
        }
        return authService.localTestSession(request.browserId());
    }

    public record LocalTestSessionRequest(@NotNull UUID browserId) {}
}
