package net.ldcc.playground.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import net.ldcc.playground.model.Member;
import net.ldcc.playground.repo.MemberRepository;
import net.ldcc.playground.util.JwtTokenProvider;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Member getMember(Long id) {
        return memberRepository.getOne(id);
    }

    public Member getMemberSec(Long id) {
        return memberRepository.getOneSec(id);
    }

    public List<Member> getMembers() {
        return memberRepository.findAll();
    }

    public List<Member> getMembersSec() {
        return memberRepository.findAllSec();
    }

    public List<Member> getMembersByUserId(String userId) {
        return memberRepository.findAllByUserId(userId);
    }

    public void postMember(Member member) {
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        memberRepository.saveAndFlush(member);
    }

    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    /**
     * 인증 및 JWS 반환
     * 
     * @param member
     * @return 인증 성공 시 JWS, 인증 실패 시 null 반환
     */
    public String doLogin(Member member) {
        List<Member> memberList = memberRepository.findAllByUserId(member.getUserId());
        return memberList.stream()
                .filter(Member::isAccountNonExpired)
                .findFirst()
                .filter(m -> passwordEncoder.matches(member.getPassword(), m.getPassword()))
                .map(m -> jwtTokenProvider.createToken(m.getUserId()))
                .orElse(null);
    }

    public boolean hasAuth(String jws) {
    	String sub = jwtTokenProvider.getSubject(jws);
    	return sub != null;
    }

}
