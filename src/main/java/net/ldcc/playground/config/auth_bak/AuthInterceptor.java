package net.ldcc.playground.config.auth_bak;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import net.ldcc.playground.util.JwtTokenProvider;

public class AuthInterceptor extends HandlerInterceptorAdapter {
	private final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

	private final JwtTokenProvider jwtTokenProvider;

	public AuthInterceptor(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String jws = request.getHeader("jws");
		String sub = jwtTokenProvider.getSubject(jws);

		if (sub == null) {
			response.sendRedirect("/error/unauthorized");
			return false;
		}

		return true;
	}

}
