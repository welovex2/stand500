package egovframework.cmm.service;

public class UploadResultDTO {
    private boolean ok;
    private String message;
    private String davPath;      // 업로드된 파일의 DAV 경로("/ERP/..../a.jpg_xxx")
    private String publicUrl;    // 필요하면 공유 raw url
    private String originalName;

    public static UploadResultDTO ok(String davPath, String publicUrl, String originalName) {
      UploadResultDTO r = new UploadResultDTO();
        r.ok = true;
        r.davPath = davPath;
        r.publicUrl = publicUrl;
        r.originalName = originalName;
        return r;
    }

    public static UploadResultDTO fail(String msg) {
      UploadResultDTO r = new UploadResultDTO();
        r.ok = false;
        r.message = msg;
        return r;
    }

    public boolean isOk() { return ok; }
    public String getMessage() { return message; }
    public String getDavPath() { return davPath; }
    public String getPublicUrl() { return publicUrl; }
    public String getOriginalName() { return originalName; }
}
