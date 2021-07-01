# JPA_연관관계매핑

### 0) 연관관계

 객체 세계와 DB 세계는 차이가 존재한다. **테이블은 외래 키로 조인을 사용해서 연관된 테이블을 찾고, 객체는 참조를 사용해서 연관된 객체를 찾는다.** 

- 테이블
  - 외래 키 하나로 양쪽 조인 가능
  - 방향이라는 개념 X
- 객체
  - 참조용 필드가 있는 쪽으롬나 참조가 가능
  - 한쪽만 참조하면 단방향
  - 양쪽이 서로 참조하면 단방향 2개

**테이블은 외래 키 하나로 두 테이블이 연관관계를 맺으나, 객체는 단방향 관계 2개가 모여야 두 객체가 양방향 관계를 맺을 수 있다.** 테이블과 객체는 이런 큰 차이가 존재하므로, 단순히 테이블에 맞추서 객체를 모델링하면 협력 관계를 만들 수 없다. 

 객체 입장에서는 양방향 관계에서 두 객체가 서로 참조를 하고 있으나, 테이블 입장에서는 한 쪽에만 외래키가 존재할 수 밖에 없다. 따라서, 둘 중에 하나의 객체에서 `외래키`를 괸리해야되고, 외래키를 관리하는 객체를 **연관관계의 주인**이라고 한다.

- 연관관계의 주인만이 외래키를 관리!
- 주인이 아니면 읽기만 가능
- 주인이 아니면 mappedBy 속성을 사용해서 주인을 지정해줘야한다.

- DB 관점에서는 외래키는 일대다 혹은 다대일 관계에서 '다' 가 관리할 수 밖에 없다. 객체 입장에서는 '일' 에 해당하는 객체가 연관관계의 주인일 수도 있다.

  --> 외래키가 있는 쪽은 연관관계 주인으로 삼는게 성능상 좋다.

- 연관관계 주인이라고 서비스에서 더 중요한 객체라는 뜻이 아니다! 단순히 외래키를 관리하는 객체를 의미한다.

<br>

### 1) 다대일 [ N : 1 ]

##### [ 다대일 단방향 ] 

다대일 관계는 실무에서 가장 많이 사용되는 관계로, 일대다 관계랑 반대이다.

![jpa_1](https://user-images.githubusercontent.com/59816811/116841785-2af63000-ac15-11eb-98d6-b1b0b5904cf6.png)

 회원은 하나의 팀을 가질 수 있고, 팀에 소속된 회원은 여러 명일 수 있는 상황이다.  이때, 테이블은 다대일 관계이므로 '다' 에 해당하는 Member 테이블에서 외래키를 관리할 수 밖에 없다. 자연스럽게 Member 객체가 연관 관계의 주인이 되어 Team 객체를 가지고 관리하는 상황을 생각해보자. 그러면 객체와 테이블 설계는 위의 그림과 같이 된다.

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @ManyToOne 
    @JoinColumn(name = "TEAM_ID")
    private Team team;
}

@Entity
public class Team {
    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private int id;

    private String name;
}

```

 Member 클래스의 Team 객체는 다대일 관계가 되므로 @ManyToOne 애노테이션을 설정해주고 "TEAM_ID" 를 외래키로 삼으므로 @JoinColumn(name = "TEAM_ID") 를 설정해야한다.

<br>

##### [ 다대일 양방향 ]

그러면, 반대로 Team 객체에서도 Member 객체를 참조하려면 어떻게 해야될까??

![jpa_10](https://user-images.githubusercontent.com/59816811/116841918-9fc96a00-ac15-11eb-9398-614f0d38ed41.png)

Team은 여러 Member를 가지고 있을 수 있으므로 List 로 선언해줘야한다.

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @ManyToOne 
    @JoinColumn(name = "TEAM_ID")
    private Team team;
}

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

Team 은 Member와의 관계가 일대다 관계이므로 @OneToMany 애노테이션과 Member객체의 "team" 필드에 의해 매핑된다고 정보를 알려줘야되므로 @OneToMany(mappedBy = "team") 을 기술해줘야한다.

<br>

### 2) 일대다 [1 : N]

##### [ 일대다 단방향 ] 

일대다 관계는 다대일 관계의 반대다.	

![jpa_11](https://user-images.githubusercontent.com/59816811/116842681-5595b800-ac18-11eb-9767-f313049a4143.png)

 같은 상황인데 이번에는 Team 객체가 연관관계의 주인이 되서 관리하는 상황이다. 하지만, DB 입장에서는 Member 테이블에서 FK 를 관리해야된다. 즉, Team 객체가 연관관계의 주인이라 외래키를 관리하지만, 테이블 입장에서는 Member 테이블에서 외래키를 관리해야된다. 따라서, Team 객체가 외래키를 관리하는 과정에서 Member 테이블에 Update Query를 날릴 수 밖에 없습니다.  **객체와 테이블의 이러한 차이 때문에 일대다 관계는 다대일 관계로 풀어서 해주는 것이 좋다.**

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;
}

@Entity
public class Team {
    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private int id;

    private String name;

    @OneToMany
    @JoinColumn(name = "TEAM_ID")
    private List<Member> members = new ArrayList<>();
}
```

다대일 관계와 마찬가지로 연관관계의 주인에 관계를 표현해주는 @OneToMany 애노테이션과 "TEAM_ID" 를 외래키로 삼으므로 @JoinColumn(name = "TEAM_ID") 를 설정해주어야한다.

```java
            //회원 저장
            Member member = new Member();
            member.setName("member1");
            em.persist(member);

            //팀 저장
            Team team = new Team();
            team.setName("TeamA");
            team.getMembers().add(member); // Team 객체의 필드를 바꾸지만 Team 테이블을 건드려야되는 작업이 아니다!
            em.persist(team);

            tx.commit();
```

Team이 연관관계의 주인이므로 회원을 저장하고, Team에 회원을 셋팅해주기만 하면 된다.

```java
        into
            Member
            (name, MEMBER_ID) 
        values
            (?, ?)
            
        into
            Team
            (name, TEAM_ID) 
        values
            (?, ?)
            
        update
            Member 
        set
            TEAM_ID=? 
        where
            MEMBER_ID=?
```

그럼 다음과 같이 Member, Team 차례대로 insert query가 나가고, Team 에 Member를 셋팅해주지만, PK는 Member 테이블에 있으므로, Member 테이블을 update하는 update query가 나가게 된다.

> JPA를 잘 알면 상관없지만, Team을 insert하는 시점에서 Member 에 update query가 발생하므로 나중에 추적이 어려울 수 있다.
>
> --> 다대일관계로 풀자...!!

만약에 @JoinColumn 을 설정해주지않고 다음과 같이 셋팅하면 @JoinTable 전략이 선택되며 테이블이 자동으로 하나 생성된다.

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;
}


@Entity
public class Team {
    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private int id;

    private String name;

    @OneToMany
    //@JoinColumn(name = "TEAM_ID")
    private List<Member> members = new ArrayList<>();
}
```

![jpa_12](https://user-images.githubusercontent.com/59816811/116843245-50396d00-ac1a-11eb-9cf4-439ed7c9c703.png)

@JoinColumn 애노테이션을 통해 두 테이블을 연결해주는 키를 설정해주지 않았으므로 Member 테이블과 Team 테이블에 둘을 연결해주는 FK 가 존재하지않고, 두 테이블을 연결해주는 새로운 테이블이 하나 생기게 된다.

<br>

##### [ 일대다 양방향 ]

마찬가지로 일대다 관계에서도 양방향 매핑을 해줄 수 있는데 다대일 양방향 관계와는 설정 방법이 다르다.

![jpa_13](https://user-images.githubusercontent.com/59816811/116843317-870f8300-ac1a-11eb-89cc-d6aa1df08f4c.png)

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;
    
    @ManyToOne
    @JoinColumn(name = "TEAM_ID", insertable = false, updatable = false)
    private Team team;
}


@Entity
public class Team {
    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private int id;

    private String name;

    @OneToMany
    @JoinColumn(name = "TEAM_ID")
    private List<Member> members = new ArrayList<>();
}
```

JPA에서 스펙상 공식적으로 지원해주는 상황은 아니고, insertable, updatable 을 false로 막으면서 일대다 상황에서 양방향 매핑을 강제로 만들 수 있다.

> insertable = false, updatable = false 가 없다면 동시에 연관관계의 주인이 두 명 존재하게된다. --> 에러 발생

<br>

### 3) 일대일 [1 : 1]

일대일 관계는 그 반대도 일대일이된다. 주 테이블이나 대상 테이블 중에 어디에도 외래키가 있어도 상관이 없다. **다만 외래키에는 UNIQUE 조건을 설정해줘야한다(직접 DB에서 쿼리날려서 만들어주자).**

회원이 하나의 락커를 가지고 있는 상황을 생각해보자.

<br>

##### [ 연관관계의 주인 테이블에 외래키가 있는 경우 ]

다대일 관계랑 설정 방법이 유사하다. 다대일 관계처럼 외래키가 있는 테이블을 주 테이블로 생각하고 반대편에는 mappedBy를 통해 양방향으로 매핑해줄 수 있다.

![jpa_14](https://user-images.githubusercontent.com/59816811/116843522-2896d480-ac1b-11eb-8983-71fba19d9780.png)

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @OneToOne
    @JoinColumn(name = "LOCKER_ID")
    private Locker locker;
}

@Entity
public class Locker {
    @Id @GeneratedValue
    @Column(name = "LOCKER_ID")
    private int id;

    private String name;
}
```

양방향 관계를 주고 싶으면 다음과 같이 설정하면 된다.

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @OneToOne
    @JoinColumn(name = "LOCKER_ID")
    private Locker locker;
}

@Entity
public class Locker {
    @Id @GeneratedValue
    @Column(name = "LOCKER_ID")
    private int id;

    private String name;
    
    @OneToOne(mappedBy = "locker")
    private Member member;
}
```

![jpa_15](https://user-images.githubusercontent.com/59816811/116843689-a955d080-ac1b-11eb-9d69-94301d004c65.png)

<br>

##### **[ 대상 테이블에 외래키가 있는 경우 ]**

똑같이 Member가 연관관계의 주인으로 Locker 객체를 참조하는데 반대로 Locker 테이블에서 외래키를 관리하는 경우다.

![jpa_16](https://user-images.githubusercontent.com/59816811/116843765-df935000-ac1b-11eb-9432-07e1702dcc21.png)

대상 테이블에 외래키가 있는 단방향 관계는 JPA에서 지원해주지 않고 구현할 방법이 없다.

다만, 양방향 관계는 구현할 수 있다.

![jpa_17](https://user-images.githubusercontent.com/59816811/116843819-18332980-ac1c-11eb-913f-8293beb0c09d.png)

이 방법은 그냥 주 테이블에 외래키가 있는 경우의 양방향과 구현 방법이 동일하다. ( Locker 가 연관관계 주인이라도 생각하면 결국 그냥 똑같다. )

<br>

##### 주 테이블에 외래 키 vs 대상 테이블에 외래키

DB 입장에서만 생각해보자!

일대일 관계에서는 누가 FK를 관리하던 상관이 없다.

하지만, 미래에 룰이 바껴서 하나의 회원이 여러 개의 Locker를 가질 수 있는 상황으로 바뀐다고 생각해보자.

그러면, Locker 테이블에서 FK 를 관리하고 있는 상황이라면 금방 수정이 가능하지만, Member 테이블에서 FK를 관리하고 있는 상황이라면 DB 에 수정해야할 변경 상황이 많이 발생한다.

반대로, 미래에 룰이 하나의 Locker를 여러 회원이 동시에 이용할 수 있는 상황으로 바뀐다고 가정해보자.

그러면, Member 테이블에서 FK를 관리하고 있는 상황이 더 좋은상황이 된다.

--> 정답이 없다.

> 지금 상황에서 내 애플리케이션은 아마 Locker 보다는 Member 테이블에서 select를 더 많이 해오게 된다. 그러면 Member에서 Locker를 바로 참조하고 Member 테이블에서 FK를 관리하는게 유리하다.  Member를 select 해오면서 join 없이 Locker_ID를 확인해서 Locker가 있는지 없는지 바로 판단이 가능하다.
>
> 이렇듯, 미래의 상황과 현재 내 애플리케이션에서 많이 사용하는 테이블을 고려해서 적절하게 셋팅해줘야된다.

<br>

- 주 테이블에 외래 키 관리

  - 주 객체가 대상 객체의 참조를 가지는 것 처럼 주 테이블에 외래키를 두고 대상 테이블을 찾는 방법
  - 객체지향 개발자 입장에서는 편하고, 주 테이블만 조회해도 대상 테이블에 데이터가 있는지 확인이 가능하다. 하지만 값이 없으면 외래 키에 null을 허용해야하는 상황이 생긴다.

- 대상 테이블에 외래 키 관리

  - 대상 테이블에 외래키가 존재하는 방법이다.
  - 데이터베이스 개발자들이 선호하고, 주 테이블과 대상 테이블을 일대일에서 일대다 관계로 변경할 때 테이블 구조를 유지하기 좋다. 단점으로는 프록시 기능의 한계로 지연 로딩이 지원되지 않는다.

  ![jpa_17](https://user-images.githubusercontent.com/59816811/116843819-18332980-ac1c-11eb-913f-8293beb0c09d.png)

  ​	Member 테이블만 참조해서는 Member 입장에서는 Locker에 값이 있는지 없는지 알 수 없다. 따라서, Member 테이블을 참조하면서 Locker 테이블도 동시에 참조할 수 밖에 없고 Locker 테이블을 참조하는 쿼리가 나갈 수 밖에 없다. 따라서, 지연 로딩이 지원되지않는다.

<br>

### 4) 다대다 [ N : M ]

다대다 관계는 실무에서 사용하면 안된다.

관계형 데이터베이스는 정규화된 테이블 2개로 다대다 관계를 표현할 수 없어서 연결 테이블을 추가해서 일대다, 다대일 관계로 풀어내야한다.

하지만, 객체는 컬렉션을 사용해서 객체 2개로 다대다 관계가 가능하다.

![jpa_18](https://user-images.githubusercontent.com/59816811/116844467-1ec2a080-ac1e-11eb-98a3-3eb158ee7dc6.png)

객체에서는 다대다 관계가 표현이 되므로, JPA에서 다대다 관계를 지원은 해줌! ( 하지만 쓰면 안된다...!!!!! )

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @ManyToMany
    @JoinTable(name = "MEMBER_PRODUCT")
    private List<Product> products = new ArrayList<>();
}

@Entity
public class Product {

    @Id @GeneratedValue
    @Column(name = "PRODUCT_ID")
    private int id;

    private String name;
}
```

@ManyToMany 애노테이션과 연결 테이블을 @JoinTable 로 추가해서 풀어낼 수 있다.

양방향은 다음과 같다.

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @ManyToMany
    @JoinTable(name = "MEMBER_PRODUCT")
    private List<Product> products = new ArrayList<>();
}

@Entity
public class Product {
    @Id @GeneratedValue
    @Column(name = "PRODUCT_ID")
    private int id;

    private String name;
    
    @ManyToMany(mappedBy = "products")
    priate List<Member> members = new ArrayList<>();
}	
```

<br>

##### 다대다 매핑의 한계

@ManyToMany 를 이용하면 다대다 관계가 연결테이블로 잘 풀어지는 것 같다. 하지만, 연결 테이블이 단순히 연결만 할 수 있다.

예를 들어, 회원이 상품을 주문하는 시간, 가격 등을 테이블에 추가해야되는데 연결 테이블 수정이 불가능하다.

따라서, 일대다 / 다대일 관계로 풀고 중간에 다른 엔티티를 추가해서 풀어줘야한다.

![jpa_19](https://user-images.githubusercontent.com/59816811/116844745-f38c8100-ac1e-11eb-86ff-edfeddec1c0a.png)

<br>

### 5) 주의사항

##### 양방향 연관관계 값 설정 주의사항

 양방향 연관관계에서는 `연관관계의 주인`이 값을 설정하면 DB 테이블 상에서는 문제가 없다. ( 연관관계의 주인이 아닌 반대쪽에서 값을 설정하면 DB 테이블에 null로 들어간다. ) Member 객체가 연관관계의 주인으로 Team 객체를 참조하고 있다고 하자.

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @ManyToOne 
    @JoinColumn(name = "TEAM_ID")
    private Team team;
}

@Entity
public class Team {
    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private int id;

    private String name;
    
    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
}


Team team = new Team();
team.setName("TeamA");
em.persist(team);

Member member = new Member();
member.setName("member1");
member.setTeam(team); //연관관계의 주인에 값 설정
em.persist(member);
```

 그러면, Team 객체의 `members` 필드에 값을 추가해주지않아도, JPA에서 DB 테이블에 알맞은 쿼리를 날려준다.

 반대로 연관관계 주인이 아닌 Team 쪽에서 값을 설정하면 어떻게 될까??

```java
Member member = new Member();
member.setName("member1");
em.persist(member);

Team team = new Team();
team.setName("TeamA");
team.getMembers().add(member);
em.persist(team);
```

 Team 객체는 연관관계의 주인이 아니므로, Member 테이블의 외래키에 NULL 이 들어가게 된다.

<br>

 그러면 연관관계 주인에 값을 제대로 설정한 아래와 같은 상황에서 , Team 객체를 find 하면 `members` 필드에 우리가 생각한 것 처럼 값이 들어가있을까??

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

Team findTeam = em.find(Team.class, team.getId()); 
// findTeam.getMembers() 하면 아무런 Member 객체가 담겨있지않다.
```

 현재 findTeam 을 찾아올 때는 team, member가 DB에 반영된 것이 아니라 1차 캐시에 올라와있는 상태이다. 따라서, findTeam.members에는 member가 셋팅이 되어있지 않다!!

--> 연관관계 편의 메서드를 만들어서 항상 동시에 둘다 값을 셋팅해주자.

```java
@Entity
public class Member {
	// ~~ 필드 ~~ 
    public void setTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
```

> 실무에서 사용할 때는 메소드 명을 setTeam 이 아니라 changeTeam 등과 같이 의미 있는 메소드 명으로 설정하자.

<br>

##### 무한루프에 갇히는 경우

양방향 매핑이 되었을 때 롬복을 이용해서 Member, Team ToString 메소드를 만들면 다음과 같이 만들어진다.

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @ManyToOne @JoinColumn(name = "TEAM_ID")
    private Team team;

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", team=" + team +
                '}';
    }
}

@Entity
public class Team {
    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private int id;

    private String name;

    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", members=" + members +
                '}';
    }
}
```

 DB에서 Member를 1개 가져와서 출력한다고 해보자.

```java
System.out.println("member = " + member);
```

그러면 member.toString() 이 호출된다. member.toString() 을 가보면 team 객체를 출력하고 있으므로, team.toString() 이 호출된다. team.toString()을 가보면 members 를 출력하고 있으므로, List<Member>에 있는 Member 객체의 toString()을 호출하게 된다.

즉, 무한루프에 갇히게 된다!!

**--> 양방향 관계를 설정할 때는 무한루프를 조심해야된다.**

<br>

