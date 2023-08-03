package egovframework.cmm.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
/**
 * SimpleCORSFilter.java 클래스
 *
 * @author 신용호
 * @since 2019. 10. 18.
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *  수정일                수정자               수정내용
 *  ----------   ----------   ----------------------
 *  2019.10.18   신용호                최초 생성
 * </pre>
 */

public class SimpleCORSFilter implements Filter {

	private final Logger log = LoggerFactory.getLogger(SimpleCORSFilter.class);
	private final List<String> allowedOrigins = Arrays.asList("http://localhost:8080", "http://stb100000.com");

	public SimpleCORSFilter() {
		log.info("SimpleCORSFilter init");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		log.debug("===>>> SimpleCORSFilter > doFilter()");
		/*HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		System.out.println("===>>>"+request.getHeader("Origin"));
		//response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		response.setHeader("Access-Control-Allow-Origin", "http://localhost:9700");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me");*/
		HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Access-Control-Allow-Origin
        String origin = request.getHeader("Origin");
//        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Origin", origin);
//        response.setHeader("Access-Control-Allow-Origin", "http://stb100000.com");
//        response.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
        
        // Access-Control-Max-Age
        response.setHeader("Access-Control-Max-Age", "3600");

        // Access-Control-Allow-Credentials
        response.setHeader("Access-Control-Allow-Credentials", "true");
        //response.setHeader("Access-Control-Allow-Credentials", "false");

        // Access-Control-Allow-Methods
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        // Allow 헤더는 Access-Control-Allow-Mehtods랑 비슷하지만 CORS 요청 외에도 적용된다는 특징이 있다.
        response.setHeader("Allow", "POST, GET, OPTIONS, DELETE");
        
        // Access-Control-Allow-Headers
        response.setHeader("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept, " + "X-CSRF-TOKEN");
        //response.setHeader("Access-Control-Allow-Headers", "X-Requested-With");
		
		chain.doFilter(req, res);
//        addSameSite(response, "None", request.getSession().getId());
	}

	@Override
	public void init(FilterConfig filterConfig) {
	}

	@Override
	public void destroy() {
	}
	
    private void addSameSite(HttpServletResponse response, String sameSite, String id) {
        Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
        boolean firstHeader = true;
        for (String header : headers) {
            if (firstHeader) {
            	response.setHeader(HttpHeaders.SET_COOKIE, String.format("%s; Secure; %s", header, "SameSite=" + sameSite));
            	System.out.println("=====setHeader");
            	System.out.println(response.getHeader(HttpHeaders.SET_COOKIE));
            	firstHeader = false;
                continue;
            }
            response.addHeader(HttpHeaders.SET_COOKIE, String.format("%s; Secure; %s", header, "SameSite=" + sameSite));
            System.out.println("=====addHeader");
            System.out.println(response.getHeader(HttpHeaders.SET_COOKIE));
        }
    }

}