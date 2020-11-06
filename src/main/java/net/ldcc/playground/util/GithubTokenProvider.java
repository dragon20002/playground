package net.ldcc.playground.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GithubTokenProvider {
    private final Logger logger = LoggerFactory.getLogger(GithubTokenProvider.class);

    private static final String CLIENT_ID = "2c1347aac22bb89c84f3";
    private static final String CLIENT_SECRET = "584fdfa0482457ad624a13042e8cf9e3817644da";
    private static final Pattern accessTokenPtn = Pattern.compile("access_token=(?<accessToken>[^&/]+)", Pattern.MULTILINE);

    private final RestTemplate restTemplate;

    public GithubTokenProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String createToken(String code, String state) {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Accept", "*/*");

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl("https://github.com/login/oauth/access_token")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("client_secret", CLIENT_SECRET)
                .queryParam("code", code)
                .queryParam("state", state);

        String response = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.POST,
                new HttpEntity<>(header),
                String.class
        ).getBody();

        if (response != null) {
            Matcher matcher = accessTokenPtn.matcher(response);
            if (matcher.find()) {
                return matcher.group("accessToken");
            }
        }
        return null;
    }

    public String getSubject(String token) {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Authorization", String.format("token %s", token));
        header.add("Accept", "*/*");

        GithubVertifyResponse response = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                new HttpEntity<>(header),
                GithubVertifyResponse.class).getBody();

        return (response != null) ? response.login : null;
    }

    static class GithubVertifyResponse {
        public String login;
    }

}
