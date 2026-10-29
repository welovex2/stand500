package egovframework.sam.util;

import java.util.ArrayList;
import java.util.List;
import egovframework.cmm.util.ErpDavPathUtil;
import egovframework.sam.dto.SamUploadFolderDTO;

public final class SamUploadFolderSupport {

  private static final String COMMON = "00.신청서 및 공통";
  private static final String PRODUCT_PHOTO = "01.제품사진";

  private SamUploadFolderSupport() {}

  public static List<SamUploadFolderDTO> buildUploadFolders(String ncFolderPath) {
    List<SamUploadFolderDTO> folders = new ArrayList<>();
    folders.add(folder("PRODUCT", "제품사진", ncFolderPath, null));
    folders.add(folder("PARTS", "부품배치도", ncFolderPath, "부품배치도"));
    folders.add(folder("APPEARANCE", "외관도", ncFolderPath, "외관도"));
    return folders;
  }

  private static SamUploadFolderDTO folder(String code, String label, String ncFolderPath,
      String leafName) {
    String relativePath = ncFolderPath + "/" + COMMON + "/" + PRODUCT_PHOTO;
    if (leafName != null && !leafName.isEmpty()) {
      relativePath = relativePath + "/" + leafName;
    }
    SamUploadFolderDTO dto = new SamUploadFolderDTO();
    dto.setCode(code);
    dto.setLabel(label);
    dto.setRelativePath(relativePath);
    dto.setDavPath(ErpDavPathUtil.normalizePathOrRoot("/ERP/" + relativePath));
    return dto;
  }

}
