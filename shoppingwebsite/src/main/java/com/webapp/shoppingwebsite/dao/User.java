package com.webapp.shoppingwebsite.dao;

import jakarta.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "users")
public class User{


    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    @Id
    @Column(name = "userid")
    private String userid;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "imageurl")
    private String imageUrl;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private AuthProvider provider;

    @Column(name = "providerid")
    private String providerId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "userid"))
    @Column(name = "role")
    private Set<String> role = new HashSet<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createddate", updatable = false)
    private Date createddate ;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modifieddate")
    private Date modifieddate ;

    @PrePersist
    void onCreate() {
        if (userid == null) {
            userid = UUID.randomUUID().toString();
        }
        if (createddate == null) {
            createddate = new Date();
        }
        modifieddate = new Date();
    }

    @PreUpdate
    void onUpdate() {
        modifieddate = new Date();
    }

    public String getEmail() {
        return email;
    }

    public String getUserid() { return userid; }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public String getPassword() {
        return password;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public Set<String> getRole() {
        return role;
    }

    public Date getCreateddate() { return createddate; }

    public Date getModifieddate() { return modifieddate; }


    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProvider(AuthProvider provider) {
        this.provider = provider;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public void setRole(Set<String> role) {
        this.role = role;
    }

    public void setCreateddate(Date createddate) { this.createddate = createddate; }

    public void setModifieddate(Date modifieddate) { this.modifieddate = modifieddate; }
}
