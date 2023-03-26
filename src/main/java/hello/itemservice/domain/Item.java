package hello.itemservice.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
//@Table(name = "item")
public class Item {

    //strategy = GenerationType.IDENTITY - pk값을 데이터베이스에서 생성하는 방식
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // length : jpa로 테이블을 생성할 때 컬럼의 길이 값으로 활용
    @Column(name = "item_name", length = 10)
    private String itemName;
    private Integer price;
    private Integer quantity;

    //JPA는 기본생성자가 필수!!
    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
