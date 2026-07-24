package com.rikkei.course141.ss1.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

import com.rikkei.course141.ss1.config.JwtProvider;
import com.rikkei.course141.ss1.dto.request.RegisterRequest;
import com.rikkei.course141.ss1.dto.response.ApiResponse;
import com.rikkei.course141.ss1.model.User;
import com.rikkei.course141.ss1.repository.UserRepository;
import com.rikkei.course141.ss1.security.UserPrincipal;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          JavaMailSender mailSender, AuthenticationManager authenticationManager,
                          JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest dto) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        User user = User.builder()
            .username(dto.getUsername())
            .password(passwordEncoder.encode(dto.getPassword()))
            .email(dto.getEmail())
            .role("USER")
            .enabled(false)
            .otpCode(otp)
            .otpExpiration(LocalDateTime.now().plusMinutes(5))
            .build();
        userRepository.save(user);
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(dto.getEmail());
        msg.setSubject("OTP Activation");
        msg.setText("Mã OTP của bạn: " + otp);
        try { mailSender.send(msg); } catch (Exception ignored) {}
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(user));
    }

    @PostMapping("/active-user")
    public ResponseEntity<ApiResponse<String>> activate(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        User user = userRepository.findByEmail(email).orElseThrow();
        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp) || user.getOtpExpiration().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "OTP không hợp lệ hoặc đã hết hạn"));
        }
        user.setEnabled(true);
        user.setOtpCode(null);
        user.setOtpExpiration(null);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Kích hoạt thành công"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(body.get("username"), body.get("password"))
        );
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        String token = jwtProvider.generateToken(principal.getUsername(), principal.getUser().getRole());
        return ResponseEntity.ok(Map.of("accessToken", token, "type", "Bearer"));
    }
}
