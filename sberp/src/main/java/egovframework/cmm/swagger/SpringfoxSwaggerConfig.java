package egovframework.cmm.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.classmate.TypeResolver;
import egovframework.chq.service.ChqDTO;
import egovframework.quo.dto.EngQuoDTO;
import egovframework.quo.service.QuoDTO;
import egovframework.raw.dto.RawDTO;
import egovframework.raw.dto.ReportDTO;
import egovframework.raw.service.FileRaw;
import egovframework.sam.dto.ImDTO;
import egovframework.sbk.service.SbkDTO;
import egovframework.sls.service.BillDTO;
import egovframework.sls.service.PayDTO;
import egovframework.sls.service.SlsDTO;
import egovframework.tst.dto.TestDTO;
import io.swagger.annotations.Api;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SpringfoxSwaggerConfig {

  @Bean
  public Docket api() {
    TypeResolver types = new TypeResolver();
    return new Docket(DocumentationType.SWAGGER_2)
        .useDefaultResponseMessages(false)
        .select()
        .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
        .paths(PathSelectors.any())
        .build()
        .apiInfo(apiInfo())
        .additionalModels(
            types.resolve(QuoDTO.Res.class),
            types.resolve(BillDTO.Res.class),
            types.resolve(PayDTO.Res.class),
            types.resolve(SlsDTO.Res.class),
            types.resolve(SbkDTO.Res.class),
            types.resolve(TestDTO.Res.class),
            types.resolve(RawDTO.class),
            types.resolve(ReportDTO.class),
            types.resolve(FileRaw.class),
            types.resolve(ChqDTO.Res.class),
            types.resolve(ImDTO.class),
            types.resolve(EngQuoDTO.class));
  }

  private static ApiInfo apiInfo() {
    return new ApiInfoBuilder()
        .title("SBERP API")
        .description("컨트롤러 @Api 태그만 노출. 목록 응답 스키마는 Models 의 QuoDTO.Res 등을 참고.")
        .version("1.0")
        .build();
  }
}
