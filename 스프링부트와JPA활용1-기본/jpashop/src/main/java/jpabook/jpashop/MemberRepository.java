package jpabook.jpashop;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class MemberRepository {

    @PersistenceContext
    EntityManager em;

    public int save(Member member){
        em.persist(member);
        return member.getId();
    }

    public Member find(int id) {
        return em.find(Member.class, id);
    }

}
