package egovframework.sys.service;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel(description = "장비 수리/정비 이력 DTO")
public class RprHist {

  @ApiModelProperty(value = "수리내역 고유번호", example = "1")
  private Integer rprSeq;

  @ApiModelProperty(value = "장비 고유번호", example = "467")
  private Integer machineSeq;

  @ApiModelProperty(value = "수리일자", example = "2025-04-08")
  private String rprDt;

  @ApiModelProperty(value = "수리 또는 정비 내용", example = "커넥터 교체 및 점검 완료")
  private String content;

  @ApiModelProperty(value = "관련 교정일자", example = "2025-04-08")
  private String calDt;

  @ApiModelProperty(value = "점검 결과", example = "적합")
  private String result;

  @ApiModelProperty(value = "사용 시작일", example = "2025-04-09")
  private String useDt;

  @ApiModelProperty(value = "접수자 서명", example = "김정주")
  private String rcvrSign;

  @ApiModelProperty(value = "확인자 서명", example = "이상민")
  private String cnfmSign;

  @ApiModelProperty(value = "접수자 서명", example = "김정주")
  private String rcvrSignUrl;

  @ApiModelProperty(value = "확인자 서명", example = "이상민")
  private String cnfmSignUrl;
  
  @ApiModelProperty(value = "입력자 ID", example = "admin")
  private String insMemId;

  @ApiModelProperty(value = "입력 일시", example = "2025-04-08T09:30:00")
  private String insDt;

  @ApiModelProperty(value = "수정자 ID", example = "admin")
  private String udtMemId;

  @ApiModelProperty(value = "수정 일시", example = "2025-04-08T10:00:00")
  private String udtDt;

  @ApiModelProperty(value = "상태값", example = "I")
  private String state;
}
