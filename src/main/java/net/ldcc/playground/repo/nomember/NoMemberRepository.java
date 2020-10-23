package net.ldcc.playground.repo.nomember;

import net.ldcc.playground.model.NoMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoMemberRepository extends JpaRepository<NoMember, Long> {

    @Query("SELECT new net.ldcc.playground.model.NoMember(m.id, m.userId, m.name, m.email, m.telNo, m.address, m.exprDate) FROM NoMember m WHERE m.id = :id")
    public NoMember getOneSec(@Param("id") Long id);

    @Query("SELECT new net.ldcc.playground.model.NoMember(m.id, m.userId, m.name, m.email, m.telNo, m.address, m.exprDate) FROM NoMember m")
    public List<NoMember> findAllSec();

    @Query
    public List<NoMember> findAllByUserId(String userId);
}
