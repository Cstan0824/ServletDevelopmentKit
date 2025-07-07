package mvc.App;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

public interface Application {
    public void initialize(ServletContextEvent sce);

    public void onError(ServletRequest request, ServletResponse response, FilterChain chain, Throwable t);

    public void onHttpRequest(ServletRequest request, ServletResponse response, FilterChain chain);

    public void onDestroy(ServletContextEvent sce);
}
