package egovframework.ncc.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import egovframework.cmm.service.SbkInfoVO;
import egovframework.cmm.util.ErpDavPathUtil;
import egovframework.sbk.service.SbkService;
import egovframework.sbk.util.SbkIdSupport;

/**
 * 파일서버 모달 등에서 재발행 신청서번호(SB26-G1128-1)로 조회할 때
 * 실제 NC_FOLDER_PATH(원본 신청서 폴더) 기준으로 DAV 경로를 맞춘다.
 */
@Service("NcBrowsePathResolver")
public class NcBrowsePathResolver {

  private static final String ROOT = "/ERP";

  /** /ERP/2026/05/SB26-G1128-1/... 또는 /ERP/SB26-G1128-1/... */
  private static final Pattern REISSUE_IN_ERP_PATH = Pattern.compile(
      "^/ERP/(?:(\\d{4}/\\d{2})/)?(SB\\d{2}-[GM]\\d{4}-\\d+)(/.*)?$",
      Pattern.CASE_INSENSITIVE);

  @Resource(name = "SbkService")
  private SbkService sbkService;

  /**
   * 조회용 path를 WebDAV 기준 경로로 변환한다.
   *
   * @param path 프론트 전달값 (예: SB26-G1128-1, /ERP/2026/05/SB26-G1128-1/재발행 및 수정/...)
   */
  public String resolveBrowsePath(String path) throws Exception {
    if (path == null || path.trim().isEmpty()) {
      return ROOT;
    }

    String trimmed = path.trim();
    String normalized = ErpDavPathUtil.normalizePathOrRoot(trimmed);

    if (SbkIdSupport.isReissueSbkId(trimmed)) {
      return resolveReissueRoot(trimmed);
    }

    Matcher m = REISSUE_IN_ERP_PATH.matcher(normalized);
    if (m.matches()) {
      String reissueSbkId = m.group(2).toUpperCase();
      if (SbkIdSupport.isReissueSbkId(reissueSbkId)) {
        String suffix = m.group(3) == null ? "" : m.group(3);
        return resolveReissueWithSuffix(reissueSbkId, suffix);
      }
    }

    return normalized;
  }

  private String resolveReissueRoot(String reissueSbkId) throws Exception {
    String baseDav = toDavPath(resolveNcFolderPath(reissueSbkId));
    return baseDav != null ? baseDav : ErpDavPathUtil.normalizePathOrRoot(reissueSbkId);
  }

  private String resolveReissueWithSuffix(String reissueSbkId, String suffix) throws Exception {
    String baseDav = toDavPath(resolveNcFolderPath(reissueSbkId));
    if (baseDav == null) {
      return ErpDavPathUtil.normalizePathOrRoot(reissueSbkId + suffix);
    }
    if (suffix == null || suffix.isEmpty()) {
      return baseDav;
    }
    return ErpDavPathUtil.normalizePath(baseDav + suffix);
  }

  private String resolveNcFolderPath(String sbkId) throws Exception {
    SbkInfoVO sbk = sbkService.findBySbkNoReadonly(sbkId);
    if (sbk != null && StringUtils.hasText(sbk.getNcFolderPath())) {
      return sbk.getNcFolderPath();
    }

    if (SbkIdSupport.isReissueSbkId(sbkId)) {
      SbkInfoVO original =
          sbkService.findBySbkNoReadonly(SbkIdSupport.toOriginalSbkId(sbkId));
      if (original != null && StringUtils.hasText(original.getNcFolderPath())) {
        return original.getNcFolderPath();
      }
    }
    return null;
  }

  private static String toDavPath(String ncFolderPath) {
    if (!StringUtils.hasText(ncFolderPath)) {
      return null;
    }
    String p = ncFolderPath.trim().replace("\\", "/");
    if (p.startsWith(ROOT + "/") || ROOT.equals(p)) {
      return ErpDavPathUtil.normalizePath(p);
    }
    if (p.startsWith("/")) {
      p = p.substring(1);
    }
    return ErpDavPathUtil.normalizePath(ROOT + "/" + p);
  }
}
