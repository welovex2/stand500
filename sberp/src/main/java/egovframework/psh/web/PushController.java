package egovframework.psh.web;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import egovframework.cmm.service.BasicResponse;
import egovframework.cmm.service.LoginVO;
import egovframework.cmm.service.ResponseMessage;
import egovframework.cmm.util.EgovUserDetailsHelper;
import egovframework.psh.service.PushDTO;
import egovframework.psh.service.PushService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * 웹푸시 알림 API.
 *
 * <p>모든 경로가 {@code .do} 로 끝나므로 AuthInterceptor 가 세션 로그인을 요구한다.
 * 구독은 로그인 이후에만 등록되며, 발송 대상은 MEMBER_TB.ID 단위다.
 */
@Slf4j
@Api(tags = {"웹푸시 알림"})
@RestController
@RequestMapping("/push")
public class PushController {

  @Resource(name = "PushService")
  private PushService pushService;

  @ApiOperation(value = "웹푸시 설정 조회",
      notes = "VAPID 공개키와 현재 사용자의 구독 여부를 돌려준다. 프론트는 이 값으로 알림 버튼 상태를 정한다.")
  @GetMapping(value = "/config.do")
  public BasicResponse config() {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    if (ObjectUtils.isEmpty(user)) {
      return BasicResponse.builder().result(false).message(ResponseMessage.NO_LOGIN).build();
    }

    PushDTO.Config config = new PushDTO.Config();
    config.setConfigured(pushService.isConfigured());
    config.setPublicKey(pushService.getPublicKey());
    config.setSubscribed(pushService.isSubscribed(user.getId()));

    return BasicResponse.builder().result(true).data(config).build();
  }

  @ApiOperation(value = "웹푸시 구독 등록",
      notes = "브라우저의 PushSubscription.toJSON() 을 그대로 보낸다. 같은 기기가 다시 보내면 갱신된다.")
  @PostMapping(value = "/subscribe.do")
  public BasicResponse subscribe(@RequestBody PushDTO.Subscribe req, HttpServletRequest request) {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    if (ObjectUtils.isEmpty(user)) {
      return BasicResponse.builder().result(false).message(ResponseMessage.NO_LOGIN).build();
    }

    try {
      pushService.subscribe(req, user.getId(), request.getHeader("User-Agent"));
      return BasicResponse.builder().result(true).message("알림이 켜졌습니다.").build();
    } catch (IllegalArgumentException e) {
      return BasicResponse.builder().result(false).message(e.getMessage()).build();
    } catch (Exception e) {
      log.error("웹푸시 구독 등록 실패: memId={}", user.getId(), e);
      return BasicResponse.builder().result(false).message(ResponseMessage.RETRY).build();
    }
  }

  @ApiOperation(value = "웹푸시 구독 해지", notes = "브라우저에서 unsubscribe() 한 뒤 같은 endpoint 를 보낸다.")
  @PostMapping(value = "/unsubscribe.do")
  public BasicResponse unsubscribe(@RequestBody PushDTO.Unsubscribe req) {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    if (ObjectUtils.isEmpty(user)) {
      return BasicResponse.builder().result(false).message(ResponseMessage.NO_LOGIN).build();
    }

    try {
      pushService.unsubscribe(req == null ? null : req.getEndpoint(), user.getId());
      return BasicResponse.builder().result(true).message("알림이 꺼졌습니다.").build();
    } catch (IllegalArgumentException e) {
      return BasicResponse.builder().result(false).message(e.getMessage()).build();
    } catch (Exception e) {
      log.error("웹푸시 구독 해지 실패: memId={}", user.getId(), e);
      return BasicResponse.builder().result(false).message(ResponseMessage.RETRY).build();
    }
  }

  @ApiOperation(value = "웹푸시 테스트 발송",
      notes = "로그인한 본인의 모든 기기로 테스트 알림을 보낸다. 발송에 성공한 기기 수를 돌려준다.")
  @PostMapping(value = "/test.do")
  public BasicResponse test() {
    LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();
    if (ObjectUtils.isEmpty(user)) {
      return BasicResponse.builder().result(false).message(ResponseMessage.NO_LOGIN).build();
    }

    PushDTO.Message message = new PushDTO.Message();
    message.setTitle("ERP 알림 테스트");
    message.setBody(user.getEmpName() + "님, 알림이 정상적으로 도착했습니다.");
    message.setUrl("/mErp/");
    message.setTag("erp-test");

    int sent = pushService.sendToMember(user.getId(), message);
    if (sent == 0) {
      return BasicResponse.builder().result(false)
          .message("발송된 기기가 없습니다. 알림 권한과 구독 상태를 확인하세요.").data(0).build();
    }
    return BasicResponse.builder().result(true).message(sent + "대 기기로 발송했습니다.").data(sent).build();
  }
}
