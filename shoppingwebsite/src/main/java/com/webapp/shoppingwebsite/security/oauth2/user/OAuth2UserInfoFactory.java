package com.webapp.shoppingwebsite.security.oauth2.user;

import com.webapp.shoppingwebsite.exception.OAuth2AuthenticationProcessingException;
import com.webapp.shoppingwebsite.dao.AuthProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class OAuth2UserInfoFactory {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2UserInfoFactory.class);

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if(registrationId.equalsIgnoreCase(AuthProvider.google.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.facebook.toString())) {
            return new FacebookOAuth2UserInfo(attributes);
        }  else {
            logger.warn("Unsupported OAuth2 provider: {}", registrationId);
            throw new OAuth2AuthenticationProcessingException("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}
