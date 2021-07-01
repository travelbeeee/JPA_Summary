# JPA_연관관계매핑예시

예시를 통해 JPA 에서 연관관계를 매핑하는 방법에 대해서 알아보자.  

### 0) 시나리오

회원(Member)과 회원마다 팀(Team)이 있는 상황이다.

따라서, 하나의 팀에는 여러 회원이 있을 수 있고, 하나의 회원에는 하나의 팀만 존재한다.

<br>

### 1) DB 테이블에 맞추어 모델링

![jpa_7](https://user-images.githubusercontent.com/59816811/116774941-f494a580-aa9a-11eb-8c18-8c25a1381cd9.png)

DB 테이블을 생각해보면, 회원과 팀의 관계는 다대일 관계이므로, 회원에 FK 가 존재하게 된다. 객체도 똑같이 설계하면 위의 그림과 같다.

그럼, 이런 불편함이 생긴다.

```java
//팀 저장
Team team = new Team();
team.setName("TeamA");
em.persist(team);

//회원 저장
Member member = new Member();
member.setName("member1");
member.setTeamId(team.getId());
em.persist(member);
```

팀과 회원 정보를 하나씩 DB에 저장했다고 하자. 회원을 DB에서 조회해서 회원이 속한 팀을 조회하려면 다음과 같은 문제가 발생한다. 회원을 DB에서 가져오고, 회원이 속한 팀을 추가로 DB에서 가져와야된다.

```java
Member findMember = em.find(Member.class, memberId);
int teamId = findMember.getTeamId();
Team findTeam = em.find(Team.class, teamId);
```

 우리가 객체 입장에서 생각해보면, Member.getTeam 으로 Team 객체를 바로 조회하는게 자연스럽다. 하지만, DB 테이블 설계에 맞춰서 객체를 설계했으므로 위와 같이 teamId 값을 참조해서 다시 Team 객체를 DB에서 꺼내와야된다.

 **테이블은 외래 키로 조인을 사용해서 연관된 테이블을 찾고, 객체는 참조를 사용해서 연관된 객체를 찾는다. 테이블과 객체는 이런 큰 차이가 존재하고 따라서 테이블에 맞추어 객체를 모델링하면 협력 관계를 만들 수 없다.**

<br>

### 2) 객체 지향 모델링

JPA는 객체와 테이블의 차이에서 오는 문제점을 해결해준다.

![jpa_8](https://user-images.githubusercontent.com/59816811/116775057-9b794180-aa9b-11eb-8f0f-8c2b0feabfbc.png)

DB 테이블과 객체를 위의 그림과 같이 설계해보자.

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;
    
    @Column(name = "USERNAME")
    private String username;

    private Team team;
}

@Entity
public class Team{
    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private int id;
    
    private string name;
}
```

이제 우리는 JPA 에게 Member 클래스 안에 있는 Team 객체에 대해서 정보를 알려주기만 하면 된다.

```java
	@ManyToOne
	@JoinColumn(name = "TEAM_ID")
    private Team team;
```

회원과 팀의 관계가 다대일 관계이므로, team 객체와 ManyToOne 관계이고, Team 테이블에 있는 "TEAM_ID" 값을 외래키로 삼는다! 라고 알려줘야된다.

```java
//팀 저장
Team team = new Team();
team.setName("TeamA");
em.persist(team);
//회원 저장
Member member = new Member();
member.setName("member1");
member.setTeam(team); //단방향 연관관계 설정, 참조 저장
em.persist(member);
```

 그럼 DB에 위와 같이 저장해놓고 다음과 같이 Team을 참조할 수 있다.

```java
System.out.println("========Member찾기=============");
Member findMember = em.find(Member.class, member.getId());
Team findTeam = findMember.getTeam();
```

```
========Member찾기=============
16:46:18.168 [main] DEBUG org.hibernate.SQL - 
    select
        member0_.MEMBER_ID as member_i1_1_0_,
        member0_.name as name2_1_0_,
        member0_.TEAM_ID as team_id3_1_0_,
        team1_.TEAM_ID as team_id1_0_1_,
        team1_.name as name2_0_1_ 
    from
        user member0_ 
    left outer join
        Team team1_ 
            on member0_.TEAM_ID=team1_.TEAM_ID 
    where
        member0_.MEMBER_ID=?
```

Member를 찾을 때, JPA에서 select join query를 이용해 TEAM까지 같이 찾아오는 것을 확인할 수 있다.

<br>

회원에 팀을 변경하려면 어떻게 해야될까?? 다음과 같이 dirty checking 을 이용해 그냥 member.setTeam 을 해주면 update query가 다음과 같이 날라간다.

```java
//팀 저장
Team team = new Team();
team.setName("TeamA");
em.persist(team);

//회원 저장
Member member = new Member();
member.setName("member1");
member.setTeam(team); //단방향 연관관계 설정, 참조 저장
em.persist(member);

//팀 저장
Team newTeam = new Team();
newTeam.setName("TeamB");
em.persist(newTeam);

member.setTeam(newTeam);

tx.commit();
```

```
    /* update
        hellojpa.Member */ update
            Member 
        set
            name=?,
            TEAM_ID=? 
        where
            MEMBER_ID=?
```

<br>

### 3) 양방향 모델링

그럼 Team 에서도 자기 팀에 속한 Member를 찾고 싶으면 어떻게 해야될까??

DB 테이블에서는 Team 테이블에서 join 을 이용해서 Member 테이블에서 내 팀에 속한 Member를 찾을 수 있다. 하지만, 객체는 지금 단방향으로 설정되어있으므로, Team 객체에서 Member를 참조할 수 없다.

따라서, 우리는 객체 모델링을 다음과 같이 수정해줘야한다.

![jpa_9](https://user-images.githubusercontent.com/59816811/116775395-d7ada180-aa9d-11eb-87a3-fe8869a9da69.png)

```java
@Entity
public class Team {
    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private int id;

    private String name;

    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
}
```

Team 클래스에도 Member를 담을 List를 선언해주고, 반대로 Team 입장에서는 일대다 관계이므로 @OneToMany 애노테이션을 통해 JPA에게 관계를 알려준다. 이때, `mappedBy` 가 중요한 개념인데 먼저 객체와 DB는 위에서 언급했던 것처럼 차이가 존재한다.

생각해보면 DB 테이블에서는 MEMBER 테이블에서 team_id 를 PK로 가지고 있으면 TEAM 테이블에서도 MEMBER 조회가 가능하고, MEMBER 테이블에서도 TEAM 조회가 가능하다. (양방향관계)

하지만, 객체를 보면 MEMBER 객체에서 team 객체를 참조하고, TEAM 객체에서 member 객체를 참조해야한다. ( 단방향관계 2개 )

이처럼, 차이가 존재하므로 객체의 양방향 관계에서는 주인을 정해줘야한다.

**주인은 테이블에서 PK를 가지고 있는 엔티티를 주인으로 정해줘야한다. ( Member 가 주인 !)**

그리고 주인이 아닌 Team 은 누구에 의해서 매핑되는지 mappedBy 속성을 통해서 알려줘야한다.

```java
    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
```

위의 코드는 Member 클래스 안에 있는 team 객체에 의해서 매핑된다는 뜻이다.

> 양방향 매핑관계 규칙!!
>
> - 하나를 연관관계의 주인으로 지정해야된다
> - 주인은 mappedBy X, 주인이 아니라면 mappedBy 필수!!
> - 외래키를 가지고 있는 엔티티를 주인으로 삼자.

<br>
