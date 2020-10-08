package net.ldcc.playground.controller;

import net.ldcc.playground.model.Member;
import net.ldcc.playground.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class LoginRestController {
    private static final Logger logger = LoggerFactory.getLogger(LoginRestController.class);

    private final MemberService memberService;

    public LoginRestController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/api/login/check-member-dup")
    public ResponseEntity<Boolean> checkMemberDup(@RequestBody Member member) {
        List<Member> memberList = memberService.getMembersByUserId(member.getUserId());
        Boolean isDup = memberList.stream().anyMatch(Member::isAccountNonExpired);
        return new ResponseEntity<>(isDup, HttpStatus.OK);
    }

    @PostMapping("/api/login/create-member")
    public ResponseEntity<Boolean> createMember(@RequestBody Member member) {
        List<Member> memberList = memberService.getMembersByUserId(member.getUserId());
        if (memberList.stream().anyMatch(Member::isAccountNonExpired))
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        try {
            memberService.postMember(member);
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @GetMapping("/api/login/has-auth")
    public ResponseEntity<Boolean> hasAuth(HttpServletRequest request) {
        String userId = (String) request.getSession().getAttribute("userId");
        Boolean hasAuth = userId != null && userId.length() > 0;
        return new ResponseEntity<>(hasAuth, HttpStatus.OK);
    }
}
