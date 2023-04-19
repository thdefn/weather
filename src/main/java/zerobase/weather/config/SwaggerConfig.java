package zerobase.weather.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("zerobase.weather")) // ErrorController는 베이스 패키지 안에 포함된 컨트롤러가 아니라 스프링 단에 있는 것임
                .paths(PathSelectors.any()) // 특정 패턴의 경로만 api 문서화 ex> amdin or dev 용 api
                .build()
                .apiInfo(apiInfo())
                ;
    }

    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title("날씨 일기 프로젝트")
                .description("날씨 일기를 crud 할 수 있는 날씨 일기 api 입니다")
                .version("2.0")
                .build();
    }
}
