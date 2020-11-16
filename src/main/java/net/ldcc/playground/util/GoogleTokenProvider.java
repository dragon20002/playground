package net.ldcc.playground.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import net.ldcc.playground.model.MemberSec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
public class GoogleTokenProvider implements OAuthTokenProvider {
    private final Logger logger = LoggerFactory.getLogger(GoogleTokenProvider.class);

    private static final String CLIENT_ID = "451544914380-m657ri1nr9i2b1qeq8jb8p3o3bl1o8b0.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "OJ5e5rg1dMVdKsXNKyZQxCqZ";

    private final RestTemplate restTemplate;

    public GoogleTokenProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String createToken(String code, String state, String redirectUri) {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Accept", "application/json");

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl("https://oauth2.googleapis.com/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("client_secret", CLIENT_SECRET)
                .queryParam("code", code)
                .queryParam("state", state)
                .queryParam("redirect_uri", redirectUri);

        GoogleTokenIssueResponse response = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.POST,
                new HttpEntity<>(header),
                GoogleTokenIssueResponse.class
        ).getBody();

        logger.debug("oauth/token response = {}", response);

        return (response != null) ? response.idToken : null;
    }

    @Override
    public MemberSec getSubject(String token) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        GoogleIdToken.Payload payload = verifier.verify(token).getPayload();

        if (payload.getEmailVerified()) {
            return new MemberSec(null, payload.getEmail(), (String) payload.get("name"), payload.getEmail(),
                    null, null, null, (String) payload.get("picture"));
        } else {
            return null;
        }
    }

    static class GoogleTokenIssueResponse {
        public String accessToken;
        public String expiresIn;
        public String idToken;
        public String scope;
        public String tokenType;
        public String refreshToken;
    }

}
