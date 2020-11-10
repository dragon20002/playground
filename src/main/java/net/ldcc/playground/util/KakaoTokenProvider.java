package net.ldcc.playground.util;

import net.ldcc.playground.model.MemberSec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class KakaoTokenProvider {
    private final Logger logger = LoggerFactory.getLogger(KakaoTokenProvider.class);

    private static final String REST_API_KEY = "edb60a1796c20a7eab7c98b12c550998";
    private static final String CLIENT_SECRET = "ixKsmExJxEXAyJMQenZlz9DmdkVeLeSI";

    private final RestTemplate restTemplate;

    public KakaoTokenProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String createToken(String code, String state, String redirectUri) {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Accept", "application/json");

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl("https://kauth.kakao.com/oauth/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", REST_API_KEY)
                .queryParam("client_secret", CLIENT_SECRET)
                .queryParam("code", code)
                .queryParam("state", state)
                .queryParam("redirect_uri", redirectUri);

        KakaoTokenIssueResponse response = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.POST,
                new HttpEntity<>(header),
                KakaoTokenIssueResponse.class
        ).getBody();

        logger.debug("oauth/token response = {}", response);

        return (response != null) ? response.accessToken : null;
    }

    public MemberSec getSubject(String token) {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Authorization", String.format("Bearer %s", token));
        header.add("Accept", "application/json");

        // 인증확인 및 id 확인
        KakaoTokenInfoResponse tokenInfo = null;
        try {
            tokenInfo = restTemplate.exchange(
                    "https://kapi.kakao.com/v1/user/access_token_info",
                    HttpMethod.GET,
                    new HttpEntity<>(header),
                    KakaoTokenInfoResponse.class
            ).getBody();
        } catch (HttpClientErrorException e) {
            logger.debug(e.getMessage());
        }

        logger.debug("user/access_token_info = {}", tokenInfo);
        if (tokenInfo == null)
            return null; // 토큰 만료

        // 회원정보 조회
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl("https://kapi.kakao.com/v2/user/me")
                .queryParam("target_id_type", "user_id")
                .queryParam("target_id", tokenInfo.id);

        KakaoUserInfoResponse response = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                new HttpEntity<>(header),
                KakaoUserInfoResponse.class).getBody();

        if (response != null) {
            return new MemberSec(null, response.kakaoAccount.email, response.kakaoAccount.profile.nickname,
                    response.kakaoAccount.email, null, null, null,
                    response.kakaoAccount.profile.profileImageUrl);
        } else {
            return null;
        }
    }

    static class KakaoTokenIssueResponse {
        public String tokenType;
        public String accessToken;
        public Integer expiresIn;
        public String refreshToken;
        public Integer refreshTokenExpiresIn;
        public String scope;
    }

    static class KakaoTokenInfoResponse {
        public Long id;
        public Integer expiresIn;
        public Integer appId;
//        public Integer expiresInMillis;
    }

    static class KakaoUserInfoResponse {
        public String id;
        public KakaoAccount kakaoAccount;
        public Properties properties;

        static class KakaoAccount {
            boolean profileNeedsAgreement;
            public Profile profile;
            public boolean emailNeedsAgreement;
            public boolean isEmailValid;
            public boolean iEmailVerified;
            public String email;
            public boolean ageRangeNeedsAgreement;
            public String ageRange;
            public boolean birthday_needs_agreement;
            public String birthday;
            public boolean genderNeedAgreement;
            public String gender;

            static class Profile {
                public String nickname;
                public String thumnailImageUrl;
                public String profileImageUrl;
            }
        }

        static class Properties {
            public String nickname;
            public String thumbnailImage;
            public String profileImage;
        }
    }

}
