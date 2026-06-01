package egovframework.ncc.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(value = "NcImagesToPdfResult", description = "이미지 PDF 저장 결과")
@Getter
@Setter
public class NcImagesToPdfResult {

  private boolean ok;
  private String message;

  @ApiModelProperty(value = "저장된 PDF DAV 경로")
  private String path;

  @ApiModelProperty(value = "PDF 파일명 (폴더명.pdf)")
  private String fileName;

  @ApiModelProperty(value = "저장 폴더 DAV 경로")
  private String folderPath;

  private int pageCount;
  private int imageCount;
  private long bytesWritten;

  public static NcImagesToPdfResult ok(String path, String fileName, String folderPath,
      int pageCount, int imageCount, long bytesWritten) {
    NcImagesToPdfResult r = new NcImagesToPdfResult();
    r.ok = true;
    r.path = path;
    r.fileName = fileName;
    r.folderPath = folderPath;
    r.pageCount = pageCount;
    r.imageCount = imageCount;
    r.bytesWritten = bytesWritten;
    return r;
  }

  public static NcImagesToPdfResult fail(String message) {
    NcImagesToPdfResult r = new NcImagesToPdfResult();
    r.ok = false;
    r.message = message;
    return r;
  }

}
