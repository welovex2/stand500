package egovframework.cmm.web;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.util.EgovUserDetailsHelper;
import io.swagger.annotations.Api;

@Api(tags = {"에러관리"})
@RestController
@RequestMapping("/err")
public class ErrorController {

	@PostMapping(value="/error.do")
	public BasicResponse error(HttpServletRequest request, Exception ex) throws Exception {
		boolean result = false;
		String msg;
		
		String error_code = request.getAttribute("javax.servlet.error.status_code").toString();
		
		try {
            int status_code = Integer.parseInt(error_code);
            switch (status_code) {
            case 400: msg = "잘못된 요청입니다."; break;
            case 403: msg = "접근이 금지되었습니다."; break;
            case 404: msg = "페이지를 찾을 수 없습니다."; break;
            case 405: msg = "요청된 메소드가 허용되지 않습니다."; break;
            case 500: msg = "서버에 오류가 발생하였습니다."; break;
            case 503: msg = "서비스를 사용할 수 없습니다."; break;
            default: msg = "알 수 없는 오류가 발생하였습니다."; break;
            }
        } catch(Exception e) {
            msg = "기타 오류가 발생하였습니다.";
        } 
		
		BasicResponse res = BasicResponse.builder().result(result)
				.message(msg)
				.build();

		return res;
	}
	
    @PostMapping(value="/view.do")
    public BasicResponse view(HttpServletRequest request, Exception ex) throws Exception {
        boolean result = true;
        String msg = "";
        LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
        
        if ("P01".equals(user.getAuthCode())) {
          LocalDate date = LocalDate.now();
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");
          String ym = date.format(formatter);
          
          byte[] bytes = Files.readAllBytes(Paths.get("logs/"+ym+"/errorLog.log"));
          
          Collections.reverse(Arrays.asList(bytes));
          
          msg = new String(bytes);
        } else {
          msg = "권한을 획득 후 접근하세요.";
        }
        
        BasicResponse res = BasicResponse.builder().result(result)
                .data(msg)
                .build();

        return res;
    }
    
}
