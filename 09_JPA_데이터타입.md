# JPA_데이터타입

### 0) JPA 데이터 타입

JPA는 모든 데이터를 2가지 타입으로 분류합니다.

- 엔티티 타입
  - @Entity 로 정의하는 객체로 데이터가 변해도 식별자로 추적이 가능하다.
- 값 타입
  - int, Integer, String 처럼 단순히 값으로 사용하는 자바 기본 타입 혹은 자바 객체 타입으로 식별자가 없고 값만 있으므로 변경시 추적이 불가능하다.

<br>

 정리하면 @Entity 로 정의하는 객체가 모두 엔티티 타입이고, 이외에는 모두 값 타입이 된다. 가장 큰 차이는 엔티티 타입은 추적이 가능하고, 값 타입은 추적이 불가능하다는 것이다. 이로 인해 값 타입은 변경 불가능하게 설계해야된다. 

<br>

### 1) 기본값 타입

우리가 잘 아는 자바의 기본 타입, 래퍼클래스, String 등!

생명주기를 엔티티에 의존하게 되고, 값 타입은 서로 다른 엔티티에서 공유하면 안된다.  ( 예를 들면 Member 엔티티가 삭제되면 그 안에 있는 name, age, id 필드가 다 같이 날라감! )

자바에서 기본 타입은 공유가 불가능하게 지원해준다. 하지만, 래퍼 클래스가 String 같은 특수한 클래스는 공유가 가능하므로 주의해야한다.

```java
int a = 10;
int b = a;

a = 20;
// a = 20, b = 10! --> b가 a의 래퍼런스를 참조하는게 아니다!

Integer a = 20;
Integer b = a;
// 이때는 b가 a의 래퍼런스를 참조하고 있다.
// --> Java에서 값 변경이 불가능하게 지원해준다.
// --> b = 10; 을 하면 b = new Integer(10) 이 작동!
```

 정리하면 기본 값 타입은 JPA 에서 값 타입으로 편하게 사용해도 된다!

<br>

### 2) 임베디드 타입

우리가 정의해서 사용하는 타입으로 주로 자주 사용되는 기본 값 타입을 모아서 만든다. 이를 JPA 에서는 임베디드 타입이라고 한다. 임베디드 타입도 `엔티티 타입`이 아니라 `값 타입`이다.

회원 정보에 도시 / 도로 / 우편번호 (주소) 데이터가 있다고 하자. 이런 주소 데이터는 다른 곳에서도 사용될 가능성이 높고 우리가 따로 클래스로 만드는 것이 객체지향적인 설계다. 

이를 위해, JPA에서는 임베디드 타입을 지원해준다.

- **@Embeddable** : 값 타입을 정의하는 곳에 표시
- **@Embedded** : 값 타입을 사용하는 곳에 표시
- 기본 생성자 필수
- **임베디드 타입은 DB 테이블이 변하는 것이 아니다.**
- 임베디드 타입 안의 필드에 @Column 을 이용해 DB Column 명을 설정해 줄 수도 있다.
- 임베디드 타입이 null 이라면 안에 있는 필드(column) 도 모두 null 값이 들어간다.

임베디드 타입을 사용하면 다음과 같은 장점이 있다.

- 재사용
- 높은 응집도
- 해당 값 타입만 사용하는 의미 있는 메소드를 만들 수 있음.
- 임베디드 타입을 포함한 엔티티의 생명주기에 의존함.

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @Embedded
    Address address;
}

@Embeddable
public class Address {
    @Column(name = "CITY")
    String city;
    String street;
    String zipcode;
}
```

 임베디드 타입을 이용한다고 DB 테이블이 변화하는 것은 아니지만, 객체 입장에서는 묶어서 클래스로 표현하면 의미있는 메소드를 만들 수 있고 응집도가 높아진다.

<br>

##### @AttributeOverrides 속성

 그러면, 같은 임베디드 타입을 중복해서 사용해야되면 어떻게 해야될까?? 예를 들어 주소를 나타내는 `Address` 임베디드 타입이 있고, `Member` 엔티티에서 집 주소를 나타내는 `homeAddress` 필드와 직장을 나타내는 `workAddress` 필드가 있다고 하면 DB에 Column이 중복될 수 밖에 없다. 

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @Embedded
    Address homeAddress;

    @Embedded
    Address workAddress;
}

@Embeddable
public class Address {
    @Column(name = "CITY")
    String city;
    String street;
    String zipcode;
}
```

 이를 위해 JPA에서는 `@Attributeoverride` 속성을 지원해준다.

```java
    @AttributeOverrides({
        @AttributeOverride(name = "city", 
                          column = @Column(name = "WORK_CITY")),
        @AttributeOverride(name = "street", 
                          column = @Column(name = "WORK_STREET")),
        @AttributeOverride(name = "zipcode", 
                          column = @Column(name = "WORK_ZIPCODE")),
    })
    @Embedded
    Address workAddress;
```

@AttributeOverrides 속성을 이용하면 중복되는 임베디드 타입의 column 명을 새롭게 지정해줄 수 있다.

<br>

### 3) 값타입 공유 참조

 기본값 타입은 자바에서 공유가 불가능하게 잘 지원해준다. **하지만, 임베디드 타입은 JPA 입장에서는 값 타입이지만 자바 입장에서는 객체타입이다.** 따라서, 공유가 가능하다.

다음 상황을 생각해보자.

```java
Address address = new Address("city", "street", "zipcode")
Member member1 = new Member();
Member member2 = new Member();

member1.setAddress(address);
member2.setAddress(address);
```

회원1과 회원2가 같은 임베디드 타입을 참조한다고 해보자. 이때, 회원1의 주소를 변경하면 회원2의 주소도 동시에 변경된다.

```java
member1.getAddress().setCity("new City");
```

이런 사이드 이펙트는 잡기가 어렵다.

--> 임베디드 타입 같은 값 타입은 값 자바 입장에서는 객체타입이므로 공유를 막을 수는 없다. 그러니까 값 변경이 불가능하게 만들어버리자

```java
@Embeddable
public class Address {
    String city;
    String street;
    String zipcode;

    public Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public String getZipcode() {
        return zipcode;
    }
}

```

**setter를 다 지워버리고, 처음에 생성자를 통해서만 값을 할당할 수 있도록 설계를 하자.**

```java
Address address = new Address("city", "street", "zipcode")
Member member1 = new Member();
Member member2 = new Member();

member1.setAddress(address);
member2.setAddress(address);
```

이 상황에서 member1의 주소를 변경하려면 다음과 같이 해야한다.

```java
member1.setAddress(new Address("new city", address.getStreet(), address.getZipcode()));
```

**코드는 복잡해보이지만, 값 타입은 절대 변경할 수 없게 설계해야된다. 아니면 굉장히 큰 부작용이 나중에 발생하게 된다!**

**불변객체로 만들자!**

<br>

### 4) 값타입 비교

기본 값 타입은 우리가 생각하는 `==` 비교 연산이 잘 작동한다. 하지만, 임베디드 타입은 자바 입장에서는 객체 타입이므로 `==` 비교 연산이 참조 하는 곳이 같은지 아닌지를 비교하지 안에 있는 값을 비교하지 않는다.

- 동일성(identity) 비교 : 인스턴스의 참조 값을 비교, == 사용
- 동등성(equivalence) 비교 : 인스턴스의 값을 비교, equals 사용

--> equals 와 hashCode 를 오버라이드해야한다.

```java
@Embeddable
public class Address {
    String city;
    String street;
    String zipcode;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(city, address.city) && Objects.equals(street, address.street) && Objects.equals(zipcode, address.zipcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, street, zipcode);
    }
}
```

이처럼 equals, hashCode 를 값 타입 비교를 위해 잘 오버라이드 해놓아야 우리가 원하는 값 타입 비교가 가능해진다!

**잊지말자, JPA 의 값 타입과 Java의 기본 값 타입은 다르다!**

<br>

### 5) 값타입 컬렉션

 값 타입을 하나 이상 저장할 때 자바의 컬렉션을 이용할 수 있다. 데이터베이스 입장에서는 컬렉션을 같은 테이블에 저장할 수 없으므로 컬렉션을 저장하기 위한 별도의 테이블을 생성해주고 일대다 관계처럼 관리해준다. 하지만, 별도의 엔티티는 아니므로 속한 엔티티의 PK를 FK로 삼아서 키를 관리해준다. 

- @ElementCollection : 값 타입 컬렉션임을 명시한다.
- @CollectionTable : 테이블을 지정해준다.

> 기본적으로 지연 로딩으로 적용!

<br>

 회원이 좋아하는 음식과 배송지 정보를 담아야된다고 가정해보자. 우리가 쿠팡같은 쇼핑몰을 생각해보면 내가 설정한 배송지 정보들이 담겨있고 배송지 정보를 추가하거나 삭제하거나 수정할 수 있다. 마찬가지로 회원이 좋아하는 음식 정보를 생각해봐도 좋아하는 음식이 추가하거나 삭제되거나 수정될 수 있다.

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    Set<String> favoriteFoods = new HashSet<>();

    List<Address> deliveryAddress = new ArrayList<>();
}
```

그러면 JPA 에게 정보를 어떻게 알려줄 수 있을까

`@ElementCollection`, `@CollectionTable` 애노테이션을 이용해야된다.

```java
@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @ElementCollection
    @CollectionTable(name = "FAVORITE_FOOD",
    joinColumns = @JoinColumn(name = "MEMBER_ID"))
    @Column(name = "FOOD_NAME")
    Set<String> favoriteFoods = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "DELIVERY_ADDRESS",
            joinColumns = @JoinColumn(name = "MEMBER_ID"))
    List<Address> deliveryAddress = new ArrayList<>();
}
```

먼저, `@ElementCollection` 애노테이션을 통해 컬렉션 타입인 것을 JPA에게 알려주고, `@CollectionTable` 애노테이션을 통해 컬렉션 타입의 데이터들을 관리하는 테이블의 이름을 설정해 줄 수 있습니다. 또, 생각해보면 좋아하는 음식 / 배송지 정보가 새로운 테이블에서 관리될 때는 회원의 PK 를 FK 로 가지고 있어야되므로 `joinColumns` 를 설정해주어야합니다.

그러면 만들어지는 테이블의 column 명을 어떻게 지정할 수 있을까요?? 좋아하는 음식 같은 경우는 값이 `String` 으로 하나입니다. 따라서 예외적으로 `@Column` 애노테이션을 통해 컬럼명을 바로 지정이 가능하나, 배송지 정보처럼 임베디드 타입이 사용되서 column 이 여러 개 생기는 경우에는  `@AttributeOverrides` 을 이용해야합니다.

![jpa_27](https://user-images.githubusercontent.com/59816811/117239956-bf0d0500-ae6a-11eb-9c6b-418fafc7c70e.png)

FAVORITE_FOOD, DELIERY_ADDRESS 테이블이 생성되고, MEMBER_ID 를 FK 로 가지고 있는 것을 볼 수 있습니다.

하지만, 이렇게 컬렉션 타입을 사용하면 다음과 같은 문제가 생깁니다.

회원이 좋아하는 음식에 "짜장면", "짬뽕", "탕수육"을 추가하고 DB에 저장한 후 다시 꺼내와서 "짬뽕" 을 "잡채밥"으로 바꾸는 상황을 생각해봅시다. 우리가 원하는건 FAVORITE_FOOD에 "짜장면", "짬뽕", "탕수육"이 3번 insert 된 후, Member를 insert한 후 Member 테이블에서 member를 select 해와서 다시 FAVORITE_FOOD 에 "잡채밥"을 update 하는 것을 원합니다. 

```java
Member member1 = new Member();
member1.getFavoriteFoods().add("짜장면");
member1.getFavoriteFoods().add("짬뽕");
member1.getFavoriteFoods().add("탕수육");
em.persist(member1);

em.flush();
em.clear();

Member findMember = em.find(Member.class, member1.getId());
findMember.getFavoriteFoods().remove("짜장면");
findMember.getFavoriteFoods().add("잡채밥");

tx.commit();
```

```sql
			delete 
        from
            FAVORITE_FOOD 
        where
            MEMBER_ID=? 
            and FOOD_NAME=?

			insert 
        into
            FAVORITE_FOOD
            (MEMBER_ID, FOOD_NAME) 
        values
            (?, ?)
```

Set 컬렉션은  원하는대로 잘 작동하는 것을 확인할 수 있습니다.

그러면, List 컬렉션에서는 어떻게 작동할까요??

```java
Member member1 = new Member();
member1.getDeliveryAddress().add(new Address("City1", "Street1", "zipCode1"));
member1.getDeliveryAddress().add(new Address("City2", "Street2", "zipCode2"));
member1.getDeliveryAddress().add(new Address("City3", "Street3", "zipCode3"));
em.persist(member1);

em.flush();
em.clear();

System.out.println("========================");
Member findMember = em.find(Member.class, member1.getId());
findMember.getDeliveryAddress().remove(new Address("City1", "Street1", "zipCode1"));
findMember.getDeliveryAddress().add(new Address("newCity1", "Street1", "Zipcode1"));

tx.commit();
```

```sql
			delete 
        from
            DELIVERY_ADDRESS 
        where
            MEMBER_ID=?

    		insert 
        into
            DELIVERY_ADDRESS
            (MEMBER_ID, city, street, zipcode) 
        values
            (?, ?, ?, ?)

			insert 
        into
            DELIVERY_ADDRESS
            (MEMBER_ID, city, street, zipcode) 
        values
            (?, ?, ?, ?)
            
			insert 
        into
            DELIVERY_ADDRESS
            (MEMBER_ID, city, street, zipcode) 
        values
            (?, ?, ?, ?)
```

배송지 정보를 저장하고 있는 DELIVERY_ADDRESS에서 주소 하나를 바꿨더니 delete query가 1번, insert 쿼리가 3번 나가는 것을 확인할 수 있습니다.

그리고 delete query를 자세히 보면 1개의 데이터를 삭제하는 것이 아니라 MEMBER_ID 와 연관된 모든 데이터를 삭제하는 것을 볼 수 있습니다. List 컬렉션은 Set 컬렉션과 달리 중복을 허용하고**, JPA 입장에서는 List 컬렉션의 데이터들의 index를 알 수 없으므로 다 삭제하고 다시 모두 추가하게 됩니다.**

**값 타입은 엔티티와 다르게 식별자 개념이 없으므로 값을 변경하면 추적이 어렵다. 따라서, 주인 엔티티와 연관된 모든 데이터를 삭제하고, 값 타입 컬렉션에 있는 현재 값을 모두 다시 저장하도록 작동하게된다.**

--> 값 타입 컬렉션을 매핑하는 테이블은 모둔 컬럼을 묶어서 기본 키로 구성해야한다. 하지만, 기본 설정이 그게 아니므로 변경과 추적이 필요하면 값 타입 컬렉션 대신에 엔티티 타입으로 만든 후 일대다 관계로 풀어서 해결하자.

<br>

##### 주의!!

**값 타입은 정말 값 타입이라 판단될 때만 사용하고, 식별자가 필요하고 값을 추적하고 변경해야한다면 값 타입이 아닌 엔티티로 풀어내자.**

