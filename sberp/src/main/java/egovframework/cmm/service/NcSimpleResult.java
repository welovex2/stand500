package egovframework.cmm.service;

public class NcSimpleResult {
    private boolean ok;
    private String message;
    private String path;

    public static NcSimpleResult ok(String path) {
        NcSimpleResult r = new NcSimpleResult();
        r.ok = true;
        r.path = path;
        return r;
    }

    public static NcSimpleResult fail(String msg) {
        NcSimpleResult r = new NcSimpleResult();
        r.ok = false;
        r.message = msg;
        return r;
    }

    public boolean isOk() { return ok; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
}
