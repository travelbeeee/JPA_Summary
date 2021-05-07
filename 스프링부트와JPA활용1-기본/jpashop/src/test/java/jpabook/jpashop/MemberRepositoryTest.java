package jpabook.jpashop;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @Transactional
    public void save() throws Exception{
        Member member = new Member();
        member.setUsername("memberA");

        //when
        memberRepository.save(member);
        Member findMember = memberRepository.find(member.getId());

        System.out.println((member == findMember));
        //then
        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());

    }
}