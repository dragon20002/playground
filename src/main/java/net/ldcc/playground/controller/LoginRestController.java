package net.ldcc.playground.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import net.ldcc.playground.model.Member;
import net.ldcc.playground.service.MemberService;

@RestController
public class LoginRestController {
    private static final Logger logger = LoggerFactory.getLogger(LoginRestController.class);

    @Autowired
    private MemberService memberService;

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
    public ResponseEntity<Map<String, Object>> hasAuth(HttpServletRequest request) {
    	String jws = request.getHeader("jws");
    	boolean hasAuth = jws != null && memberService.hasAuth(jws);

    	Map<String, Object> response = new HashMap<>();
    	response.put("jws", jws);
    	response.put("hasAuth", hasAuth);

    	return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/api/login")
    public ResponseEntity<Map<String, Object>> doLogin(HttpServletRequest request, @RequestBody Member member) {
    	String jws = memberService.doLogin(member);
    	boolean hasAuth = jws != null;

    	Map<String, Object> response = new HashMap<>();
    	response.put("jws", jws);
    	response.put("hasAuth", hasAuth);

    	return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
