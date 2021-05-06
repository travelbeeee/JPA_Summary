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
            Member member1 = new Member();
            member1.getDeliveryAddress().add(new Address("City1", "Street1", "zipCode1"));
            member1.getDeliveryAddress().add(new Address("City2", "Street2", "zipCode2"));
            member1.getDeliveryAddress().add(new Address("City3", "Street3", "zipCode3"));
            em.persist(member1);

            em.flush();
            em.clear();

            System.out.println("========================");
            Member findMember = em.find(Member.class, member1.getId());
            findMember.getDeliveryAddress().remove(new Address("City1", "Street1", "zipCode1"));
            findMember.getDeliveryAddress().add(new Address("newCity1", "Street1", "Zipcode1"));

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
