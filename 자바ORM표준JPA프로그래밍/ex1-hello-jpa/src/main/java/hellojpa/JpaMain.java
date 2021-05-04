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
            //팀 저장
            Team team = new Team();
            team.setName("team1");

            //회원 저장
            Member member = new Member();
            member.setName("member1");
            member.setTeam(team);

            em.persist(member);
            em.persist(team);

            //회원 저장
//            Member member2 = new Member();
//            member2.setName("member2");
//            member2.setTeam(team);
//
//            em.persist(member2);

            em.flush();
            em.clear();

            System.out.println("===========em.find Before================");
            Member findMember = em.find(Member.class, member.getId());
            System.out.println("===========em.find After================");
            System.out.println("Member.team = " + findMember.getTeam().getName());

//            System.out.println("===========em.find Before================");
//            Team findTeam = em.find(Team.class, team.getId());
//            System.out.println("===========em.find After================");
//            for (Member m : findTeam.getMembers()) {
//                System.out.println("Member = " + m.getName());
//            }
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
