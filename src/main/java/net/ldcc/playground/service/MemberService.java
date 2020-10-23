package net.ldcc.playground.service;

import net.ldcc.playground.annotation.DbType;
import net.ldcc.playground.dao.MemberDao;
import net.ldcc.playground.model.Member;
import net.ldcc.playground.model.MemberSec;
import net.ldcc.playground.util.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    private final Logger logger = LoggerFactory.getLogger(MemberService.class);

    private final MemberDao memberDao;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public MemberService(MemberDao memberDao, JwtTokenProvider jwtTokenProvider, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.memberDao = memberDao;
        this.jwtTokenProvider = jwtTokenProvider;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public Member getMember(Long id) {
        return memberDao.findById(id);
    }

    public MemberSec getMemberSec(Long id) {
        return memberDao.findByIdSec(id);
    }

    public List<MemberSec> getMembersSec() {
        return memberDao.findAllSec();
    }

    public List<Member> getMembersByUserId(String userId) {
        return memberDao.findAllByUserId(userId);
    }

    public void postMember(Member member) {
        member.setPassword(bCryptPasswordEncoder.encode(member.getPassword()));
        memberDao.save(member);
    }

    public void deleteMember(Long id) {
        memberDao.deleteById(id);
    }

    public boolean hasAuth(String jws) {
        String sub = jwtTokenProvider.getSubject(jws);
        return sub != null;
    }

    public String doLogin(Member member) {
        String userId = member.getUserId();
        String password = member.getPassword();

        List<Member> memberList = memberDao.findAllByUserId(userId);
        return memberList.stream()
                .filter(Member::isAccountNonExpired)
                .findFirst()
                .filter(m -> bCryptPasswordEncoder.matches(password, m.getPassword()))
                .map(m -> jwtTokenProvider.createToken(userId))
                .orElseThrow(() -> new BadCredentialsException("Invalid Password for " + userId));
    }

}
