# JPA_FetchJoin

### 1) FetchJoin

 페치조인은 SQL 조인의 종류가 아니고, JPQL에서 성능 최적화를 위해 제공하는 기능입니다. **연관된 엔티티나 컬렉션을 SQL 한 번에 함께 조회하는 기능**으로 정말 많이 사용하는 기능입니다.

 페치조인을 사용하면 조인의 N + 1 문제를 해결할 수 있습니다.

>  페치조인은 LAZY, EAGER 같은 Fetch 전략보다 우선시 되므로 전략은 의미가 없어집니다.

<br>

### 2) FetchJoin 엔티티 타입

<img src="https://user-images.githubusercontent.com/59816811/117401770-25178c00-af40-11eb-8ab2-7e83ed850a94.png" alt="jpa_30" width="500" />

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
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

 위의 그림과 같이 데이터를 셋팅하고 Member 엔티티에서 참조 중인 Team 엔티티를 join fetch 를 이용해 가져와보겠습니다.

```java
List<Member> members = em.createQuery("select m from Member m join fetch m.team", Member.class)
    .getResultList();

System.out.println("=====================================");
for (Member member : members) {
    System.out.println("member = " + member);
    System.out.println("member.team = " + member.getTeam());
}
```

```sql
	select
            member0_.member_id as member_i1_0_0_,
            team1_.team_id as team_id1_1_1_,
            member0_.name as name2_0_0_,
            member0_.team_id as team_id3_0_0_,
            team1_.name as name2_1_1_ 
        from
            Member member0_ 
        inner join
            Team team1_ 
                on member0_.team_id=team1_.team_id
                
=====================================
member = Member{id=4, name='member1'}
member.team = Team{name='teamA'}
member = Member{id=5, name='member2'}
member.team = Team{name='teamA'}
member = Member{id=6, name='member3'}
member.team = Team{name='teamA'}
member = Member{id=7, name='member4'}
member.team = Team{name='teamB'}
member = Member{id=8, name='member5'}
member.team = Team{name='teamB'}
member = Member{id=9, name='member6'}
member.team = Team{name='teamC'}
```

 select 쿼리 한 번으로 우리가 원하는 정보를 다 가지고 오는 것을 확인할 수 있습니다.

 **주의해서 봐야되는 점은 Member 엔티티를 페치조인으로 Team과 함께 가지고올 때 SQL 입장에서는 SELECT JOIN 쿼리가 발생하므로 Team 이 없는 Member는 SELECT 되지 않은 것을 볼 수 있습니다.**  

<br>

### 3) FetchJoin 컬렉션 타입

Team 엔티티에서 참조 중인 Member 컬렉션 타입을 join fetch를 이용해 가져와보겠습니다.

```java
List<Team> teams = em.createQuery("select t from Team t join fetch t.members", Team.class)
    .getResultList();

System.out.println("=====================================");
for (Team team : teams) {
    System.out.println("team = " + team);
    for (Member m : team.getMembers()) {
        System.out.println("m = " + m);
    }
}
```

```sql
		select
            team0_.TEAM_ID as team_id1_9_0_,
            members1_.MEMBER_ID as member_i1_5_1_,
            team0_.name as name2_9_0_,
            members1_.name as name2_5_1_,
            members1_.TEAM_ID as team_id3_5_1_,
            members1_.TEAM_ID as team_id3_5_0__,
            members1_.MEMBER_ID as member_i1_5_0__ 
        from
            Team team0_ 
        inner join
            Member members1_ 
                on team0_.TEAM_ID=members1_.TEAM_ID
                
                
=====================================
team = Team{name='teamA'}
m = Member{id=4, name='member1'}
m = Member{id=5, name='member2'}
m = Member{id=6, name='member3'}
team = Team{name='teamA'}
m = Member{id=4, name='member1'}
m = Member{id=5, name='member2'}
m = Member{id=6, name='member3'}
team = Team{name='teamA'}
m = Member{id=4, name='member1'}
m = Member{id=5, name='member2'}
m = Member{id=6, name='member3'}
team = Team{name='teamB'}
m = Member{id=7, name='member4'}
m = Member{id=8, name='member5'}
team = Team{name='teamB'}
m = Member{id=7, name='member4'}
m = Member{id=8, name='member5'}
team = Team{name='teamC'}
m = Member{id=9, name='member6'}
```

 select 쿼리 한 번에 연관된 엔티티 혹은 컬렉션 값들을 다 가지고 오는 것을 확인할 수 있습니다.

 **주의해야될 점은 Team 엔티티를 페치조인으로 Member 와 함께 가지고올 때 마찬가지로 SELECT JOIN 쿼리가 발생하므로 데이터가 중복되는 것을 볼 수 있습니다.**

<br>

### 4) FetchJoin 과 Distinct

 JPA에서 제공하는 FetchJoin 도 결국 연관된 엔티티를 가져오기 위해, JOIN을 이용한 Query 가 발생한다. 그러다보니 컬렉션 타입 Fetch Join에서는 데이터의 중복이 발생하게 됩니다. 

 이를 위해 JPQL 에서는 `DISTINCT` 기능을 제공해줍니다. SQL 에서의 `DISTINCT`와는 다른 기능으로 **SQL에 DISTINCT 를 추가하고, 또 애플리케이션 레벨에서 엔티티의 중복 또한 제거해줍니다.**

```java
List<Team> teams = em.createQuery("select distinct(t) from Team t join fetch t.members", Team.class)
				.getResultList();
```

```java
		select
            distinct team0_.TEAM_ID as team_id1_9_0_,
            members1_.MEMBER_ID as member_i1_5_1_,
            team0_.name as name2_9_0_,
            members1_.name as name2_5_1_,
            members1_.TEAM_ID as team_id3_5_1_,
            members1_.TEAM_ID as team_id3_5_0__,
            members1_.MEMBER_ID as member_i1_5_0__ 
        from
            Team team0_ 
        inner join
            Member members1_ 
                on team0_.TEAM_ID=members1_.TEAM_ID
                
=====================================
team = Team{name='teamA'}
m = Member{id=4, name='member1'}
m = Member{id=5, name='member2'}
m = Member{id=6, name='member3'}
team = Team{name='teamB'}
m = Member{id=7, name='member4'}
m = Member{id=8, name='member5'}
team = Team{name='teamC'}
m = Member{id=9, name='member6'}
```

<br>

### 5) FetchJoin 한계

##### 	5-1) 페치 조인 대상에는 별칭을 주지말자!

```java
"select t From Team t join fetch t.members" // 가능
"select t From Team t join fetch t.members as m" // 가능하다 하지만, JPA가 의도한 설계가 아님 하지말자!
"select t from Team t join fetch t.members as m where m.username := name" // JPA가 의도한 설계가 아님 
```

​	페치 조인은 연관된 엔티티를 다 가져오기위해 JPA에서 지원해주는 기능이다. 따라서, 위와 같이 다 가져오는게 아니라 별칭을 통해 조건을 줄 수는 있지만 JPA 가 원하는 상황이 아니므로 쓰지말자.

##### 	5-2) 둘 이상의 컬렉션은 페치 조인 할 수 없다.

​	예를 들어, Member엔티티 안에 일대다 관계고 Team 도 있고, Order도 있다고 하자. 2개의 컬렉션을 동시에 페치 조인하게되면 일대다대다의 관계가 되면서 데이터가 굉장히 커진다.

##### 	5-3) 컬렉션을 페치 조인하면 페이징 API 를 사용할 수 없다.

 일대일, 다대일 같은 단일 값 상황에서는 페치 조인을 해도 페이징 API 를 사용할 수 있으나, 일대다 상황에서는 사용하면 안된다.

 아래와 같이 회원1,2가 팀 A에 속하는 상황에서 페치 조인을 하게 되면 기본적으로 SQL Join 이므로 아래와 같이 결과 테이블이 나오게 된다. 그러면, 페이징 API 를 사용하면 우리가 원하는 상황과 달라지게 된다. ( 우리가 원한건 팀A 1개에 회원1,회원2를 가져오는 상황인데 , 팀A 회원1만 가지고 오게 된다.)

![jpa_41](https://user-images.githubusercontent.com/59816811/117630879-9a8c9200-b1b6-11eb-9a2b-0aea6442795e.png)

​	--> 일대다가 아니라 다대일로 뒤집어서 꺼내오자.

