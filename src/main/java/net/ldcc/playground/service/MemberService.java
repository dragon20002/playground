package net.ldcc.playground.service;

import net.ldcc.playground.model.Member;
import net.ldcc.playground.repo.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    @Autowired
    MemberRepository memberRepository;

    public Member getMember(String id) {
        return memberRepository.getOne(id);
    }

    public List<Member> getMembers() {
        return memberRepository.findAll();
    }

    public void postMember(Member member) {
        memberRepository.saveAndFlush(member);
    }

    public void deleteMember(String id) {
        memberRepository.deleteById(id);
    }
}
