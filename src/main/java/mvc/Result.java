package mvc;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import mvc.Http.HttpStatusCode;

public class Result {
    private Object data;
    private String redirectPath = "";
    private boolean isRedirect = false;
    private String contentType = "text/html";
    private HttpStatusCode statusCode = HttpStatusCode.OK;
    private String charset = "UTF-8";
    private final Map<String, String> headers = new HashMap<>();
    private JsonNode params;

    public Result(Object data) {
        this.data = data;
    }

    public Result(Object data, String redirectPath) {
        this.redirectPath = redirectPath;
    }

    public Result() {
    }

    public void setParams(JsonNode params) {
        this.params = params;
    }

    public JsonNode getParams() {
        return params;
    }

    public boolean isRedirect() {
        return isRedirect;
    }

    public void setRedirect(boolean isRedirect) {
        this.isRedirect = isRedirect;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(HttpStatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public String getPath() {
        return redirectPath;
    }

    public void setPath(String path) {
        this.redirectPath = path;
    }
}