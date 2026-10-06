package com.webapp.shoppingwebsite.security;

import com.webapp.shoppingwebsite.dao.AuthProvider;
import com.webapp.shoppingwebsite.dao.User;
import com.webapp.shoppingwebsite.exception.ResourceNotFoundException;
import com.webapp.shoppingwebsite.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_returnsPrincipalWithRolesForLocalUser() {
        when(userRepository.findByEmail("jane@example.com"))
                .thenReturn(Optional.of(user(AuthProvider.local, User.ROLE_USER, User.ROLE_ADMIN)));

        UserDetails details = service.loadUserByUsername("jane@example.com");

        assertThat(details).isInstanceOf(UserPrincipal.class);
        UserPrincipal principal = (UserPrincipal) details;
        assertThat(principal.getUserid()).isEqualTo("u-1");
        assertThat(principal.getEmail()).isEqualTo("jane@example.com");
        assertThat(principal.getPassword()).isEqualTo("hash");
        assertThat(principal.isEnabled()).isTrue();
        assertThat(principal.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_throwsWhenEmailUnknown() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("nobody@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("nobody@example.com");
    }

    @Test
    void loadUserByUsername_rejectsPasswordLoginForOAuthAccount() {
        when(userRepository.findByEmail("jane@example.com"))
                .thenReturn(Optional.of(user(AuthProvider.google, User.ROLE_USER)));

        assertThatThrownBy(() -> service.loadUserByUsername("jane@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("not a signed up user");
    }

    @Test
    void loadUserById_returnsPrincipal() {
        when(userRepository.findByUserid("u-1")).thenReturn(Optional.of(user(AuthProvider.google, User.ROLE_USER)));

        UserPrincipal principal = (UserPrincipal) service.loadUserById("u-1");

        assertThat(principal.getUserid()).isEqualTo("u-1");
    }

    @Test
    void loadUserById_throwsNotFoundWhenMissing() {
        when(userRepository.findByUserid("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserById("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");
    }

    private static User user(AuthProvider provider, String... roles) {
        User user = new User();
        user.setUserid("u-1");
        user.setName("Jane");
        user.setEmail("jane@example.com");
        user.setPassword("hash");
        user.setEnabled(true);
        user.setImageUrl("");
        user.setProvider(provider);
        user.setRole(Set.of(roles));
        return user;
    }
}
