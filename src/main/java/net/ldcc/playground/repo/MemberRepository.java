package net.ldcc.playground.repo;

import net.ldcc.playground.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT new net.ldcc.playground.model.Member(m.id, m.userId, m.name, m.email, m.telNo, m.address, m.exprDate) FROM Member m WHERE m.id = :id")
    public Member getOneSec(@Param("id") Long id);

    @Query("SELECT new net.ldcc.playground.model.Member(m.id, m.userId, m.name, m.email, m.telNo, m.address, m.exprDate) FROM Member m")
    public List<Member> findAllSec();

    @Query
    public List<Member> findAllByUserId(String userId);
}
