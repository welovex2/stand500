package egovframework.ncc.dto;

import java.util.List;
import egovframework.cmm.util.NcImagePdfPathUtil.NcImagePdfImage;
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
  /** PDF 삽입 순서·라벨이 확정된 이미지 목록 (폴더별 정렬 규칙 적용). */
  private final List<NcImagePdfImage> orderedImages;

}
