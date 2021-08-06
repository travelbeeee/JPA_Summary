

# JPA_EAGER,LAZY

### 1) 즉시로딩

즉시로딩은 엔티티를 조회할 때, 연관된 모든 엔티티들도 한 번에 조회하는 전략을 말한다.

```java
@Entity
public class Member extends BaseEntity{
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @ManyToOne(fetch = FetchType.EAGER) // 즉시로딩 전략 사용!
    @JoinColumn(name = "TEAM_ID")
    private Team team;
}

@Entity
public class Team {
    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private int id;

    private String name;

    @OneToMany(mappedBy = "team", fetch = FetchType.EAGER)
    private List<Member> members = new ArrayList<>();
}
```

그러면, 다음과 같이 Member 엔티티를 가지고 오거나, Team 엔티티를 가지고 올 때 Join을 이용한 query가 발생하게 된다.

```java
//팀 저장
Team team = new Team();
team.setName("team1");

//회원 저장
Member member = new Member();
member.setName("member1");
member.setTeam(team);

em.persist(member);
em.persist(team);

em.flush();
em.clear();

Member findMember = em.find(Member.class, member.getId());

// 찾아온 Member엔티티의 Team객체 참조
System.out.println("findMember.Team = " + findMember.getTeam().getName()); // findMember.Team = team1
```

```sql
    select
        member0_.MEMBER_ID as member_i1_4_0_,
        member0_.name as name4_4_0_,
        member0_.TEAM_ID as team_id5_4_0_,
        team1_.TEAM_ID as team_id1_7_1_,
        team1_.name as name2_7_1_ 
    from
        Member member0_ 
    left outer join
        Team team1_ 
            on member0_.TEAM_ID=team1_.TEAM_ID 
    where
        member0_.MEMBER_ID=?
```

Join을 이용한 query가 발생하고, Member 엔티티를 찾았지만, 참조하고 있는 Team 엔티티도 같이 찾아지는 것을 볼 수 있습니다.

```java
//팀 저장
Team team = new Team();
team.setName("team1");

//회원 저장
Member member = new Member();
member.setName("member1");
member.setTeam(team);

em.persist(member);
em.persist(team);

em.flush();
em.clear();

Team findTeam = em.find(Team.class, team.getId());
for (Member m : findTeam.getMembers()) {
    System.out.println("Member = " + m.getName()); // Member = member1
}
```

```sql
    select
        team0_.TEAM_ID as team_id1_7_0_,
        team0_.name as name2_7_0_,
        members1_.TEAM_ID as team_id3_4_1_,
        members1_.MEMBER_ID as member_i1_4_1_,
        members1_.MEMBER_ID as member_i1_4_2_,
        members1_.name as name2_4_2_,
        members1_.TEAM_ID as team_id3_4_2_ 
    from
        Team team0_ 
    left outer join
        Member members1_ 
            on team0_.TEAM_ID=members1_.TEAM_ID 
    where
        team0_.TEAM_ID=?
```

Team 엔티티를 찾아올 때, 참조하고있는 Member도 다 찾아오는 것을 확인할 수 있다.

<br>

### 2) 지연로딩

 지연로딩은 즉시로딩과 반대로 프록시를 이용한 전략이다. 엔티티를 조회할 때, 연관된 엔티티들을 같이 조회하는 것이 아니라 프록시 객체로 일단은 가짜 엔티티를 채워두고 필요할 때 프록시를 초기화해서 사용하는 전략이다.

```java
@Entity
public class Member extends BaseEntity{
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY) // 즉시로딩 전략 사용!
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

> EAGER가 아니라 LAZY로 설정하면 된다.
>

Team 객체를 조회해보자.

```java
Team team = new Team();
team.setName("team1");

//회원 저장
Member member = new Member();
member.setName("member1");
member.setTeam(team);

em.persist(member);
em.persist(team);


em.flush();
em.clear();

System.out.println("===========em.find Before================");
Member findMember = em.find(Member.class, member.getId());
System.out.println("===========em.find Before================");
System.out.println("Member.team = " + findMember.getTeam().getName());
```

```sql
===========em.find Before================
    select
        member0_.MEMBER_ID as member_i1_4_0_,
        member0_.name as name2_4_0_,
        member0_.TEAM_ID as team_id3_4_0_ 
    from
        Member member0_ 
    where
        member0_.MEMBER_ID=?
===========em.find After================
    select
        team0_.TEAM_ID as team_id1_7_0_,
        team0_.name as name2_7_0_ 
    from
        Team team0_ 
    where
        team0_.TEAM_ID=?
        
        Member.team = team1
```

Member 엔티티를 처음 find할 때는 join 을 이용한 query가 아니라 Member 엔티티 정보만 가지고 오고, 이후에 Team 객체를 참조할 때 추가 query가 나가는 것을 볼 수 있습니다.

<br>

### 3) 즉시로딩 vs 지연로딩

##### 항상 지연로딩을 사용하자.

즉시로딩을 사용하면 다음과 같은 문제가 생깁니다.

- 즉시 로딩을 적용하면 예상하지 못한 SQL이 발생합니다. 예를 들어, 여러 테이블이 서로서로 다 참조하고 있는 복잡한 상황이라고 가정합시다. 그러면, 하나의 엔티티를 참조하는데 join 쿼리만 여러 개가 나가게 되고 이는 추적하기도 어렵고 성능도 저하됩니다.
- 즉시 로딩은 JPQL 에서 N + 1 문제가 발생합니다. 예를 들어, 회원 엔티티를 모두 조회하는 "select m from Member m" 쿼리를 날린다고 해봅시다. JPQL은 SQL로 번역되서 일단 DB에 쿼리가 그대로 날라가기 때문에 Member 엔티티를 모두 조회하는 쿼리가 먼저 날라가게 됩니다. 그 후, 회원 엔티티가 다른 엔티티를 참조하고 있다면 즉시 로딩 전략에서는 연관된 엔티티도 모두 조회하게 됩니다. 따라서, N개의 회원 엔티티가 있다면 연관된 엔티티를 select 하는 추가 쿼리가 N개 발생하게 됩니다. 이처럼, N + 1 문제가 발생할 수 있으므로 즉시 로딩을 사용하지 않는 것이 좋습니다.

<br>

> @ManyToOne, @OneToOne 은 기본이 즉시 로딩
>
> --> 지연 로딩으로 항상 바꾸자

