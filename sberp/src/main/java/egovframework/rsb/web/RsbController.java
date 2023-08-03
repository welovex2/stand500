package egovframework.rsb.web;

import javax.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.rsb.RsbDTO;
import egovframework.rsb.service.RsbService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(tags = {"재발생 신청서"})
@RestController
@RequestMapping("/rsb")
public class RsbController {

  @Resource(name = "RsbService")
  private RsbService rsbService;

  @ApiOperation(value = "재발행 신청서 작성")
  @PostMapping(value = "/makeSbk.do")
  public BasicResponse makeSbk(@ApiParam(value = "sbkId 값만 전송") @RequestBody RsbDTO.Req req)
      throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    String sbkId = "";

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {

      // 재발행은 9번까지 가능
      int maxRev = rsbService.selectMaxRevision(req);
      if (maxRev >= 9) {
        result = false;
        msg = ResponseMessage.MAX_REVISION;
      } else {

        // 재발생 신청서 생성
        result = rsbService.insert(req);

        // 재발행 번호 설정
        sbkId = req.getSbkId().concat("-").concat(String.valueOf(req.getRevision()));

        // // 신청서 정보 보내주기
        // detail = sbkService.selectDetail(req);
        //
        // if (detail == null) {
        // result = false;
        // msg = ResponseMessage.NO_DATA;
        // }

      }

    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(sbkId).build();

    return res;

  }


  @ApiOperation(value = "재발행번호 확인")
  @PostMapping(value = "/selectMaxRevision.do")
  public BasicResponse selectMaxRevision(
      @ApiParam(value = "sbkId 값만 전송") @RequestBody RsbDTO.Req req) throws Exception {

    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    boolean result = true;
    String msg = "";
    int data = 0;

    // 로그인정보
    req.setInsMemId(user.getId());
    req.setUdtMemId(user.getId());

    Boolean isAuthenticated = EgovUserDetailsHelper.isAuthenticated();

    if (isAuthenticated) {

      // 재발생 신청서 생성
      data = rsbService.selectMaxRevision(req);

    }

    BasicResponse res = BasicResponse.builder().result(result).message(msg).data(data).build();

    return res;

  }

}
