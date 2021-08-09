# JPA_경로표현식

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

### 1) 경로 표현식

경로 표현식이란 .(점)을 찍어 객체 그래프를 탐색하는 것을 말합니다.

- **상태필드 : 단순히 값을 저장하기 위한 필드 ( m.username )**

  상태필드는 경로 탐색의 끝으로 더 이상 탐색이 안된다!

- **연관 필드 : 연관관계를 위한 필드**

  - **단일 값 연관 필드**

    @ManyToOne, @OneToOne --> 대상이 엔티티 ( m.team )

    **묵시적 내부 조인이 발생한다. ( 중요 !! )**

  - **컬렉션 값 연관 필드**

    @OneToMany, @ManyToMany --> 대상이 컬렉션 ( m.orders )

    **묵시적 내부 조인이 발생하고 추가 탐색이 불가능하다!** FROM 절에서 명시적 조인을 통해 별칭을 얻으면 별칭을 통해 탐색 가능

<br>

### 2) 단일 값 연관 필드

```java
// 묵시적 조인
List<Team> resultList = em.createQuery("SELECT m.team FROM Member m", Team.class).getResultList();

// 명시적 조인
"SELECT t from Member m join m.team t";
```

Member 의 Team을 참조하려면 묵시적으로 내부 조인이 발생한다.

```sql
		select
            team1_.TEAM_ID as team_id1_9_,
            team1_.name as name2_9_ 
        from
            Member member0_ 
        inner join
            Team team1_ 
                on member0_.TEAM_ID=team1_.TEAM_ID
```

묵시적 내부 조인이 발생하게 되면, 나중에 쿼리를 추적하기 어려우므로 명시적 조인을 사용하자.

<br>

### 3) 컬렉션 값 연관 필드

컬렉션 값 연관 필드는 추가 탐색이 불가능하다.

```java
Collection result = em.createQuery("SELECT t.members From Team t", Collection.class)
    .getResultList();

// em.createQuery("SELECT t.members.name From Team t", Collection.class) 불가능!
```

팀에 있는 Members의 Member 를 추가로 탐색하는 것이 불가능하다.

마찬가지로 묵시적 조인이 발생하므로, 명시적 조인을 이용해야한다. 또, 명시적 조인을 이용해서 추가로 컬렉션에 별칭을 부여한다면 추가 탐색이 가능하다.

```java
List<Member> result = em.createQuery("SELECT m From Team t join t.members m", Member.class)
    .getResultList();

for (Member member : result) {
    System.out.println("member = " + member);
}
```

<br>

### 4) 주의

단일 값 연관 필드, 컬렉션 값 연관 필드 모두 경로 표현식에 의해 묵시적 조인이 발생한다. 하지만, 묵시적 조인은 우리가 Query 를 보고 조인이 나가는지 알 수 없으므로 사용하면 안된다. ( 실무에서 테이블이 복잡하면 묵시적 조인으로 꼬리를 물고 조인 쿼리가 발생할 수 있음)  

**명시적 조인을 사용하자.**