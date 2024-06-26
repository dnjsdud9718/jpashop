package jpabook.jpashop.service;

import static org.assertj.core.api.Assertions.*;

import jakarta.persistence.EntityManager;
import java.util.List;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NoEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Slf4j
class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        //given
        Member member = getMember("회원1", "서울", "강가", "123-123");
        Book book = getBook("태백산맥", 10000, 10);

        int orderCount = 2;
        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order findOrder = orderRepository.findOne(orderId);
        assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.ORDER);
        assertThat(findOrder.getOrderItems().size()).isEqualTo(1);
        assertThat(findOrder.getTotalPrice()).isEqualTo(10000 * orderCount);
        assertThat(book.getStockQuantity()).isEqualTo(8);
    }

    @Test
    public void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = getMember("회원1", "서울", "강가", "123-123");
        Book book = getBook("태백산맥", 10000, 10);

        int orderCount = 1000;

        // expected
        assertThatThrownBy(() -> orderService.order(member.getId(), book.getId(), orderCount))
            .isInstanceOf(NoEnoughStockException.class);
    }
    @Test
    public void 상품취소() throws Exception {
        //given
        Member member = getMember("회원1", "서울", "강가", "123-123");
        Book book = getBook("태백산맥", 10000, 10);
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order findOrder = orderRepository.findOne(orderId);
        assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(book.getStockQuantity()).isEqualTo(10);

    }

    @Test
    void 상품조회() {
        // given
        Member member = getMember("회원1", "서울", "강가", "123-123");
        Member member2 = getMember("회원2", "서울", "강가", "123-123");
        Book book1 = getBook("태백산맥1", 10000, 10);
        Book book2 = getBook("태백산맥2", 10000, 10);
        Book book3 = getBook("칼의 노래", 10000, 10);
        int orderCount = 2;
        orderService.order(member.getId(), book1.getId(), orderCount);
        orderService.order(member.getId(), book2.getId(), orderCount);
        orderService.order(member2.getId(), book3.getId(), orderCount);
        OrderSearch orderSearch = new OrderSearch();
        orderSearch.setMemberName(member.getName());
//        orderSearch.setOrderStatus(OrderStatus.ORDER);

        // when
        List<Order> orders = orderService.findOrders(orderSearch);
        // then
        log.info("size={}", orders.size());
        Assertions.assertThat(orders.size()).isEqualTo(2);
        for (Order o : orders) {
            log.info("member name = {}", o.getMember().getName());
            Assertions.assertThat(o.getMember()).usingRecursiveComparison().isEqualTo(member);
        }
    }

    private Book getBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member getMember(String name, String city, String street, String zipcode) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(new Address(city, street, zipcode));
        em.persist(member);
        return member;
    }

}