package hellojpa;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();

        tx.begin();

        try {
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");
            Team teamC = new Team("teamC");

            Member member1 = new Member("member1", teamA);
            Member member2 = new Member("member2", teamA);
            Member member3 = new Member("member3", teamA);
            Member member4 = new Member("member4", teamB);
            Member member5 = new Member("member5", teamB);
            Member member6 = new Member("member6", teamC);
            Member member7 = new Member("member7", null);

            teamA.getMembers().add(member1);
            teamA.getMembers().add(member2);
            teamA.getMembers().add(member3);
            teamB.getMembers().add(member4);
            teamB.getMembers().add(member5);
            teamC.getMembers().add(member6);

            em.persist(teamA);
            em.persist(teamB);
            em.persist(teamC);
            em.persist(member1);
            em.persist(member2);
            em.persist(member3);
            em.persist(member4);
            em.persist(member5);
            em.persist(member6);
            em.persist(member7);

            em.flush();
            em.clear();

            List<Member> members = em.createQuery("select m from Member m join fetch m.team", Member.class)
                    .getResultList();

            System.out.println("=====================================");
            for (Member member : members) {
                System.out.println("member = " + member);
                System.out.println("member.team = " + member.getTeam());
            }

            em.clear();

            List<Team> teams = em.createQuery("select distinct(t) from Team t join fetch t.members", Team.class)
                    .getResultList();

            System.out.println("=====================================");
            for (Team team : teams) {
                System.out.println("team = " + team);
                for (Member m : team.getMembers()) {
                    System.out.println("m = " + m);
                }
            }



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
