package egovframework.cmm.filter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import egovframework.cmm.service.BasicResponse;
import egovframework.ncc.dto.NcSimpleResult;

@RestControllerAdvice // 전역 예외 처리기
public class GlobalExceptionHandler {

  // 서비스에서 throw new IllegalArgumentException(...) 했을 때 잡는 핸들러
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<BasicResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
    BasicResponse response = BasicResponse.builder().result(false).message(ex.getMessage()).build();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(NcBizException.class)
  @ResponseBody
  public NcSimpleResult handleNcBizException(NcBizException e) {
    return NcSimpleResult.fail(e.getMessage());
  }

  // ← 추가: 파일 용량 초과 (2GB 제한)
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  @ResponseBody
  public NcSimpleResult handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
    return NcSimpleResult.fail("파일 크기가 2GB를 초과하였습니다.");
  }

  // ← 추가: 기타 멀티파트 오류 (파싱 실패 등)
  @ExceptionHandler(MultipartException.class)
  @ResponseBody
  public NcSimpleResult handleMultipartException(MultipartException e) {
    return NcSimpleResult.fail("파일 업로드 오류: " + e.getMessage());
  }
}
