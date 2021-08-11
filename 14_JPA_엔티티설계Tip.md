# JPA_엔티티설계Tip

### 1) 엔티티에는 Setter를 사용하지 말자.

​	Setter가 열려있으면, 변경 포인트가 많아져서 유지보수가 어려워집니다. 생성자에서 값 입력을 모두 끝내고, 값을 변경해야되면 Setter가 아니라 의미 있는 메소드를 따로 만드는 것이 좋습니다.

```java
// Ex) Member 클래스의 name 필드값을 변경

// setter 가 있으면 서비스의 여러 곳에서 변경 포인트가 많아진다.
public void setName(String name){
    this.name = name;
}

// setter를 없애고 의미 있는 메소드를 만들고 주석을 달아주자.
public void changeName(String name){
    this.name = name;
}
```

<br>

### 2) 모든 연관관계는 지연로딩으로 설정.

 즉시 로딩 `Eager` 는 `N + 1` 문제를 유발한다. 따라서, 모든 연관 관계는 기본적으로 `Lazy` 전략으로 설정하고 필요하면 `FetchJoin`을 이용해서 연관된 값들을 같이 가져오자.

> `Eager`를 써야되는 상황이 생기면 테이블 구조를 변경하자. 

<br>

### 3) 컬렉션은 필드에서 초기화 하자.

 일대다 매핑을 하다보면 컬렉션이 필드에 등장합니다. 이때, 컬렉션은 필드에서 초기화 하는 것이 좋습니다.

```java
@Entity
public class Member {
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}
```

  생성자 혹은 Setter로 초기화를 하게 되면 다음과 같은 문제가 발생합니다. 하이버네이트는 엔티티를 영속화 할 때, 컬렉션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변경합니다. 그 후에 컬렉션을 맘대로 초기화하게 되면, JPA가 원하는대로 엔티티가 동작하지 않게 됩니다.

```java
Member member = new Member();
System.out.println(member.getOrders().getClass());
em.persist(team);
System.out.println(member.getOrders().getClass());

//출력 결과
class java.util.ArrayList // 기존 클래스
class org.hibernate.collection.internal.PersistentBag // 하이버네이트가 만든 클래스
```

<br>

### 4) 양방향 연관관계는 연관관계 편의 메서드를 만들자.

 DB 입장에서는 연관관계 주인에서만 값을 잘 관리하면 되지만, 영속성 컨텍스트의 내용이 DB에 반영되었는지, 아닌지에 따라 신경써야하는 상황이 생기므로 무조건 양방향 연관관계는 편의 메서드를 만들어서 같이 관리해주자.

```java
@Entity
public class Member {
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}

@Entity
public class Order {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}

// Order가 연관관계의 주인이다
// --> order.setMember(member) 를 통해 연관관계의 주인에 Member 엔티티를 할당할 수 있고, 
// DB 입장에서는 우리가 원하는대로 값이 Insert 될 것이다.
// 하지만, Member 입장에서도 orders에 order가 추가되야맞는 상황이다.
// --> 연관관계 편의 메서드를 만들자

@Entity
public class Order {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    
    public changeMember(Member member){
        this.member = member;
        mebmer.getOrders.add(this);
	}
}
```

