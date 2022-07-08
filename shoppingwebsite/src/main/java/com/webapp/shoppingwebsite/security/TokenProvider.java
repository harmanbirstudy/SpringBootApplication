package com.webapp.shoppingwebsite.security;

import com.webapp.shoppingwebsite.config.AppProperties;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.*;

@Service
public class TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private AppProperties appProperties;

    public TokenProvider(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String createToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getTokenExpirationMsec());

        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(appProperties.getAuth().getTokenSecret());
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.getJcaName());

        Map map = new HashMap<String,Object>();
        map.put("alg","HS256");
        map.put("typ","JWT");

//        Set<String> userroles=new HashSet<>()
//        for(String role : userPrincipal.getAttributes()){
//            authorities.add(new SimpleGrantedAuthority(role));
//        }
        String token= Jwts.builder()
                .setHeader(map)
                .setSubject((userPrincipal.getUserid()))
                .claim("NAME",userPrincipal.getName())
                .claim("IMAGEURL",userPrincipal.getImageurl())
                .claim("ROLE",userPrincipal.getAuthorities().toString())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(signingKey,SignatureAlgorithm.HS256)
                .compact();

        return token;
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(appProperties.getAuth().getTokenSecret())
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(appProperties.getAuth().getTokenSecret()).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }
}
