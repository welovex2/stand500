package egovframework.raw.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import egovframework.cmm.service.EgovFileMngService;
import egovframework.cmm.service.ResponseMessage;
import egovframework.raw.dto.EnvMetKey;
import egovframework.raw.dto.RawEnvDTO;
import egovframework.raw.service.RawEnv;
import egovframework.raw.service.RawEnvMapper;
import egovframework.raw.service.RawEnvOverlap;
import egovframework.raw.service.RawEnvService;
import egovframework.raw.service.RawEnvZone;
import lombok.extern.slf4j.Slf4j;

/**
 * 로데이터 측정일·환경조건 (RAW_ENV_TB). 설계: docs/design/raw-env-msrmn-multi.md
 *
 * <ul>
 * <li>[시작]·[종료]·[저장]은 방법별 insert.do 에서 방법 데이터와 같은 트랜잭션으로 처리 (applyFromMethod)</li>
 * <li>시작·종료 시각은 DB NOW() (클라이언트 시각을 받지 않는다). MEM_ID 는 [시작] 누른 로그인 사용자</li>
 * <li>진행 중 행: 누구나 온습도·이름·서명 입력 ([저장]·[종료]), [중단]</li>
 * <li>종료된 행: 기술책임자(MEMBER_TB.REV_YN=1)만 수정(update.do)·삭제</li>
 * <li>[종료] 누락: 기술책임자가 update.do 로 시작·종료 일시를 넣어 닫는다 (기록 유지)</li>
 * <li>챔버(구역): [시작] 때 필수. 같은 구역에 진행 중인 측정·로데이터 외 사용이 있으면 거부
 * (docs/design/chamber-erp.md)</li>
 * <li>삭제·중단은 소프트 삭제 + 시험 작성내역(TEST_MEMO_TB) 기록</li>
 * </ul>
 */
@Service("RawEnvService")
@Slf4j
public class RawEnvServiceImpl implements RawEnvService {

  private static final int MAX_REASON = 200;
  private static final int MAX_VALUE = 50;

  private static final DateTimeFormatter OUT_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter MEMO_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  private static final DateTimeFormatter IN_DT_SEC = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static final DateTimeFormatter IN_DT_MIN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  @Autowired
  RawEnvMapper rawEnvMapper;

  @Autowired
  RawSupport rawSupport;

  @Autowired
  EgovFileMngService fileMngService;

  // ------------------------------------------------------------------ 조회

  @Override
  public List<RawEnvDTO.Table> tables(int rawSeq, int testSeq, String loginId, String... metKeys) {
    boolean rev = isRev(loginId);
    boolean editable = rawSupport.isRawEditable(testSeq, rawSeq);
    List<RawEnv> rows = rawSeq == 0 ? Collections.<RawEnv>emptyList()
        : rawEnvMapper.selectList(rawSeq, null);

    List<RawEnvDTO.Table> out = new ArrayList<RawEnvDTO.Table>();
    for (String metKey : metKeys) {
      EnvMetKey key = requireKey(metKey);
      RawEnvDTO.Table table = newTable(key);
      boolean anyOpen = false;
      for (RawEnv env : rows) {
        if (EnvMetKey.of(env.getMetKey()) != key) {
          continue;
        }
        table.getRows().add(toRow(env, rev, editable));
        if (env.getTestEDt() == null) {
          anyOpen = true;
        }
      }
      table.setCanAdd(editable && table.getRows().size() < key.getMaxRows() && !anyOpen);
      out.add(table);
    }
    return out;
  }

  @Override
  public List<RawEnvDTO.Zone> zones(String metKey) {
    EnvMetKey key = requireKey(metKey);
    List<RawEnvDTO.Zone> out = new ArrayList<RawEnvDTO.Zone>();
    int lastZoneSeq = 0;
    for (RawEnvZone z : rawEnvMapper.selectZones(key.name())) {
      // 한 구역에 사용 중인 건이 여러 개면(잘못된 데이터) 가장 먼저 시작한 건만
      if (z.getZoneSeq() == lastZoneSeq) {
        continue;
      }
      lastZoneSeq = z.getZoneSeq();

      RawEnvDTO.Zone zone = new RawEnvDTO.Zone();
      zone.setZoneSeq(z.getZoneSeq());
      zone.setChamberSeq(z.getChamberSeq());
      zone.setChamberName(z.getChamberName());
      zone.setZoneName(z.getZoneName());
      zone.setMultiYn(z.getMultiYn());
      zone.setInUse(z.getMultiYn() == 0 && z.getUseSrc() != null);
      if (zone.isInUse()) {
        zone.setUseSrc(z.getUseSrc());
        zone.setUseTestId(z.getUseTestId());
        zone.setUseMetKey(z.getUseMetKey());
        zone.setUsePurpose(z.getUsePurpose());
        zone.setUseMemName(z.getUseMemName());
        zone.setUseStartDt(z.getUseStartDt() == null ? null : z.getUseStartDt().format(OUT_DT));
      }
      out.add(zone);
    }
    return out;
  }

  // ------------------------------------------------------------------ 방법 저장과 함께 ([시작]·[종료]·[저장])

  @Override
  @Transactional
  public void applyFromMethod(int rawSeq, int testSeq, String loginId, String action,
      String metKey, List<RawEnvDTO.RowReq> rows, String... allowedKeys) {
    String act = StringUtils.hasText(action) ? action.trim().toUpperCase() : null;
    if (act != null && !RawEnvDTO.ACTION_START.equals(act) && !RawEnvDTO.ACTION_END.equals(act)) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_ACTION);
    }

    boolean hasRows = rows != null && !rows.isEmpty();
    if (act == null && !hasRows) {
      return; // 측정 행 관련 없는 일반 저장
    }
    if (rawSeq == 0) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_DATA);
    }

    List<String> allowed = Arrays.asList(allowedKeys);
    EnvMetKey target = null;
    if (act != null) {
      // 표가 하나뿐인 화면은 envMetKey 생략 가능. RE(표 3개)만 필수
      target = !StringUtils.hasText(metKey) && allowedKeys.length == 1
          ? requireKey(allowedKeys[0]) : requireKey(metKey);
      if (!allowed.contains(target.name())) {
        throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_MET_KEY);
      }
    }

    lockRaw(rawSeq);

    // 1) 진행 중 행 값 저장 (종료·삭제된 행, 다른 로데이터·다른 방법 행은 건드리지 않음)
    RawEnvDTO.RowReq newRowValues = null;
    if (hasRows) {
      for (RawEnvDTO.RowReq r : rows) {
        if (r == null) {
          continue;
        }
        if (r.getEnvSeq() == 0) {
          // 새 행 값은 [시작]일 때만 envMetKey 표의 새 행에 저장
          if (RawEnvDTO.ACTION_START.equals(act)) {
            newRowValues = r;
          }
          continue;
        }
        RawEnv env = rawEnvMapper.selectOne(r.getEnvSeq());
        if (env == null || env.getRawSeq() != rawSeq || !allowed.contains(env.getMetKey())) {
          throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_STATE);
        }
        if (env.getDelYn() != 0 || !env.isInProgress()) {
          log.info("진행 중이 아닌 측정 행 값은 무시 - envSeq={}", env.getEnvSeq());
          continue;
        }
        saveValues(env, r.getTemp(), r.getTempPlus(), r.getHmdt(), r.getHmdtPlus(), r.getBrmt(),
            r.getBrmtPlus(), r.getMemName(), r.getAtchFileId(), loginId);
        // 진행 중 행의 챔버는 [저장] 때 바꿀 수 있다 (빈 값이면 그대로)
        if (r.getZoneSeq() != null && !r.getZoneSeq().equals(env.getZoneSeq())) {
          changeZone(env, r.getZoneSeq(), loginId);
        }
      }
    }

    // 2) [시작] / [종료]
    if (RawEnvDTO.ACTION_START.equals(act)) {
      RawEnv started = startRow(rawSeq, testSeq, target,
          newRowValues == null ? null : newRowValues.getZoneSeq(), loginId);
      if (newRowValues != null) {
        saveValues(started, newRowValues.getTemp(), newRowValues.getTempPlus(),
            newRowValues.getHmdt(), newRowValues.getHmdtPlus(), newRowValues.getBrmt(),
            newRowValues.getBrmtPlus(), newRowValues.getMemName(), newRowValues.getAtchFileId(),
            loginId);
      }
    } else if (RawEnvDTO.ACTION_END.equals(act)) {
      endRow(rawSeq, testSeq, target, loginId);
    }
  }

  // ------------------------------------------------------------------ 중단·수정·삭제 (행 단위 API)

  @Override
  @Transactional
  public boolean stop(int envSeq, RawEnvDTO.ReasonReq req, String loginId) {
    String reason = requireReason(req.getReason());

    RawEnv env = requireAlive(envSeq);
    if (!env.isInProgress()) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_STATE);
    }
    rawSupport.assertRawEditable(req.getTestSeq(), env.getRawSeq());
    lockRaw(env.getRawSeq());

    if (rawEnvMapper.softDelete(envSeq, reason, loginId) == 0) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_STATE);
    }

    int testSeq = rawSupport.resolveTestSeq(req.getTestSeq(), env.getRawSeq());
    writeMemo(testSeq, "측정 중단", env, reason, loginId);
    return true;
  }

  @Override
  @Transactional
  public RawEnvDTO.Row update(int envSeq, RawEnvDTO.UpdateReq req, String loginId) {
    if (!isRev(loginId)) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_AUTH);
    }

    RawEnv env = requireAlive(envSeq);
    rawSupport.assertRawEditable(req.getTestSeq(), env.getRawSeq());
    lockRaw(env.getRawSeq());
    // 잠금 사이에 [종료]·[중단]됐을 수 있으니 다시 읽는다
    env = requireAlive(envSeq);
    if (env.getTestSDt() == null) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_STATE);
    }
    // 진행 중 행 = [종료] 누락 처리 : 시작·종료 일시 둘 다 필수, 저장하면 종료된 행이 된다
    boolean closing = env.isInProgress();
    LocalDateTime startedAt = env.getTestSDt();

    saveValues(env, req.getTemp(), req.getTempPlus(), req.getHmdt(), req.getHmdtPlus(),
        req.getBrmt(), req.getBrmtPlus(), req.getMemName(), req.getAtchFileId(), loginId);

    // 챔버: 빈 값이면 그대로. 바꾸면 그 시험방법에서 고를 수 있는 구역인지 확인 (시간 겹침은 아래에서)
    boolean zoneChanged = req.getZoneSeq() != null && !req.getZoneSeq().equals(env.getZoneSeq());
    if (zoneChanged) {
      requireZone(requireKey(env.getMetKey()), req.getZoneSeq());
      if (rawEnvMapper.updateZone(envSeq, req.getZoneSeq(), loginId) == 0) {
        throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_STATE);
      }
    }

    // 일시: 둘 다 비우면 그대로, 하나만 주면 오류. 화면은 분 단위로 보낸다 → 같은 분이면 저장된 값(초) 유지
    boolean hasS = StringUtils.hasText(req.getTestSDt());
    boolean hasE = StringUtils.hasText(req.getTestEDt());
    if (closing && !(hasS && hasE)) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_DATE);
    }
    boolean datesChanged = false;
    if (hasS || hasE) {
      LocalDateTime s = sameMinuteOrNew(parseDateTime(req.getTestSDt()), env.getTestSDt());
      LocalDateTime e = sameMinuteOrNew(parseDateTime(req.getTestEDt()), env.getTestEDt());
      if (s == null || e == null || e.isBefore(s) || e.isAfter(LocalDateTime.now())) {
        throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_DATE);
      }
      if (!s.equals(env.getTestSDt()) || !e.equals(env.getTestEDt())) {
        datesChanged = true;
        if (rawEnvMapper.updateDates(envSeq, s, e, loginId) == 0) {
          throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_STATE);
        }
        RawEnv saved = rawEnvMapper.selectOne(envSeq);
        afterDateChange(saved, rawSupport.resolveTestSeq(req.getTestSeq(), saved.getRawSeq()));
      }
    }

    // 일시나 서명(시험원)·챔버가 바뀌었을 수 있으니 최종 값으로 겹침 확인
    RawEnv result = rawEnvMapper.selectOne(envSeq);
    checkOverlap(result);
    if (zoneChanged || datesChanged) {
      checkZoneConflict(result);
    }

    if (closing) {
      int testSeq = rawSupport.resolveTestSeq(req.getTestSeq(), result.getRawSeq());
      EnvMetKey key = EnvMetKey.of(result.getMetKey());
      writeMemo(testSeq, String.format("[측정 종료 처리] %s %d행 (시작 %s → 종료 %s)",
          key == null ? result.getMetKey() : key.getLabel(), result.getRowNo(),
          startedAt.format(MEMO_DT), result.getTestEDt().format(MEMO_DT)), result, loginId);
    }
    return toRow(result, true, true);
  }

  @Override
  @Transactional
  public boolean delete(int envSeq, RawEnvDTO.ReasonReq req, String loginId) {
    if (!isRev(loginId)) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_AUTH);
    }
    String reason = requireReason(req.getReason());

    RawEnv env = requireAlive(envSeq);
    rawSupport.assertRawEditable(req.getTestSeq(), env.getRawSeq());
    lockRaw(env.getRawSeq());

    if (rawEnvMapper.softDelete(envSeq, reason, loginId) == 0) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_STATE);
    }

    int testSeq = rawSupport.resolveTestSeq(req.getTestSeq(), env.getRawSeq());
    writeMemo(testSeq, "측정일 삭제", env, reason, loginId);

    // 종료된 행이 빠졌으니 시험기간 다시 계산
    rawSupport.recalcAndUpdateTestDtRange(env.getRawSeq(), testSeq);
    return true;
  }

  // ------------------------------------------------------------------ 내부

  /**
   * [시작] : 살아있는 행 최대 개수(기본 10, TEL 1) 미만 + 진행 중 행 없음 → 새 행, TEST_S_DT = NOW().
   * 시작일 기준으로 성적서 발급일·측정설비 교정일 체크. 챔버(구역) 필수 + 사용 중이면 거부.
   */
  private RawEnv startRow(int rawSeq, int testSeq, EnvMetKey key, Integer zoneSeq,
      String loginId) {
    List<RawEnv> alive = rawEnvMapper.selectList(rawSeq, key.name());
    if (alive.size() >= key.getMaxRows()) {
      throw new IllegalArgumentException(String.format(ResponseMessage.CHECK_ENV_MAX, key.getMaxRows()));
    }
    for (RawEnv env : alive) {
      if (!env.isEnded()) {
        throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_NOT_ENDED);
      }
    }

    if (zoneSeq == null) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_ZONE_REQUIRED);
    }
    requireZone(key, zoneSeq);

    RawEnv env = new RawEnv();
    env.setRawSeq(rawSeq);
    env.setMetKey(key.name());
    env.setZoneSeq(zoneSeq);
    env.setRowNo(rawEnvMapper.selectNextRowNo(rawSeq, key.name()));
    env.setMemId(loginId);
    env.setInsMemId(loginId);
    rawEnvMapper.insertStart(env);

    RawEnv saved = rawEnvMapper.selectOne(env.getEnvSeq());

    // 같은 챔버에 진행 중인 측정·로데이터 외 사용이 있으면 안 된다 (구역 잠금은 requireZone 에서)
    checkZoneConflict(saved);

    LocalDate startDt = saved.getTestSDt().toLocalDate();

    // 시작일도 성적서 발급일 이후면 안 된다 (예외 시 방법 저장까지 롤백)
    rawSupport.validateMsrmnDt(rawSupport.resolveTestSeq(testSeq, rawSeq), startDt);

    // 시작 시점에 측정설비 교정일이 지나 있으면 안 된다 (같은 요청에서 저장한 장비 포함)
    for (String macType : key.getMacTypes()) {
      rawSupport.checkMacReformDate(rawSeq, macType, startDt);
    }
    return saved;
  }

  /** [종료] : 그 표의 진행 중 행에 TEST_E_DT = NOW(). 온도·습도(·기압)·시험자명 필수 */
  private void endRow(int rawSeq, int testSeq, EnvMetKey key, String loginId) {
    RawEnv open = null;
    for (RawEnv env : rawEnvMapper.selectList(rawSeq, key.name())) {
      if (env.isInProgress()) {
        open = env;
        break;
      }
    }
    if (open == null) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_STATE);
    }

    // 서명은 같은 시험원 시간 겹침 확인에 쓰므로 필수
    if (!StringUtils.hasText(open.getTemp()) || !StringUtils.hasText(open.getHmdt())
        || !StringUtils.hasText(open.getMemName()) || !StringUtils.hasText(open.getSignUrl())
        || (key.hasBrmt() && !StringUtils.hasText(open.getBrmt()))) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_REQUIRED);
    }

    if (rawEnvMapper.updateEnd(open.getEnvSeq(), loginId) == 0) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_STATE);
    }

    RawEnv saved = rawEnvMapper.selectOne(open.getEnvSeq());
    checkOverlap(saved);
    afterDateChange(saved, rawSupport.resolveTestSeq(testSeq, rawSeq));
  }

  /**
   * 온습도·기압·이름·서명 저장. 기압은 THP 방법만. 저장한 값으로 env 도 갱신.
   * 서명은 첨부파일 ID(SIGN_URL)만 받는다. SIGN(서명자 이름)은 이관 데이터용이라 건드리지 않는다.
   */
  private void saveValues(RawEnv env, String temp, String tempPlus, String hmdt, String hmdtPlus,
      String brmt, String brmtPlus, String memName, String atchFileId, String loginId) {
    EnvMetKey key = requireKey(env.getMetKey());

    env.setTemp(value(temp));
    env.setTempPlus(value(tempPlus));
    env.setHmdt(value(hmdt));
    env.setHmdtPlus(value(hmdtPlus));
    env.setBrmt(key.hasBrmt() ? value(brmt) : null);
    env.setBrmtPlus(key.hasBrmt() ? value(brmtPlus) : null);
    env.setMemName(value(memName));
    env.setSignUrl(StringUtils.hasText(atchFileId) ? atchFileId.trim() : null);
    env.setUdtMemId(loginId);

    if (rawEnvMapper.updateValues(env) == 0) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_STATE);
    }
  }

  /**
   * 종료일시가 정해지거나 바뀐 뒤: 측정일(종료일 기준) ≤ 성적서 발급일, 측정설비 교정일, 시험기간 재계산.
   */
  private void afterDateChange(RawEnv env, int testSeq) {
    if (env.getTestEDt() == null) {
      return;
    }
    LocalDate msrmnDt = env.getTestEDt().toLocalDate();

    rawSupport.validateMsrmnDt(testSeq, env.getTestSDt().toLocalDate());
    rawSupport.validateMsrmnDt(testSeq, msrmnDt);

    EnvMetKey key = EnvMetKey.of(env.getMetKey());
    if (key != null) {
      for (String macType : key.getMacTypes()) {
        rawSupport.checkMacReformDate(env.getRawSeq(), macType, msrmnDt);
      }
    }

    rawSupport.recalcAndUpdateTestDtRange(env.getRawSeq(), testSeq);
  }

  /**
   * 같은 시험원(서명)의 다른 측정과 시간이 겹치면 거부 (예외 → 방법 저장까지 롤백).
   * 서명이 없거나 시작·종료가 비어 있으면 비교할 수 없어 넘어간다. 이관 행끼리는 비교하지 않는다.
   */
  private void checkOverlap(RawEnv env) {
    if (env == null || env.getMigYn() != 0 || !StringUtils.hasText(env.getSignUrl())
        || env.getTestSDt() == null || env.getTestEDt() == null) {
      return;
    }
    RawEnvOverlap hit = rawEnvMapper.selectOverlap(env.getEnvSeq(), env.getSignUrl().trim(),
        env.getTestSDt(), env.getTestEDt());
    if (hit == null) {
      return;
    }
    EnvMetKey key = EnvMetKey.of(hit.getMetKey());
    String period = hit.getTestSDt().format(MEMO_DT) + " ~ "
        + (hit.getTestEDt() == null ? "진행 중" : hit.getTestEDt().format(MEMO_DT));
    log.info("측정 시간 겹침 - envSeq={}, 상대 envSeq={}", env.getEnvSeq(), hit.getEnvSeq());
    throw new IllegalArgumentException(String.format(ResponseMessage.CHECK_ENV_OVERLAP,
        hit.getRawId() == null ? "-" : hit.getRawId(),
        key == null ? hit.getMetKey() : key.getLabel(), period));
  }

  private void writeMemo(int testSeq, String title, RawEnv env, String reason, String loginId) {
    EnvMetKey key = EnvMetKey.of(env.getMetKey());
    String label = key == null ? env.getMetKey() : key.getLabel();
    String started = env.getTestSDt() == null ? "-" : env.getTestSDt().format(MEMO_DT);

    writeMemo(testSeq, String.format("[%s] %s %d행 (시작 %s) / 사유: %s", title, label,
        env.getRowNo(), started, reason), env, loginId);
  }

  private void writeMemo(int testSeq, String memo, RawEnv env, String loginId) {
    if (testSeq <= 0) {
      log.warn("작성내역 기록 대상 시험 없음 - envSeq={}, rawSeq={}", env.getEnvSeq(), env.getRawSeq());
      return;
    }
    rawEnvMapper.insertTestMemo(testSeq, memo, loginId);
  }

  // ------------------------------------------------------------------ 챔버(구역)

  /**
   * 구역 행을 잠그고(RAW_TB 잠금 뒤), 그 시험방법에서 고를 수 있는 구역인지 확인한다.
   * 구역·챔버가 사용 중(USE_YN=1)이고 CHAMBER_ZONE_MET_TB 에 시험방법이 매핑돼 있어야 한다.
   */
  private RawEnvZone requireZone(EnvMetKey key, int zoneSeq) {
    if (rawEnvMapper.lockZone(zoneSeq) == null) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_ZONE);
    }
    RawEnvZone zone = rawEnvMapper.selectZoneForMet(zoneSeq, key.name());
    if (zone == null) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_ZONE);
    }
    return zone;
  }

  /** 진행 중 행의 챔버 변경 ([저장]) : 고를 수 있는 구역 + 시간 겹침 없음 */
  private void changeZone(RawEnv env, int zoneSeq, String loginId) {
    requireZone(requireKey(env.getMetKey()), zoneSeq);
    if (rawEnvMapper.updateZone(env.getEnvSeq(), zoneSeq, loginId) == 0) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_STATE);
    }
    env.setZoneSeq(zoneSeq);
    checkZoneConflict(env);
  }

  /**
   * 같은 구역에서 이 행과 시간이 겹치는 측정 행·로데이터 외 사용이 있으면 거부 (예외 → 방법 저장까지 롤백).
   * 진행 중 행은 끝이 없는 것으로 본다. 구역이 없거나 동시 사용 허용(MULTI_YN=1) 구역, 이관 행은 넘어간다.
   */
  private void checkZoneConflict(RawEnv env) {
    if (env == null || env.getZoneSeq() == null || env.getMigYn() != 0
        || env.getTestSDt() == null) {
      return;
    }
    RawEnvZone zone = rawEnvMapper.selectZone(env.getZoneSeq());
    if (zone == null || zone.getMultiYn() == 1) {
      return;
    }
    // 이미 잠갔으면 그대로 통과한다 (같은 트랜잭션)
    rawEnvMapper.lockZone(env.getZoneSeq());

    RawEnvZone hit = rawEnvMapper.selectZoneConflict(env.getEnvSeq(), env.getZoneSeq(),
        env.getTestSDt(), env.getTestEDt());
    if (hit == null) {
      return;
    }
    log.info("챔버 사용 겹침 - envSeq={}, zoneSeq={}, 상대 {} {}", env.getEnvSeq(), env.getZoneSeq(),
        hit.getUseSrc(), hit.getUseSeq());
    throw new IllegalArgumentException(
        String.format(ResponseMessage.CHECK_ENV_ZONE_BUSY, describeUse(hit)));
  }

  /** 예) SB26-G3712-EM3320 ESD 김시험 2026-11-05 14:33 ~ 진행 중 / 점검 홍길동 2026-11-12 09:00 ~ 진행 중 */
  private String describeUse(RawEnvZone use) {
    StringBuilder sb = new StringBuilder();
    if ("ETC".equals(use.getUseSrc())) {
      sb.append(purposeLabel(use.getUsePurpose()));
    } else {
      EnvMetKey key = EnvMetKey.of(use.getUseMetKey());
      sb.append(use.getUseTestId() == null ? "-" : use.getUseTestId()).append(' ')
          .append(key == null ? use.getUseMetKey() : key.getLabel());
    }
    if (StringUtils.hasText(use.getUseMemName())) {
      sb.append(' ').append(use.getUseMemName());
    }
    sb.append(' ').append(use.getUseStartDt() == null ? "-" : use.getUseStartDt().format(MEMO_DT))
        .append(" ~ ")
        .append(use.getUseEndDt() == null ? "진행 중" : use.getUseEndDt().format(MEMO_DT));
    return sb.toString();
  }

  /** 로데이터 외 사용 용도 (chamber-svc 구분값) */
  private String purposeLabel(String purpose) {
    if (purpose == null) {
      return "로데이터 외 사용";
    }
    switch (purpose) {
      case "PRE":
        return "예비시험";
      case "DEBUG":
        return "고객 디버깅";
      case "CHECK":
        return "점검";
      case "WITNESS":
        return "입회";
      default:
        return "기타 사용";
    }
  }

  private RawEnvDTO.Table newTable(EnvMetKey key) {
    RawEnvDTO.Table table = new RawEnvDTO.Table();
    table.setMetKey(key.name());
    return table;
  }

  private RawEnvDTO.Row toRow(RawEnv env, boolean rev, boolean editable) {
    RawEnvDTO.Row row = new RawEnvDTO.Row();
    row.setEnvSeq(env.getEnvSeq());
    row.setRawSeq(env.getRawSeq());
    row.setMetKey(env.getMetKey());
    row.setRowNo(env.getRowNo());
    row.setTestSDt(env.getTestSDt() == null ? null : env.getTestSDt().format(OUT_DT));
    row.setTestEDt(env.getTestEDt() == null ? null : env.getTestEDt().format(OUT_DT));
    if (env.getMigYn() == 0 && env.getTestSDt() != null && env.getTestEDt() != null) {
      row.setElapsedMin(Duration.between(env.getTestSDt(), env.getTestEDt()).toMinutes());
    }
    row.setZoneSeq(env.getZoneSeq());
    row.setChamberName(env.getChamberName());
    row.setZoneName(env.getZoneName());
    row.setTemp(env.getTemp());
    row.setTempPlus(env.getTempPlus());
    row.setHmdt(env.getHmdt());
    row.setHmdtPlus(env.getHmdtPlus());
    row.setBrmt(env.getBrmt());
    row.setBrmtPlus(env.getBrmtPlus());
    row.setMemId(env.getMemId());
    row.setMemName(env.getMemName());
    row.setAtchFileId(env.getSignUrl());
    row.setSignUrl(resolveImageUrlSafe(env.getSignUrl()));
    row.setMigYn(env.getMigYn());

    boolean inProgress = env.isInProgress();
    boolean ended = env.isEnded();
    row.setCanEdit(editable && inProgress);
    row.setCanEnd(editable && inProgress);
    row.setCanStop(editable && inProgress);
    // 진행 중 행도 기술책임자는 [수정]으로 종료 처리할 수 있다
    row.setCanRevEdit(editable && rev && (ended || inProgress));
    row.setCanDelete(editable && rev);
    return row;
  }

  private EnvMetKey requireKey(String metKey) {
    EnvMetKey key = EnvMetKey.of(metKey);
    if (key == null) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_MET_KEY);
    }
    return key;
  }

  private RawEnv requireAlive(int envSeq) {
    RawEnv env = rawEnvMapper.selectOne(envSeq);
    if (env == null || env.getDelYn() != 0) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_STATE);
    }
    return env;
  }

  private void lockRaw(int rawSeq) {
    if (rawEnvMapper.lockRaw(rawSeq) == null) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_DATA);
    }
  }

  private boolean isRev(String loginId) {
    if (!StringUtils.hasText(loginId)) {
      return false;
    }
    Integer revYn = rawEnvMapper.selectRevYn(loginId);
    return revYn != null && revYn == 1;
  }

  private String requireReason(String reason) {
    if (!StringUtils.hasText(reason) || reason.trim().length() > MAX_REASON) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_ENV_REASON);
    }
    return reason.trim();
  }

  /** 입력값 정리: 공백만이면 null, 길이 초과면 거부 */
  private String value(String v) {
    if (!StringUtils.hasText(v)) {
      return null;
    }
    String t = v.trim();
    if (t.length() > MAX_VALUE) {
      throw new IllegalArgumentException(ResponseMessage.CHECK_DATA);
    }
    return t;
  }

  /** 입력이 분 단위로 저장값과 같으면 저장값(초 포함)을, 아니면 입력값을 쓴다 */
  private LocalDateTime sameMinuteOrNew(LocalDateTime input, LocalDateTime saved) {
    if (input != null && saved != null && input.getSecond() == 0 && input.getNano() == 0
        && input.equals(saved.truncatedTo(ChronoUnit.MINUTES))) {
      return saved;
    }
    return input;
  }

  private LocalDateTime parseDateTime(String value) {
    if (!StringUtils.hasText(value)) {
      return null;
    }
    String v = value.trim().replace('T', ' ');
    try {
      return LocalDateTime.parse(v, v.length() > 16 ? IN_DT_SEC : IN_DT_MIN);
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  private String resolveImageUrlSafe(String atchFileId) {
    if (atchFileId == null || atchFileId.trim().isEmpty()) {
      return "";
    }
    try {
      return fileMngService.resolveImageUrl(atchFileId);
    } catch (Exception e) {
      log.warn("resolveImageUrl failed. atchFileId={}", atchFileId, e);
      return "";
    }
  }
}
