### 실험! 

##### Transactional이 걸려있는 Service 에서 영속성 상태인 Member 를 찾아서 Controller 로 넘겨주면 Member는 계속 영속성 상태일까??

##### 내 추측 : Transactional을 벗어났으므로 영속성 상태로 관리되는 엔티티가 아니라 그냥 Member 객체가 된다!

##### 

[ 셋팅 ]

 @Transactional 애노테이션 제거를 위해 미리 DB에 다음과 같은 데이터를 넣어놓음.

![image-20210525143710954](C:\Users\sochu\AppData\Roaming\Typora\typora-user-images\image-20210525143710954.png)

[ 실험 ]

아이디가 1L인 Member를 찾아와서 비밀번호를 바꿔보자!

```java
// MemberService 클래스의 findMember 메소드
// --> repository를 통해 DB에 접근해 엔티티를 찾아오고 반환해준다.
@Override
public Member findMember(Long memberId) throws PDFLOException {
    Optional<Member> findMember = memberRepository.findById(memberId);
    if(findMember.isEmpty()) throw new PDFLOException(ErrorCode.MEMBER_NO_EXIST);
    return findMember.get();
}

@Test
public void 영속성_관리_테스트() throws Exception {
    // given
    Member member = memberService.findMember(1L);
    member.changePassword("newPassword");
}
```

[ 결과 ]

![image-20210525143729274](C:\Users\sochu\AppData\Roaming\Typora\typora-user-images\image-20210525143729274.png)

예상대로 트랜젝션 밖이라 값을 변경할 수 없다!

