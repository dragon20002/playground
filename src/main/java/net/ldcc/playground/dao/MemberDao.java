package net.ldcc.playground.dao;

import net.ldcc.playground.model.Member;
import net.ldcc.playground.model.MemberSec;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class MemberDao extends BaseDao {

    public MemberDao(ApplicationContext context, JdbcTemplate jdbcTemplate) {
        super(context, jdbcTemplate);
    }

    public Member findById(Long id) {
        String sql = "SELECT * FROM MEMBER WHERE ID = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new MemberRowMapper());
    }

    public MemberSec findByIdSec(Long id) {
        String sql = "SELECT ID, USER_ID, NAME, TEL_NO, EMAIL, ADDRESS, EXPR_DATE FROM MEMBER WHERE ID = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new MemberSecRowMapper());
    }

    public List<MemberSec> findAllSec() {
        String sql = "SELECT ID, USER_ID, NAME, TEL_NO, EMAIL, ADDRESS, EXPR_DATE FROM MEMBER";
        return jdbcTemplate.query(sql, new Object[]{}, new MemberSecRowMapper());
    }

    public List<Member> findAllByUserId(String userId) {
        String sql = "SELECT * FROM MEMBER WHERE USER_ID = ?";
        return jdbcTemplate.query(sql, new Object[]{userId}, new MemberRowMapper());
    }

    public void save(Member member) {
        String sql = "INSERT INTO MEMBER(ID, USER_ID, PASSWORD, NAME, EMAIL, TEL_NO, ADDRESS, EXPR_DATE)" +
                " VALUES((SELECT SEQ_MEMBER_ID.NEXTVAL FROM DUAL), ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, member.getUserId(), member.getPassword(), member.getName(), member.getEmail(),
                member.getTelNo(), member.getAddress(), member.getExprDate());
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM MEMBER WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public static class MemberRowMapper implements RowMapper<Member> {

        @Override
        public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
            Member member = new Member();
            member.setId(rs.getLong("ID"));
            member.setUserId(rs.getString("USER_ID"));
            member.setPassword(rs.getString("PASSWORD"));
            member.setName(rs.getString("NAME"));
            member.setEmail(rs.getString("EMAIL"));
            member.setTelNo(rs.getString("TEL_NO"));
            member.setAddress(rs.getString("ADDRESS"));
            member.setExprDate(rs.getString("EXPR_DATE"));
            return member;
        }
    }

    public static class MemberSecRowMapper implements RowMapper<MemberSec> {

        @Override
        public MemberSec mapRow(ResultSet rs, int rowNum) throws SQLException {
            MemberSec memberSec = new MemberSec();
            memberSec.setId(rs.getLong("ID"));
            memberSec.setUserId(rs.getString("USER_ID"));
            memberSec.setName(rs.getString("NAME"));
            memberSec.setEmail(rs.getString("EMAIL"));
            memberSec.setTelNo(rs.getString("TEL_NO"));
            memberSec.setAddress(rs.getString("ADDRESS"));
            memberSec.setExprDate(rs.getString("EXPR_DATE"));
            return memberSec;
        }
    }
}
