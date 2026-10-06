package com.webapp.shoppingwebsite.controller;

import com.webapp.shoppingwebsite.dao.User;
import com.webapp.shoppingwebsite.exception.ResourceNotFoundException;
import com.webapp.shoppingwebsite.repository.UserRepository;
import com.webapp.shoppingwebsite.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private static final UserPrincipal PRINCIPAL =
            new UserPrincipal("u-1", "Jane", "jane@example.com", "hash", true, "", List.of());

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController controller;

    @Test
    void getCurrentUser_returnsUserForPrincipal() {
        User user = new User();
        user.setUserid("u-1");
        when(userRepository.findByUserid("u-1")).thenReturn(Optional.of(user));

        assertThat(controller.getCurrentUser(PRINCIPAL)).isSameAs(user);
    }

    @Test
    void getCurrentUser_throwsNotFoundWhenUserDeleted() {
        when(userRepository.findByUserid("u-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getCurrentUser(PRINCIPAL))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("u-1");
    }
}
