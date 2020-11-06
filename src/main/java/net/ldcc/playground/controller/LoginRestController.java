package net.ldcc.playground.controller;

import net.ldcc.playground.model.Member;
import net.ldcc.playground.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> hasAuth(HttpServletRequest request) throws GeneralSecurityException, IOException {
    	String loginType = request.getHeader("loginType");
        String jws = request.getHeader("jws");
    	boolean hasAuth = jws != null;

    	Map<String, Object> model = new HashMap<>();
    	if (hasAuth) {
    	    String username = memberService.getSubject(loginType, jws);
    	    hasAuth = username != null;
    	    if (hasAuth) {
                model.put("loginType", loginType);
                model.put("jws", jws);
                model.put("username", username);
            }
        }
    	model.put("hasAuth", hasAuth);

    	return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @PostMapping("/api/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Member member) throws GeneralSecurityException, IOException {
        String jws = memberService.doLogin(member);
        boolean hasAuth = jws != null;
        String username = memberService.getSubject("", jws);

        Map<String, Object> model = new HashMap<>();
        model.put("jws", jws);
        model.put("hasAuth", hasAuth);
        model.put("username", username);

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @PostMapping("/api/login/oauth")
    public ResponseEntity<Map<String, Object>> oauthLogin(@RequestBody Map<String, Object> loginParams) throws GeneralSecurityException, IOException {
        String token = memberService.doLogin(loginParams);
        boolean hasAuth = token != null;
        String username = memberService.getSubject((String) loginParams.get("loginType"), token);

        Map<String, Object> model = new HashMap<>();
        model.put("token", token);
        model.put("hasAuth", hasAuth);
        model.put("username", username);

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

}
