package egovframework.ncc.service;

/**
 * 이미지 PDF 변환 검증/처리 실패. HTTP 상태 코드를 함께 전달한다.
 */
public class NcImagePdfException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final int statusCode;

  public NcImagePdfException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }

}
