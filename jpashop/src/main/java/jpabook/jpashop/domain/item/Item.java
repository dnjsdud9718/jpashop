package jpabook.jpashop.domain.item;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NoEnoughStockException;
import lombok.Getter;
import lombok.Setter;

/**
 * 추상 클래스는 인스턴스를 생성할 수 없다.
 * 상속되어 사용된다.
 * 여기서는 Movie, Album, Book 에 상속되어 사용 된다.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
@Setter
public abstract class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    // Collection은 field에서 초기화하자
    // 초기화에 대한 고민 필요 없다 -> null 체크 불필요
    // hibernate 는 collection을 래핑한다.
    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    // == 비즈니스 로직 == //
    // entity 안에 비즈니즈 로직 넣는 것이 좋다.
    // entity 자체가 해결할 수 있는 것에 대한 내용

    /**
     * stock 증가
     * @param quantity
     */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * stock 감소
     */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new NoEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }
}
