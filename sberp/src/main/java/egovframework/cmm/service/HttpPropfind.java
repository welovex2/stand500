package egovframework.cmm.service;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import java.net.URI;

public class HttpPropfind extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "PROPFIND";

    public HttpPropfind(final String uri) {
        super();
        setURI(URI.create(uri));
    }
    public HttpPropfind(final URI uri) {
        super();
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
