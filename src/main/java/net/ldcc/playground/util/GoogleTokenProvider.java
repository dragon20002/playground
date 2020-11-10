package net.ldcc.playground.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import net.ldcc.playground.model.MemberSec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
public class GoogleTokenProvider {
    private final Logger logger = LoggerFactory.getLogger(GoogleTokenProvider.class);

    private static final String GOOGLE_CLIENT_ID = "451544914380-m657ri1nr9i2b1qeq8jb8p3o3bl1o8b0.apps.googleusercontent.com";

//    public String createToken(String code, String state) {
//        return null;
//    }

    public MemberSec getSubject(String token) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                .build();

        GoogleIdToken.Payload payload = verifier.verify(token).getPayload();
        if (payload.getEmailVerified()) {
            return new MemberSec(null, payload.getEmail(), (String) payload.get("name"), payload.getEmail(),
                    null, null, null, (String) payload.get("picture"));
        } else {
            return null;
        }
    }

}
