package net.ldcc.playground.config.auth;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import net.ldcc.playground.service.MemberService;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final MemberService memberService;

	public CustomOAuth2UserService(MemberService memberService) {
		this.memberService = memberService;
	}

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		// 현재 로그인 진행 중인 서비스 구분 코드 (google, github, kakao, ...)
		String registrationId = userRequest.getClientRegistration().getRegistrationId();

		// OAuth2 계정 키값 (google: "sub", ...)
		String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
				.getUserNameAttributeName();

		// 속성값
		Map<String, Object> attributes = oAuth2User.getAttributes();

		return memberService.getOAuth2User(registrationId, userNameAttributeName, attributes);
	}

}
