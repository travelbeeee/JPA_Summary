### 0) 상황

Parent 클래스와 Children 클래스가 있고, Parent 클래스는 일대다, Children 클래스는 다대일로 양방향 관계다.

<br>

### 1) 영속성 전이 : CASCADE

CASCADE 속성은 특정 엔티티를 영속 상태로 만들 때 연관된 엔티티도 함께 관리해주는 설정이다.

- CascadeType.PERSIST

  영속할 때 연관된 엔티티도 함께 영속 상태로 만들어준다. 

- CascadeType.REMOVE

  준영속 상태로 만들 때 연관된 엔티티도 함께 준영속 상태로 만들어준다.

- CascadeType.ALL

  연관된 엔티티와 엔티티 주기를 함께한다.

```java
@Entity
public class Parent {
    @Id @GeneratedValue
    @Column(name = "PARENT_ID")
    int id;

    String name;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    List<Child> childList = new ArrayList<>();

}

@Entity
public class Child {
    @Id @GeneratedValue
    @Column(name = "CHILD_ID")
    int id;

    String name;

    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    Parent parent;
}
```

```java
Child child1 = new Child();
Child child2 = new Child();
Parent parent = new Parent();

parent.getChildList().add(child1);
parent.getChildList().add(child2);

em.persist(parent);
```

```sql
    /* insert hellojpa.Parent
        */ insert 
        into
            Parent
            (name, PARENT_ID) 
        values
            (?, ?)
    /* insert hellojpa.Child
        */ insert 
        into
            Child
            (name, PARENT_ID, CHILD_ID) 
        values
            (?, ?, ?)

    /* insert hellojpa.Child
        */ insert 
        into
            Child
            (name, PARENT_ID, CHILD_ID) 
        values
            (?, ?, ?)
```

parent 를 insert하는 query와 함께 child를 insert 하는 쿼리도 발생한다.

```java
Child child1 = new Child();
Child child2 = new Child();
Parent parent = new Parent();

parent.getChildList().add(child1);
parent.getChildList().add(child2);

em.persist(parent);

em.remove(parent);
```

```sql
    /* delete hellojpa.Child */ delete 
        from
            Child 
        where
            CHILD_ID=?

    /* delete hellojpa.Child */ delete 
        from
            Child 
        where
            CHILD_ID=?

    /* delete hellojpa.Parent */ delete 
        from
            Parent 
        where
            PARENT_ID=?
```

parent를 remove했는데 연관된 child도 delete query가 발생하게 된다.

<br>

이처럼, CASCADE 속성을 이용하면 연관된 엔티티도 함께 주기를 관리할 수 있다.

<br>

### 2) 고아 객체 : orphanRemoval

고앙 객체란 부모 엔티티와 연관관계가 끊어진 자식 엔티티를 말하고, 이를 자동으로 삭제해주는 설정이 `orphanRemoval` 이다.

```jade
@Entity
public class Parent {
    @Id @GeneratedValue
    @Column(name = "PARENT_ID")
    int id;

    String name;

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    List<Child> childList = new ArrayList<>();
}
```

같은 상황에서 이번에는 `cascade` 속성이 아니라 `orphanRemoval` 속성을 적용해보자.

```java
Child child1 = new Child();
Child child2 = new Child();
Parent parent = new Parent();

parent.getChildList().add(child1);
parent.getChildList().add(child2);
child1.setParent(parent);
child2.setParent(parent);

em.persist(child1);
em.persist(child2);
em.persist(parent);

Parent findParent = em.find(Parent.class, parent.getId());
findParent.getChildList().remove(0); // child1 엔티티 삭제
```

 cascade 속성이 없으므로 엔티티를 하나 하나 다 추가해주고, Parent 엔티티에서 자식 객체를 고아 객체로 만들어주면 delete query가 나가야된다. 하지만, 아직까지는 update query가 나간다. 흠....!

> JPA 스펙상 원칙적으로 CascadeType.PERSIST이 없어도 orphanRemoval만으로 삭제되어야 하는 것이 맞습니다.
>
> 하이버네이트 구현체에서는 해당 기능에 버그가 있고, 그래서 CascadeType.PERSIST(또는 ALL)이 함께 적용되어야 동작합니다.

<br>

##### 주의사항!

- 고아 객체는 참조가 제거된 엔티티가 삭제되므로 다른 곳에서 참조하지 않는 상황에서만 고아 객체 삭제 기능을 사용해야한다. 즉, Parent 와 Child에서 Child를 Parent에서만 참조하는 상황에서만 사용하자!
- @OneToOne, @OneToMany 만 가능하다. 

<br>

##### CascadeType.ALL + orphanRemoval=true

2가지 속성을 적용하면 부모 엔티티를 통해서 자식의 생명 주기를 모두 관리하게 된다.