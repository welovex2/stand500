package egovframework.cnf;

import java.util.List;
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
import egovframework.cnf.dto.StoreDetailDTO;
import egovframework.cnf.dto.WhStopReq;
import egovframework.cnf.dto.ZoneRangeReq;
import egovframework.cnf.dto.ZoneRangeResult;
import egovframework.cnf.dto.ZoneStopReq;
import egovframework.cnf.service.StrService;
import egovframework.cnf.service.Warehouse;
import egovframework.cnf.service.Zone;
import egovframework.sam.service.SamBizException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(tags = {"창고관리"})
@RestController
@RequestMapping("/cnf/str")
public class StrController {

  @Resource(name = "StrService")
  private StrService strService;

  /* ===================== 창고 ===================== */

  @ApiOperation(value = "창고리스트",
      notes = "searchWord = 창고명, 관리자명, 위치 검색. includeStopped = Y 전송시 사용중지 창고도 조회\n\n"
          + "zoneCnt = 사용중 구역수, stopZoneCnt = 중지 구역수, itemCnt = 보관 시료수")
  @GetMapping(value = "/list.do")
  public BasicResponse list(
      @ApiParam(value = "창고명, 관리자명, 위치 검색", example = "1창고")
      @RequestParam(name = "searchWord", required = false) String searchWord,
      @ApiParam(value = "Y 전송시 사용중지 창고 포함", example = "Y")
      @RequestParam(name = "includeStopped", required = false) String includeStopped) throws Exception {
    if (!EgovUserDetailsHelper.isAuthenticated()) {
      return BasicResponse.builder().result(false).message(ResponseMessage.UNAUTHORIZED).build();
    }
    boolean withStopped = "Y".equalsIgnoreCase(includeStopped);
    List<Warehouse> list = strService.selectList(searchWord, withStopped);
    return BasicResponse.builder().result(true).data(list).build();
  }

  @ApiOperation(value = "창고 상세보기",
      notes = "구역리스트 포함, 중지된 창고와 구역도 조회됨\n\n"
          + "state : I 등록, U 수정, D 사용중지")
  @GetMapping(value = "/{strSeq}/detail.do")
  public BasicResponse detail(
      @ApiParam(value = "창고 SEQ", required = true, example = "1")
      @PathVariable(name = "strSeq") int strSeq) throws Exception {
    if (!EgovUserDetailsHelper.isAuthenticated()) {
      return BasicResponse.builder().result(false).message(ResponseMessage.UNAUTHORIZED).build();
    }
    StoreDetailDTO detail = strService.detail(strSeq);
    if (detail == null) {
      return BasicResponse.builder().result(false).message(ResponseMessage.NO_DATA).build();
    }
    return BasicResponse.builder().result(true).data(detail).build();
  }

  @ApiOperation(value = "창고 등록",
      notes = "whName 필수, mngId(관리자)와 deptSeq(부서)는 선택이라 부서만 입력해도 저장\n\n"
          + "mngId = MEMBER_TB.ID, deptSeq = DEPT_TB.DEPT_SEQ (/cnf/mem/dept/list.do)\n\n"
          + "typeCode 미전송시 01, 공통코드 WH (/cmm/comcode/list.do?code=WH)")
  @PostMapping(value = "/insert.do")
  public BasicResponse insert(@RequestBody Warehouse req) throws Exception {
    return saveWh(req, true);
  }

  @ApiOperation(value = "창고 수정",
      notes = "strSeq 필수, 수정할 항목만 전송하고 미전송 항목은 기존값 유지\n\n"
          + "whLoc, mngId, deptSeq 는 빈값 전송시 삭제\n\n"
          + "사용중지는 /cnf/str/stop.do")
  @PostMapping(value = "/update.do")
  public BasicResponse update(@RequestBody Warehouse req) throws Exception {
    return saveWh(req, false);
  }

  @ApiOperation(value = "창고 사용중지",
      notes = "strSeq 만 전송, 보관 시료 0건일때만 가능\n\n"
          + "구역은 그대로 유지, 중지후 state=D, udtDt=중지일, 되살리기 없음")
  @PostMapping(value = "/stop.do")
  public BasicResponse stop(@RequestBody WhStopReq req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    if (!EgovUserDetailsHelper.isAuthenticated()) {
      return BasicResponse.builder().result(false).message(ResponseMessage.UNAUTHORIZED).build();
    }
    if (req == null || req.getStrSeq() <= 0) {
      return BasicResponse.builder().result(false).message(ResponseMessage.CHECK_DATA).build();
    }
    Warehouse wh = new Warehouse();
    wh.setStrSeq(req.getStrSeq());
    wh.setUdtMemId(user.getId());
    try {
      return BasicResponse.builder().result(strService.stop(wh)).build();
    } catch (SamBizException e) {
      log.warn("str stop: {}", e.getMessage());
      return BasicResponse.builder().result(false).message(e.getMessage()).build();
    }
  }

  /* ===================== 구역 ===================== */

  @ApiOperation(value = "구역 등록",
      notes = "strSeq, zoneName 필수\n\n"
          + "zoneName 은 100자까지, 따옴표 불가, 같은 창고 사용중 구역끼리 중복불가\n\n"
          + "useCode : 01 일반, 02 인수요청, 03 장기 (미전송시 01)\n\n"
          + "여러개 등록은 /cnf/str/zone/insertRange.do")
  @PostMapping(value = "/zone/insert.do")
  public BasicResponse insertZone(@RequestBody Zone req) throws Exception {
    return saveZone(req, true);
  }

  @ApiOperation(value = "구역 범위등록",
      notes = "prefix, fromNo, toNo 로 구역명 생성 (가, 1, 12 → 가-01 ~ 가-12)\n\n"
          + "1회 50개까지, 사용중인 이름은 제외하고 등록, 전부 중복이면 거절\n\n"
          + "zoneDesc 는 등록후 /cnf/str/zone/update.do 에서 입력")
  @PostMapping(value = "/zone/insertRange.do")
  public BasicResponse insertZoneRange(@RequestBody ZoneRangeReq req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    if (!EgovUserDetailsHelper.isAuthenticated()) {
      return BasicResponse.builder().result(false).message(ResponseMessage.UNAUTHORIZED).build();
    }
    if (req == null || req.getStrSeq() <= 0) {
      return BasicResponse.builder().result(false).message(ResponseMessage.CHECK_DATA).build();
    }
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());
    try {
      ZoneRangeResult data = strService.insertZoneRange(req);
      return BasicResponse.builder().result(true).data(data).build();
    } catch (SamBizException e) {
      log.warn("zone range: {}", e.getMessage());
      return BasicResponse.builder().result(false).message(e.getMessage()).build();
    }
  }

  @ApiOperation(value = "구역 수정",
      notes = "zoneSeq 필수, 수정할 항목만 전송\n\n"
          + "zoneName 구역명, zoneDesc 구역설명(빈값 전송시 삭제), useCode 용도\n\n"
          + "useCode : 01 일반, 02 인수요청, 03 장기")
  @PostMapping(value = "/zone/update.do")
  public BasicResponse updateZone(@RequestBody Zone req) throws Exception {
    return saveZone(req, false);
  }

  @ApiOperation(value = "구역 사용중지",
      notes = "zoneSeq 만 전송, 보관 시료 0건일때만 가능\n\n"
          + "마지막 사용중 구역과 중지된 창고의 구역은 불가, 되살리기 없음")
  @PostMapping(value = "/zone/stop.do")
  public BasicResponse stopZone(@RequestBody ZoneStopReq req) throws Exception {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    if (!EgovUserDetailsHelper.isAuthenticated()) {
      return BasicResponse.builder().result(false).message(ResponseMessage.UNAUTHORIZED).build();
    }
    if (req == null || req.getZoneSeq() <= 0) {
      return BasicResponse.builder().result(false).message(ResponseMessage.CHECK_DATA).build();
    }
    Zone zone = new Zone();
    zone.setZoneSeq(req.getZoneSeq());
    zone.setUdtMemId(user.getId());
    try {
      return BasicResponse.builder().result(strService.stopZone(zone)).build();
    } catch (SamBizException e) {
      log.warn("zone stop: {}", e.getMessage());
      return BasicResponse.builder().result(false).message(e.getMessage()).build();
    }
  }

  /* ===================== 내부 ===================== */

  private BasicResponse saveWh(Warehouse req, boolean insert) {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    if (!EgovUserDetailsHelper.isAuthenticated()) {
      return BasicResponse.builder().result(false).message(ResponseMessage.UNAUTHORIZED).build();
    }
    if (req == null || (!insert && req.getStrSeq() <= 0)) {
      return BasicResponse.builder().result(false).message(ResponseMessage.CHECK_DATA).build();
    }
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());
    try {
      boolean result = insert ? strService.insert(req) : strService.update(req);
      return BasicResponse.builder().result(result).data(req).build();
    } catch (SamBizException e) {
      log.warn("str save: {}", e.getMessage());
      return BasicResponse.builder().result(false).message(e.getMessage()).build();
    }
  }

  private BasicResponse saveZone(Zone req, boolean insert) {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    if (!EgovUserDetailsHelper.isAuthenticated()) {
      return BasicResponse.builder().result(false).message(ResponseMessage.UNAUTHORIZED).build();
    }
    if (req == null || (!insert && req.getZoneSeq() <= 0)) {
      return BasicResponse.builder().result(false).message(ResponseMessage.CHECK_DATA).build();
    }
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());
    try {
      boolean result = insert ? strService.insertZone(req) : strService.updateZone(req);
      return BasicResponse.builder().result(result).data(req).build();
    } catch (SamBizException e) {
      log.warn("zone save: {}", e.getMessage());
      return BasicResponse.builder().result(false).message(e.getMessage()).build();
    }
  }
}
