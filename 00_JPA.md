

# JPA 등장배경

 대부분 객체 지향 언어와 관계형 데이터베이스를 결합해서 서버를 개발합니다.(ex : spring + Oracle, spring + MySql ) 그러다보면, 반복적인 SQL 문 작성을 피할 수 없고, SQL 의존적인 개발을 할 수 밖에 없습니다.

<br>

### 1) SQL 중심적 개발의 문제점

우리는 객체지향언어를 통해 개발을 합니다. 그리고 객체들의 정보를 대부분 관계형 데이터베이스에 저장합니다.

그러나, 관계형 데이터베이스는 데이터를 잘 정규화해서 보관하는 것이 목표이고, 객체 지향 언어는 추상화, 캡슐화, 정보은닉, 상속, 다형성 등의 특징을 이용해 유연한 개발을 하는 것이 목표입니다. 당연히 객체와 관계형 DB에서의 데이터는 많은 차이가 있습니다.  

**차이점**

- 상속
- 연관관계
- 데이터 타입
- 데이터 식별방법

이러한 객체를 관계형 데이터베이스에 데이터로 넣으려다 보니까  차이에서 오는 문제가 발생할 수 밖에 없습니다.

##### 1-1) 상속

![JPA](https://user-images.githubusercontent.com/59816811/116509967-26710500-a8ff-11eb-971f-140a9458f50f.png)

Item의 카테고리로 Album, Movie, Book이 있는 상황을 생각해보자. 객체는 왼쪽 그림과 같이 상속을 이용해서 설계를 하면 된다. 그리고 DB는 오른쪽 그림과 같이 설계하게 된다.

**[ Album 저장하는 상황 ]**

- 객체는 Album 객체를 생성해서 값을 넣어주면 된다.


- 데이터베이스는 ITEM, ALBUM 테이블 2개를 Insert 해야된다.


**[ Album 을 조회하는 상황 ]**

- 각각의 테이블에서 Item, Album 정보를 받아오고, 각각의 객체를 생성해서 정보를 넣어주는 등 복잡한 상황이 발생한다.

  --> DB에 저장할 객체에는 상속 관계를 사용하기 어렵다.

<br>

##### 1-2) 연관관계

![JPA_2](https://user-images.githubusercontent.com/59816811/116510313-b020d280-a8ff-11eb-8056-2d0d6d86af00.png)

Member 객체는 Team 객체를 하나씩 포함하고 있다고 하자.

그러면, 객체는 Member.getTeam() 을 이용해서 Team을 참조할 수 있지만, 테이블은 외래키를 사용해야된다.

또, DB 테이블에서는 TEAM 에서도 MEMBER 테이블을 참조할 수 있고, MEMBER 테이블도 TEAM 테이블을 참조할 수 있지만 객체는 아니다.

<br>

##### 1-3) 객체 탐색

더 나아가 Member 객체에 주문한 데이터에 대한 정보 Order 클래스가 추가되었다고 하자. Member 객체를 조회할 때, 어떻게 SQL 쿼리를 작성하느냐에 따라, Team, Order 객체 정보도 조회할지 결정되므로 우리는 Member 객체를 조회해도 Team, Order 객체 정보가 같이 넘어온지 바로 알 수 없다.

```java
memberDAO.getMember();
memberDAO.getMemberWithTeam();
memberDAO.getMemberWithTeamWithOrder();
```

 이처럼 우리가 그냥 객체 입장에서 생각하면 Member 객체에서 Order객체, Team 객체 모두 자유롭게 탐색할 수 있는 것이 맞으나! 테이블 설계상 그럴 수 없다.

<br>

##### 1-4) 객체 비교

```java
Member member1 = memberDAO.getMember(memberId);
Member member2 = memberDAO.getMember(memberId);
// member1 == member2 : false
```

DB 테이블에서 memberID 를 이용해 select 쿼리를 날려서 Member 객체를 가지고오면, 같은 memberID 라도 member1과 member2는 다른 객체가 된다.

<br>

**객체를 자바 컬렉션에 저장하듯이, 객체 참조, 상속 등 위의 문제들이 없을 수는 없을까??**

**--> JPA 등장!!!! ( ORM )**

<br>

### 2) JPA ( Java Persistence API )

JPA는 자바 진영의 **ORM** 기술 표준으로 JAVA 애플리케이션과 JDBC 사이에서 동작하는 ORM 프레임워크

- JPA 1.0 (JSR 220) 2006년 : 초기 버전.
- JPA 2.0 (JSR 317) 2009년 : 대부분의 ORM 기능을 포함, JPA Criteria 추가
- JPA 2.1 (JSR 338) 2013년 : 컨버터, 엔티티 그래프, 스토어드 프로시저 접근 기능이 추가

> ORM
>
> - Object-Relational-Mapping
> - 객체는 객체대로 설계, 관계형 데이터베이스는 관계형 데이터베이스대로 설계하면 ORM이 중간에서 다리가 되어준다!

JPA를 사용하면 위에서 언급한 패러다임의 불일치로 나타나는 모든 문제가 해결된다.

##### 2-1) 반복적인 SQL 쿼리 개발

JPA 에서는 반복적인 SQL 쿼리를 개발할 필요가 없습니다.

- 저장 : jpa.persist()
- 조회 : jpa.find()
- 수정 : 객체.setName()
- 삭제 : jpa.remove()

이미 JPA 에서 CRUD 를 다 지원해줍니다.

<br>

##### 2-2) 유지 보수

JPA 에서 관리하는 객체는 필드가 변경되더라도 SQL 쿼리를 수정할 필요가 없습니다.

```java
public class Member {
	private Long memberId;
	private Long phoneNumber; // 추가된 필드!
}
// INSERT INTO MEMBER (member_id, phone_number) 로 수정할 필요가 없다.
```

<br>

##### 2-3) 상속

![JPA](https://user-images.githubusercontent.com/59816811/116509967-26710500-a8ff-11eb-971f-140a9458f50f.png)

```java
jpa.persist(Album);
// --> INSERT 쿼리가 Item, Album 테이블에 자동으로 두 번 날라간다.

jpa.find(Album);
// --> JOIN을 이용한 SELECT 쿼리가 자동으로 날라간다.
```

상속 관계의 객체도 JPA 에서 알아서 쿼리를 작성해서 테이블에 저장하고, 테이블에서 조회할 수 있도록 도와줍니다.

<br>

##### 2-4) 객체 탐색

JPA를 이용해 find 한 객체가 참조하는 모든 객체를 조회할 수 있습니다.

```java
Member member = jpa.find(memberId, Member.class);
member.getTeam(); // jpa에서 필요하면 select 쿼리를 날려준다.
member.getOrder(); // jpa에서 필요하면 select 쿼리를 날려준다. 
```

<br>

##### 2-5) 객체 비교

JPA를 이용해 조회한 객체는 동일한 트랜잭션에서 같음을 보장해줍니다.

```java
Member member1 = jpa.find(memberId, Member.class);
Memeber member2 = jpa.find(memberId, Member.class);
// member1 == member2 : true
```

<br>

##### 2-6) 성능 최적화

JPA 는 다음과 같은 기능등을 지원해주고, 이를 이용해 JPA를 안쓰는 경우보다 오히려 성능을 최적화 할 수 있습니다.

- 1차 캐시와 동일성 보장
- 트랜잭션을 지원하는 쓰기 지연
- 지연 로딩 

