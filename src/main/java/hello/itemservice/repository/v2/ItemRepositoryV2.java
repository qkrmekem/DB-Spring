package hello.itemservice.repository.v2;

import hello.itemservice.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
/*
* 단순한 CRUD 작업에 사용
* */
public interface ItemRepositoryV2 extends JpaRepository<Item, Long> {
}
