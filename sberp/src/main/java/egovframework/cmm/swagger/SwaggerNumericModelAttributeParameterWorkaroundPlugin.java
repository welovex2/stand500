package egovframework.cmm.swagger;

import com.google.common.base.Optional;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ExpandedParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterExpansionContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

/**
 * {@link org.springframework.web.bind.annotation.ModelAttribute} 로 펼쳐지는 필드 중, 숫자 타입에
 * {@link ApiModelProperty#example()} 가 빈 문자열인 경우 문서 직렬화 오류가 난다. 안전한 {@code scalarExample} 을 넣는다.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class SwaggerNumericModelAttributeParameterWorkaroundPlugin
    implements ExpandedParameterBuilderPlugin {

  @Override
  public void apply(ParameterExpansionContext context) {
    Class<?> erased = context.getFieldType().getErasedType();
    if (!isNumeric(erased)) {
      return;
    }

    Optional<ApiModelProperty> ann = context.findAnnotation(ApiModelProperty.class);
    if (!ann.isPresent()) {
      context.getParameterBuilder().scalarExample(defaultScalar(erased));
      return;
    }

    String ex = ann.get().example();
    if (ex == null || ex.isEmpty()) {
      context.getParameterBuilder().scalarExample(defaultScalar(erased));
    }
  }

  private static Object defaultScalar(Class<?> erased) {
    if (erased == Long.class || erased == long.class) {
      return 0L;
    }
    if (erased == Double.class || erased == double.class) {
      return 0.0d;
    }
    if (erased == Float.class || erased == float.class) {
      return 0.0f;
    }
    if (erased == BigDecimal.class) {
      return BigDecimal.ZERO;
    }
    return 0;
  }

  private static boolean isNumeric(Class<?> c) {
    return c == int.class || c == Integer.class || c == long.class || c == Long.class
        || c == short.class || c == Short.class || c == byte.class || c == Byte.class
        || c == double.class || c == Double.class || c == float.class || c == Float.class
        || c == BigDecimal.class;
  }

  @Override
  public boolean supports(DocumentationType delimiter) {
    return SwaggerPluginSupport.pluginDoesApply(delimiter);
  }
}
