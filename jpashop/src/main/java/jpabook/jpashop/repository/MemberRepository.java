package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import jpabook.jpashop.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class MemberRepository {

    // @Autowired 도 가능... -> private final EntityManager em; Springboot에서 지원, 정확히 spring boot jpa에서 지원
    @PersistenceContext // spring bean으로 등록된 entitymanager를 주입해준다.
    private EntityManager em;

//    @PersistenceUnit : EntityManagerFactory를 주입 받고 싶을 때
//    private EntityManagerFactory entityManagerFactory;

    public void save(Member member) {
        log.info("save - em = {}", em.getClass());
        em.persist(member);
    }

    public Member findOne(Long id) {
        log.info("findOne() - em = {}", em.getClass());
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        log.info("findAll() - em = {}", em.getClass());
        return em.createQuery("select m from Member m", Member.class) // 영속성 컨텍스트 flush()가 일어난다.
            .getResultList();
    }

    public List<Member> findByName(String name) {
        log.info("findByName() - em = {}", em.getClass());
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
            .setParameter("name", name)
            .getResultList();
    }
}
