package egovframework.cmm.service;

import java.util.ArrayList;
import java.util.List;

public class WebDavNodeDTO {
    private String davPath;
    private String name;
    private boolean directory;
    private Long size;
    private String lastModified;
    private boolean canWrite;
    private List<WebDavNodeDTO> children = new ArrayList<WebDavNodeDTO>();

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
    public List<WebDavNodeDTO> getChildren() { return children; }
    public void setChildren(List<WebDavNodeDTO> children) { this.children = children; }
}
