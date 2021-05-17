package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false)
public class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @Test
    void testCRUD(){
        //given
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(member1).isEqualTo(findMember1);
        assertThat(member2).isEqualTo(findMember2);

        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        Long count = memberRepository.count();
        assertThat(count).isEqualTo(2L);
    }


    @Test
    public void test() throws Exception{
        //given
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member1", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);
        //when
        List<Member> res = memberRepository.findByUsernameAndAgeGreaterThan("member1", 15);

        //then
        assertThat(res.size()).isEqualTo(1);
        assertThat(res.get(0).getUsername()).isEqualTo("member1");
        assertThat(res.get(0).getAge()).isEqualTo(20);
    }

    @Test
    public void DTOtest() throws Exception{
        //given
        Team teamA = new Team("teamA");
        teamRepository.save(teamA);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        memberRepository.save(member1);
        memberRepository.save(member2);

        //when
        List<MemberDto> memberDto = memberRepository.findMemberDto();

        //then
        for (MemberDto dto : memberDto) {
            System.out.println("DTO : " + dto);
        }
    }

    @Test
    public void findByNames테스트() throws Exception{
        //given
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member1", 20);
        Member member3 = new Member("member2", 20);
        Member member4 = new Member("member3", 20);

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);

        //when
        List<Member> members = memberRepository.findByNames(Arrays.asList("member1", "member2"));
        for (Member member : members) {
            System.out.println(member);
        }
    }

    @Test
    public void paging() throws Exception{
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        memberRepository.save(new Member("member6", 10));
        memberRepository.save(new Member("member7", 10));
        memberRepository.save(new Member("member8", 10));
        memberRepository.save(new Member("member9", 10));

        //when
        PageRequest pageRequest = PageRequest.of(1, 3, Sort.by(Sort.Direction.DESC, "username"));
        Slice<Member> pages = memberRepository.findByAge(10, pageRequest);

        System.out.println("Pages hasNext? : " + pages.hasNext());
        System.out.println("PageNumber : " + pages.getNumber());

        List<Member> members = pages.getContent();
        for (Member m : members) {
            System.out.println(m);
        }
    }

}