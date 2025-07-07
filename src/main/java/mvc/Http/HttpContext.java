package mvc.Http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mvc.Result;

public class HttpContext {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Result result;
    private boolean isRequestCancelled = false;

    public HttpContext(HttpServletRequest request, HttpServletResponse response, Result result) {
        this(request, response);
        this.result = result;
    }

    public HttpContext(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public HttpContext() {

    }

    public boolean isRequestCancelled() {
        return isRequestCancelled;
    }

    public void setRequestCancelled(boolean isRequestCancelled) {
        this.isRequestCancelled = isRequestCancelled;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
