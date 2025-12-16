package egovframework.cmm.service;

public interface NextcloudShareService {

    /** /ERP public share token을 가져오거나(있으면), 없으면 생성해서 token 반환 */
    String getOrCreateErpFolderShareToken() throws Exception;
    
    /** 폴더/파일을 특정 사용자에게 공유 */
    void shareToUser(String path, String targetUserId, int permissions) throws Exception;

    /** 폴더/파일을 특정 그룹에게 공유 */
    void shareToGroup(String path, String targetGroupId, int permissions) throws Exception;
    
    /* 사용자 공유(share_type=0)만 삭제 */
    void revokeUserSharesByPath(String davPath) throws Exception;
}
