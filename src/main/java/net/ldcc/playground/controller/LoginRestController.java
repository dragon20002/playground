package net.ldcc.playground.controller;

import net.ldcc.playground.model.Member;
import net.ldcc.playground.model.MemberSec;
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
        String jws = request.getHeader("token");
    	boolean hasAuth = jws != null;

    	Map<String, Object> model = new HashMap<>();
    	if (hasAuth) {
    	    MemberSec member = memberService.getLoginUserInfo(loginType, jws);
    	    hasAuth = member != null;
    	    if (hasAuth) {
                model.put("loginType", loginType);
                model.put("token", jws);
                model.put("imageUrl", member.getImageUrl());
                model.put("name", member.getName());
            }
        }
    	model.put("hasAuth", hasAuth);

    	return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @PostMapping("/api/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Member member) throws GeneralSecurityException, IOException {
        String jws = memberService.doLogin(member);
        MemberSec loginUserInfo = memberService.getLoginUserInfo("", jws);

        Map<String, Object> model = new HashMap<>();
        if (loginUserInfo != null) {
            model.put("token", jws);
            model.put("hasAuth", true);
            model.put("imageUrl", loginUserInfo.getImageUrl());
            model.put("name", loginUserInfo.getName());
            model.put("loginType", "");
        } else {
            model.put("hasAuth", false);
        }

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @PostMapping("/api/login/oauth")
    public ResponseEntity<Map<String, Object>> oauthLogin(@RequestBody Map<String, Object> loginParams) throws GeneralSecurityException, IOException {
        String loginType = (String) loginParams.get("loginType");
        String token = memberService.doLogin(loginParams);
        MemberSec loginUserInfo = memberService.getLoginUserInfo(loginType, token);

        Map<String, Object> model = new HashMap<>();
        if (loginUserInfo != null) {
            model.put("token", token);
            model.put("hasAuth", true);
            model.put("imageUrl", loginUserInfo.getImageUrl());
            model.put("name", loginUserInfo.getName());
            model.put("loginType", loginType);
        } else {
            model.put("hasAuth", false);
        }

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @GetMapping("/api/login/get-user-info")
    public ResponseEntity<MemberSec> getUserInfo(HttpServletRequest request) throws GeneralSecurityException, IOException {
        String loginType = request.getHeader("loginType");
        String jws = request.getHeader("token");
        boolean hasAuth = jws != null;

        if (hasAuth) {
            MemberSec member;
            member = memberService.getLoginUserInfo(loginType, jws);
            return new ResponseEntity<>(member, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

}
