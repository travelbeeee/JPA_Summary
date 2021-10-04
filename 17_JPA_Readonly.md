# JPA_Readonly

 JPA에서는 엔티티가 영속성 컨텍스트에 관리되면 변경 감지를 위해서 스냅샷 인스턴스를 보관하게 됩니다. 1차 캐시부터 변경 감지까지 많은 혜택을 얻을 수 있으나, 더 많은 메모리를 사용하는 단점이 존재합니다. 

 그렇기 때문에 JPA 에서는 읽기 전용으로 엔티티를 조회할 수 있습니다. 단순히 조회만 하는 경우에는 Context flush 와 Dirty checking 기능이 필요하지 않으므로 읽기 전용으로 엔티티를 조회해 메모리 사용량을 최적화 할 수 있습니다.

<br>

### 1) @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))

  `Hibernate` 에서 지원하는 기능으로 스냅샷을 만들지 않음으로써 `Dirty Checking` 기능을 포기하고 메모리 낭비하는 것을 방지합니다. 

<br>

### 2) @Transaction(readOnly = true)

  DB에 반영할 것이 없다는 것을 명시하여 영속성 `Context flush` 기능을 포기하고, `Context flush` 기능을 포기했기 때문에 `Dirty Checking` 기능도 포기해 메모리 낭비하는 것을 방지합니다.

>  스프링 5.1 버전 이후부터는 @Transaction(readOnly=true) 로 설정하면, @QueryHints 의 readOnly 기능까지 자동으로 동작합니다.

