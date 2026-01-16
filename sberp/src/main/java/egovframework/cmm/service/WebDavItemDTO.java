package egovframework.cmm.service;

public class WebDavItemDTO {
    private String davPath;     // "/ERP/2025/12/..."
    private String name;
    private boolean directory;
    private Long size;
    private String lastModified;
    private boolean canWrite;

    public String getDavPath() { return davPath; }
    public void setDavPath(String davPath) { this.davPath = davPath; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isDirectory() { return directory; }
    public void setDirectory(boolean directory) { this.directory = directory; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public String getLastModified() { return lastModified; }
    public void setLastModified(String lastModified) { this.lastModified = lastModified; }
    public boolean isCanWrite() { return canWrite; }
    public void setCanWrite(boolean canWrite) { this.canWrite = canWrite; }
}
