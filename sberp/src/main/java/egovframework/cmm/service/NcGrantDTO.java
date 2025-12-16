package egovframework.cmm.service;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class NcGrantDTO  {

    /** 신청서번호 */
    private String sbkId;

    /** Nextcloud 폴더 DAV 경로 (예: /ERP/2025/12/SB25-G1578) */
    private String ncFolderPath;

    /** 공유 대상 Nextcloud 사용자ID(uid) */
    private String ncTargetUserId;

}
