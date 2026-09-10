package egovframework.psh.service;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("PushMapper")
public interface PushMapper {

  /** 구독 등록. 같은 엔드포인트가 이미 있으면 되살리고 소유자·키를 갱신한다. */
  int upsertSub(PushSub sub);

  /** 사용자가 직접 해지. 본인 구독만 지운다. */
  int deleteSub(@Param("endpointHash") String endpointHash, @Param("memId") String memId);

  /** 푸시 서비스가 404/410 을 준 구독을 만료 처리한다. */
  int expireSub(@Param("endpointHash") String endpointHash, @Param("lastErr") String lastErr);

  /** 발송 대상 조회. STATE='I' 만. */
  List<PushSub> selectByMemId(String memId);

  List<PushSub> selectByMemIds(@Param("memIds") List<String> memIds);

  /**
   * 살아 있는 구독 전체. 공지사항처럼 수신자를 특정하지 않는 알림에 쓴다.
   *
   * @param exceptMemId 제외할 사용자(보통 작성자 본인). 없으면 null
   */
  List<PushSub> selectAllActive(@Param("exceptMemId") String exceptMemId);

  int countByMemId(String memId);

  int updateSendSuccess(Long pushSeq);

  int updateSendFail(@Param("pushSeq") Long pushSeq, @Param("lastErr") String lastErr);
}
