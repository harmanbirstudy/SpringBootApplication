package com.webapp.shoppingwebsite.security;

import com.webapp.shoppingwebsite.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenProviderTest {

    // 32 bytes, the minimum jjwt accepts for HS256
    private static final String SECRET = Base64.getEncoder().encodeToString(new byte[32]);
    private static final String OTHER_SECRET = Base64.getEncoder().encodeToString("another-32-byte-secret-for-test!".getBytes());

    @Mock
    private AppProperties appProperties;

    private final AppProperties.Auth auth = new AppProperties.Auth();

    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        auth.setTokenSecret(SECRET);
        auth.setTokenExpirationMsec(60_000);
        when(appProperties.getAuth()).thenReturn(auth);
        tokenProvider = new TokenProvider(appProperties);
    }

    @Test
    void createdToken_isValidAndCarriesUserId() {
        String token = tokenProvider.createToken(authenticationFor("u-1"));

        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.getUserIdFromToken(token)).isEqualTo("u-1");
    }

    @Test
    void validateToken_rejectsTokenSignedWithDifferentSecret() {
        auth.setTokenSecret(OTHER_SECRET);
        String foreignToken = tokenProvider.createToken(authenticationFor("u-1"));
        auth.setTokenSecret(SECRET);

        assertThat(tokenProvider.validateToken(foreignToken)).isFalse();
    }

    @Test
    void validateToken_rejectsExpiredToken() {
        auth.setTokenExpirationMsec(-1_000);
        String expired = tokenProvider.createToken(authenticationFor("u-1"));

        assertThat(tokenProvider.validateToken(expired)).isFalse();
    }

    @Test
    void validateToken_rejectsMalformedAndEmptyTokens() {
        assertThat(tokenProvider.validateToken("not-a-jwt")).isFalse();
        assertThat(tokenProvider.validateToken("")).isFalse();
    }

    @Test
    void validateToken_rejectsTamperedToken() {
        String token = tokenProvider.createToken(authenticationFor("u-1"));
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

        assertThat(tokenProvider.validateToken(tampered)).isFalse();
    }

    private static Authentication authenticationFor(String userid) {
        UserPrincipal principal = new UserPrincipal(userid, "Jane", "jane@example.com", "hash", true, "",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
