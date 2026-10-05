package com.webapp.shoppingwebsite.security;

import com.webapp.shoppingwebsite.dao.AuthProvider;
import com.webapp.shoppingwebsite.exception.ResourceNotFoundException;
import com.webapp.shoppingwebsite.dao.User;
import com.webapp.shoppingwebsite.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email : " + email);
                });
        if(!user.getProvider().equals(AuthProvider.local)){
            logger.warn("Local login attempted for {} account, email: {}", user.getProvider(), email);
            throw new UsernameNotFoundException("User found this email is not a signed up user : " + email);
        }

        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(String userid) {
        User user = userRepository.findByUserid(userid).orElseThrow(() -> {
            logger.warn("User not found with userid: {}", userid);
            return new ResourceNotFoundException("User", "userid", userid);
        });

        return UserPrincipal.create(user);
    }
}
