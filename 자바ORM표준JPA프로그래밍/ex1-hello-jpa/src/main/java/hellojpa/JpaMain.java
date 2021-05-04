package hellojpa;

import org.hibernate.Hibernate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();

        tx.begin();

        try {
            //회원 저장
            Member member = new Member();
            member.setName("member1");
            em.persist(member);

            em.flush();
            em.clear();

            Member refMember = em.getReference(Member.class, member.getId());
            System.out.println("refMember.getName() 실행 전");
            System.out.println(emf.getPersistenceUnitUtil().isLoaded(refMember));

            Hibernate.initialize(refMember);

            System.out.println("refMember.getName() 실행 후");
            System.out.println(emf.getPersistenceUnitUtil().isLoaded(refMember));

            tx.commit();
        }
        catch (Exception e){
                tx.rollback();
                e.printStackTrace();
        }finally{
            em.close();
        }
        emf.close();
    }
}
