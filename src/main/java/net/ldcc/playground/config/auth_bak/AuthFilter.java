package net.ldcc.playground.config.auth_bak;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ldcc.playground.model.Member;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.InputMismatchException;

public class AuthFilter extends UsernamePasswordAuthenticationFilter {

    public AuthFilter(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        UsernamePasswordAuthenticationToken authRequest;
        try {
            Member member = new ObjectMapper().readValue(request.getInputStream(), Member.class);
            authRequest = new UsernamePasswordAuthenticationToken(member.getUserId(), member.getPassword());
        } catch (IOException e) {
            throw new InputMismatchException();
        }

        setDetails(request, authRequest);
        return getAuthenticationManager().authenticate(authRequest);
    }
}
