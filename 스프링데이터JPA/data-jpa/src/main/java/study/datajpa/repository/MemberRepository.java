package study.datajpa.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.List;x

public interface MemberRepository extends JpaRepository<Member, Integer> {
//    List<Member> findByUsername(String username);

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);

    /**
     * NamedQuery는 엔티티에 사용해야되서 @Query 를 더 많이사용함.
     * @Query를 이용하는 것도 이름이 없는 NamedQuery와 동일
     * 애플리케이션 가동 시점에 오타 등을 잡아줌!! ( 굉장히 좋은 기능 )
     */
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    Slice<Member> findByAge(int age, Pageable pageable);

    @Modifying(clearAutomatically = true) // @Modifying 애노테이션이있어야 JPA .executeUpdate 처럼 Update가 제대로 동작한다.
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);
}
