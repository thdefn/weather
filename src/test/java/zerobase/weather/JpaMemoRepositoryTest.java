package zerobase.weather;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.Memo;
import zerobase.weather.repository.JpaMemoRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // 테스트 코드를 작성할때는 트랜잭셔널을 쓰면 다 롤백시켜줌
public class JpaMemoRepositoryTest {
    @Autowired
    JpaMemoRepository jpaMemoRepository;

    @Test
    void insertMemoTest() {
        //given
        Memo newMemo = new Memo(10, "this is jpa memo");
        //when
        jpaMemoRepository.save(newMemo); // 그 어떠한 함수도 구현하지 않았지만 jpa가 다 구현해줌
        //then
        List<Memo> memos = jpaMemoRepository.findAll();
        assertTrue(memos.size() > 0);
    }

    @Test
    void findByIdTest() {
        //given
        Memo newMemo = new Memo(11, "jpa"); // 아이디를 11로 지정해도 GenerationType.IDENTITY 정책 때문에 반영되지 않는다
        //when
        Memo memo = jpaMemoRepository.save(newMemo);
        System.out.println(memo.getId());
        //then
        Optional<Memo> result = jpaMemoRepository.findById(memo.getId());
        assertEquals(result.get().getText(), newMemo.getText());
    }
}
