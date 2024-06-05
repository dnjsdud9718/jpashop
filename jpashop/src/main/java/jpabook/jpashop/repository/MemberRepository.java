package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import java.util.List;
import jpabook.jpashop.domain.Member;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {

    @PersistenceContext // spring bean으로 등록된 entitymanager를 주입해준다.
    private EntityManager em;

//    @PersistenceUnit : EntityManagerFactory를 주입 받고 싶을 때
//    private EntityManagerFactory entityManagerFactory;

    public void save(Member member) {
        em.persist(member);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
            .getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
            .setParameter("name", name)
            .getResultList();
    }
}
