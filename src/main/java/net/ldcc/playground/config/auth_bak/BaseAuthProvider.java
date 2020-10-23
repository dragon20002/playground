package net.ldcc.playground.config.auth_bak;

import net.ldcc.playground.model.Member;
import net.ldcc.playground.repo.member.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

public class BaseAuthProvider implements AuthenticationProvider {
    private final Logger logger = LoggerFactory.getLogger(BaseAuthProvider.class);

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userId = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        List<Member> memberList = memberRepository.findAllByUserId(userId);
        Member member = memberList.stream()
                .filter(Member::isAccountNonExpired)
                .findFirst()
                .filter(m -> bCryptPasswordEncoder.matches(password, m.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("Invalid Password for " + userId));

        return new UsernamePasswordAuthenticationToken(member, password, member.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
