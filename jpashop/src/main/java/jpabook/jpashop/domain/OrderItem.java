package jpabook.jpashop.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; // 주문 가격
    private int count; // 주문 수량

    protected OrderItem() {}

    // == 생성 메서드 == //
    // Item 에 수량이 있는데 왜 가격을 따로 받는가? 쿠폰 적용, 할이 정책 등이 포함될 수 있다.
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count);
        return orderItem;
    }

    // == 비즈니스 로직 == //
    public void cancel() {
        getItem().addStock(count);
    }

    // == 조회 로직 == //
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}
