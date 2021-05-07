### JPA Join, FetchJoin 상황별 비교 정리

<br>

### 0) 엔티티 

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @ManyToOne(fetch = FetchType.XXX)
    @JoinColumn(name = "TEAM_ID")
    private Team team;
}	

@Entity
public class Team {
    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private int id;

    private String name;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    private List<Member> members = new ArrayList<>();
}
```

<img src="https://user-images.githubusercontent.com/59816811/117401770-25178c00-af40-11eb-8ab2-7e83ed850a94.png" alt="jpa_30" width="500" />

<br>

### 1) SELECT m FROM Member m ( EAGER )

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TEAM_ID")
    private Team team;
}	

List<Member> members = em.createQuery("select m from Member m", Member.class)
                    	.getResultList();
```

```sql
		select
            member0_.MEMBER_ID as member_i1_5_,
            member0_.name as name2_5_,
            member0_.TEAM_ID as team_id3_5_ 
        from
            Member member0_
            
        select
            team0_.TEAM_ID as team_id1_9_0_,
            team0_.name as name2_9_0_ 
        from
            Team team0_ 
        where
            team0_.TEAM_ID=?
        
        select
            team0_.TEAM_ID as team_id1_9_0_,
            team0_.name as name2_9_0_ 
        from
            Team team0_ 
        where
        team0_.TEAM_ID=?
        
        select
            team0_.TEAM_ID as team_id1_9_0_,
            team0_.name as name2_9_0_ 
        from
            Team team0_ 
        where
            team0_.TEAM_ID=?
```

Meber 엔티티를 모두 조회해달라고 했으므로 SQL 쿼리는 Member 엔티티를 모두 조회해야되므로  SELECT 쿼리를 1번 날리게 된다. 그 후, Member 엔티티가 Team 엔티티를 참조하고 있고, EAGER 인 것을 확인하고 Team 을 조회하는 3개의 SELECT 쿼리가 추가로 날라가게 된다.

##### --> Member 객체에서 Team 객체를 참조하지 않아도 N + 1 문제 발생!

<br>

### 2) SELECT m FROM Member m ( LAZY )

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @ManyToOne(fetch = FetchType.XXX)
    @JoinColumn(name = "TEAM_ID")
    private Team team;
}	

List<Member> members = em.createQuery("select m from Member m", Member.class)
    .getResultList();
            
System.out.println("=====================================");

for (Member member : members) {
    System.out.println(member);
    System.out.println(member.getTeam());
}
```

```sql
		select
            member0_.MEMBER_ID as member_i1_5_,
            member0_.name as name2_5_,
            member0_.TEAM_ID as team_id3_5_ 
        from
            Member member0_
            
        -- 자바출력 --
        Member{id=4, name='member1'} -- 
            
        select
            team0_.TEAM_ID as team_id1_9_0_,
            team0_.name as name2_9_0_ 
        from
            Team team0_ 
        where
            team0_.TEAM_ID=?
        
        -- 자바출력 --
        Team{name='teamA'}
        Member{id=5, name='member2'}
        Team{name='teamA'}
        Member{id=6, name='member3'}
        Team{name='teamA'}
        Member{id=7, name='member4'}

      	select
            team0_.TEAM_ID as team_id1_9_0_,
            team0_.name as name2_9_0_ 
        from
            Team team0_ 
        where
            team0_.TEAM_ID=?
        
        -- 자바출력 --
        Team{name='teamB'}
        Member{id=8, name='member5'}
        Team{name='teamB'}
        Member{id=9, name='member6'}

    	select
            team0_.TEAM_ID as team_id1_9_0_,
            team0_.name as name2_9_0_ 
        from
            Team team0_ 
        where
            team0_.TEAM_ID=?
        
        -- 자바출력 --
        Team{name='teamC'}
        Member{id=10, name='member7'}
        null
```

Member 엔티티를 모두 조회할 때는 LAZY 이므로 Member 엔티티를 대상으로만 SELECT 쿼리가 발생한다. 하지만, Member 객체에서 Team 객체를 참조하려면 그때마다 Team 엔티티를 대상으로 SELECT 쿼리가 발생한다.

##### --> Member 객체에서 Team 객체를 참조할 때 N + 1 문제 발생.

<br>

### 3) SELECT m FROM Member m inner join m.team t  ( EAGER )

JOIN Query 를 직접 날리므로 쿼리에서 Team 을 참조해서 사용이 가능! 또한, JOIN Query 이므로 Team 이 없는 Member7은 SELECT되지 않는다.

ex) "SELECT m, t.name FROM Member m inner join m.team t"

```sql
=====================================
Member{id=4, name='member1'}
Team{name='teamA'}
Member{id=5, name='member2'}
Team{name='teamA'}
Member{id=6, name='member3'}
Team{name='teamA'}
Member{id=7, name='member4'}
Team{name='teamB'}
Member{id=8, name='member5'}
Team{name='teamB'}
Member{id=9, name='member6'}
Team{name='teamC'}
```

##### --> EAGER 이므로 SELECT JOIN Query 후에 해당하는 Team에 대한 SELECT Query가 발생한다. 즉, N + 1의 문제가 발생하고 1)의 상황과 동일하다. 

<br>

### 4) SELECT m FROM Member m inner join m.team t  ( LAZY )

JOIN Query 를 직접 날리므로 쿼리에서 Team 을 참조해서 사용이 가능! 또한, JOIN Query 이므로 Team 이 없는 Member7은 SELECT되지 않는다.

ex) "SELECT m, t.name FROM Member m inner join m.team t"

##### --> LAZY 이므로 Team 객체를 참조할 때 N + 1 문제가 발생한다. 2)의 상황과 동일하다.

<br>

### 5) SELECT m FROM Member m JOIN FETCH m.team ( EAGER, LAZY )

페치 조인은 EAGER / LAZY 보다 우선시되고, Member 와 연관된 Team 을 한 번에 가지고 온다.

```sql
		select
            member0_.MEMBER_ID as member_i1_5_0_,
            team1_.TEAM_ID as team_id1_9_1_,
            member0_.name as name2_5_0_,
            member0_.TEAM_ID as team_id3_5_0_,
            team1_.name as name2_9_1_ 
        from
            Member member0_ 
        inner join
            Team team1_ 
                on member0_.TEAM_ID=team1_.TEAM_ID
                
=====================================
Member{id=4, name='member1'}
Team{name='teamA'}
Member{id=5, name='member2'}
Team{name='teamA'}
Member{id=6, name='member3'}
Team{name='teamA'}
Member{id=7, name='member4'}
Team{name='teamB'}
Member{id=8, name='member5'}
Team{name='teamB'}
Member{id=9, name='member6'}
Team{name='teamC'}
```

##### --> 1회의 쿼리로 다 가지고 오게 된다.

