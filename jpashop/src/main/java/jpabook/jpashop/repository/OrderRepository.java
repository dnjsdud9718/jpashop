package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.hibernate.dialect.HANADialect;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    /**
     * 단순 문자 합치기
     * 개발자 실수가 많을 수 있다.
     * 코드가 길어진다.
     */
    public List<Order> findAll(OrderSearch orderSearch) {
        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition = true;

        // 주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
            .setMaxResults(1000);
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    /**
     * JPA Criteria
     * 실무에서 권장하지 않는 방법
     * 무슨 쿼리가 날라가는 지 감이 안 온다.
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }


    public List<Order> findAllWithMemberDelivery() {
        String jpql = "select o from Order o"
            + " join fetch o.member m"
            + " join fetch o.delivery d";

        return em.createQuery(jpql, Order.class).getResultList();
    }

    public List<OrderSimpleQueryDto> findOrderDtos() {
        String jpql = "select new jpabook.jpashop.repository.OrderSimpleQueryDto("
            + " o.id,"
            + " m.name,"
            + " o.orderDate,"
            + " o.status,"
            + " d.address)"
            + " from Order o"
            + " join o.member m"
            + " join o.delivery d";
        return em.createQuery(jpql, OrderSimpleQueryDto.class)
            .getResultList();
    }

    public List<Order> findAllWithItem() {
        String jpql = "select distinct o from Order o"
            + " join fetch o.member m"
            + " join fetch o.delivery d"
            + " join fetch o.orderItems oi"
            + " join fetch oi.item i";
        return em.createQuery(jpql, Order.class)
            .getResultList();

    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        String jpql = "select o from Order o"
            + " join fetch o.member m"
            + " join fetch o.delivery d";

        return em.createQuery(jpql, Order.class)
            .setFirstResult(offset)
            .setMaxResults(limit)
            .getResultList();
    }
}
