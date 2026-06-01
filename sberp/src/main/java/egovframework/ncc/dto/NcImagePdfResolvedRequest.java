package egovframework.ncc.dto;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 검증 완료된 PDF 변환 요청 컨텍스트. */
@Getter
@RequiredArgsConstructor
public class NcImagePdfResolvedRequest {

  private final List<String> normalizedPaths;
  private final String folderPath;
  private final String pdfFileName;
  private final String outputDavPath;
  private final String pathsSummaryForLog;

}
