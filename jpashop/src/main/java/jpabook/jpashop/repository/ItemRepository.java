package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) { // item은 jpa에 저장되기 전까지 id값이 없다.
            em.persist(item);
        } else { // 준영속 상태구나 -> merge해서 영속성 컨텍스트에서 관리하자
            // 실무에서 거의 안 쓴다. -> 변경 감지(Dirty Checking)로 업데이트하는 것이 권장된다.
            // 그리고 item은 여전히 영속성 컨텍스트 관리 대상이 아니다.
            // merge vs dirty checking
            // merge는 모든 컬럼이 변경된다. -> 특정 속성에 값이 없다면 null이 들어간다.(기존 속성값이 의도와 다르게 null로 갱신된다)
            // 변경 감지 원하는 속성만 선택해서 변경 가능.
            Item mergedItem = em.merge(item); // mergedItem이 관리 대상이 된다.
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
            .getResultList();
    }
}
