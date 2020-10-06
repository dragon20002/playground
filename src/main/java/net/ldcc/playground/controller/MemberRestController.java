package net.ldcc.playground.controller;

import net.ldcc.playground.model.Member;
import net.ldcc.playground.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MemberRestController {
    private final Logger logger = LoggerFactory.getLogger(MemberRestController.class);

    private final MemberService memberService;

    public MemberRestController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/api/members/{id}")
    public ResponseEntity<Member> getMember(@PathVariable String id) {
        Member member;
        try {
            member = memberService.getMember(id);
        } catch (Exception e) {
            logger.debug("Fail to getMember id={}", id);
            logger.debug(e.getMessage());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(member, HttpStatus.OK);
    }

    @GetMapping("/api/members")
    public ResponseEntity<List<Member>> getMembers() {
        List<Member> memberList = memberService.getMembers();

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

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/api/members/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable String id) {
        try {
            memberService.deleteMember(id);
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
