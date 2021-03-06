package net.ldcc.playground.util;

import net.ldcc.playground.model.MemberSec;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.GeneralSecurityException;

public abstract class OAuthTokenProvider {
    protected final String clientId;
    protected final String clientSecret;
    protected final RestTemplate restTemplate;

    protected OAuthTokenProvider(RestTemplate restTemplate, String clientId, String clientSecret) {
        this.restTemplate = restTemplate;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public abstract String createToken(String code, String state, String redirectUri);

    public abstract MemberSec getSubject(String token) throws GeneralSecurityException, IOException;
}
