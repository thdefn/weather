package zerobase.weather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.weather.domain.Memo;

/**
 * jpa
 * - jpa는 java의 표준 orm 명세
 * - java에서 orm을 활용할 때 쓰는 함수들은 JpaRepository 에 다 명세되어 있음
 */

@Repository
public interface JpaMemoRepository extends JpaRepository<Memo, Integer> { // <Class, Key>

}
