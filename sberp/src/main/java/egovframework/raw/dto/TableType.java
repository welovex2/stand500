package egovframework.raw.dto;

public enum TableType {
  
  CE("METHOD_CE_TB"),
  CK("METHOD_CK_TB"),
  CS("METHOD_CS_TB"),
  DP("METHOD_DP_TB"),
  EFT("METHOD_EFT_TB"),
  ESD("METHOD_ESD_TB"),
  IMG("METHOD_IMG_TB"),
  MF("METHOD_MF_TB"),
  RE("METHOD_RE_TB"),
  RS("METHOD_RS_TB"),
  SURGE("METHOD_SURGE_TB"),
  TEL("METHOD_TEL_TB"),
  VDIP("METHOD_VDIP_TB");
  ;

  private final String tableName;

  TableType(String tableName) {
      this.tableName = tableName;
  }

  public String getTableName() {
      return tableName;
  }
  
}
