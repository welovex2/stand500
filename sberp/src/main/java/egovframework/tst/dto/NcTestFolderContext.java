package egovframework.tst.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NcTestFolderContext {

  /** 신청서 NC_FOLDER_PATH (재발행 시험은 원본 신청서 경로) */
  private String ncFolderPath;

  private int sbkRevision;

  private String sbkYm;

  private String sbkType;

  private int sbkSeq;

}
