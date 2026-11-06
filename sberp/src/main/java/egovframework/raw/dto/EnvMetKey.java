package egovframework.raw.dto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 측정일·환경조건 표(RAW_ENV_TB.MET_KEY) 구분.
 *
 * <ul>
 * <li>format : TH = 온도/습도, THP = 온도/습도/기압</li>
 * <li>macTypes : 교정일 검사에 쓰는 측정설비 구분 (RAW_MAC_TB.MACHINE_TYPE, RawServiceImpl 의 macList 와 동일)</li>
 * </ul>
 */
public enum EnvMetKey {

  CE("CE", "TH", "CA", "CB"),
  RE1("RE (1)", "TH", "RE1", "RA"),
  RE2("RE (2)", "TH", "RE2", "RB"),
  RE3("RE (3)", "TH", "RE3"),
  ESD("ESD", "THP", "ED"),
  RS("RS", "THP", "RS"),
  EFT("EFT/BURST", "THP", "ET"),
  SURGE("SURGE", "THP", "SG"),
  CS("CS", "THP", "CS"),
  MF("M-Field", "THP", "MF"),
  VDIP("V-Dip", "THP", "VD"),
  CLK("Click", "TH", "CK"),
  DP("DP", "TH", "DP"),
  /** TEL(KS X 3078) 은 측정 1회만 입력 */
  TEL(1, "TEL", "TH");

  /** 표 하나에 살아있는 행 기본 최대 개수 */
  public static final int MAX_ROWS = 10;

  private final int maxRows;
  private final String label;
  private final String format;
  private final List<String> macTypes;

  EnvMetKey(String label, String format, String... macTypes) {
    this(MAX_ROWS, label, format, macTypes);
  }

  EnvMetKey(int maxRows, String label, String format, String... macTypes) {
    this.maxRows = maxRows;
    this.label = label;
    this.format = format;
    this.macTypes = Collections.unmodifiableList(Arrays.asList(macTypes));
  }

  /** 표 하나에 살아있는 행 최대 개수 */
  public int getMaxRows() {
    return maxRows;
  }

  public String getLabel() {
    return label;
  }

  /** 기압 입력 여부 */
  public boolean hasBrmt() {
    return "THP".equals(format);
  }

  public List<String> getMacTypes() {
    return macTypes;
  }

  /** 측정설비 구분(RAW_MAC_TB.MACHINE_TYPE) → 그 설비를 쓰는 시험방법 표. 없으면 null */
  public static EnvMetKey ofMacType(String macType) {
    if (macType == null) {
      return null;
    }
    for (EnvMetKey k : values()) {
      if (k.macTypes.contains(macType.trim())) {
        return k;
      }
    }
    return null;
  }

  /** 문자열 → enum. 없으면 null */
  public static EnvMetKey of(String key) {
    if (key == null) {
      return null;
    }
    for (EnvMetKey k : values()) {
      if (k.name().equalsIgnoreCase(key.trim())) {
        return k;
      }
    }
    return null;
  }
}
