### 0) 상속관계 매핑

관계형 데이터베이스는 객체처럼 상속 관계가 따로 없다. 그래서, 객체 상속과 유사한 슈퍼타입 - 서브타입 모델링 기법을 이용해서 객체의 상속 관계를 표현해야한다.

물품이 있고, 물품의 카테고리가 음반, 영화, 책으로 나뉘는 경우를 생각해보자. 객체에서는 Item 클래스에 공통된 속성인 name, price를 필드에 추가하고 Album, Movie, Book 클래스에서 Item 클래스를 상속하면서 추가로 필요한 필드를 추가하면 된다.

그러면, RDB 에서는 어떻게 구현해야될까??

![jpa_20](https://user-images.githubusercontent.com/59816811/116847356-57b24380-ac25-11eb-9799-fd1568a3b7ee.png)

슈퍼타입 - 서브타입 모델링 기법을 구현하는 물리적인 방법은 3가지 방법이 있다.

- 조인 전략

  ![jpa_21](https://user-images.githubusercontent.com/59816811/116847528-aeb81880-ac25-11eb-8711-f65604ae58d4.png)
  DB의 Join 을 이용해서 구현하는 방법이다.

- 단일 테이블 전략

  ![jpa_22](https://user-images.githubusercontent.com/59816811/116847525-ae1f8200-ac25-11eb-8f28-30c72999e61d.png)

  테이블 하나에 모든 정보를 넣는 방법이다.

- 구현 클래스마다 테이블 전략

  ![jpa_23](https://user-images.githubusercontent.com/59816811/116847523-ad86eb80-ac25-11eb-89d6-a103d977eaa5.png)

  클래스마다 테이블을 따로 만들어서 관리하는 전략이다.

3가지 방법 모두 큰 틀은 다음과 같다.

- @Inheritance(strategy=InheritanceType.XXX)

  @Inheritance 애노테이션을 통해 3가지 전략 중 어떤 전략을 사용할지 정해줄 수 있다. **기본값은 단일 테이블 전략이다.**

  - JOINED / SINGLE_TABLE / TABLE_PER_CLASS

- @DiscriminatorColumn(name="컬럼명")

  @DiscriminatorColumn 애노테이션을 통해 슈퍼 테이블에 서브 테이블을 구분하는 컬럼을 추가해줄 수 있다. **기본값은 "DTYPE"이다.**

- @DiscriminatorValue

  @DiscriminatorValue 애노테이션을 통해 슈퍼 테이블에서 서브 테이블을 구분하는 컬럼에 어떤 값을 입력해줄지 정할 수 있다. **기본 값은 엔티티명이 그대로 들어간다.**

<br>

부모 클래스인 Item 클래스는 단독으로 엔티티로 사용될 수 없게, abstract class로 선언해줘야한다.

```java
@Entity
@Inheritance(strategy=InheritanceType.XXX)
@DiscriminatorColumn(name="컬럼명")
public abstract class Item {
    @Id @GeneratedValue
    @Column(name = "ITEM_ID")
    private int id;

    String name;
    int price;
}

@Entity
@DiscriminatorValue("ALBUM")
public class Album extends Item{
    private String artist;
}

@Entity
@DiscriminatorValue("BOOK")
public class Book extends Item {
    private String author;
    private String ISBN;
}

@Entity
@DiscriminatorValue("MOVIE")
public class Movie extends Item {
    private String director;
    private String actor;
}


```

<br>

### 1) 조인 전략

![jpa_21](https://user-images.githubusercontent.com/59816811/116847528-aeb81880-ac25-11eb-8711-f65604ae58d4.png)

```java
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn
public abstract class Item {
    @Id @GeneratedValue
    @Column(name = "ITEM_ID")
    private int id;

    String name;
    int price;
}

@Entity
public class Album extends Item{
    private String artist;
}

@Entity
public class Book extends Item {
    private String author;
    private String ISBN;
}

@Entity
public class Movie extends Item {
    private String director;
    private String actor;
}
```

조인전략은 기본적으로 @DiscriminatorColumn 을 설정해주지않으면 자식 클래스를 구분해주는 컬럼이 추가되지않는다. ALBUM, MOVIE, BOOK 테이블이 따로 있기 때문에 구분해주는 컬럼이 없더라도 구분이 가능하기 때문이다. 하지만, 실무에서는 슈퍼 테이블에 구분해주는 컬럼을 추가해주는게 좋다.

- 장점
  - 테이블이 정규화되어있음 --> 외래 키 참조 무결성 제약조건 활요 ㅇ가능
  - 저장공간 효율화
- 단점
  - 조회시 조인을 많이 사용해서 성능을 저하할 수 있다.
  - 조회 쿼리가 조인을 이용해야하므로 복잡하다.
  - 데이터 저장시 INSERT Query가 2번 나간다.

> 조인 전략을 3가지 전략 중 가장 기본 전략으로 많이 사용한다.
>
> abstract class로 부모 클래스를 설정해줘야 하고,
>
> @DiscriminatorColumn 애노테이션을 설정해줘야하는 것을 기억하자!

<br>

### 2) 단일 테이블 전략

![jpa_22](https://user-images.githubusercontent.com/59816811/116847525-ae1f8200-ac25-11eb-8f28-30c72999e61d.png)

```java
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class Item {
    @Id @GeneratedValue
    @Column(name = "ITEM_ID")
    private int id;

    String name;
    int price;
}

@Entity
public class Album extends Item{
    private String artist;
}

@Entity
public class Book extends Item {
    private String author;
    private String ISBN;
}

@Entity
public class Movie extends Item {
    private String director;
    private String actor;
}
```

단일 테이블 전략은 하나의 테이블에 모든 정보가 들어가있는 전략이다. 따라서, 어떤 서브클래스의 정보인지 구분하기 위해 @DiscriminatorColumn 애노테이션이 없더라도 기본으로 구분하는 컬럼이 추가된다.

- 장점
  - 조인이 필요 없으므로 성능이 빠름
- 단점
  - 자식 엔티티가 매핑한 컬럼은 모두 null을 허용해야한다. ( Album 의 경우는 Book, Movie 에 해당하는 컬럼에 null을 넣어줘야한다.)
  - 단일 테이블에 모든 것을 저장하므로 테이블이 커질 수 있다.

<br>

### 3) 구현 클래스마다 테이블 전략

![jpa_23](https://user-images.githubusercontent.com/59816811/116847523-ad86eb80-ac25-11eb-89d6-a103d977eaa5.png)

```java
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class Item {
    @Id @GeneratedValue
    @Column(name = "ITEM_ID")
    private int id;

    String name;
    int price;
}

@Entity
public class Album extends Item{
    private String artist;
}

@Entity
public class Book extends Item {
    private String author;
    private String ISBN;
}

@Entity
public class Movie extends Item {
    private String director;
    private String actor;
}
```

이 전략은 사용하지 않는게 좋다. 예를 들어, 전체 판매 가격을 조회하기 위해 모든 Item의 price를 조회하려고 해도 3개의 테이블을 참조해야되고, Item 번호가 N번인 Item을 조회하려면 3개의 테이블을 UNION 해서 모두 조회해야하는 등 많은 단점이 있다.