package egovframework.cmm.swagger;

import com.google.common.base.Optional;
import java.math.BigDecimal;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

/**
 * Springfox + swagger-models: integer 계열 파라미터에 {@code example} 이 빈 문자열이면 JSON 직렬화 시
 * {@code Long.parseLong("")} 가 나며 문서 생성이 깨진다. 메서드 파라미터에 대해 {@code scalarExample} 을 채운다.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class SwaggerNumericParameterWorkaroundPlugin implements ParameterBuilderPlugin {

  @Override
  public void apply(ParameterContext context) {
    ResolvedMethodParameter rmp = context.resolvedMethodParameter();
    Class<?> erased = rmp.getParameterType().getErasedType();
    if (!isNumeric(erased)) {
      return;
    }

    // Springfox 2.9.2: ParameterContext#methodParameter() 는 UnsupportedOperationException 을 던짐
    Optional<RequestParam> rpOpt = rmp.findAnnotation(RequestParam.class);
    if (rpOpt.isPresent()) {
      RequestParam rp = rpOpt.get();
      if (hasNonSentinelDefault(rp.defaultValue())) {
        String dv = rp.defaultValue();
        context.parameterBuilder().defaultValue(dv);
        context.parameterBuilder().scalarExample(parseScalar(erased, dv));
        return;
      }
    }

    if (rmp.findAnnotation(PathVariable.class).isPresent()) {
      context.parameterBuilder().scalarExample(defaultScalar(erased));
      return;
    }

    context.parameterBuilder().scalarExample(defaultScalar(erased));
  }

  private static boolean hasNonSentinelDefault(String defaultValue) {
    return defaultValue != null && !ValueConstants.DEFAULT_NONE.equals(defaultValue);
  }

  private static Object parseScalar(Class<?> erased, String dv) {
    if (erased == Long.class || erased == long.class) {
      return Long.valueOf(dv);
    }
    if (erased == Integer.class || erased == int.class) {
      return Integer.valueOf(dv);
    }
    if (erased == Short.class || erased == short.class) {
      return Short.valueOf(dv);
    }
    if (erased == Byte.class || erased == byte.class) {
      return Byte.valueOf(dv);
    }
    if (erased == Double.class || erased == double.class) {
      return Double.valueOf(dv);
    }
    if (erased == Float.class || erased == float.class) {
      return Float.valueOf(dv);
    }
    if (erased == BigDecimal.class) {
      return new BigDecimal(dv);
    }
    return dv;
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
