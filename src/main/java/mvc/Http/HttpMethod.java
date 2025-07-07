package mvc.Http;

public enum HttpMethod {
    POST("POST"),
    GET("GET"),
    PUT("PUT"),
    DELETE("DELETE");

    private final String method;

    private HttpMethod(String method) {
        this.method = method;
    }
    
    public String get() {
        return method;
    }
}
