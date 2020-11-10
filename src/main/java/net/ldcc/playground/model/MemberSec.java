package net.ldcc.playground.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class MemberSec {
    private static final Logger logger = LoggerFactory.getLogger(MemberSec.class);

    private Long id;
    private String userId;
    private String name;
    private String email;
    private String telNo;
    private String address;
    private String exprDate;
    private String imageUrl;

    public MemberSec() {}

    public MemberSec(Long id, String userId, String name, String email, String telNo, String address, String exprDate,
                     String imageUrl) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.telNo = telNo;
        this.address = address;
        this.exprDate = exprDate;
        this.imageUrl = imageUrl;
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

    public String getUsername() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public boolean isAccountNonLocked() {
        return isAccountNonExpired();
    }

    public boolean isCredentialsNonExpired() {
        return isAccountNonExpired();
    }

    public boolean isEnabled() {
        return isAccountNonExpired();
    }

    @Override
    public String toString() {
        return "MemberSec{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", telNo='" + telNo + '\'' +
                ", address='" + address + '\'' +
                ", exprDate='" + exprDate + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
