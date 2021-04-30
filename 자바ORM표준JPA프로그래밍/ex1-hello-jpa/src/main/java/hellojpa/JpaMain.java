package hellojpa;

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
            Member member2 = new Member();
            member2.setId(2L);
            member2.setName("member2");

            em.persist(member2);

            tx.commit();
        }
        catch (Exception e){
                tx.rollback();
        }finally{
            em.close();
        }
        emf.close();
    }
}
