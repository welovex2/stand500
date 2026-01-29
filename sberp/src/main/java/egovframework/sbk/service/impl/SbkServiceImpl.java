package egovframework.sbk.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import egovframework.cmm.service.CmmMapper;
import egovframework.cmm.service.ComParam;
import egovframework.cmm.service.EgovFileMngService;
import egovframework.cmm.service.FileVO;
import egovframework.cmm.service.HisDTO;
import egovframework.cmm.service.JobMngr;
import egovframework.cmm.service.NextcloudFolderService;
import egovframework.cmm.service.SbkInfoVO;
import egovframework.rte.fdl.idgnr.EgovIdGnrService;
import egovframework.sbk.service.SbkDTO;
import egovframework.sbk.service.SbkDTO.Req;
import egovframework.sbk.service.SbkDTO.Res;
import egovframework.sbk.service.SbkMapper;
import egovframework.sbk.service.SbkService;
import egovframework.tst.dto.TestItemDTO;
import egovframework.tst.service.TestItemRej;
import lombok.extern.slf4j.Slf4j;

@Service("SbkService")
@Slf4j
public class SbkServiceImpl implements SbkService {

  @Autowired
  SbkMapper sbkMapper;

  @Autowired
  CmmMapper cmmMapper;

  @Autowired
  NextcloudFolderService nextcloudFolderService;

  @Resource(name = "egovFileIdGnrService")
  EgovIdGnrService idgenService;

  @Autowired
  EgovFileMngService fileMngService;

  @Override
  public SbkDTO.Res selectDetail(Req req) {
    SbkDTO.Res detail;

    detail = sbkMapper.selectDetail(req);
    if (detail != null) {
      detail.setItems(sbkMapper.selectTestItemList(req));
    }
    return detail;
  }

  @Override
  @Transactional
  public boolean insert(Req req) throws Exception {
    boolean result = true;

    // 신청서 생성
    sbkMapper.insert(req);
    req.setSbkId(sbkMapper.selectRef(req));

    // 업무서 공통 정보
    if (StringUtils.isEmpty(req.getQuoId())) {
      sbkMapper.insertJob(req);
      // 업무담당자 히스토리 저장
      JobMngr job = new JobMngr();
      job.setJobSeq(req.getJobSeq());
      job.setMngId(req.getMngId());
      job.setInsMemId(req.getInsMemId());
      job.setUdtMemId(req.getUdtMemId());
      cmmMapper.insertJobMng(job);
    } else
      sbkMapper.updateJobSbk(req);

    try {

      String yearMonth =
          LocalDate.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy/MM"));


      String davPath = nextcloudFolderService.ensureApplyFolder(yearMonth, req.getSbkId());

      SbkInfoVO sbFolder = new SbkInfoVO();
      sbFolder.setNcFolderPath(davPath);
      sbFolder.setSbkId(req.getSbkId());
      sbkMapper.updateNcFolderPath(sbFolder);

      req.setNcFolderPath(davPath);
    } catch (Exception e) {
      // 트랜잭션은 유지하되, 폴더 생성 실패로 신청서까지 롤백시키지 않음
      log.error("Nextcloud folder create fail applyNo={}", req.getSbkId(), e);
      // TODO: 재시도 테이블/큐에 저장
    }

    return result;
  }

  @Override
  @Transactional
  public boolean update(Req sbk) {
    boolean result = true;

    // 신청서 수정
    result = sbkMapper.update(sbk);

    // 업무서 공통 정보
    result = sbkMapper.updateJob(sbk);

    return result;
  }

  @Override
  public int selectListCnt(ComParam param) {
    return sbkMapper.selectListCnt(param);
  }

  @Override
  public List<Res> selectList(ComParam param) throws Exception {
    List<SbkDTO.Res> list = sbkMapper.selectList(param);

    // 세부 아이템 정렬
    for (Res item : list) {

      if (item.getTestItemCnt() > 1) {


        List<TestItemDTO> subList = sbkMapper.selectSubList(item.getSbkId(), param.getSearchVO());

        if (!ObjectUtils.isEmpty(subList)) {
          item.setItems(subList);
        }
      }
    }

    // 번호 매기기
    for (int i = 0; i < list.size(); i++) {
      list.get(i)
          .setNo(param.getTotalCount() - (((param.getPageIndex() - 1) * param.getPageUnit()) + i));
    }

    return list;
  }

  @Override
  public boolean updateTestItemSign(TestItemDTO req) {
    return sbkMapper.updateTestItemSign(req);
  }

  @Override
  public List<TestItemRej> signRejectList(String testItemSeq) {
    return sbkMapper.signRejectList(testItemSeq);
  }

  @Override
  public boolean signRejectInsert(TestItemRej req) {
    return sbkMapper.signRejectInsert(req);
  }

  @Override
  public List<HisDTO> hisList(String sbkId) {
    return sbkMapper.hisList(sbkId);
  }

  @Override
  public Res selectDirtInfo(Req req) {
    return sbkMapper.selectDirtInfo(req);
  }

  @Override
  /**
   * 신청서번호로 신청서를 조회한다. - 신청서가 존재하면, 파일서버 사용을 위해 필요한 리소스 (NC_FOLDER_PATH, ATCH_FILE_ID)가 없을 경우 자동으로
   * 생성(provision)한다.
   *
   * ⚠️ 주의: - 이 메소드는 조회와 동시에 DB 상태를 변경할 수 있다. - 업로드 등 "쓰기 동작"에서만 사용해야 한다.
   */
  public SbkInfoVO findBySbkNoAndProvision(String sbkId) throws Exception {
    if (StringUtils.isEmpty(sbkId)) {
      return null;
    }

    SbkInfoVO result = sbkMapper.selectSbkBySbkNo(sbkId);
    if (result != null) {
      result.setSbkId(sbkId);
      provisionIfMissing(result);
    }
    return result;
  }

  @Override
  /**
   * 신청서번호로 신청서를 조회한다. (조회 전용)
   *
   * - DB 조회만 수행하며, 파일서버 폴더 생성이나 ATCH_FILE_ID 생성 등 어떠한 부수 효과(side effect)도 발생시키지 않는다. - 파일 목록
   * 조회(list), 화면 표시 등 읽기 전용 로직에서 사용한다.
   */
  public SbkInfoVO findBySbkNoReadonly(String sbkId) throws Exception {
    if (StringUtils.isEmpty(sbkId)) {
      return null;
    }

    SbkInfoVO result = sbkMapper.selectSbkBySbkNo(sbkId);
    if (result != null) {
      result.setSbkId(sbkId);
    }
    return result;
  }


  private void provisionIfMissing(SbkInfoVO sbk) throws Exception {

    boolean needFolder = StringUtils.isEmpty((sbk.getNcFolderPath()));
    boolean needAtchId = StringUtils.isEmpty((sbk.getAtchFileId()));

    if (!needFolder && !needAtchId)
      return;

    // 1) ins_dt → yyyy/MM
    String yearMonth = formatYearMonthFromInsDt(sbk.getInsDt()); // "2025/12"

    // 2) 폴더 생성 + DB 반영
    if (needFolder) {

      String davPath = nextcloudFolderService.ensureApplyFolder(yearMonth, sbk.getSbkId());
      sbk.setNcFolderPath(davPath);
      sbkMapper.updateNcFolderPath(sbk);

    }

    // 3) atch_file_id 생성 + FILE_TB 마스터 생성 + DB 반영
    if (needAtchId) {
      FileVO fileMaster = new FileVO();
      fileMaster.setAtchFileId(idgenService.getNextStringId());
      String atchFileId = fileMngService.insertFileMaster(fileMaster); // 기존 로직 그대로
      sbk.setAtchFileId(atchFileId);
      sbkMapper.updateAtchFileIdBySbkNoIfNull(sbk); // ✅ 조건부 업데이트 추천
      sbk.setAtchFileId(atchFileId);
    }

    // (선택) 조건부 업데이트로 인해 내가 set한 값이 실제 DB 반영 실패했을 수 있으니
    // 안전하게 재조회하는 방식도 가능:
    // sbk = sbkService.findBySbkNo(sbk.getSbkNo());
  }

  private String formatYearMonthFromInsDt(Date insDt) {
    if (insDt == null) {
      // fallback: 혹시 ins_dt가 없는 데이터가 있으면 현재로
      return LocalDate.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy/MM"));
    }
    Instant instant = insDt.toInstant();
    return instant.atZone(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy/MM"));
  }


}
