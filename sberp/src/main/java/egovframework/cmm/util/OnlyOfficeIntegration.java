package egovframework.cmm.util;

import egovframework.rte.fdl.property.EgovPropertyService;

/**
 * ONLYOFFICE 미리보기 활성 여부. Nextcloud ONLYOFFICE 커넥터(OCS) 연동 기준.
 */
public final class OnlyOfficeIntegration {

  private OnlyOfficeIntegration() {}

  public static boolean isEnabled(EgovPropertyService propertyService) {
    if (propertyService == null) {
      return false;
    }
    try {
      String base = propertyService.getString("Globals.nc.base");
      String u = propertyService.getString("Globals.nc.user");
      String pw = propertyService.getString("Globals.nc.appPassword");
      return base != null && !base.trim().isEmpty() && u != null && !u.trim().isEmpty()
          && pw != null && !pw.trim().isEmpty();
    } catch (Exception e) {
      return false;
    }
  }
}
