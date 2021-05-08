package net.ldcc.playground.util;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import net.ldcc.playground.model.MemberSec;
import net.ldcc.playground.model.Role;

@Component
public class GithubTokenProvider extends OAuthTokenProvider {
	private final Logger logger = LoggerFactory.getLogger(GithubTokenProvider.class);

	private static final Pattern accessTokenPtn = Pattern.compile("access_token=(?<accessToken>[^&/]+)",
			Pattern.MULTILINE);

	public GithubTokenProvider(RestTemplate restTemplate, @Value("${spring.security.oauth.client.registration.github.client-id}") String clientId,
			@Value("${spring.security.oauth.client.registration.github.client-secret}") String clientSecret) {
		super(restTemplate, clientId, clientSecret);
	}

	@Override
	public String createToken(String code, String state, String redirectUri) {
		MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		header.add("Accept", "*/*");

		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromHttpUrl("https://github.com/login/oauth/access_token").queryParam("client_id", clientId)
				.queryParam("client_secret", clientSecret).queryParam("code", code).queryParam("state", state);

		String response = restTemplate
				.exchange(uriBuilder.toUriString(), HttpMethod.POST, new HttpEntity<>(header), String.class).getBody();

		if (response != null) {
			Matcher matcher = accessTokenPtn.matcher(response);
			if (matcher.find()) {
				return matcher.group("accessToken");
			}
		}
		return null;
	}

	@Override
	public MemberSec getSubject(String token) {
		MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		header.add("Authorization", String.format("token %s", token));
		header.add("Accept", "*/*");

		GithubVertifyResponse response = restTemplate.exchange("https://api.github.com/user", HttpMethod.GET,
				new HttpEntity<>(header), GithubVertifyResponse.class).getBody();

		if (response != null) {
			List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(Role.USER.name()));
			return new MemberSec(null, response.login, "github", response.name, response.email, null, null, null,
					response.avatarUrl, authorities);
		} else {
			return null;
		}
	}

	@Override
	public MemberSec getSubject(Map<String, Object> attributes) {
		String login = (String) attributes.getOrDefault("login", "");
		String name = (String) attributes.getOrDefault("name", "");
		String email = (String) attributes.getOrDefault("email", "");
		String avatarUrl = (String) attributes.getOrDefault("avatarUrl", "");
		List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(Role.USER.name()));
		return new MemberSec(null, login, "github", name, email, null, null, null,
				avatarUrl, authorities);
	}

	static class GithubVertifyResponse {
		public String login;
		public Long id;
		public String nodeId;
		public String avatarUrl;
		public String gravatarId;
		public String url;
		public String htmlUrl;
		public String followersUrl;
		public String followingUrl;
		public String gistsUrl;
		public String starredUrl;
		public String subscriptionsUrl;
		public String organizationsUrl;
		public String reposUrl;
		public String eventsUrl;
		public String receivedEventsUrl;
		public String type;
		public Boolean siteAdmin;
		public String name;
		public String company;
		public String blog;
		public String location;
		public String email;
		public Boolean hireable;
		public String bio;
		public String twitterUsername;
		public Integer publicRepos;
		public Integer publicGists;
		public Integer followers;
		public Integer following;
		public String createdAt;
		public String updatedAt;
	}

}
