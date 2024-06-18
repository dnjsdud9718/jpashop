package jpabook.jpashop.api;

import static java.util.stream.Collectors.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    /**
     * Entity 직접 노출 -> 하이버네이트6모듈 등록, Lazy=null 처리, 지연 로딩인 것은 무시한다. -> 양방향 연관관계 문제 -> @JsonIgnore
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    /**
     * Dto 사용 -> 엔티티를 외부에 노출하지 마라.
     *
     * N+1문제  발생
     * order -> member (lazy loading) 2
     * order -> delivery (lazy loading) 2
     * order -> orderItems (lazy loading) 2
     * orderItem -> item (lazy loading) 2 * 2
     */
    @GetMapping("/api/v2/orders")
    public Result ordersV2() {
        List<Order> all = orderRepository.findAll(new OrderSearch()); // 1
        List<OrderDto> collect = all.stream()
            .map(OrderDto::new)
            .collect(toList());
        return new Result(collect.size(), collect);
    }

    /**
     * fetch join
     *
     * hibernate6부터 distinct 가 자동 적용된다(fetch join 시)
     *
     * 문제
     * 1. 페이징 처리 불가
     * 2. 컬렉션 fetch join은 1개만 사용할 수 있다.
     *
     * 특징 -> 쿼리 한 번만 날라가지만 데이터 중복이 많고 데이터 전송량이 많다.
     */
    @GetMapping("/api/v3/orders")
    public Result ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> collect = orders.stream()
            .map(o -> new OrderDto(o))
            .collect(toList());
        return new Result(collect.size(), collect);
    }

    /**
     * Paging 처리
     *
     * ToOne 관계를 모두 fetch join 한다 -> row를 증가 시키지 않는다.
     * 컬렉션은 지연 로딩으로 조회한다.
     * 지연 로딩 성능 최적화를 위해 @BatchSize 또는 hibernate.default_batch_fetch_size를 적용한다.
     * -> hibernate.default_batch_fetch_size : global 설정(application.yml)
     * -> @BatchSize : 개별 최적화
     * -> 이 옵션을 사용하면 컬렉션이나, 프록시 객체를 한꺼번에 설정한 size만큰 IN 쿼리로 조회
     *
     * 특징
     * -> 쿼리는 몇 번 더 나가지만, 페이징 처리가 되고, 데이터 전송량이 적다.
     */
    @GetMapping("/api/v3.1/orders")
    public Result ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
        @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        List<OrderDto> collect = orders.stream()
            .map(o -> new OrderDto(o))
            .collect(toList());
        return new Result(collect.size(), collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {

        private int count;
        private T data;
    }

    @Data
    static class OrderItemDto {

        private String itemName; // 상품 명
        private int orderPrice; // 주문 가격
        private int count; // 주문 수량
        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }

    }

    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        /**
         * DTO 안에는 Entity가 있으면 안 된다.
         * 외부에 엔티티 정보가 유출된다.
         * OrderItem도 DTO로 변경해야 한다.
         * 계층 분리
         */
        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream().
                map(orderItem -> new OrderItemDto(orderItem))
                .collect(toList());

        }
    }
}
