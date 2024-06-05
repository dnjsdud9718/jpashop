package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest // SpringBoot 띄우고 테스트 -> 없으면 @Autowired 다 실패
@Transactional // rollback 된다.
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;

    @DisplayName("회원가입 성공")
    @Test
//    @Rollback(value = false)
    void joinSuccessTest() {
        // given
        Member member = new Member();
        member.setName("kim");

        // when
        Long saveId = memberService.join(member);

        // then
        Assertions.assertThat(member).isEqualTo(memberRepository.findOne(saveId));
    }

    @DisplayName("회원가입 회원명 중복")
    @Test
    void joinDuplicatedTest() {
        // given
        Member member1 = new Member();
        member1.setName("kim");
        Member member2 = new Member();
        member2.setName("kim");
        // when
        memberService.join(member1);

        // then
        Assertions.assertThatThrownBy(() -> memberService.join(member2))
            .isInstanceOf(IllegalStateException.class);
    }
}