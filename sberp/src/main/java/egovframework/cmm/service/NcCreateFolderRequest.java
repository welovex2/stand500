package egovframework.cmm.service;

public class NcCreateFolderRequest {
    private String parentPath; // "/ERP/2025/12"
    private String name;       // "새폴더"

    public String getParentPath() { return parentPath; }
    public void setParentPath(String parentPath) { this.parentPath = parentPath; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
