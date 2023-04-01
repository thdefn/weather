package zerobase.weather.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import zerobase.weather.domain.Memo;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

/**
 * jdbc
 * - query를 직접 써야한다
 */
@Repository // 이 클래스는 레포지토리임을 알려줌
public class JdbcMemoRepository {
    private final JdbcTemplate jdbcTemplate;

    // application.properties 에 지정한 datasource 정보들이 datasource 객체에 담기고,
    // 해당 DataSource 활용해서 JdbcTemplate 만듦
    @Autowired // datasource 정보를 application.properties 에서 가져오려면
    public JdbcMemoRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Memo save(Memo memo) {
        String sql = "insert into memo values(?,?)";
        jdbcTemplate.update(sql, memo.getId(), memo.getText());
        return memo;
    }

    // ResultSet 데이터를 memoRowMapper 함수를 이용해서 Memo 객체로 가져옴
    public List<Memo> findAll() {
        String sql = "select * from memo";
        return jdbcTemplate.query(sql, memoRowMapper()); // 반환해온 데이터 값을 어떻게 처리할건지
    }

    // 혹시 객체가 없는 경우 Optional 객체로 Wrapping해서 null값 처리를 쉽게 함
    public Optional<Memo> findById(int id) {
        String sql = "select * from memo where id = ?";
        return jdbcTemplate.query(sql, memoRowMapper(), id)
                .stream().findFirst();
    }

    // jdbc를 통해 데이터를 가져오면 가져온 데이터 값은 ResultSet
    // ResultSet {id = 1, text = "this is memo"} -> Memo
    private RowMapper<Memo> memoRowMapper() {
        return (rs, rowNum) -> new Memo(
                rs.getInt("id"),
                rs.getString("text")
        );
    }
}
