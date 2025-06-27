package egovframework.cmm.filter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import egovframework.cmm.service.BasicResponse;

@RestControllerAdvice  // 전역 예외 처리기
public class GlobalExceptionHandler {

    // ❗ 서비스에서 throw new IllegalArgumentException(...) 했을 때 잡는 핸들러
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BasicResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        BasicResponse response = BasicResponse.builder()
                .result(false)
                .message(ex.getMessage())
                .build();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // ❗ 기타 모든 예외 처리
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<BasicResponse> handleGeneralException(Exception ex) {
//        BasicResponse response = BasicResponse.builder()
//                .result(false)
//                .message("서버 내부 오류가 발생했습니다.")
//                .build();
//        return ResponseEntity
//                .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(response);
//    }
}
