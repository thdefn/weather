package zerobase.weather.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // 전역의 예외 처리를 함, 이 클래스 안에 전역의 예외가 모이도록 함
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class) // 해당 컨트롤러의 예외 처리를 함
    public Exception handleAllException(){
        System.out.println("error from globalExceptionHandler");
        return new Exception();
    }
}
