# JPA_영속성

### 1) JPA 기본 동작 방식

![jpa_3](https://user-images.githubusercontent.com/59816811/116521142-8e7b1780-a90e-11eb-86f1-fa87b4152b66.png)

JPA는 먼저`Persistence` 객체에서 설정 정보를 조회해야한다. 그 후, `Persistence` 객체가 `EntityManagerFactory` 를 만들어주고, 필요할 때마다 `EntityManager`를 만들어서 실제 쿼리를 날릴 수 있다. 웹 어플리케이션을 예로 들면, `엔티티 매니저 팩토리`가 고객의 요청이 올 때마다 `엔티티 매니저`를 생성하고, `엔티티 매니저`는 내부적으로 `DB 커넥션`을 사용해서 데이터 베이스를 사용합니다.

<br>

> ##### 자주 사용되는 설정정보
>
> ```xml
> <property name="hibernate.show_sql" value="true"/> 1번
> <property name="hibernate.format_sql" value="true"/> 2번
> <property name="hibernate.use_sql_comments" value="true"/> 3번
> 
> // persistence.xml 파일
> ```
>
> 1번 설정 정보를 통해 sql 문을 아래와 같이 출력하고, 2번 설정 정보를 통해 sql 문을 아래처럼 예쁘게 출력하고, 3번 설정 정보를 통해 /* insert hellojpa.Member */ 부분과 같이 우리에게 도움이 되는 주석 정보도 같이 출력해준다.
>
> ```
> Hibernate: 
>     /* insert hellojpa.Member
>         */ insert 
>         into
>             Member
>             (name, id) 
>         values
>             (?, ?)
>             
> ```

<br>

### 2) 엔티티

 JPA는 내부적으로 `엔티티 매니저 팩토리`, `엔티티 매니저`가 동작한다고 언급했습니다. 그럼, 계속 등장하는 엔티티란 무엇일까요?? **엔티티란 JPA가 관리하는 객체를 의미**합니다.

 쉽게 생각하면, DB 테이블에 대응하는 하나의 클래스라고 생각할 수도 있습니다.

<br>

### 3) 영속성 컨텍스트

 JPA 는 엔티티를 `영속성 컨텍스트` 에 저장합니다. `영속성 컨텍스트`는 논리적 개념으로 물리적으로 존재하는 공간은 아닙니다. **엔티티를 영구 저장하는 환경**이라는 뜻으로 JPA에서 제일 중요한 개념입니다. 엔티티 매니저를 통해 영속성 컨텍스트에 접근을 할 수 있다.

 JPA를 이용하면 엔티티를 `DB`에 저장하는 것이 아니라 `영속성 컨텍스트`에 저장하는 것이다.

<br>

### 4) 엔티티 생명주기

- 비영속 (new/ transient)

  : 영속성 컨텍스트와 전혀 관계가 없는 새로운 상태

- 영속 (managed)

  : 영속성 컨텍스트에 관리 되는 상태

- 준영속 (detached)

  : 영속성 컨텍스트에 저장되었다가 분리된 상태

- 삭제 (removed)

  : 삭제된 상태

![jpa_4](https://user-images.githubusercontent.com/59816811/116646416-f2efb280-a9b2-11eb-902f-60634963187e.png)

​	준영속 상태는 영속상태에 있던 엔티티가 영속성 컨텍스트에서 분리된 상태를 말하고, 당연히 영속성 컨텍스트가 제공하는 기능을 사용하지 못하게 됩니다. 아래 3가지 방법으로 영속성 엔티티를 준영속 엔티티로 만들 수 있습니다.

```java
entityManager.detach(entity) // 특정 엔티티만 준영속 상태로 전환
entityManager.clear(); // 영속성 컨텍스트 초기화 --> 엔티티 매니저 안에 있는 영속성 컨텍스트를 초기화
entityManager.close(); // 영속성 컨텍스트 종료
```

<br>

### 5) 영속성 컨텍스트 장점

- #### **1차 캐시**

   영속성 컨텍스트는 내부에 1차 캐시를 가지고 있어서 DB에 접근하기 전에 먼저 1차 캐시에 접근한다. 따라서, 1차 캐시에 올라가있는 엔티티들은 DB 접근 없이도 정보를 조회할 수 있다.

  ```java
  // 이때는 1차 캐시에 1번 member정보가 없으므로 DB접근!
  Member member1 = em.find(Member.class, 1L);
  
  // 위에서 DB에 접근해 1차 캐시에 1번 Member를 저장해두었으므로 1차 캐시에서 바로 가져올 수 있다.
  Member member2 = em.find(Member.class, 1L);
  ```

- ####  엔티티의 동일성 보장

   JPA는 기본적으로 자바 컬렉션에서 객체를 다루듯이 엔티티를 다룰 수 있도록 해준다. 따라서, 같은 member 엔티티를 찾아오면 둘은 같은 객체가 된다.

  ```java
  Member member1 = em.find(Member.class, 1L);
  Member member2 = em.find(Member.class, 1L);
  
  // member1 == member2 는 true가된다.
  ```

- #### 트랜젝션을 지원하는 쓰기 지연

   EntityManager는 데이터 변경시 트랜잭션 단위 안에서 작업을 해야한다. 따라서, 트랜잭션 단위로 SQL Query를 모아서 수행하고, 매번 DB에 접근해 SQL Query 를 수행하지 않는다. 수행해야하는 SQL Query를 쓰기 지연 SQL 저장소에 모아두었다가 한 번에 수행한다.

  ![JPA_5](https://user-images.githubusercontent.com/59816811/116646763-dbfd9000-a9b3-11eb-9338-08c6c8478ebd.png)

  ```java
  EntityManager em = emf.createEntityManager();
  EntityTransaction transaction = em.getTransaction();
  
  //엔티티 매니저는 데이터 변경시 트랜잭션을 시작해야 한다.
  transaction.begin(); // [트랜잭션] 시작
  
  em.persist(memberA);
  em.persist(memberB);
  //여기까지 INSERT SQL을 데이터베이스에 보내지 않는다.
  //커밋하는 순간 데이터베이스에 INSERT SQL을 보낸다.
  
  transaction.commit(); // [트랜잭션] 커밋
  ```

  > ```
  > <property name="hibernate.jdbc.batch_size" value="10"/>
  > ```
  >
  > 이 속성을 통해 한 번에 몇 개씩 쌓아뒀다가 쿼리를 날리지 정할 수 있음!

- #### 엔티티 변경 감지 (dirty checking)

  JPA는 엔티티를 변경할 때 update 를 따로 수행해주지 않아도 엔티티 값을 변경하면 자동으로 update query를 생성해서 수행해준다.

  ```java
  // 영속 엔티티 조회
  Member memberA = em.find(Member.class, "memberA");
  // 영속 엔티티 데이터 수정
  memberA.setUsername("hi");
  memberA.setAge(10);
  // 엔티티 데이터를 수정하면 update query가 위에서 설명한 쓰기 지연 SQL 저장소에 쌓이게 된다.
  ```

  원리는 다음과 같다.

  ![JPA_6](https://user-images.githubusercontent.com/59816811/116646878-2979fd00-a9b4-11eb-8f08-569723a1da1f.png)

   영속성 컨텍스트에서 flush() 가 일어나면 1차 캐시에 올라와있는 엔티티 정보(스냅샷, 처음에 엔티티 정보를 읽어올 때 저장해둔 정보)와 현재 엔티티 정보를 하나하나 다 비교 (dirty checking) 해서 update query를 쓰기 지연 SQL 저장소에 쌓아주고 쿼리가 다른 쿼리들과 한 번에 수행된다.

<br>

### 6) 플러시

 영속성 컨텍스트의 변경내용을 데이터베이스에 반영하는 작업을 말합니다.

 플러시가 실행되면 변경 감지가 일어나고, 수정된 엔티티 값들이 쓰기 지연 SQL 저장소에 등록되고, 쓰기 지연 SQL 저장소에 쌓인 쿼리를 데이터베이스에 전송하게 된다. 또, 플러시를 한다고 1차 캐시에 있는 정보가 지워지지는 않는다.

 아래와 같은 3가지 상황에서 플러시가 발생한다.

```java
em.flush(); // 직접 플러시를 수행
transaction.commit(); // 트랜젝션 커밋 시점 -> 플러시 자동 호출
em.createQuery(); // JPQL 쿼리 실행 -> 플러시 자동 호출
```

 트랜젝션을 커밋하는 시점에서는 당연히 영속성 컨텍스트의 변경내용을 데이터베이스에 반영해야 하므로 플러시가 일어나야된다. 그러면 JPQL 쿼리를 실행할 때는 왜 플러시가 일어나야될까??

```java
em.persist(memberA);
em.persist(memberB);
em.persist(memberC);
//중간에 JPQL 실행
query = em.createQuery("select m from Member m", Member.class);
List<Member> members= query.getResultList();
```

 위와 같은 상황에서 memberA, memberB, memberC는 영속성 컨텍스트의 1차 캐시에 올라와있고, insert 문이 DB에 날라간 것이 아니라 쓰기 지연 SQL 저장소에 저장되어있는 상태다. 이때, 모든 member 엔티티를 가져와달라는 JPQL 쿼리를 날리게 되면 flush 가 선행되지않는다면 memberA, memberB, memberC 엔티티 정보를 가져올 수 없게 된다. 이런 여러 상황을 때문에 JPQL 쿼리를 날리게 되면 자동으로 flush가 수행된다.

<br>

##### 플러시 모드 옵션

플러시는 2가지 모드가 있습니다.

- FlushModeType.AUTO : 커밋이나 쿼리를 실행할 때 플러시 (디폴트)
- FlushModeType.COMMIT : 커밋할 때만 플러시

AUTO 모드를 주로 사용하게 됩니다.

> 플러시는 영속성 컨텍스트를 비우는게 아니라 변경 내용을 데이터베이스에 동기화하는 작업이다!!!
