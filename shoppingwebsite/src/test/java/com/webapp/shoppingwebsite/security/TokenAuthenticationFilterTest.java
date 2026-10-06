package com.webapp.shoppingwebsite.security;

import com.webapp.shoppingwebsite.exception.ResourceNotFoundException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationFilterTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private TokenAuthenticationFilter filter;

    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/user/me");
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validBearerToken_authenticatesUser() throws Exception {
        UserPrincipal principal = new UserPrincipal("u-1", "Jane", "jane@example.com", "hash", true, "",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        request.addHeader("Authorization", "Bearer good-token");
        when(tokenProvider.validateToken("good-token")).thenReturn(true);
        when(tokenProvider.getUserIdFromToken("good-token")).thenReturn("u-1");
        when(customUserDetailsService.loadUserById("u-1")).thenReturn(principal);

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isSameAs(principal);
        assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void missingAuthorizationHeader_leavesRequestUnauthenticated() throws Exception {
        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(tokenProvider, customUserDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void nonBearerHeader_isIgnored() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(tokenProvider);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void invalidToken_leavesRequestUnauthenticated() throws Exception {
        request.addHeader("Authorization", "Bearer bad-token");
        when(tokenProvider.validateToken("bad-token")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).getUserIdFromToken(anyString());
        verifyNoInteractions(customUserDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void tokenForDeletedUser_isSwallowedAndChainContinues() throws Exception {
        request.addHeader("Authorization", "Bearer good-token");
        when(tokenProvider.validateToken("good-token")).thenReturn(true);
        when(tokenProvider.getUserIdFromToken("good-token")).thenReturn("gone");
        when(customUserDetailsService.loadUserById("gone"))
                .thenThrow(new ResourceNotFoundException("User", "userid", "gone"));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
