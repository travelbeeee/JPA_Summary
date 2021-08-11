# JPA_Merge,DirtyChecking

 영속 상태의 엔티티는 JPA가 변경 상태를 `DirtyChecking`을 통해서 자동으로 Update Query를 만들어줍니다. 

```java
@Transactional
void update(Long itemId, String newItemName){
    Item item = em.find(Item.class, itemId);
    item.changeName(newItemName); // JPA가 변경 상태를 감지하고 Update Query를 실행합니다.
}
```

 그러면, 준 영속 상태의 엔티티는 변경 상태를 어떻게 DB에 반영할 수 있을까요?? 이를 위해, JPA는 `Merge`를 지원해줍니다. 

```java
@Transactional
void update(Item item, String newItemName){
	item.changeName(newItemName); // JPA가 변경 상태를 감지X
    em.merge(item);
}
```

 `Merge`는 다음과 같이 동작합니다.

- merge() 를 실행한다.
- 파라미터로 넘어온 준영속 엔티티의 식별자 값으로 엔티티를 조회한다. 1차 캐시에 있다면 1차 캐시에서 꺼내오고, 없다면 데이터 베이스에서 조회한다.  (찾아온 엔티티는 영속 상태 엔티티)
- 찾아온 영속 상태 엔티티에 merge() 에 넘어온 엔티티의 값들을 다 채워넣는다. ( 이때, Dirty Checking 발생 )
- 영속 상태 엔티티를 반환한다.

> Merge 동작 방식
>
> 1. 준영속 엔티티의 식별자 값으로 영속 엔티티를 조회한다. 
> 2. 영속 엔티티의 값을 준영속 엔티티의 값으로 모두 교체한다.(병합한다.)
> 3. 트랜잭션 커밋 시점에 변경 감지 기능이 동작해서 데이터베이스에 UPDATE SQL이 실행

 결국, `Merge` 는 `DirtyChecking` 방식으로 동작합니다. 하지만, 다음과 같은 차이가 있습니다.

 변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만, 병합을 사용하면 모든 속성이 변경됩니다. 따라서, 병합시 값이 없으면 null 로 업데이트 할 위험도 있다. (병합은 모든 필드를 교체한다.)

