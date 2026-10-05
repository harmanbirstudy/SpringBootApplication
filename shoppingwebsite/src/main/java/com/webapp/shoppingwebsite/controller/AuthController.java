package com.webapp.shoppingwebsite.controller;

import com.webapp.shoppingwebsite.exception.BadRequestException;
import com.webapp.shoppingwebsite.dao.AuthProvider;
import com.webapp.shoppingwebsite.dao.User;
import com.webapp.shoppingwebsite.payload.ApiResponse;
import com.webapp.shoppingwebsite.payload.AuthResponse;
import com.webapp.shoppingwebsite.payload.LoginRequest;
import com.webapp.shoppingwebsite.payload.SignUpRequest;
import com.webapp.shoppingwebsite.repository.UserRepository;
import com.webapp.shoppingwebsite.security.TokenProvider;
import com.webapp.shoppingwebsite.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for email: {}", loginRequest.getEmail());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (AuthenticationException ex) {
            logger.warn("Login failed for email: {} - {}", loginRequest.getEmail(), ex.getMessage());
            throw ex;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.createToken(authentication);
        logger.info("Login successful for userid: {}", ((UserPrincipal) authentication.getPrincipal()).getUserid());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        logger.info("Signup request for email: {}", signUpRequest.getEmail());
        Optional<User> userOptional=userRepository.findByEmail(signUpRequest.getEmail());

        userOptional.ifPresent(user1 ->
              {logger.warn("Signup rejected, email already in use: {}", signUpRequest.getEmail());
               throw new BadRequestException("Email address already in use.");
            })
        ;

        // Creating user's account
        User user = new User();
        user.setName(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        //user.setPassword(signUpRequest.getPassword());
        user.setProvider(AuthProvider.local);
        user.setProviderId("");
        user.setImageUrl("");
        user.setRole(new HashSet<String>(Arrays.asList(User.ROLE_USER)));
        //user.getUserid("ACDEF");
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setCreateddate(new Date());
        user.setModifieddate(new Date());
       user.setEnabled(Boolean.TRUE);
        User result = userRepository.save(user);
        logger.info("User registered successfully with userid: {}", result.getUserid());

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/user/me")
                .buildAndExpand(result.getUserid()).toUri();


        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "User registered successfully"));
    }

}
