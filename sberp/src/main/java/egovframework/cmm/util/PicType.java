package egovframework.cmm.util;

import java.util.HashMap;
import java.util.Map;

/**
 * picId별 시험구분 Enum
 */
public enum PicType {

    CE_MAIN_POWER("1", "CE (Main Power)"),
    CE_ISN("2", "CE (ISN)"),
    CE_B_BROADCAST_TUNER("3", "CE (B급/방송수신기/튜너)"),
    CE_B_RF_MOD_OUTPUT("4", "CE (B급/RF변조기/출력)"),
    RE_BELOW_1G("5", "RE B"),
    RE_ABOVE_1G("6", "RE A"),
    ESD("7", "ESD"),
    RS("8", "RS"),
    EFT_BURST("9", "EFT Burst"),
    SURGE("10", "Surge"),
    CS("11", "CS"),
    M_Field("12", "M-Field"),
    V_Dip("13", "V-Dip"),
    MAC("14", "시험기자재"),
    CE_LOAD_AUX_PORT("15", "CE (부하 및 부가포트)"),
    CLICK("16", "Click"),
    DP("17", "DP"),
    RE("18", "RE"),
    MEASURE_PIC("19", "측정사진");
  
    private final String picId;
    private final String description;

    private static final Map<String, PicType> MAP = new HashMap<>();

    static {
        for (PicType type : values()) {
            MAP.put(type.picId, type);
        }
    }

    PicType(String picId, String description) {
        this.picId = picId;
        this.description = description;
    }

    public String getPicId() {
        return picId;
    }

    public String getDescription() {
        return description;
    }

    public static PicType fromId(String picId) {
        return MAP.get(picId);
    }
}
