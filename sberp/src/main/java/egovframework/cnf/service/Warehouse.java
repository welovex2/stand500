package egovframework.cnf.service;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "창고")
public class Warehouse {

  @ApiModelProperty(value = "창고 고유번호", example = "1")
  private int strSeq;

  @ApiModelProperty(value = "창고명", example = "1창고", required = true)
  private String whName;

  @ApiModelProperty(value = "위치", example = "본관 1층 시료실")
  private String whLoc;

  @ApiModelProperty(value = "창고형태(공통코드:WH) 01 방 02 복도 영역 03 기타",
      notes = "미전송시 01", example = "01")
  private String typeCode;

  @ApiModelProperty(value = "창고형태명", example = "방")
  private String typeName;

  @ApiModelProperty(value = "창고 관리자 아이디", notes = "선택, MEMBER_TB.ID", example = "hong")
  private String mngId;

  @ApiModelProperty(value = "창고 관리자 이름", example = "박형태")
  private String mngName;

  @ApiModelProperty(value = "창고 관리자 부서명", example = "전자파")
  private String mngDeptName;

  @ApiModelProperty(value = "담당부서 고유번호", notes = "선택, DEPT_TB.DEPT_SEQ", example = "3")
  private Integer deptSeq;

  @ApiModelProperty(value = "담당부서명", example = "경영지원실")
  private String deptName;

  @ApiModelProperty(value = "사용중 구역수", example = "5")
  private int zoneCnt;

  @ApiModelProperty(value = "사용중지 구역수", example = "1")
  private int stopZoneCnt;

  @ApiModelProperty(value = "보관 시료수", example = "45")
  private int itemCnt;

  @ApiModelProperty(hidden = true)
  private String insMemId;

  @ApiModelProperty(value = "등록일", example = "2026-10-01")
  private String insDt;

  @ApiModelProperty(hidden = true)
  private String udtMemId;

  @ApiModelProperty(value = "최종 수정자 이름", example = "이준우")
  private String udtMemName;

  @ApiModelProperty(value = "수정일", notes = "사용중지 창고는 중지일", example = "2026-10-16")
  private String udtDt;

  @ApiModelProperty(value = "상태 I 등록 U 수정 D 사용중지", example = "I")
  private String state;
}
