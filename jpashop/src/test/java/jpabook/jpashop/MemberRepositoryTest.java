package jpabook.jpashop;

import static org.assertj.core.api.Assertions.*;

import jpabook.jpashop.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @Transactional
    @Rollback(false) // insert 쿼리가 데이터베이스에 쓰기되는 것을 확인하고 싶어서
    void testMember() {
        // given
        Member member = new Member();
        member.setUsername("userA");
        log.info("비영속 member id={}", member.getId());
        // when
        Long savedId = memberRepository.save(member);
        log.info("준영속 member id={}", member.getId()); // key 가지고 있다.
        Member findMember = memberRepository.find(savedId);
        // then
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        log.info("findMember = {}", findMember);
        log.info("member = {}", member);
        // why? 하나의 트랜잭션 에서 관리된다. 하나의 영속석 컨텍스트(entity manager)
        // Test에 @Transactional을 추가한 상태. 제거하면 실패한다.
        assertThat(findMember).isEqualTo(member);
    }
}