package net.ldcc.playground.model;

import javax.persistence.*;

@Entity(name = "GRANTEDAUTH")
public class BaseGrantedAuthority {
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GRANTEDAUTH_ID")
    @Id
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Column
    private String authority;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
