package net.ldcc.playground.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import net.ldcc.playground.model.Member;
import net.ldcc.playground.service.MemberService;

@RestController
public class MemberRestController {
    private final Logger logger = LoggerFactory.getLogger(MemberRestController.class);

    @Autowired
    private MemberService memberService;

    @GetMapping("/api/members/{id}")
    public ResponseEntity<Member> getMember(@PathVariable Long id) {
        Member member;
        try {
            member = memberService.getMemberSec(id);
        } catch (Exception e) {
            logger.debug("Fail to getMember id={}", id);
            logger.debug(e.getMessage());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(member, HttpStatus.OK);
    }

    @GetMapping("/api/members")
    public ResponseEntity<List<Member>> getMembers() {
        List<Member> memberList = memberService.getMembersSec();

        return new ResponseEntity<>(memberList, HttpStatus.OK);
    }

    @PostMapping("/api/members")
    public ResponseEntity<Member> postMember(@RequestBody Member member) {
        try {
            memberService.postMember(member);
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/api/members/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        try {
            memberService.deleteMember(id);
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
