package net.ldcc.playground.config.auth_bak;

import net.ldcc.playground.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthInterceptor extends HandlerInterceptorAdapter {
	private final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

//	private final JwtTokenProvider jwtTokenProvider;
	MemberService memberService;

	public AuthInterceptor(MemberService memberService) {
		this.memberService = memberService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String loginType = request.getHeader("loginType");
		String jws = request.getHeader("jws");
		boolean hasAuth = memberService.getSubject(loginType, jws) != null;

		if (!hasAuth) {
			response.sendRedirect("/error/unauthorized");
			return false;
		}

		return true;
	}

}
