package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

//    @NotEmpty -> 표현 계층 검증 로직이 entity에 포함되었다.
//    만약에 name -> username으로 변경하면 API 스펙이 변경되어 버린다.
//    엔티티 변경으로 API 스펙 자체가 1:1 매핑되어 있다.
//    DTO를 만들어서 사용해야 한다. 엔티티를 외부 바인딩에 사용하면 이후 큰 장애를 겪을 확률이 크다.
//    실무에서는 회원가입 방법도 다양한다. 기본, 소셜 등,,,
//    !엔티티를 외부에 노출하지 마라.
    private String name;

    @Embedded
    private Address address;

    @JsonIgnore
    @OneToMany(mappedBy = "member") // order table에 있는 member 필드에 의해 매핑 됨을 표시
    private List<Order> orders = new ArrayList<>();

    public Member() {}

}
