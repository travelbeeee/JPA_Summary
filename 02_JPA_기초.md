### 1) JPA 기초

##### 1-1) JPA 구동 방식

![jpa_3](https://user-images.githubusercontent.com/59816811/116521142-8e7b1780-a90e-11eb-86f1-fa87b4152b66.png)

JPA는 먼저`Persistence` 객체에서 설정 정보를 조회해야한다. 그 후, `Persistence` 객체가 `EntityManagerFactory` 를 만들어주고, 필요할 때마다 `EntityManager`를 만들어서 실제 쿼리를 날릴 수 있다.

<br>

##### 1-2) 자주 사용되는 설정정보

```xml
<property name="hibernate.show_sql" value="true"/> 1번
<property name="hibernate.format_sql" value="true"/> 2번
<property name="hibernate.use_sql_comments" value="true"/> 3번
```

1번 설정 정보를 통해 sql 문을 아래와 같이 출력하고, 2번 설정 정보를 통해 sql 문을 아래처럼 예쁘게 출력하고, 3번 설정 정보를 통해 /* insert hellojpa.Member */ 부분과 같이 우리에게 도움이 되는 주석 정보도 같이 출력해준다.

```
Hibernate: 
    /* insert hellojpa.Member
        */ insert 
        into
            Member
            (name, id) 
        values
            (?, ?)
```

<br>

##### 1-3) JPQL

JPA를 사용하면 엔티티 객체를 중심으로 개발할 수 있는데 검색 쿼리 등 테블이 아닌 엔티티 객체를 대상으로 검색하면 안되는 상황들이 발생!

--> 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검색 조건이 포함된 SQL이 필요

--> 엔티티 객체를 대상으로 쿼리를 날릴 수 있는 JPQL을 지원해준다!

> JPQL은 SQL을 추상화한 객체 지향 쿼리 언어

