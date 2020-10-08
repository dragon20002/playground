package net.ldcc.playground.service;

import net.ldcc.playground.model.Member;
import net.ldcc.playground.repo.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

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
}
