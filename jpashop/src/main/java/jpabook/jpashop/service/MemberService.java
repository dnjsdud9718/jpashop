package jpabook.jpashop.service;

import java.util.List;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional // JPA의 데이터 변경이나 로직들은 트랜잭션 내에서 이뤄져야 한다, 스프링이 제공하는 @Transactional 쓰자
@RequiredArgsConstructor // final 이 있는 필드만을 가지고 생성자를 만들어 준다.
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원 가입
     *
     * 중복 회원 처리
     */
    public Long join(Member member) {
        validateDuplicatedMember(member); // 중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    /**
     * 중복 회원 검증이 완벽하게 동작할까???
     * -> 만약 두 명의 사용자가 동일한 이름으로 동시에 회원가입을 요청한다면??? 동일한 이름을 가진 두 명의 사용자가 회원가입이 된다.
     * 해결책 -> DB에서 최후 방어선 구축 -> Member 테이블의 name column에 unique 제약 조건을 추가한다.
     */
    private void validateDuplicatedMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());
        // EXCEPTION
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    // 회원 전체 조회
    @Transactional(readOnly = true) // 조회에서는 읽기 전용 트랜잭션(성능 향상), 자세한건 공부해야 한다.
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    // 단건 조회
    @Transactional(readOnly = true)
    public Member findOne(Long id) {
        return memberRepository.findOne(id);
    }

    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setName(name);
    }
}
