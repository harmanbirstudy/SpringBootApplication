package com.webapp.shoppingwebsite.controller;

import com.webapp.shoppingwebsite.exception.ResourceNotFoundException;
import com.webapp.shoppingwebsite.dao.User;
import com.webapp.shoppingwebsite.repository.UserRepository;
import com.webapp.shoppingwebsite.security.CurrentUser;
import com.webapp.shoppingwebsite.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController implements SecuredRestController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public User getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return userRepository.findByUserid(userPrincipal.getUserid())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getUserid()));
    }
}
