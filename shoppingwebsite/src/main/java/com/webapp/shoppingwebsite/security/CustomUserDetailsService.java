package com.webapp.shoppingwebsite.security;

import com.webapp.shoppingwebsite.dao.AuthProvider;
import com.webapp.shoppingwebsite.exception.ResourceNotFoundException;
import com.webapp.shoppingwebsite.dao.User;
import com.webapp.shoppingwebsite.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email : " + email)
                );
        if(!user.getProvider().equals(AuthProvider.local)){
            throw new UsernameNotFoundException("User found this email is not a signed up user : " + email);
        }

        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(String userid) {
        User user = userRepository.findByUserid(userid).orElseThrow(
                () -> new ResourceNotFoundException("User", "userid", userid)
        );

        return UserPrincipal.create(user);
    }
}
