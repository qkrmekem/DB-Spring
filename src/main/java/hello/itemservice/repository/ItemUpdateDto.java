package hello.itemservice.repository;

import lombok.Data;

// DTO를 사용하는 최종계층이 어딘지에 따라 최동 계층 패키지에 저장
@Data
public class ItemUpdateDto {
    private String itemName;
    private Integer price;
    private Integer quantity;

    public ItemUpdateDto() {
    }

    public ItemUpdateDto(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
