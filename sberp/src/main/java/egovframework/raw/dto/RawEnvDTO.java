package egovframework.raw.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 로데이터 측정일·환경조건 (RAW_ENV_TB) 요청/응답.
 *
 * <ul>
 * <li>[시작]·[종료]·[저장] : 방법별 insert.do 에 envAction·envMetKey·envRows 를 실어 방법 데이터와 한 번에 저장</li>
 * <li>종료된 행 수정 : 기술책임자 전용 /raw/env/{envSeq}/update.do (UpdateReq)</li>
 * <li>[중단]·[삭제] : /raw/env/{envSeq}/stop.do, delete.do (ReasonReq)</li>
 * </ul>
 */
public class RawEnvDTO {

  private RawEnvDTO() {}

  /** 방법별 insert.do 의 envAction 값 */
  public static final String ACTION_START = "START";
  public static final String ACTION_END = "END";

  @ApiModel(value = "RawEnvRowReq", description = "방법 저장 시 함께 저장할 진행 중 측정 행 값")
  @Getter
  @Setter
  @ToString
  public static class RowReq {
    @ApiModelProperty(value = "행 고유번호. [시작]으로 새로 만들 행은 0", example = "0")
    private int envSeq;

    @ApiModelProperty(value = "온도", example = "23.1")
    private String temp;
    @ApiModelProperty(value = "온도 ±", example = "0.5")
    private String tempPlus;
    @ApiModelProperty(value = "습도", example = "45.2")
    private String hmdt;
    @ApiModelProperty(value = "습도 ±", example = "0.5")
    private String hmdtPlus;
    @ApiModelProperty(value = "기압 (ESD·RS·EFT·SURGE·CS·MF·VDIP 만 저장)", example = "99.8")
    private String brmt;
    @ApiModelProperty(value = "기압 ±", example = "0.1")
    private String brmtPlus;

    @ApiModelProperty(value = "시험자명", example = "김시험")
    private String memName;
    @ApiModelProperty(value = "서명 첨부파일 ID (빈 값이면 서명 취소)", example = "FILE_000000000000862")
    private String atchFileId;
  }

  @ApiModel(value = "RawEnvUpdateReq", description = "측정일·환경조건 > 종료된 행 수정 (기술책임자)")
  @Getter
  @Setter
  @ToString
  public static class UpdateReq {
    @ApiModelProperty(value = "시험 고유번호 (성적서 발급일 등 시험 기준 검증용, 없으면 로데이터의 시험)", example = "15")
    private int testSeq;

    @ApiModelProperty(value = "시작일시 yyyy-MM-dd HH:mm[:ss]. 시작·종료 둘 다 비우면 일시는 그대로",
        example = "2026-11-04 09:10")
    private String testSDt;
    @ApiModelProperty(value = "종료일시 yyyy-MM-dd HH:mm[:ss]", example = "2026-11-04 11:40")
    private String testEDt;

    @ApiModelProperty(value = "온도", example = "23.1")
    private String temp;
    @ApiModelProperty(value = "온도 ±", example = "0.5")
    private String tempPlus;
    @ApiModelProperty(value = "습도", example = "45.2")
    private String hmdt;
    @ApiModelProperty(value = "습도 ±", example = "0.5")
    private String hmdtPlus;
    @ApiModelProperty(value = "기압 (ESD·RS·EFT·SURGE·CS·MF·VDIP 만 저장)", example = "99.8")
    private String brmt;
    @ApiModelProperty(value = "기압 ±", example = "0.1")
    private String brmtPlus;

    @ApiModelProperty(value = "시험자명", example = "김시험")
    private String memName;
    @ApiModelProperty(value = "서명 첨부파일 ID (빈 값이면 서명 취소)", example = "FILE_000000000000862")
    private String atchFileId;
  }

  @ApiModel(value = "RawEnvReasonReq", description = "측정일·환경조건 > 중단·삭제 사유")
  @Getter
  @Setter
  @ToString
  public static class ReasonReq {
    @ApiModelProperty(value = "시험 고유번호 (작성내역 기록 대상, 없으면 로데이터의 시험)", example = "15")
    private int testSeq;

    @ApiModelProperty(value = "사유 (필수, 최대 200자)", example = "장비 이상으로 재시험", required = true)
    private String reason;
  }

  @ApiModel(value = "RawEnvRow", description = "측정일·환경조건 행")
  @Getter
  @Setter
  @ToString
  public static class Row {
    private int envSeq;
    private int rawSeq;
    private String metKey;
    private int rowNo;

    @ApiModelProperty(value = "시작일시 yyyy-MM-dd HH:mm:ss")
    private String testSDt;
    @ApiModelProperty(value = "종료일시 yyyy-MM-dd HH:mm:ss")
    private String testEDt;
    @ApiModelProperty(value = "소요시간(분). 종료 전·이관 행은 null")
    private Long elapsedMin;

    private String temp;
    private String tempPlus;
    private String hmdt;
    private String hmdtPlus;
    private String brmt;
    private String brmtPlus;

    @ApiModelProperty(value = "시험원 아이디 ([시작] 누른 사용자)")
    private String memId;
    private String memName;
    @ApiModelProperty(value = "서명 첨부파일 ID ([저장]·수정 때 그대로 돌려보냄)")
    private String atchFileId;
    @ApiModelProperty(value = "서명 이미지 URL (표시용)")
    private String signUrl;

    @ApiModelProperty(value = "이관 행 여부")
    private int migYn;

    @ApiModelProperty(value = "진행 중 행: 화면에서 온습도·이름·서명 입력 가능 ([저장]·[종료] 때 envRows 로 저장)")
    private boolean canEdit;
    @ApiModelProperty(value = "[종료] 가능")
    private boolean canEnd;
    @ApiModelProperty(value = "[중단] 가능 (종료 전)")
    private boolean canStop;
    @ApiModelProperty(value = "종료된 행 수정(일시·온습도·이름·서명) 가능 : 기술책임자, update.do")
    private boolean canRevEdit;
    @ApiModelProperty(value = "삭제 가능 (기술책임자)")
    private boolean canDelete;
  }

  @ApiModel(value = "RawEnvTable", description = "시험방법별 측정일·환경조건 표")
  @Getter
  @Setter
  @ToString
  public static class Table {
    private String metKey;
    @ApiModelProperty(value = "[시작] 가능 : 살아있는 행 10개 미만 + 종료 안 된 행 없음 + 로데이터 수정 가능")
    private boolean canAdd;
    private List<Row> rows = new ArrayList<Row>();
  }
}
