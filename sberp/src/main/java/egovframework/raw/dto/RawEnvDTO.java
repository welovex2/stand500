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

    @ApiModelProperty(value = "시험 챔버(구역) zones.do 의 zoneSeq. [시작] 새 행은 필수, 진행 중 행은 바꿀 때만 (빈 값이면 그대로)",
        example = "12")
    private Integer zoneSeq;

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

  @ApiModel(value = "RawEnvUpdateReq", description = "측정일·환경조건 > 행 수정·종료 처리 (기술책임자)")
  @Getter
  @Setter
  @ToString
  public static class UpdateReq {
    @ApiModelProperty(value = "시험 고유번호 (성적서 발급일 등 시험 기준 검증용, 없으면 로데이터의 시험)", example = "15")
    private int testSeq;

    @ApiModelProperty(value = "시작일시 yyyy-MM-dd HH:mm[:ss]. 종료된 행은 시작·종료 둘 다 비우면 그대로, 진행 중 행(종료 처리)은 둘 다 필수",
        example = "2026-11-04 09:10")
    private String testSDt;
    @ApiModelProperty(value = "종료일시 yyyy-MM-dd HH:mm[:ss]", example = "2026-11-04 11:40")
    private String testEDt;

    @ApiModelProperty(value = "시험 챔버(구역). 빈 값이면 그대로", example = "12")
    private Integer zoneSeq;

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

    @ApiModelProperty(value = "시험 챔버(구역). 이관 행·챔버 도입 전 행은 null")
    private Integer zoneSeq;
    @ApiModelProperty(value = "챔버 이름 (표시용)")
    private String chamberName;
    @ApiModelProperty(value = "구역 이름 (표시용, 구역이 하나뿐인 챔버는 빈 값)")
    private String zoneName;

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
    @ApiModelProperty(value = "[수정] 가능 : 기술책임자, update.do. 진행 중 행이면 종료 처리(시작·종료 일시 필수)")
    private boolean canRevEdit;
    @ApiModelProperty(value = "삭제 가능 (기술책임자)")
    private boolean canDelete;
  }

  @ApiModel(value = "RawEnvZone", description = "[시작] 때 고를 챔버(구역)")
  @Getter
  @Setter
  @ToString
  public static class Zone {
    private int zoneSeq;
    private int chamberSeq;
    private String chamberName;
    @ApiModelProperty(value = "구역 이름 (구역이 하나뿐인 챔버는 빈 값)")
    private String zoneName;
    @ApiModelProperty(value = "1이면 동시 사용 허용 (일반 시험실). 항상 inUse=false")
    private int multiYn;

    @ApiModelProperty(value = "사용 중이면 true (선택 불가로 표시)")
    private boolean inUse;
    @ApiModelProperty(value = "사용 중인 쪽 : RAW 측정 / ETC 로데이터 외 사용(현황판)")
    private String useSrc;
    @ApiModelProperty(value = "사용 중인 측정의 접수번호 (RAW)")
    private String useTestId;
    @ApiModelProperty(value = "사용 중인 측정의 시험방법 키 (RAW)")
    private String useMetKey;
    @ApiModelProperty(value = "로데이터 외 사용 용도 (ETC) : PRE 예비시험 / DEBUG 고객 디버깅 / CHECK 점검 / WITNESS 입회 / ETC 기타")
    private String usePurpose;
    private String useMemName;
    @ApiModelProperty(value = "사용 시작일시 yyyy-MM-dd HH:mm:ss")
    private String useStartDt;
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
