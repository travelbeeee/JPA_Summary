# @MappedSuperclass

### 1) @MappedSuperclass

 엔티티마다 공통된 필드를 추가해야되면, 객체에서는 상속을 이용해서 문제를 해결할 수 있다. 하지만, 이 상황은 공통된 필드를 추가하는 상황이지 DB 입장에서는 슈퍼타입 - 서브타입 관계가 아닙니다. 따라서, 상속관계매핑처럼 해결하면 안됩니다.

이를 @MappedSuperclass 애노테이션을 통해 해결할 수 있다.

모든 엔티티에 생성한 개발자와 생성 시간을 추가해야된다고 해보자.

```java
@MappedSuperclass
public abstract class BaseEntity {
    private String createdBy;
    private LocalDateTime createdTime;
}

@Entity
public class Member extends BaseEntity {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;
}
```

 객체에서는 상속을 통해 이 상황을 해결할 수 있다. 하지만, DB 입장에서는 이는 단순히 컬럼이 추가되는것이지 슈퍼타입 - 서브타입 관계는 아니므로 @MapperSuperclass 애노테이션을 통해서 해결해야한다.

@MapperSuperclass 애노테이션을 아래와 같이 테이블을 생성한다. 즉, 테이블에 컬럼명이 추가되지 슈퍼타입 - 서브타입 개념이 아니다.

![jpa_24](https://user-images.githubusercontent.com/59816811/116849985-c2b24900-ac2a-11eb-997a-0a3148cb0295.png)

- 상속관계 매핑이 아니고, @MapperSuperclass 는 엔티티도 아니다. 따라서, 테이블과 매핑되지 않는다.
- 부모 클래스를 상속 받는 자식 클래스에 매핑 정보만 제공하기 위해 사용된다.
- em.find(BaseEntity.class) 와 같이 직접 조회, 검색이 불가능하다.
- 직접 생성해서 사용할 일이 없으므로 추상 클래스로 사용하자.