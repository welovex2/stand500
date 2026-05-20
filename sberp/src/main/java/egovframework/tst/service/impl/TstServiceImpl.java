package egovframework.tst.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import egovframework.cmm.service.ComParam;
import egovframework.ncc.service.NextcloudFolderService;
import egovframework.ncc.service.NextcloudShareService;
import egovframework.sbk.service.SbkDTO;
import egovframework.sys.service.TestStndr;
import egovframework.tst.dto.CanCelDTO;
import egovframework.tst.dto.DebugDTO;
import egovframework.tst.dto.TestDTO.Req;
import egovframework.tst.dto.TestDTO.Res;
import egovframework.tst.dto.TestItemDTO;
import egovframework.tst.dto.TestMngrDTO;
import egovframework.tst.service.DbgMapper;
import egovframework.tst.service.Test;
import egovframework.tst.service.TestCate;
import egovframework.tst.service.TestMngr;
import egovframework.tst.service.TstMapper;
import egovframework.tst.service.TstParam;
import egovframework.tst.service.TstService;
import lombok.extern.slf4j.Slf4j;

@Service("TstService")
@Slf4j
public class TstServiceImpl implements TstService {

  @Autowired
  TstMapper tstMapper;

  @Autowired
  DbgMapper dbgMapper;

  @Autowired
  NextcloudShareService nextcloudShareService;

  @Autowired
  NextcloudFolderService nextcloudFolderService;

  @Override
  public List<TestCate> selectCrtfList(int topCode) {
    return tstMapper.selectCrtfList(topCode);
  }

  @Override
  public List<TestStndr> selectStndrList(TstParam param) {
    return tstMapper.selectStndrList(param);
  }

  @Override
  public Test selectDetail(Req req) {
    return tstMapper.selectDetail(req);
  }

  @Override
  @Transactional
  public boolean insert(Req req) throws Exception {

    // 시험부 코드: TEST_ITEM_TB 단일 원천 (요청값과 불일치 시 요청 무시)
    String authoritative = tstMapper.selectTestTypeCodeByTestItemSeq(req.getTestItemSeq());
    if (!StringUtils.hasText(authoritative)) {
      log.warn("makeTest: TEST_ITEM_TB.TEST_TYPE_CODE 없음 또는 삭제된 항목. testItemSeq={}",
          req.getTestItemSeq());
      return false;
    }
    authoritative = authoritative.trim();

    String fromClient = req.getTestTypeCode();
    if (StringUtils.hasText(fromClient) && !sameTestTypeBucket(authoritative, fromClient.trim())) {
      log.warn(
          "makeTest: 요청 testTypeCode 와 TEST_ITEM_TB 불일치 — DB 값 사용. testItemSeq={} db={} client={}",
          req.getTestItemSeq(), authoritative, fromClient);
    }
    req.setTestTypeCode(authoritative);

    // 1. 별도의 쿼리로 testNo를 먼저 계산해서 객체에 담음
    int nextNo = tstMapper.selectNextTestNo(req);
    req.setTestNo(nextNo);

    boolean result = tstMapper.insert(req);

    // insert 실패면 중단
    if (!result) {
      return false;
    }

    // insert 후 시험ID 확보
    int testNo = req.getTestNo();
    String testTypeCode = req.getTestTypeCode();

    // 1) TEST_TYPE_CODE 변환 (NS → SF)
    String type = "NS".equals(testTypeCode) ? "SF" : testTypeCode;

    // 2) TEST_NO 4자리 0패딩
    String paddedTestNo = String.format("%04d", testNo);

    // testSeq로 ERP DB에서 Nextcloud 경로/대상유저 조회
    String g = tstMapper.selectNcGrantByApplyNo(req.getTestSeq());

    // 최상위 폴더 경로
    String basePath = g + "/" + type + paddedTestNo;

    // Nextcloud 폴더 생성
    nextcloudFolderService.ensureFolder(basePath);

    // typeCode별 하위 폴더 구조 생성
    createSubFolders(basePath, type);

    return true;
  }

  /** NS/SF 는 채번·표시 로직과 동일하게 동일 시험부 묶음으로 본다. */
  private static boolean sameTestTypeBucket(String a, String b) {
    if (a == null || b == null) {
      return false;
    }
    String na = "NS".equals(a) ? "SF" : a;
    String nb = "NS".equals(b) ? "SF" : b;
    return na.equals(nb);
  }

  /**
   * typeCode별 하위 폴더 구조 생성
   * @throws Exception 
   */
  private void createSubFolders(String basePath, String type) throws Exception {
    switch (type) {
      case "EM":
        createEmFolders(basePath);
        break;
      case "RF":
        createRfFolders(basePath);
        break;
      case "SR":
        createSrFolders(basePath);
        break;
      case "SF":
        createSfFolders(basePath);
        break;
      case "MD":
        createMdFolders(basePath);
        break;
      default:
        // 최상위 폴더만 생성 (이미 위에서 생성됨)
        break;
    }
  }

  /**
   * EM 폴더 구조
   * ├── 01.성적서
   * ├── 02.데이터
   * │   ├── 측정데이터
   * │   └── Raw-Data
   * ├── 03.시험사진
   * │   └── 정전기포인트
   * └── 04.참고자료
   * @throws Exception 
   */
  private void createEmFolders(String basePath) throws Exception {
    // 1뎁스
    nextcloudFolderService.ensureFolder(basePath + "/01.성적서");
    nextcloudFolderService.ensureFolder(basePath + "/02.데이터");
    nextcloudFolderService.ensureFolder(basePath + "/03.시험사진");
    nextcloudFolderService.ensureFolder(basePath + "/04.참고자료");

    // 2뎁스 - 02.데이터 하위
    nextcloudFolderService.ensureFolder(basePath + "/02.데이터/측정데이터");
    nextcloudFolderService.ensureFolder(basePath + "/02.데이터/Raw-Data");

    // 2뎁스 - 03.시험사진 하위
    nextcloudFolderService.ensureFolder(basePath + "/03.시험사진/정전기포인트");
  }

  /**
   * RF 폴더 구조
   * ├── 01.성적서
   * ├── 02.데이터
   * │   ├── 측정데이터
   * │   ├── Raw-Data
   * │   └── 환경차트
   * ├── 03.시험사진
   * └── 04.참고자료
   *     ├── 안테나사양서
   *     ├── 사용자설명서
   *     └── 회로도
   * @throws Exception 
   */
  private void createRfFolders(String basePath) throws Exception {
    // 1뎁스
    nextcloudFolderService.ensureFolder(basePath + "/01.성적서");
    nextcloudFolderService.ensureFolder(basePath + "/02.데이터");
    nextcloudFolderService.ensureFolder(basePath + "/03.시험사진");
    nextcloudFolderService.ensureFolder(basePath + "/04.참고자료");

    // 2뎁스 - 02.데이터 하위
    nextcloudFolderService.ensureFolder(basePath + "/02.데이터/측정데이터");
    nextcloudFolderService.ensureFolder(basePath + "/02.데이터/Raw-Data");
    nextcloudFolderService.ensureFolder(basePath + "/02.데이터/환경차트");

    // 2뎁스 - 04.참고자료 하위
    nextcloudFolderService.ensureFolder(basePath + "/04.참고자료/안테나사양서");
    nextcloudFolderService.ensureFolder(basePath + "/04.참고자료/사용자설명서");
    nextcloudFolderService.ensureFolder(basePath + "/04.참고자료/회로도");
  }

  /**
   * SAR 폴더 구조
   * ├── 01.성적서
   * ├── 02.데이터
   * │   ├── 01.Conducted Power
   * │   │   └── Template
   * │   ├── 02.Liquid
   * │   ├── 03.Plot
   * │   ├── 04.Worksheet Template
   * │   └── 05.Raw data
   * │       ├── VALIDATION
   * │       └── PHONE
   * ├── 03.시험사진
   * └── 04.참고자료
   * @throws Exception 
   */
  private void createSrFolders(String basePath) throws Exception {
    // 1뎁스
    nextcloudFolderService.ensureFolder(basePath + "/01.성적서");
    nextcloudFolderService.ensureFolder(basePath + "/02.데이터");
    nextcloudFolderService.ensureFolder(basePath + "/03.시험사진");
    nextcloudFolderService.ensureFolder(basePath + "/04.참고자료");

    // 2뎁스 - 02.데이터 하위
    nextcloudFolderService.ensureFolder(basePath + "/02.데이터/01.Conducted Power");
    nextcloudFolderService.ensureFolder(basePath + "/02.데이터/02.Liquid");
    nextcloudFolderService.ensureFolder(basePath + "/02.데이터/03.Plot");
    nextcloudFolderService.ensureFolder(basePath + "/02.데이터/04.Worksheet Template");
    nextcloudFolderService.ensureFolder(basePath + "/02.데이터/05.Raw data");

    // 3뎁스 - 01.Conducted Power 하위
    nextcloudFolderService.ensureFolder(basePath + "/02.데이터/01.Conducted Power/Template");

    // 3뎁스 - 05.Raw data 하위
    nextcloudFolderService.ensureFolder(basePath + "/02.데이터/05.Raw data/VALIDATION");
    nextcloudFolderService.ensureFolder(basePath + "/02.데이터/05.Raw data/PHONE");
  }

  /**
  * SAFETY 폴더 구조
  * ├── 01.성적서
  * ├── 02.이력보관용
  * ├── 03.기술문서
  * ├── 04.부품
  * ├── 05.시험데이터
  * └── 06.제출서류
  *     └── KTC
   * @throws Exception 
  */
  private void createSfFolders(String basePath) throws Exception {
    // 1뎁스
    nextcloudFolderService.ensureFolder(basePath + "/01.성적서");
    nextcloudFolderService.ensureFolder(basePath + "/02.이력보관용");
    nextcloudFolderService.ensureFolder(basePath + "/03.기술문서");
    nextcloudFolderService.ensureFolder(basePath + "/04.부품");
    nextcloudFolderService.ensureFolder(basePath + "/05.시험데이터");
    nextcloudFolderService.ensureFolder(basePath + "/06.제출서류");

    // 2뎁스 - 06.제출서류 하위
    nextcloudFolderService.ensureFolder(basePath + "/06.제출서류/KTC");
  }

  /**
  * MEDICAL 폴더 구조
  * ├── 01.성적서
  * ├── 02.이력보관용
  * ├── 03.기술문서
  * ├── 04.부품
  * ├── 05.시험데이터
  * ├── 06.검토 [기술책임자 검토]
  * └── 07.제출서류
  *     ├── 식약처
  *     └── CB
   * @throws Exception 
  */
  private void createMdFolders(String basePath) throws Exception {
    // 1뎁스
    nextcloudFolderService.ensureFolder(basePath + "/01.성적서");
    nextcloudFolderService.ensureFolder(basePath + "/02.이력보관용");
    nextcloudFolderService.ensureFolder(basePath + "/03.기술문서");
    nextcloudFolderService.ensureFolder(basePath + "/04.부품");
    nextcloudFolderService.ensureFolder(basePath + "/05.시험데이터");
    nextcloudFolderService.ensureFolder(basePath + "/06.검토 [기술책임자 검토]");
    nextcloudFolderService.ensureFolder(basePath + "/07.제출서류");

    // 2뎁스 - 07.제출서류 하위
    nextcloudFolderService.ensureFolder(basePath + "/07.제출서류/식약처");
    nextcloudFolderService.ensureFolder(basePath + "/07.제출서류/CB");
  }

  @Override
  public int selectListCnt(ComParam param) {
    return tstMapper.selectListCnt(param);
  }

  @Override
  public List<Res> selectList(ComParam param) {
    List<Res> result = tstMapper.selectList(param);

    // 번호 매기기
    for (int i = 0; i < result.size(); i++) {
      result.get(i)
          .setNo(param.getTotalCount() - (((param.getPageIndex() - 1) * param.getPageUnit()) + i));
    }

    return result;
  }

  @Override
  @Transactional
  public boolean testMemInsert(TestMngrDTO req) {

    boolean result = true;

    tstMapper.testInfoUpate(req);

    for (TestMngr detail : req.getItems()) {

      detail.setTestSeq(req.getTestSeq());
      detail.setInsMemId(req.getInsMemId());
      detail.setUdtMemId(req.getUdtMemId());

      tstMapper.testMemInsert(detail);
    }

    return result;
  }

  @Override
  public boolean testMemSatetUpdate(TestMngrDTO req) {

    boolean result = true;

    tstMapper.testMemSatetUpdate(req);

    return result;
  }


  @Override
  public TestMngrDTO testMemList(String testSeq) {

    // 시험의 기본 정보
    TestMngrDTO detail = tstMapper.testMemInfo(testSeq);

    if (!ObjectUtils.isEmpty(detail)) {
      // 시험의 담당자 정보
      detail.setItems(tstMapper.testMemList(testSeq));
    }
    return detail;
  }

  @Override
  @Transactional
  public boolean testStateInsert(Req req) {
    boolean result = true;

    tstMapper.testStateInsert(req);

    // 디버깅 접수시 디버깅 리스트에 추가
    if ("3".equals(req.getStateCode())) {
      DebugDTO dto = new DebugDTO();
      dto.setTestStateSeq(req.getTestStateSeq());
      dto.setMemo(req.getMemo());
      dto.setInsMemId(req.getInsMemId());
      dbgMapper.insert(dto);
    }

    // 시험상태설정>사유기입 (#25)
    if (!StringUtils.isEmpty(req.getMemo())) {
      req.setMemo("[시험상태변경] ".concat(req.getMemo()));
      tstMapper.testBoardInsert(req);
    }

    // (#18) 시험상태 변경시, 시험테이블에 최신상태 SEQ 업데이트
    tstMapper.testStateUpdate(req);

    // 상태 18이면 Nextcloud 권한 회수
    if ("18".equals(req.getStateCode())) {
      try {
        revokeNcFolderGrant(req); // private 함수
      } catch (Exception e) {
        // 권한 회수 실패해도 시험상태 변경 자체는 살려두는 게 운영상 안전
        log.error("Nextcloud revoke fail testStateSeq={}, sbkId={}", req.getTestStateSeq(),
            req.getSbkId(), e);
      }
    }

    return result;
  }

  @Override
  public List<Res> testStateList(String testSeq) {
    return tstMapper.testStateList(testSeq);
  }

  @Override
  public boolean testBoardInsert(Req req) {
    return tstMapper.testBoardInsert(req);
  }

  @Override
  public List<Res> testBoardList(String testSeq) {
    return tstMapper.testBoardList(testSeq);
  }

  @Override
  public SbkDTO.Res testBoardAppDetail(String sbkId) {
    return tstMapper.testBoardAppDetail(sbkId);
  }

  @Override
  public boolean update(Req req) {
    return tstMapper.update(req);
  }

  @Override
  public List<Res> selectSaleList(ComParam param) {


    List<Res> result = tstMapper.selectSaleList(param);

    for (Res item : result) {

      if (item.getTestCnt() > 1) {


        List<TestItemDTO> subList = tstMapper.selectSubList(item.getSbkId(), param.getSearchVO());

        if (!ObjectUtils.isEmpty(subList)) {
          item.setItems(subList);
        }
      }
    }

    return result;
  }

  @Override
  public int selectSaleListCnt(ComParam param) {
    return tstMapper.selectSaleListCnt(param);
  }


  @Override
  public List<Res> selectRevList(ComParam param) {
    List<Res> result = tstMapper.selectRevList(param);

    // 번호 매기기
    for (int i = 0; i < result.size(); i++) {
      result.get(i)
          .setNo(param.getTotalCount() - (((param.getPageIndex() - 1) * param.getPageUnit()) + i));
    }

    return result;
  }

  @Override
  public CanCelDTO cancelInfo(int testItemSeq) {
    return tstMapper.cancelInfo(testItemSeq);
  }

  @Override
  @Transactional
  public boolean cancelInsert(CanCelDTO req) {

    boolean result = true;

    result = tstMapper.cancelInsert(req);
    result = tstMapper.cancelQuoUpdate(req);

    return result;
  }

  @Override
  public Res checkTestState(int testSeq) {
    return tstMapper.checkTestState(testSeq);
  }

  @Override
  public boolean checkInsert(Req req) {
    return tstMapper.checkInsert(req);
  }

  @Override
  public int selectCheckInfo(String testSeq) {
    return tstMapper.selectCheckInfo(testSeq);
  }

  @Override
  public boolean saleMemoInsert(Req req) {
    return tstMapper.saleMemoInsert(req);
  }

  private void revokeNcFolderGrant(Req req) throws Exception {

    // sbkId로 ERP DB에서 Nextcloud 경로/대상유저 조회
    String g = tstMapper.selectNcGrantByApplyNo(req.getTestSeq());
    if (g == null)
      return;

    String davPath = g; // "/ERP/2025/12/SB25-G1578"

    nextcloudShareService.revokeUserSharesByPath(davPath);
  }



}
