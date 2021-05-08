package net.ldcc.playground.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import net.ldcc.playground.model.MemberSec;
import net.ldcc.playground.model.Role;

@Component
public class GoogleTokenProvider extends OAuthTokenProvider {
	private final Logger logger = LoggerFactory.getLogger(GoogleTokenProvider.class);

	public GoogleTokenProvider(RestTemplate restTemplate, @Value("${spring.security.oauth.client.registration.google.client-id}") String clientId,
			@Value("${spring.security.oauth.client.registration.google.client-secret}") String clientSecret) {
		super(restTemplate, clientId, clientSecret);
	}

	@Override
	public String createToken(String code, String state, String redirectUri) {
		MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		header.add("Accept", "application/json");

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl("https://oauth2.googleapis.com/token")
				.queryParam("grant_type", "authorization_code").queryParam("client_id", clientId)
				.queryParam("client_secret", clientSecret).queryParam("code", code).queryParam("state", state)
				.queryParam("redirect_uri", redirectUri);

		GoogleTokenIssueResponse response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.POST,
				new HttpEntity<>(header), GoogleTokenIssueResponse.class).getBody();

		logger.debug("oauth/token response = {}", response);

		return (response != null) ? response.idToken : null;
	}

	@Override
	public MemberSec getSubject(String token) throws GeneralSecurityException, IOException {
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
				.setAudience(Collections.singletonList(clientId)).build();

		GoogleIdToken.Payload payload = verifier.verify(token).getPayload();

		if (payload.getEmailVerified()) {
			List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(Role.USER.name()));
			return new MemberSec(null, payload.getEmail(), "google", (String) payload.get("name"), payload.getEmail(), null, null,
					null, (String) payload.get("picture"), authorities);
		} else {
			return null;
		}
	}

	@Override
	public MemberSec getSubject(Map<String, Object> attributes) {
		Boolean emailVerified = (Boolean) attributes.getOrDefault("email_verified", false);
		if (emailVerified) {
			String email = (String) attributes.getOrDefault("email", "");
			String name = (String) attributes.getOrDefault("name", "");
			String picture = (String) attributes.getOrDefault("picture", "");
			List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(Role.USER.name()));
			return new MemberSec(null, email, "google", name, email, null, null, null, picture, authorities);
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
