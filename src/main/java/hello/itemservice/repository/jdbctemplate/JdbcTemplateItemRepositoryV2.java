package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * NamedParameterJdbcTemplate
 * 파라미터 매핑 방법
 * SqlParameterSource 인터페이스
 * - 구현체
 * - BeanPropertySqlParameterSource 객체의 필드명과 파라미터를 맵핑
 * - MapSqlParameterSource
 * Map : 그냥 map을 사용해 파라미터를 맵핑
 * ResultSet과 반환 객체 매핑 방법
 * BeanPropertyRowMapper
 *
 */
@Slf4j
public class JdbcTemplateItemRepositoryV2 implements ItemRepository {

    // 파라미터 바인딩을 순서가 아니라 이름 단위로 바인딩 함
    private final NamedParameterJdbcTemplate template;

    public JdbcTemplateItemRepositoryV2(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Item save(Item item) {
        String sql = "insert into item (item_name, price, quantity) " +
                "values (:itemName, :price, :quantity)";

        // SqlParameterSource을 이용해 파라미터를 넘기는 방법
        // 자바빈 프로퍼티 규약을 통해서 자동으로 파라미터 객체를 생성
        // get 메서드를 이용해 맵핑
        SqlParameterSource param = new BeanPropertySqlParameterSource(item);
        // id값을 자동으로 생성해주는 객체
        // id값을 데이터베이스에서 생성해주는 방식을 generate 전략이라고 함
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder);

        long key = keyHolder.getKey().longValue();
        item.setId(key);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item " +
                "set item_name=:itemName, price=:price, quantity=:quantity from item where id=:id";
        // MapSqlParameterSource를 이용해 파라미터를 넘기는 방법
        // 메서드 체인을 통해 편리한 사용법을 제공
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity())
                // updateParam에는 id라는 속성이 없기 때문에
                // 아래와 같이 따로 맵핑하는 부분이 필요하다.
                // 따라서 이런 경우에는 BeanPropertySqlParameterSource를 활용할 수가 없다
                .addValue("id", itemId);

        template.update(sql, param);
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price, quantity from item where id = :id";
        try {
            // 맵으로 파라미터를 넘기는 방법
            Map<String, Object> param = Map.of("id", id);
            Item item = template.queryForObject(sql, param, itemRowMapper());
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }



    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        SqlParameterSource param = new BeanPropertySqlParameterSource(cond);

        // 만약 테이블의 컬럼 이름과 객체 이름이 다른 경우에
        // 매핑이 어려울 수 있는데,
        // 별칭 'as'를 사용해서 sql조회 결과의 이름을 변경하면 된다.
        // 실무에서도 자주 사용하니 기억해두자!!
        String sql = "select id, item_name, price, quantity from item";
        //동적 쿼리
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }
        boolean andFlag = false;
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%',:itemName,'%')";
            andFlag = true;
        }
        if (maxPrice != null) {
            if (andFlag) {
                sql += " and";
            }
            sql += " price <= :maxPrice";
        }
        log.info("sql={}", sql);
        return template.query(sql, param, itemRowMapper());
    }

    private RowMapper<Item> itemRowMapper() {
        // ResultSet과 파라미터로 넘긴 걕체를 맵핑하여 값을 넘겨줌
        // 스네이크 케이스를 camel케이스 변환 지원한다.
        // 만약 컬럼 이름과 객체 속성 이름이 전혀 다를 경우에는 별칭을 이용해
        // sql결과 컬럼의 이름을 변경해주자!!!
        return BeanPropertyRowMapper.newInstance(Item.class);
    }
}
