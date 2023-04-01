package zerobase.weather.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service // 어노테이션을 붙여주지 않으면 스프링 부트가 서비스 클래스임을 인식하지 못하고 빈으로 등록도 안됨
public class DiaryService {
    public void createDiary(LocalDate date, String text) {

    }
}
