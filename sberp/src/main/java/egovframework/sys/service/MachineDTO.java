package egovframework.sys.service;

import java.util.List;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class MachineDTO extends Machine {

  @ApiModelProperty(value = "순서 ", example = "")
  private int no;
  
  @ApiModelProperty(value="업무 담당자", example = "김정미")
  private String memName;

  @ApiModelProperty(value = "화면노출번호 ", example = "")
  private int disOrdr;
  
  @ApiModelProperty(value="장비번호", example = "SB-001")
  private String machineCode;
  
  @ApiModelProperty(value="관리번호", example = "ST-001")
  private String mgntCode;
  
  @ApiModelProperty(value = "시험장비타입 ", example = "", hidden = true)
  private String Type;
  
  @ApiModelProperty(value = "사진파일아이디 ", example = "", hidden = true)
  private String atchFileId;
  
  @ApiModelProperty(value = "사진파일삭제여부 ", example = "", hidden = true)
  private String atchFileDelYn;
  
  @ApiModelProperty(value = "교정파일 링크 ", example = "", hidden = true)
  private String calFileLink;
  
  @ApiModelProperty(value = "교정주기명 ", example = "", hidden = true)
  private String reformPeriodName;
  
  @ApiModelProperty(value = "삭제할 교정성적서 리스트 ", example = "", hidden = true)
  private List<String> delFileList;
  
  @ApiModelProperty(value = "교정정보 리스트 ", example = "", hidden = true)
  private List<MacCal> macCalList;
  
  @ApiModelProperty(value = "수리내역 리스트 ", example = "", hidden = true)
  private List<RprHist> rprHistList;
}
