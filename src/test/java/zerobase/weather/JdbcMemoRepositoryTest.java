package zerobase.weather;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.Memo;
import zerobase.weather.repository.JdbcMemoRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest // 테스트임을 말해줌
@Transactional // 테스트 코드가 전체 다 진행되면 원 상태로 복구되게
public class JdbcMemoRepositoryTest {
    @Autowired
    JdbcMemoRepository jdbcMemoRepository;

    @Test
    void insertMemoTest() {
        //given 주어진 것
        Memo newMemo = new Memo(2, "this is new memo");

        //when 무엇을 했을 때,
        jdbcMemoRepository.save(newMemo);

        //then 이럴 것이다
        Optional<Memo> result = jdbcMemoRepository.findById(2);
        assertEquals(result.get().getText(), newMemo.getText());
    }

    @Test
    void findAllMemoTest() {
        //given
        //when
        List<Memo> memos = jdbcMemoRepository.findAll();
        //then
        System.out.println(memos);
        assertNotNull(memos);
    }
}
