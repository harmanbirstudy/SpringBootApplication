package com.webapp.shoppingwebsite.security;

import com.webapp.shoppingwebsite.dao.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

public class UserPrincipal implements OAuth2User, UserDetails {
    private String userid;
    private String name;
    private String email;
    private String password;
    private String imageurl;
    private  Boolean enabled;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    public UserPrincipal(String id, String name,String email,
                         String password,Boolean enabled,String imageurl,
                         Collection<? extends GrantedAuthority> authorities) {
        this.userid = id;
        this.name = name;
        this.email = email;
        this.enabled=enabled;
        this.password = password;
        this.imageurl = imageurl;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Set<String> userroles=user.getRole();
        for(String role : userroles){
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return new UserPrincipal(
                user.getUserid(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getEnabled(),
                user.getImageUrl(),
                authorities
        );


    }

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    public String getUserid() {
        return userid;
    }

    public String getEmail() {
        return email;
    }

    public String getImageurl() {
        return imageurl;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return  name;
    }
}
