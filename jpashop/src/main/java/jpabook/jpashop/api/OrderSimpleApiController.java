package jpabook.jpashop.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSimpleQueryDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    /**
     * XToOne : Collection이 아닌 것을 다룬다.
     * <p>
     * Order Order -> Member (Many To One)
     * <p>
     * Order -> Delivery (One ToOne)
     * <p>
     * 양방향 연관관계 무한 재귀가 일어난다. order는 member를 가지고 있고, member는 order collection을 가지고 있으니 무한 재귀가 발생. ->
     *
     * @JsonIgnore를 사용해야 한다. (둘 중에 하나는 끊어줘야 한다) : Member, Delivery, OrderItem
     * <p>
     * 또 다른 문제,,,,bytebuddy 어쩌구 저쩌구,,, 현재 order -> member, order -> delivery는 지연로딩이다. 실제 entity가 아닌 프록시가
     * 존재(ByteBuddyInterceptor) jackson 라이브러리는 기본적으로 이 프록시 객체를 json으로 어떻게 생성해야 하는지 모른다. -> 예외 발생!
     * solution -> hibernate 5 module 설치해야 한다.
     * <p>
     * 위와 같은 해결 방법은 문제가 많기 때문에 가급적 지양해야 한다. Why? 엔티티를 외부에 노출하지 마라 -> @JsonIgnore가 엔티티에 들어간다. Web 관련 내용이
     * 들어갔다.
     * <p>
     * { "id": 1, "member": null, // 지연 로딩이닌까 "orderItems": null, // 지연로딩이닌까 "delivery": null, //
     * 지연로딩이닌까 "orderDate": "2024-06-13T21:31:12.220436", "status": "ORDER", "totalPrice": 50000 },
     * 지연로딩을 강제로 무시할 수 있다. -> hibernate5JakartaModule.configure(Feature.FORCE_LAZY_LOADING, true);
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
//        프록시 객체를 강제 초기화해서 값을 불러올 수 있지만 비추천
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
        return all;
    }

    /**
     * DTO 사용 => entity를 외부에 노출하지 마라
     * N+1 문제
     * 지연로딩으로 설정했지만 N+1문제가 생길 수 있다.
     * 참고 : delivery_id를 조건절로 가지고 주문테이블을 조회하는 쿼리가 발생(하이버네이트6 버그???)
     */
    @GetMapping("/api/v2/simple-orders")
    public Result ordersV2() {
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        List<SimpleOrderDto> collect = orders.stream()
            .map(o -> new SimpleOrderDto(o))
            .collect(Collectors.toList());
        return new Result<>(collect.size(), collect);
    }

    /**
     * <h1>fetch join</h1>
     * <p>연관된 엔티티나 컬렉션을 한 번에 같이 조인하는 기능</p>
     *
     * <div>
     *     지연 로딩 & fetch join 으로 N + 1 문제 해결
     * </div>
     */
    @GetMapping("/api/v3/simple-orders")
    public Result ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> collect = orders.stream()
            .map(o -> new SimpleOrderDto(o))
            .collect(Collectors.toList());
        return new Result(collect.size(), collect);

    }

    /**
     * <h1>레포지토리에서 DTO로 조회하기</h1>
     *
     * V3 vs. V4
     * 둘 다 트레이드 오프가 있기 때문에 우열 가리기 어렵다.
     * V3는 재상용성이 높다(여러 API에서 사용할 수 있다)
     * V3는 영속성 컨텍스트 관리 대상
     * V4는 재사용성이 떨어진다(특정 API에서만 사용할 수 있다)
     * V4가 성능 최적화면에서 낫다(생각보다 미비하다). 하지만 엔티티가 아니기 때문에 영속성 컨텍스트 관리 대상 제외
     * V4 물리적으로 계층구분(웹, 레포지토리)되어 보이지만 논리적으로는 계층 구분이 깨졌다.
     * 레포지토리가 웹 계층(화면)에 의존적이다. -> 종속적으로 설계되었다. -> API 수정 시 레포지토리도 변경 불가피하다.
     *
     * 성능차이가 거의 없기 때문에 fetch join으로 최적화하는 것을 추천(select 절 몇 개 줄인다고 극적인 성능 향상 없다)
     * 단, 컬럼이 많고(20~30) 그리고 API 요청이 많다면 DTO를 사용하여 필요한 부분만 가져오는 것을 추천
     * -> Dto를 사용하는 레포지토리는 순수한 레포지토리와 분리하여 관리 하는 것이 좋다. 순수한 레포지토리를 유지 -> 유지보수성이 좋다.
     */
    @GetMapping("/api/v4/simple-orders")
    public Result odersV4() {
        List<OrderSimpleQueryDto> orderDtos = orderRepository.findOrderDtos();
        return new Result(orderDtos.size(), orderDtos);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {

        private int count;
        private T data;

    }

    @Data
    static class SimpleOrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address; // 배송지 정보

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }
}
