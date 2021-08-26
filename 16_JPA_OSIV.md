# JPA_OSIV

#### OSIV ( Open Session In View )

이름 그대로, 영속성 컨텍스트를 뷰까지 유지하는 기능입니다. 영속성 컨텍스트가 유지되면 관리하고 있는 엔티티도 영속 상태로 유지가 됩니다. 따라서, 뷰에서도 지연 로딩등 엔티티를 관리할 수 있습니다.

> JPA 에서는 OEIV ( Open EntityManager In View )
>
> Hibernate 에서는 OSIV ( Open Session In View ) 지만 관례상 둘 다 OSIV라 불립니다.

<br>

#### OSIV vs Spring OSIV

##### OSIV 장점

view 까지 영속성 컨텍스트를 유지하기 때문에 controller 단에서도 쓰기 지연을 이용해 필요한 정보를 가지고 올 수 있습니다.

> 서비스 로직에서 엔티티를 반환하면 컨트롤러 단에서 필요한 정보들만 쓰기 지연으로 필요한 정보를 가져올 수 있다는 장점이 있습니다.
>
> 예를 들어, A 엔티티가 B, C, D, E 엔티티와 관계가 있을 때 서비스는 단순히 A엔티티만 조회해서 넘기고 컨트롤러에서 B가 필요하면 B를 쓰기 지연을 이용해 정보를 가져오고, C가 필요하면 C를 가져올 수 있습니다.

<br>

##### OSIV 단점

일반적으로 controller 단에서 엔티티의 정보를 수정하는 일은 없습니다. 하지만, 영속성 컨텍스트가 view 까지 유지되므로 controller 단에서도 엔티티의 정보를 수정할 수 있는 위험이 생긴다는 심각한 단점이 있습니다.

<br>

##### Spring OSIV

 그럼, VIEW, Controller 단에서는 쓰기 지연을 이용하면서 엔티티의 수정을 막을 수 있는 방법은 없을까??  

 스프링 컨테이너는 OSIV의 치명적인 단점을 보완하고, 장점만을 살릴 수 있도록 기본 전략을 가지고 있습니다.

![그림1](C:\Users\sochu\바탕 화면\그림1.png)

1) 스프링은 클라이언트 요청이 들어오면 서블릿 필터나 스프링 인터셉터에서 영속성 컨텍스트를 생성하고 트랜잭션은 시작하지 않습니다.

2) 서비스 계층에서 트랜잭션을 시작할 때, 미리 생성한 영속성 컨텍스트를 찾아와서 트랜잭션을 시작하고 작업이 끝나면 영속성 컨텍스트를 플러시하고 트랜잭션을 커밋합니다. 이때, 영속성 컨텍스트는 종료되지 않고 유지됩니다.

3) 영속성 컨텍스트가 유지되므로 `Controller` 와 `View` 단에서도 계속 지연로딩이 가능합니다.

4) 요청이 모두 끝나고 응답이 돌아오면 영속성 컨텍스트를 종료합니다.

> 트랜젝션이 없어도 조회를 할 수 있는 것은 `Nontrasaction reads`라고 한다.

<br>

##### Spring OSIV 단점

OSIV의 장점만 살리고 단점은 보완한 것 같지만, Spring OSIV에도 여전히 단점이 많이 있습니다.

 먼저, 영속성 컨텍스트와 DB 커넥션은 1:1로 물고있는 관계이기 때문에 프레젠테이션 로직까지 DB 커넥션 자원을 낭비하게 됩니다.

 또, OSIV를 적용하면 같은 영속성 컨텍스트를 여러 트랜잭션이 공유하게되므로 문제가 생길 수 있습니다.

 마지막으로, 가장 치명적인 단점으로 프레젠테이션에서 엔티티를 수정하고 비지니스 로직을 수행하면 엔티티가 수정될 수 있습니다.

```java
    // 변경 X
	@GetMapping("/test1Member")
    @ResponseBody
    public String test1Member(){
        Member findMember = memberService.findOne(1L);
        findMember.setName("newName");
        return "ok";
    }

	// "newName" 으로 DB에 변경이 일어난다.
    @GetMapping("/test2Member")
    @ResponseBody
    public String test2Member(){
        Member findMember = memberService.findOne(1L);
        findMember.setName("newName");
        memberService.testMethod();
        return "ok";
    }
```

`Controller` 단에서는 트랜젝션이 없으므로 `/test1Member` 같은 경우에는 Member의 정보가 수정되지 않습니다.

하지만, `/test2Member` 처럼 `Controller` 단에서 엔티티 정보를 수정하고, `Service`단에서 트랜젝션이 실행되면 영속성 컨텍스트가 계속 살아있으므로 `Controller` 단에서 엔티티 정보를 수정한 것이 반영이 됩니다.

<br>

##### 결론

OSIV를 끈 상태로 개발을 진행하자! OSIV를 써서 얻는 이득보다는 심각한 문제가 생길 가능성이 있다는 점에서 안쓰는 것이 더 좋을 것 같다..!!

