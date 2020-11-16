package net.ldcc.playground.config.auth_bak;

import net.ldcc.playground.model.Member;
import net.ldcc.playground.util.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Not using current
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final Logger logger = LoggerFactory.getLogger(LoginSuccessHandler.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        Member member = ((Member) authentication.getPrincipal());
        String token = jwtTokenProvider.createToken(String.valueOf(member.getId()));
        logger.debug("token={}", token);

        response.addHeader("token", token);
        response.setStatus(HttpServletResponse.SC_OK);
        response.sendRedirect("/api/login/doLogin");
    }

}
