package com.internship.tool.controller;

import com.internship.tool.entity.PasswordResetToken;
import com.internship.tool.entity.User;
import com.internship.tool.repository.PasswordResetTokenRepository;
import com.internship.tool.repository.UserRepository;
import com.internship.tool.security.JwtUtil;
import com.internship.tool.service.EmailNotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private static final int RESET_TOKEN_EXPIRY_MINUTES = 20;

    private final UserRepository repo;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailNotificationService emailNotificationService;

    public AuthController(UserRepository repo,
                          PasswordResetTokenRepository resetTokenRepository,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder,
                          EmailNotificationService emailNotificationService) {
        this.repo = repo;
        this.resetTokenRepository = resetTokenRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.emailNotificationService = emailNotificationService;
    }

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {
        repo.findByUsername(request.username()).ifPresent(existing -> {
            throw new IllegalArgumentException("Username already exists");
        });
        repo.findByEmail(request.email()).ifPresent(existing -> {
            throw new IllegalArgumentException("Email already exists");
        });

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(passwordEncoder.encode(request.password()));

        repo.save(user);
        emailNotificationService.sendWelcomeEmail(user);
        return "User registered";
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest request) {
        User dbUser = repo.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordMatches(request.password(), dbUser)) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return jwtUtil.generateToken(dbUser.getUsername());
    }

    @PostMapping("/refresh")
    public String refresh(@Valid @RequestBody LoginRequest request) {
        return login(request);
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        repo.findByEmail(request.email()).ifPresent(user -> {
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setUser(user);
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES));
            resetToken.setUsed(false);
            resetTokenRepository.save(resetToken);
            emailNotificationService.sendPasswordResetEmail(user, resetToken.getToken());
        });

        return "If an account exists for that email, a reset link has been sent";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        repo.save(user);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);

        return "Password reset successful";
    }

    private boolean passwordMatches(String rawPassword, User user) {
        String storedPassword = user.getPassword();
        if (storedPassword != null && storedPassword.startsWith("$2") && passwordEncoder.matches(rawPassword, storedPassword)) {
            return true;
        }

        if (storedPassword != null && storedPassword.equals(rawPassword)) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            repo.save(user);
            return true;
        }

        return false;
    }

    public record RegisterRequest(
            @NotBlank(message = "Username is required") String username,
            @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,
            @NotBlank(message = "Phone number is required")
            @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be 10 to 15 digits and may start with +")
            String phoneNumber,
            @NotBlank(message = "Password is required") String password
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "Username is required") String username,
            @NotBlank(message = "Password is required") String password
    ) {
    }

    public record ForgotPasswordRequest(
            @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email
    ) {
    }

    public record ResetPasswordRequest(
            @NotBlank(message = "Token is required") String token,
            @NotBlank(message = "New password is required") String newPassword
    ) {
    }
}
