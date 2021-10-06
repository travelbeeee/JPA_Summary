# JPA_Proxy

### 1) 프록시

##### - Proxy

 엔티티를 조회할 때, 연관된 다른 엔티티가 필요한 상황도 있고, 연관된 다른 엔티티가 필요하지 않는 상황도 존재합니다. 예를 들어, Member 엔티티와 Team 엔티티가 서로 일대다 관계일 때 회원이 속한 팀을 체크하는 로직에서는 Member 엔티티를 조회하면서 Team 엔티티도 같이 조회하는게 좋지만, 회원 정보를 체크하는 로직에서는 Member 엔티티만 조회하면 되지 연관된 Team 엔티티도 같이 조회할 필요가 없습니다. 이런 문제를 해결하기 위해 JPA에서는 `Proxy`를 지원해줍니다.

 JPA에서의 `Proxy` 는 가짜 엔티티를 의미합니다. 즉, 실제 DB에서 정보를 가져와서 만들어낸 엔티티가 아니라 비어있는 가짜 엔티티입니다. 

<br>

##### - em.find() vs em.getReference()

 `em.find()` 는 데이터베이스를 통해서 실제 엔티티 객체를 조회하는 메소드입니다.

 `em.getReference()` 는 데이터베이스 조회를 미루는 가짜(프록시) 엔티티 객체를 조회하는 메소드입니다. 즉, `em.getReference()` 메소드를 이용하면 프록시 객체를 조회하게 됩니다.

![jpa_25](https://user-images.githubusercontent.com/59816811/116967402-2dc45400-aced-11eb-9ce3-fc60fdb4d06e.png)

 프록시 클래스는 실제 객체 클래스를 상속 받아서 만들어진 클래스로 실제 객체를 참조할 수 있는 target을 가지고 있고, 프록시 객체를 호출하면 프록시 객체는 참조(target)을 이용해 실제 객체를 호출하게 됩니다.

```java
            //회원 저장
            Member member = new Member();
            member.setName("member1");
            em.persist(member);

            em.flush();
            em.clear();

            Member ref = em.getReference(Member.class, member.getId());
            System.out.println("ref = " + ref.getClass());
```

 멤버 프록시 객체를 가져와 class 명을 출력하면 다음과 같다.

```
ref = class hellojpa.Member$HibernateProxy$57ljURU3
```

Member class가 아니라 HibernateProxy 가 추가로 클래스명에 붙어있는 것을 알 수 있다. 

그러면, em.getReference 메소드를 실행할 때 select query 가 발생할까??

 결론은 select query가 발생하지 않습니다.  **프록시 객체는 실제 객체를 참조해야될 때 select query를 발생해 실제 객체를 DB에서 가져와 영속성 컨텍스트에 보관하고 프록시 객체의 참조(target) 에 실제 객체를 연결해준다.**

<br>

##### - em.getReference() 와 영속성 컨텍스트

 처음에 em.getReference 로 Member 객체를 가지고 오게 되면, MemberProxy 객체가 반환이 되고, 실제 Member 객체의 이름을 참조하기 위해 getName 메소드를 실행하면 이때, select query가 발생해 DB에서 실제 엔티티 정보를 조회해오게 된다.

![jpa_26](https://user-images.githubusercontent.com/59816811/116967664-cd81e200-aced-11eb-99f6-382165e027e5.png)

 당연히, 영속성 컨텍스트에 가져오려는 엔티티 정보가 있다면 em.getReference 로 객체를 찾아와도 프록시 객체가 아니라 실제 객체를 가지고오게된다. 프록시 객체를 굳이 생성해서 반환해야할 이유가 없기 때문이다.

```java
//회원 저장
Member member = new Member();
member.setName("member1");
em.persist(member);

//            em.flush();	
//            em.clear();

Member ref = em.getReference(Member.class, member.getId()); 
System.out.println("ref = " + ref.getClass()); 
```

```java
ref = class hellojpa.Member 
```

<br>

그러면, em.getReference 로 프록시 객체를 먼저 현재 트랜잭션에서 가지고 있으면 em.find 로 진짜 객체를 찾아올 수 있을까??

em.find 는 진짜 객체를 찾아와야되므로 일단 select query는 나간다. 하지만, em.getReference로 프록시 객체를 먼저 찾아왔으므로 같은 트랜젝션에서 같은 객체를 참조하도록 em.find 의 결과도 프록시 객체가 된다. 하지만, em.find 로 실제 DB에서 엔티티 정보를 찾아왔으므로 프록시 객체의 참조(target)가 셋팅되어있다!

```java
//회원 저장
Member member = new Member();
member.setName("member1");
em.persist(member);

em.flush();
em.clear();

Member ref = em.getReference(Member.class, member.getId());
System.out.println("ref = " + ref.getClass());
Member findMember = em.find(Member.class, member.getId());
System.out.println("findMember = " + findMember.getClass());
System.out.println("ref.getName() = " + ref.getName());
```

```
ref = class hellojpa.Member$HibernateProxy$IEIEhzbw
    select
        member0_.MEMBER_ID as member_i1_4_0_,
        member0_.createdBy as createdb2_4_0_,
        member0_.createdTime as createdt3_4_0_,
        member0_.name as name4_4_0_ 
    from
        Member member0_ 
    where
        member0_.MEMBER_ID=?
findMember = class hellojpa.Member$HibernateProxy$IEIEhzbw
ref.getName() = member1
```

<br>

반대로 em.find 를 먼저 하고, em.getReference를 하면 어떻게 될까

em.find 실행 시점에 DB에서 엔티티를 조회하고 영속성 컨텍스트에서 엔티티를 관리하게 된다. 따라서, 그 뒤에 getReference로 조회하더라도 같은 트랜젝션에서 같은 객체를 참조하게 하기 위해 JPA 에서 프록시 객체가 아닌 실제 객체를 반환해준다.

```java
//회원 저장
Member member = new Member();
member.setName("member1");
em.persist(member);

em.flush();
em.clear();


Member findMember = em.find(Member.class, member.getId());
System.out.println("findMember = " + findMember.getClass());
Member ref = em.getReference(Member.class, member.getId());
System.out.println("ref = " + ref.getClass());
System.out.println("ref.getName() = " + ref.getName());
```

```
    select
        member0_.MEMBER_ID as member_i1_4_0_,
        member0_.createdBy as createdb2_4_0_,
        member0_.createdTime as createdt3_4_0_,
        member0_.name as name4_4_0_ 
    from
        Member member0_ 
    where
        member0_.MEMBER_ID=?
findMember = class hellojpa.Member
ref = class hellojpa.Member
ref.getName() = member1
```

<br>

##### - PersistenceUnitUtil.isLoaded(Obejct entity)

PersistenceUnitUtil.isLoaded(Obejct entity)   메소드를 이용해 초기화 여부를 확인할 수 있다.

```java
Member refMember = em.getReference(Member.class, member.getId());
System.out.println("refMember.getName() 실행 전");
System.out.println(emf.getPersistenceUnitUtil().isLoaded(refMember)); // false

refMember.getName();
System.out.println("refMember.getName() 실행 후");
System.out.println(emf.getPersistenceUnitUtil().isLoaded(refMember)); // true
```

<br>

##### - Hibernate.initialize(Object entity)

Hibernate.initialize(Object entity) 메소드를 이용해 강제 초기화할 수 있다.

```java
Member refMember = em.getReference(Member.class, member.getId());
System.out.println("refMember.getName() 실행 전");
System.out.println(emf.getPersistenceUnitUtil().isLoaded(refMember)); // false

Hibernate.initialize(refMember);

System.out.println("refMember.getName() 실행 후");
System.out.println(emf.getPersistenceUnitUtil().isLoaded(refMember)); // true
```

<br>

##### 프록시 정리

- 프록시 객체는 처음 사용할 때 한 번만 초기화( 참조에 값 설정 )을 하게 되고, 이는 실제 엔티티로 바뀌는 것이 아니라 프록시 객체를 통해서 실제 엔티티에 접근이 가능하게 되는 것이다.

- 프록시 객체는 원본 엔티티를 상속받으므로 `==` 으로 타입 비교를 하면 안된다.

- 영속성 컨텍스트의 도움을 받을 수 없는 준영속 상태일 때, 프록시를 초기화하면 문제가 발생한다.

  ```java
  //회원 저장
  Member member = new Member();
  member.setName("member1");
  em.persist(member);
  
  em.flush();
  em.clear();
  
  Member refMember = em.getReference(Member.class, member.getId());
  
  // em.close(); 도 마찬가지!
  em.detach(refMember);
  
  refMember.getName();
  // --> 결과는 에러 발생!
  // org.hibernate.LazyInitializationException: could not initialize proxy [hellojpa.Member#1] 
  ```

  refMember는 프록시 객체이므로 영속성 컨텍스트에게 초기화 요청을 해야한다. 하지만, refMember가 준영속 상태이므로 영속성 컨텍스트에게 초기화 요청을 할 수 없게 된다. 

- 영속성 상태의 프록시 객체에 값을 변경하면 영속성 컨텍스트의 `Dirty Checking` 기능을 이용할 수 있다. 즉, 실제 엔티티와 동일하게 동작한다.

  ```java
  @Test
  @Transactional
  void 프록시객체테스트(){
      Member refMember = em.getReference(Member.class, 1L);
      PersistenceUnitUtil persistenceUnitUtil = emf.getPersistenceUnitUtil();
      System.out.println("isLoaded : " + persistenceUnitUtil.isLoaded(refMember)); // false
      refMember.getEmail(); // select query
      System.out.println("isLoaded : " + persistenceUnitUtil.isLoaded(refMember)); // true
      refMember.changePassword("kkkkk"); 
      em.flush(); // update query
  }
  ```

   

  

