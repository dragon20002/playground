package net.ldcc.playground.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;

@Entity(name = "GRANTEDAUTH")
@Table(name = "GRANTEDAUTH")
public class BaseGrantedAuthority implements GrantedAuthority {
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GRANTEDAUTH_ID")
    @Id
    private Long id;

    @Column(name = "MEMBER_ID")
    private Long memberId;

    @Column
    @Enumerated(EnumType.STRING)
    private Role role;

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

    @Override
    public String getAuthority() {
        return role.name();
    }

    public void setAuthority(String authority) {
        this.role = Role.valueOf(authority);
    }
}
