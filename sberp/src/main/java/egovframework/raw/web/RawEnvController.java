package egovframework.raw.web;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.raw.dto.RawEnvDTO;
import egovframework.raw.service.RawEnvService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * 로데이터 측정일·환경조건 (RAW_ENV_TB). 설계: docs/design/raw-env-msrmn-multi.md §4
 *
 * [시작]·[종료]·[저장]은 방법별 insert.do(RawController)가 방법 데이터와 함께 처리한다. 여기는 조회와 행 단위 작업만.
 *
 * 응답: result=false 이면 message 에 사유. 성공 시 data 에 행(Row) 또는 표 목록(ListRes).
 */
@Api(tags = {"로데이터 측정일·환경조건"})
@RestController
@RequestMapping("/raw")
@Slf4j
public class RawEnvController {

  @Resource(name = "RawEnvService")
  private RawEnvService rawEnvService;

  /** 서비스 호출 + 공통 응답 (로그인 확인, 검증 실패 메시지 전달) */
  private interface Call {
    Object run(String loginId);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private BasicResponse respond(String what, Object req, Call call) {
    if (!Boolean.TRUE.equals(EgovUserDetailsHelper.isAuthenticated())) {
      return BasicResponse.builder().result(false).message(ResponseMessage.UNAUTHORIZED).build();
    }
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

    try {
      Object data = call.run(user.getId());
      return BasicResponse.builder().result(true).data(data).message("").build();
    } catch (IllegalArgumentException e) {
      log.info("{} 거부 - user={}, req={}, msg={}", what, user.getId(), req, e.getMessage());
      return BasicResponse.builder().result(false).message(e.getMessage()).build();
    } catch (Exception e) {
      log.error("{} 실패 - user={}, req={}", what, user.getId(), req, e);
      return BasicResponse.builder().result(false).message(ResponseMessage.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  @ApiOperation(value = "측정일·환경조건 조회",
      notes = "rawSeq 가 0 이면 빈 목록(로데이터 미작성). metKey 를 주면 그 시험방법 표만 (행이 없어도 빈 표)\n"
          + "표: canAdd([추가] 가능), envFormat(TH 온습도 / THP 온습도·기압)\n"
          + "행: canEdit(진행 중 입력), canEnd, canStop, canRevEdit(기술책임자 수정), canDelete, elapsedMin(분)\n"
          + "[시작]·[종료]·[저장]은 방법별 insert.do 의 envAction·envMetKey·envRows 로 처리")
  @GetMapping(value = "/{rawSeq}/env/list.do")
  public BasicResponse list(
      @ApiParam(value = "로데이터 고유번호", required = true, example = "9") @PathVariable(
          name = "rawSeq") final int rawSeq,
      @ApiParam(value = "시험 고유번호 (수정 가능 여부 판단용)", example = "15") @RequestParam(
          name = "testSeq", required = false, defaultValue = "0") final int testSeq,
      @ApiParam(value = "시험방법 키 (CE, RE1, RE2, RE3, ESD, RS, EFT, SURGE, CS, MF, VDIP, CLK, DP, TEL)",
          example = "ESD") @RequestParam(name = "metKey", required = false) final String metKey) {
    return respond("측정일 조회", rawSeq + "/" + metKey, new Call() {
      @Override
      public Object run(String loginId) {
        return rawEnvService.list(rawSeq, testSeq, metKey, loginId);
      }
    });
  }

  @ApiOperation(value = "측정일·환경조건 [중단]",
      notes = "진행 중(종료 전) 행 삭제. 사유 필수(200자) → 시험 작성내역에 기록")
  @PostMapping(value = "/env/{envSeq}/stop.do")
  public BasicResponse stop(
      @ApiParam(value = "행 고유번호", required = true, example = "1") @PathVariable(
          name = "envSeq") final int envSeq,
      @RequestBody final RawEnvDTO.ReasonReq req) {
    return respond("측정일 중단", envSeq + " " + req, new Call() {
      @Override
      public Object run(String loginId) {
        return rawEnvService.stop(envSeq, req, loginId);
      }
    });
  }

  @ApiOperation(value = "측정일·환경조건 종료된 행 수정 (기술책임자)",
      notes = "기술책임자만, 종료된 행만. 진행 중 행은 방법별 insert.do 의 envRows([저장]·[종료])로 저장\n"
          + "일시: testSDt·testEDt 둘 다 주면 수정(종료 ≥ 시작, 미래 불가), 둘 다 비우면 그대로\n"
          + "기압은 ESD·RS·EFT·SURGE·CS·MF·VDIP 만 저장 (그 외는 무시). 서명 취소는 sign 빈 값")
  @PostMapping(value = "/env/{envSeq}/update.do")
  public BasicResponse update(
      @ApiParam(value = "행 고유번호", required = true, example = "1") @PathVariable(
          name = "envSeq") final int envSeq,
      @RequestBody final RawEnvDTO.UpdateReq req) {
    return respond("측정일 저장", envSeq + " " + req, new Call() {
      @Override
      public Object run(String loginId) {
        return rawEnvService.update(envSeq, req, loginId);
      }
    });
  }

  @ApiOperation(value = "측정일·환경조건 행 삭제",
      notes = "기술책임자만. 사유 필수(200자) → 시험 작성내역에 기록")
  @PostMapping(value = "/env/{envSeq}/delete.do")
  public BasicResponse delete(
      @ApiParam(value = "행 고유번호", required = true, example = "1") @PathVariable(
          name = "envSeq") final int envSeq,
      @RequestBody final RawEnvDTO.ReasonReq req) {
    return respond("측정일 삭제", envSeq + " " + req, new Call() {
      @Override
      public Object run(String loginId) {
        return rawEnvService.delete(envSeq, req, loginId);
      }
    });
  }
}
