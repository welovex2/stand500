package egovframework.cmm.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 성적서 PDF·미리보기용 공개 이미지 URL.
 * {@link AuthInterceptor} 보조 — servletPath/URI 패턴을 넓게 매칭한다.
 */
public class PublicImageAccessFilter implements Filter {

  public static final String ATTR_PUBLIC_IMAGE = "PUBLIC_IMAGE_ACCESS";

  @Override
  public void init(FilterConfig filterConfig) {
    // no-op
  }

  @Override
  public void destroy() {
    // no-op
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    if (req instanceof HttpServletRequest) {
      HttpServletRequest request = (HttpServletRequest) req;
      if (AuthInterceptor.isPublicImagePath(extractPath(request))) {
        request.setAttribute(ATTR_PUBLIC_IMAGE, Boolean.TRUE);
      }
    }
    chain.doFilter(req, res);
  }

  static String extractPath(HttpServletRequest request) {
    String servletPath = request.getServletPath();
    if (servletPath != null && !servletPath.isEmpty()) {
      return servletPath;
    }
    String pathInfo = request.getPathInfo();
    if (pathInfo != null && !pathInfo.isEmpty()) {
      return pathInfo;
    }
    return request.getRequestURI();
  }
}
