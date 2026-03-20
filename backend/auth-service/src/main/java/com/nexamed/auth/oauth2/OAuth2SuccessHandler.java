package com.nexamed.auth.oauth2;

import com.nexamed.auth.model.AuthProvider;
import com.nexamed.auth.model.Role;
import com.nexamed.auth.model.User;
import com.nexamed.auth.repository.UserRepository;
import com.nexamed.auth.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Called after successful Google OAuth2 login.
 *
 * Flow:
 *  1. Google redirects back with user profile
 *  2. This handler finds or creates the user in DB
 *  3. Generates JWT tokens
 *  4. Redirects React frontend with token in URL param
 *     (frontend stores it in localStorage)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    // After login, redirect React app with token
    private static final String FRONTEND_REDIRECT = "http://localhost:3000/oauth2/callback";

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email      = oAuth2User.getAttribute("email");
        String name       = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub"); // Google's unique user ID

        // Find existing user or create new one
        User user = userRepository
                .findByProviderAndProviderId(AuthProvider.GOOGLE, providerId)
                .orElseGet(() -> {
                    // First time Google login — create account
                    User newUser = User.builder()
                            .email(email)
                            .fullName(name)
                            .provider(AuthProvider.GOOGLE)
                            .providerId(providerId)
                            .role(Role.STUDENT)
                            .build();
                    return userRepository.save(newUser);
                });

        String accessToken  = jwtService.generateAccessToken(user, user.getRole().name(), user.getId().toString());
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("OAuth2 login success: {}", email);

        // Redirect frontend with tokens in query params
        String redirectUrl = FRONTEND_REDIRECT
                + "?token=" + accessToken
                + "&refreshToken=" + refreshToken
                + "&role=" + user.getRole().name();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}