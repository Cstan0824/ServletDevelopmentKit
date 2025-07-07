package mvc.App;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import mvc.ApplicationContext;

@WebFilter("/*")
public class HttpRequestListener implements Filter {

    private Application application = ApplicationContext.getInstance();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            application.onHttpRequest(request, response, chain);
        } catch (Throwable t) {
            application.onError(request, response, chain, t);
        }
        chain.doFilter(request, response);
    }
}
