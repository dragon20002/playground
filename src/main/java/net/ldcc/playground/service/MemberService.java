package net.ldcc.playground.service;

import net.ldcc.playground.dao.MemberDao;
import net.ldcc.playground.model.Member;
import net.ldcc.playground.model.MemberSec;
import net.ldcc.playground.util.GithubTokenProvider;
import net.ldcc.playground.util.GoogleTokenProvider;
import net.ldcc.playground.util.JwtTokenProvider;
import net.ldcc.playground.util.KakaoTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

@Service
public class MemberService {
    private final Logger logger = LoggerFactory.getLogger(MemberService.class);

    private final MemberDao memberDao;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleTokenProvider googleTokenProvider;
    private final GithubTokenProvider githubTokenProvider;
    private final KakaoTokenProvider kakaoTokenProvider;

    public MemberService(MemberDao memberDao, BCryptPasswordEncoder bCryptPasswordEncoder,
                         JwtTokenProvider jwtTokenProvider,
                         GoogleTokenProvider googleTokenProvider,
                         GithubTokenProvider githubTokenProvider,
                         KakaoTokenProvider kakaoTokenProvider) {
        this.memberDao = memberDao;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.googleTokenProvider = googleTokenProvider;
        this.githubTokenProvider = githubTokenProvider;
        this.kakaoTokenProvider = kakaoTokenProvider;
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

    public MemberSec getLoginUserInfo(String loginType, String jws) throws GeneralSecurityException, IOException {
        return switch (loginType) {
            case "google" -> googleTokenProvider.getSubject(jws);
            case "github" -> githubTokenProvider.getSubject(jws);
            case "kakao" -> kakaoTokenProvider.getSubject(jws);
            default -> {
                Long memberId = jwtTokenProvider.getSubject(jws);
                yield (memberId != null) ? this.getMemberSec(memberId) : null;
            }
        };
    }

    public String doLogin(Member member) {
        String userId = member.getUserId();
        String password = member.getPassword();

        List<Member> memberList = memberDao.findAllByUserId(userId);
        return memberList.stream()
                .filter(Member::isAccountNonExpired)
                .findFirst()
                .filter(m -> bCryptPasswordEncoder.matches(password, m.getPassword()))
                .map(m -> jwtTokenProvider.createToken(m.getId()))
                .orElseThrow(() -> new BadCredentialsException("Invalid Password for " + userId));
    }

    public String doLogin(Map<String, Object> loginParams) {
        String loginType = (String) loginParams.get("loginType");

        return switch (loginType) {
//            case "google" -> null;
            case "github" -> githubTokenProvider.createToken((String) loginParams.get("code"), (String) loginParams.get("state"));
            case "kakao" -> kakaoTokenProvider.createToken((String) loginParams.get("code"), (String) loginParams.get("state"),
                    (String) loginParams.get("redirectUri"));
            default -> null;
        };
    }

}
