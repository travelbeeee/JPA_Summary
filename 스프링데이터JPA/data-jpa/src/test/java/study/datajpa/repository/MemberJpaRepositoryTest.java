package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class MemberJpaRepositoryTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void saveTest() throws Exception{
        //given
        Member member = new Member("memberA");
        memberJpaRepository.save(member);

        //when
        Member findMember = memberJpaRepository.find(member.getId());


        //then
        assertThat(member).isEqualTo(findMember);
        assertThat(member.getId()).isEqualTo(findMember.getId());
        assertThat(member.getUsername()).isEqualTo(findMember.getUsername());
    }

    @Test
    public void basicCRUD() throws Exception{
        //given
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
        Member findMember2 = memberJpaRepository.findById(member2.getId()).get();
        assertThat(member1).isEqualTo(findMember1);
        assertThat(member2).isEqualTo(findMember2);

        List<Member> all = memberJpaRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        Long count = memberJpaRepository.count();
        assertThat(count).isEqualTo(2L);
    }

    @Test
    public void test() throws Exception{
        //given
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member1", 20);
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);
        //when
        List<Member> res = memberJpaRepository.findByUsernameAndAgeGreaterThan("member1", 15);

        //then
        assertThat(res.size()).isEqualTo(1);
        assertThat(res.get(0).getUsername()).isEqualTo("member1");
        assertThat(res.get(0).getAge()).isEqualTo(20);
    }

    @Test
    public void paging() throws Exception{
        //given
        memberJpaRepository.save(new Member("member1", 10));
        memberJpaRepository.save(new Member("member2", 10));
        memberJpaRepository.save(new Member("member3", 10));
        memberJpaRepository.save(new Member("member4", 10));
        memberJpaRepository.save(new Member("member5", 10));
        memberJpaRepository.save(new Member("member6", 10));
        memberJpaRepository.save(new Member("member7", 10));
        memberJpaRepository.save(new Member("member8", 10));
        memberJpaRepository.save(new Member("member9", 10));

        //when
        List<Member> members = memberJpaRepository.findByPage(10, 0, 3);
        for (Member member : members) {
            System.out.println(member);
        }
        long totalCount = memberJpaRepository.totalCount(10);
        System.out.println("totalCount : " + totalCount);

    }
}