# JPA_엔티티매핑

 우리가 만든 클래스와 DB 테이블을 매핑하는 방법에 대해서 정리해보겠습니다.

<br>

### 1) 엔티티 매핑

 엔티티를 매핑하기 위해 사용하는 애노테이션들입니다.

- 객체와 테이블 매핑 : @Entity, @Table
- 필드와 컬럼 매핑 : @Column
- 기본 키 매핑 : @Id
- 연관관계 매핑 : @ManyToOne, @JoinColumn 등등

<br>

### 2) @Entity

@Entity가 붙은 클래스는 JPA가 관리하는 엔티티가 됩니다. JPA를 사용해서 테이블과 매핑할 클래스는 @Entity 애노테이션이 필수입니다.

@Entity 애노테이션을 통해 엔티티로 등록할 객체는 다음과 같은 조건을 만족해야합니다.

- 기본 생성자(파라미터가 없는 생성자) 필수( public 또는 protected 로 )
- final 클래스, inner 클래스, enum, interface 에 사용 불가능
- 저장할 필드에 final 사용 불가능

```java
@Entity
public class Member {
    public Member() {
    }
}
```

#### - name 속성

**@Entity(name = "")**  를 통해 JPA에서 사용할 엔티티 이름을 지정할 수 있습니다. 기본 값은 클래스 이름을 그대로 사용하게 되고, 가급적 기본값을 사용하는게 좋습니다.

<br>

### 3) @Table

@Table은 엔티티와 매핑할 테이블을 지정합니다. 기본적으로 엔티티로 등록된 클래스는 **@Table 애노테이션이 없다면 엔티티 이름으로 된 테이블과 매핑됩니다.**

```java
// 디폴트 값으로 엔티티명이 Member가 된다. --> Member 테이블과 매핑
@Entity
public class Member {
    public Member() {
    }
}

// 엔티티명인 user 테이블과 매핑
@Entity(name = "user")
public class Member {
    public Member() {
    }
}

// 엔티티 명은 Member 이지만 user 테이블과 매핑
@Entity
@Table(name = "user")
public class Member {
    public Member() {
    }
}
```

@Table 애노테이션을 이용하면 매핑할 테이블을 직접 지정할 수 있습니다.

<br>

### 4)  DB Schema 자동 생성

xml에 아래와 같은 속성을 지정하면 데이터베이스 스키마 생성 방식을 정할 수 있습니다. 데이터베이스 방언을 활용해서 데이터베이스에 맞는 DDL 쿼리를 생성해줍니다.

```xml
<property name="hibernate.hbm2ddl.auto" value="" />
```

- create : 기존 테이블 삭제 후 다시 생성
- create-drop : create와 같으나 종료시점에 테이블 DROP
- update : 변경분만 반영
- validate : 엔티티와 테이블이 정상 매핑되었는지 확인
- none : 사용하지 않음

> 운영 서버에서는 절대 절대 create, create-drop, update를 사용하면 안된다!!
>
> why?  테이블이 자동 create, drop 되면서 운영 데이터가 다 날라갈 수 있음!!

<br>

### 5) @Column

@Column 애노테이션을 이용하면 필드와 테이블이 column 과 매핑할 수 있습니다. 기본적으로는 필드명이 그대로 테이블의 column 명이 됩니다.

- name : 매핑할 테이블의 column 명을 지정

- nullable : 기본값은 true로 테이블의 해당 column에 null이 가능한지 지정할 수 있습니다.

- insertable : 기본값은 true로 테이블의 해당 column에 값을 insert가 가능한지 지정할 수 있습니다.

- updatable : 기본값은 true로 테이블의 해당 column에 값을 update가 가능한지 지정할 수 있습니다.

  > @Column(updatable = false)
  >
  > --> insertable은 기본값이므로 insert는 가능하나 update는 불가능한 컬럼!

- unique : 기본값은 false로 테이블의 해당 column에 unique 속성을 부여할 수 있습니다.

  > unique 속성은 unique 이름이 랜덤으로 생성되서 실제로는 자주 사용 X!!
  >
  > ex) alter table T add constraint UK_ektea7vp6e~~~ unique 이런 식으로 unique 제약 조건 이름이 랜덤하게 만들어진다.
  >
  > --> @Table(uniqueConstraints = "") 를 사용하자.

- length : 문자 타입에서 글자수 제한을 줄 수 있습니다.

  ```java
  @Column(length = 10) // 회원 이름은 10자 초과 X
  ```

- precision, scale : BigDecimal, BigInteger 타입에서 사용하고 precision은 소수점을 포함한 전체 자릿수를 scale은 소수의 자릿수를 의미합니다. 아주 큰 숫자가 정밀한 소수를 다루어야할 때만 사용합니다.

- columnDefinition : 데이터베이스 컬럽 정보를 직접 줄 수 있습니다.

  > @columnDefinion("varchar(100) default 'EMPTY'")

<br>

### 6) @Enumerated

자바 enum 타입을 매핑할 때 사용합니다!

- EnumType.ORDINAL : enum 순서를 데이터베이스에 저장
- EnumType.STRING : enum 이름을 데이터베이스에 저장

```java
public enum UserType {
    USER, VIP
}

@Entity(name = "user")
public class Member {
    @Enumerated(EnumType.STRING)
    private UserType userType;
}
```

기본 값은 ORDINAL 이므로 항상 STRING 으로 바꿔줘야된다. ORDINAL 을 사용하게 되면 enum 타입에서 먼저 써있는 USER가 0번, VIP를 1번으로 인식해서 DB에 쿼리를 날리게 되는데 enum 타입에 VVIP 를 중간에 추가하게 되면 이미 기존에 VIP 회원들은 1번으로 들어가있으므로 VVIP 가 1번, VIP 가 2번으로 바뀌게 되었을 때 많은 문제가 발생한다!! 

> 절대 절대 ORDINAL 속성을 사용하지 말고, STRING 속성을 사용하자!!

<br>

### 7) @Temporal

날짜 타입을 매핑할 때 사용합니다. (java.util.Date, java.util.Calendar)

- TemporalType.DATE : 날짜, 데이터베이스 date 타입과 매핑
- TemporalType.TIME : 시간, 데이터베이스 time 타입과 매핑
- TemporalType.TIMESTAMP : 날짜와 시간, 데이터베이스 timestamp 타입과 매핑

> LocalDate, LocalDateTime 을 사용할 때는 최신 하이버네이트에서 지원하므로 생략 가능!!
>
> --> LocalDate, LocalDateTime 을 필드에서 그냥 사용하자.

<br>

### 8) @Lob

데이터베이스 BLOB, CLOB 타입과 매핑가능합니다. 지정할 수 있는 속성이 없고, 매핑하는 필드 타입이 문자면 CLOB 매핑, 나머지는 BLOB 으로 매핑해줍니다.

<br>

### 9) @Transient

데이터베이스에 저장하고 조회하고 싶지도 않고 매핑하고 싶지도 않은 필드는 @Transient 애노테이션을 이용하면 됩니다. 주로 메모리상에서만 임시로 어떤 값을 보관하고 싶을 때 사용합니다.

<br>

### 10) @Id

@Id 애노테이션이 붙은 필드는 테이블의 primary_key가 됩니다.

<br>

### 11) @GeneratedValue

@GeneratedValue 는 @Id 로 primary_key 로 지정된 테이블의 속성의 값을 이름 그대로 자동으로 생성해주는 애노테이션입니다.

- **@GeneratedValue(strategy = GenerationType.AUTO)**

: 기본설정으로 방언에 따라 자동으로 primary key 전략을 지정해줍니다. DB에 따라 IDENTITY, SEQUENCE, TABLE 3가지 전략 중 하나를 설정합니다.

- **@GeneratedValue(strategy = GenerationType.IDENTITY)**

  : 기본키 생성을 DB에게 위임합니다. ( MYSQL에서 사용 )

  > IDENTITY 전략은 대표적으로 MySQL의 AUTO_INCREMENT 를 생각하면 된다. AUTO_INCREMENT는 데이터베이스에 Insert SQL을 실행 한 후에 ID 값을 알 수 있다. 우리가 영속성 컨텍스트에 엔티티를 넣고 보관하려면 ID 값이 필요하다. 따라서, IDENTITY 전략은 em.persist() 시점에 즉시 Insert SQL 이 실행된다.

- **@GeneratedValue(strategy = GenerationType.SEQUENCE)**

  : Sequence 를 이용해서 기본키를 생성해줍니다. ( oracle에서 사용 )

  > SEQUENCE 전략도 영속성 컨텍스트에 엔티티를 넣고 보관하려면 ID 값이 필요합니다. 따라서, em.persist() 시점에 call next value for sequence 쿼리가 날라가서 id 값을 받아옵니다. 그 후, commit() 할 때 Insert SQL 이 실행됩니다.

  ```sql
  create sequence hibernate_sequence start with 1 increment by 1
  ```

  - @SequenceGenerator 를 이용해서 Sequence 를 직접 설정할 수 있습니다.

  ```java
  @Entity
  @SequenceGenerator( name = “MEMBER_SEQ_GENERATOR",
  sequenceName = “MEMBER_SEQ", //매핑할 데이터베이스 시퀀스 이름
  initialValue = 1, allocationSize = 1)
  public class Member {
      @Id
      @GeneratedValue(strategy = GenerationType.SEQUENCE,
      generator = "MEMBER_SEQ_GENERATOR")
      private Long id;
  }
  ```

  @Sequence Generator 에 name 값은 필수입니다.

- **@GeneratedValue(strategy = GenerationType.TABLE)**

  : 키 생성용 테이블 사용하는 방식으로 모든 DB에서 사용합니다. 테이블이 하나 더 만들어지므로 성능이 좋지 않습니다.

  > TABLE 전략도 마찬가지로 엔티티를 영속성 컨텍스트에 넣는 em.persist() 시점에 ID값이 필요하다. 따라서, 아래와 같은 쿼리가 em.persist() 시점에 동작한다.
  >
  > ​    update
  > ​        hibernate_sequences 
  > ​    set
  > ​        next_val=?  
  > ​    where
  > ​        next_val=? 
  > ​        and sequence_name=?
  
  ```sql
  create table hibernate_sequences (
      sequence_name varchar(255) not null,
      next_val bigint,
      primary key (sequence_name)
  )
  ```
  
  - @TableGenerator 를 통해 키 생성용 테이블을 설정 할 수 있습니다.
  
    ```java
    @Entity
    @TableGenerator( name = "MEMBER_SEQ_GENERATOR", table = "MY_SEQUENCES",
    				pkColumnValue = "MEMBER_SEQ", allocationSize = 1)
    public class Member {
        @Id
        @GeneratedValue(strategy = GenerationType.TABLE,
        generator = "MEMBER_SEQ_GENERATOR")
        private Long id;
    }
    ```
    

> 주의!!
>
> TABLE / SEQUENCE 전략 모두 @SequenceGenerator, @TableGenerator 없이 여러 개의 엔티티에서 기본 값으로 설정하게 되면 Table 명과 Sequence 명이 겹치게 되서 에러가 발생한다!
>
> --> @SequenceGenerator, @TableGenerator 를 사용해서 명시해주자. 

<br>

- **TABLE / SEQUENCE 전략 최적화 설정**

  SEQUENCE, TABLE 전략은 em.persist() 시점에 ID 값을 알기 위해 Insert 쿼리는 아니지만 DB 와 커넥션이 필요합니다. 이를 최적화 하기 위해 allocationSize 속성을 이용할 수 있습니다. allocationSize 속성은 시퀀스 한 번 호출에 증가하는 수 입니다. 기본 값은 50입니다. 예를 들어, 50인 기본값으로 설정하면 하나의 트랜잭션에서 동시에 50번의 em.persist() 하더라도 한 번만 SEQUENCE / TABLE 를 참조하면 됩니다. 한 번에 50개를 미리 가져와 메모리 상에서 사용하는 방법입니다. 그 대신에 ID 값이 50개씩 증가하므로 ID 값으로 쓰이지 않는 값들이 존재하게 될 수 있습니다.

  
