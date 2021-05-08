package net.ldcc.playground.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "MEMBER")
public class Member implements UserDetails {
    private static final Logger logger = LoggerFactory.getLogger(Member.class);

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MEMBER_ID")
    @Id
    private Long id;

    @Column(name = "USER_ID", unique = true)
    private String userId;

    @Column
    private String password;

    @Column
    private String loginType;
    
    @Column
    private String name;

    @Column(unique = true)
    private String email;

    @Column(name = "TEL_NO")
    private String telNo;

    @Column
    private String address;

    @Column(name = "EXPR_DATE")
    private String exprDate;

    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    @JoinColumn(name = "MEMBER_ID")
    private List<BaseGrantedAuthority> authorities;

    public Member() {}

    public Member(Long id, String userId, String loginType, String name, String email, String telNo, String address, String exprDate) {
        this.id = id;
        this.userId = userId;
        this.loginType = loginType;
        this.name = name;
        this.email = email;
        this.telNo = telNo;
        this.address = address;
        this.exprDate = exprDate;
    }

    public void update(String name, String email, String telNo, String address, String exprDate) {
        this.name = name;
        this.email = email;
        this.telNo = telNo;
        this.address = address;
        this.exprDate = exprDate;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String getUsername() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLoginType() {
    	return loginType;
    }
    
    public void setLoginType(String loginType) {
    	this.loginType = loginType;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelNo() {
        return telNo;
    }

    public void setTelNo(String telNo) {
        this.telNo = telNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getExprDate() {
        return exprDate;
    }

    public void setExprDate(String exprDate) {
        this.exprDate = exprDate;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<BaseGrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public boolean isAccountNonExpired() {
        // 1. 만료일자가 빈 경우, 유효
        if (exprDate == null || exprDate.length() == 0)
            return true;

        // 2. 만료일자 이전인 경우, 유효
        try {
            return DateFormat.getDateInstance().parse(exprDate).before(new Date());
        } catch (ParseException e) {
            logger.debug(e.getMessage());
        }

        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isAccountNonExpired();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isAccountNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return isAccountNonExpired();
    }

    @Override
    public String toString() {
        return "Member{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", telNo='" + telNo + '\'' +
                ", address='" + address + '\'' +
                ", exprDate='" + exprDate + '\'' +
                '}';
    }
}
