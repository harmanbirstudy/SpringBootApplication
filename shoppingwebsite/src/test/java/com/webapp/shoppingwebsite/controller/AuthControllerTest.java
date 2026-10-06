package com.webapp.shoppingwebsite.controller;

import com.webapp.shoppingwebsite.dao.AuthProvider;
import com.webapp.shoppingwebsite.dao.User;
import com.webapp.shoppingwebsite.exception.BadRequestException;
import com.webapp.shoppingwebsite.payload.ApiResponse;
import com.webapp.shoppingwebsite.payload.AuthResponse;
import com.webapp.shoppingwebsite.payload.LoginRequest;
import com.webapp.shoppingwebsite.payload.SignUpRequest;
import com.webapp.shoppingwebsite.repository.UserRepository;
import com.webapp.shoppingwebsite.security.TokenProvider;
import com.webapp.shoppingwebsite.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUpRequestContext() {
        // registerUser builds the Location header from the current request
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @AfterEach
    void clearContexts() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_withValidCredentials_returnsTokenAndSetsSecurityContext() {
        LoginRequest request = new LoginRequest();
        request.setEmail("jane@example.com");
        request.setPassword("secret1");

        UserPrincipal principal = new UserPrincipal("u-1", "Jane", "jane@example.com", "hash", true, "", List.of());
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.createToken(authentication)).thenReturn("jwt-token");

        ResponseEntity<?> response = authController.authenticateUser(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((AuthResponse) response.getBody()).getAccessToken()).isEqualTo("jwt-token");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("jane@example.com");
        assertThat(captor.getValue().getCredentials()).isEqualTo("secret1");
    }

    @Test
    void login_withBadCredentials_rethrowsAndIssuesNoToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("jane@example.com");
        request.setPassword("wrong");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authController.authenticateUser(request))
                .isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(tokenProvider);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void signup_withNewEmail_savesLocalUserWithEncodedPassword() {
        SignUpRequest request = signUpRequest("jane@example.com");
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret1")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setUserid("new-id");
            return u;
        });

        ResponseEntity<?> response = authController.registerUser(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasPath("/user/me");
        assertThat(((ApiResponse) response.getBody()).isSuccess()).isTrue();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("jane");
        assertThat(saved.getEmail()).isEqualTo("jane@example.com");
        assertThat(saved.getPassword()).isEqualTo("encoded");
        assertThat(saved.getProvider()).isEqualTo(AuthProvider.local);
        assertThat(saved.getRole()).isEqualTo(Set.of(User.ROLE_USER));
        assertThat(saved.getEnabled()).isTrue();
    }

    @Test
    void signup_withExistingEmail_throwsBadRequestAndDoesNotSave() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authController.registerUser(signUpRequest("jane@example.com")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email address already in use.");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    private static SignUpRequest signUpRequest(String email) {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("jane");
        request.setEmail(email);
        request.setPassword("secret1");
        request.setMatchingPassword("secret1");
        return request;
    }
}
