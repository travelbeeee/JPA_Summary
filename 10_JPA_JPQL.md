# JPA_JPQL ( Java Persistence Query Language )

### 0) JPQL 이란

 JPA를 사용하면 엔티티 객체를 중심으로 개발을 하게 된다. 따라서, 검색을 할 때도 테이블이 아닌 엔티티 객체를 대상으로 검색을 하게된다. 결국 애플리케이션이 필요한 데이터만 DB에서 불러오려면 검색 조건이 포함된 SQL이 필요하다.

 JPA는 SQL을 추상화한 JPQL 이라는 객체 지향 쿼리 언어를 제공하고, JPQL을 이용하면 엔티티 객체를 대상으로 쿼리를 날릴 수 있습니다. (테이블을 대상으로 쿼리를 날리는 SQL과 차이)  또, SQL 과 문법이 유사해 SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 을 지원해줍니다.

> **정리!!**
>
> ##### JPQL 이란 객체 지향 SQL ! 또, 특정 SQL 문법에 의존적이지 않다. ( 추상화해서 다 지원해줌 )

<br>

### 1) JPQL 문법

- JPQL 키워드는 대소문자 구분 X ( select, from )

- 엔티티와 속성은 대소문자 구분 O ( Member m, m.age )

- 테이블 이름이 아니라 엔티티 이름을 사용하고 **별칭이 필수**다!

- GROUP BY, HAVING, ORDER BY, COUNT, SUM, AVG, MIN , DISTINCT, BETWEEN, LIKE, IS NULL 등 다 사용 가능

- TypeQuery : 반환 타입이 명확할 때 사용한다.

  ```java
  TypedQuery<Member> query = em.createQuery("SELECT m FROM Member m", Member.class);
  ```

- Query : 반환 타입이 명확하지 않을 때 사용

  ```java
  Query query = em.createQuery("SELECT m.username, m.age FROM Member m");
  ```

- query.getResultList() : 결과가 없으면 빈 리스트를 반환하고, 결과가 있으면 담아서 리스트로 반환해준다. ( NullPointerException 에서 자유롭다. )

- query.getSingleResult() : 결과가 정확히 하나! 단일 객체를 반환해준다. 결과가 없으면 `javax.persistence.NoResultException` 이 발생하고, 둘 이상이면 `javax.persistence.NonUniqueResultException` 이 발생한다.

- 파라미터 바인딩 - 이름 기준, 위치 기준

  ```java
  TypedQuery<Member> query = em.createQuery("SELECT m FROM Member m where m.username=:username", Member.class)
      .setParameter("username", usernameValue);
  	//.setParameter(1, usernameValue);
  ```

기본 문법은 SQL 과 비슷하다. 다만, 엔티티를 대상으로 조회를 한다는 차이가 있다.

<br>

### 2) 프로젝션

 프로젝션은 SELECT 절에 조회할 대상을 지정하는 행위를 말한다. 대상으로는 엔티티 타입, 임베디드 타입, 스칼라 타입(숫자, 문자 등 기본 데이터 타입) 이 있다.

- SELECT m FROM Member m --> 엔티티 프로젝션
- SELECT m.team FROM Member m --> 엔티티 프로젝션
- SELECT m.address FROM Member m --> 임베디드 타입 프로젝션
- SELECT m.username, m.age FROM Member m --> 스칼라 타입 프로젝션

> 엔티티 프로젝션으로 가져온 객체는 모두 영속성 컨텍스트에서 관리된다.

 엔티티 프로젝션 혹은 임베디드 타입 프로젝션은 우리가 반환 타입을 명확히 기술해 줄 수 있다. 하지만, 스칼라 타입을 여러 가지 조회하면 어떻게 받을 수 있을까??

 먼저, Object[] 객체로 값을 받아올 수 있다. 

```java
List<Object[]> resultList = em.createQuery("select m.username, m.age from Member m")
    .getResultList();
Object[] result = resultList.get(0);
// result[0] 은 m.username, result[1]은 m.age 가 된다.
```

하지만, 사용하기가 불편하다. 

DTO 를 통해 값을 받아올 수 있다.

```java
package jpql;

public class MemberDTO {
    private String username;
    private int age;

    public MemberDTO(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
```

```java
em.createQuery("select new jpql.MemberDTO(m.username, m.age) from Member m", MemberDTO.class)
                    .getResultList();
```

 **DTO를 이용할 때는 패키지 명을 포함한 전체 클래스명을 입력해줘야되고, 순서와 타입이 일치하는 생성자가 필요합니다.** 

<br>

### 3) 페이징

JPA는 페이징을 다음 두 API로 추상화해서 지원합니다.

- setFirstResult(int startPosition) : 조회 시작 위치
- setMaxResults(int maxResult) : 조회할 데이터 수

```java
em.createQuery("select m from Member m order by m.age")
    .setFirstResult(0)
    .setMaxResults(10)
    .getResultList();
// --> 맨 앞에서부터 10개 뽑아오기!
```

> SQL 방언에 따라 적절하게 SQL Query 를 생성해준다.

<br>

### 4) 조인

JPA는 SQL에서 지원해주는 조인을 모두 지원해준다.

- 세타 조인 ( Theta Join )

  세타 조인을 하게 되면 테이블의 행을 모두 조인하는 Cartesian Product 가 일어난다.

  ```java
  select count(m) from Member m, Team t where m.username = t.name
  ```

- 내부 조인 ( Inner Join )

  ```java
  em.createQuery("select m from Member m inner join m.team t", Member.class)
          .getResultList();
  
          
  em.createQuery("select m from Member m inner join m.team t on m.xxx = t.xxx", Member.class)
          .getResultList();
  
  // Member Team 엔티티가 연관관계가 없을 때
  em.createQuery("select m from Member m inner join Team t", Member.class)
          .getResultList();
  
  // Member Team 엔티티가 연관관계가 없을 때
  em.createQuery("select m from Member m inner join Team t on m.xxx = t.xxx", Member.class)
          .getResultList();
  ```

  연관관계가 있는 엔티티끼리도 내부 조인이 가능하고, 연관관계가 없는 엔티티끼리도 내부 조인이 가능하다. ON 절을 이용한 결합 조건을 설정하는 것도 가능하다.

- 외부 조인 ( Outer Join )

  ```java
  em.createQuery("select m from Member m left join m.team t", Member.class)
          .getResultList();
          
  em.createQuery("select m from Member m left join m.team t on m.xxx = t.xxx", Member.class)
          .getResultList();
  
  em.createQuery("select m from Member m left join Team t", Member.class)
          .getResultList();
  
  em.createQuery("select m from Member m left join Team t on m.xxx = t.xxx", Member.class)
          .getResultList();
  ```

  외부 조인도 마찬가지다.

<br>

### 5) 서브쿼리

JPA는 일반적인 SQL의 서브 쿼리를 다 지원해준다.

- [NOT] EXISTS : 서브쿼리에 결과가 존재하면 참
  - ALL 모두 만족하면 참
  - ANY, SOME : 조건을 하나라도 만족하면 참
- [NOT] IN : 서브쿼리의 결과 중 하나라도 같은 것이 있으면 참

하지만, SQL의 서브 쿼리와 다르게 다음의 제약 조건이 있다.

- **JPA는 WHERE, HAVING 절에서만 서브 쿼리 사용 가능**
- **하이버네이트에서는 SELECT 절에서도 서브 쿼리 사용 지원**
- **FROM 절의 서브 쿼리는 JPQL 에서 사용 불가능**
  - 조인으로 풀 수 있으면 풀어서 해결하고, 아니면 Native SQL 을 이용해야한다.

```java
//예시

// 팀 A 소속인 회원
select m from Member m where exists (select t from m.team t where t.name = ‘팀A')
           
// 전체 상품 각각의 재고보다 주문량이 많은 주문들
select o from Order o where o.orderAmount > ALL (select p.stockAmount from Product p)
                                     
// 어떤 팀이든 소속된 회원
select m from Member m where m.team = ANY (select t from Team t)
                                     
// 불가능 --> from 절 안에 서브쿼리 존재!
select mm from (select m. age from Member m) as mm
```

<br>

### 6) JPQL 타입 표현

- 문자 : 'Hello', 'She''s'
- 숫자 : 10L, 10D, 10F, 10
- Boolean : True, False
- ENUM : 패키지명을 포함해서 기술해야한다.

```java
Strign query = "select m.uesrname from Member m where m.type = jpql.MemberType.USER"; // 패키지명 기술

// 파라미터 바인딩을 이용
Strign query = "select m.uesrname from Member m where m.type = :type"; 

em.createQuery(query)
    .setParameter("type", MemberType.USER);
```

<br>

### 7) 조건식 - CASE 식

 JPQL 에서는 기본 CASE 식, 단순 CASE 식, COALESCE, NULLIF 를 모두 사용할 수 있다.

- COALESCE : 하나씩 조회해서 null이 아니면 반환
- NULLIF : 두 값이 같으면 null 반환, 다르면 첫 번째 값 반환

```java
"select case when m.age <= 10 then '학생요금' " +
			"when m.age >= 60 then '경로요금' " +
			"else '일반요금' " +
            "end " +
            "from Member m";
            
"select
	case t.name
		when '팀A' then '인센티브110%'
		when '팀B' then '인센티브120%'
		else '인센티브100%'
    end
from Team t"

// 사용자 이름이 없으면 이름없는회원을 반환
"select coalesce(m.username, '이름없는회원') from Member m"

// 사용자 이름이 '관리자' 면 null을 반환, 나머지는 본인의 이름 반환
"select NULLIF(m.username, '관리자') from Member m"
```

<br>

### 8) JPQL 기본 함수

- CONCAT
- SUBSTRING
- TRIM
- LOWER, UPPER
- LENGTH
- LOCATE
- ABS, SQRT, MOD
- SIZE ( 컬렉션의 크기를 반환해줌, JPA 용도)

위의 함수들도 다 지원해줍니다.

<br>