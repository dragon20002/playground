package net.ldcc.playground.service;

import net.ldcc.playground.model.Member;
import net.ldcc.playground.repo.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaseUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public BaseUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<Member> memberList = memberRepository.findAllByUserId(username);
        return memberList.stream()
                .filter(Member::isAccountNonExpired)
                .findFirst()
                .orElse(null);
    }

}
