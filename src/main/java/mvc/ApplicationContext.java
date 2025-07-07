package mvc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mvc.Annotations.AuthorizationHandler;
import mvc.Annotations.SyncCacheHandler;
import mvc.App.Application;
import mvc.Cache.Redis;
import mvc.Exceptions.PageNotFoundException;
import mvc.Helpers.Audits.AuditService;
import mvc.Helpers.Audits.AuditService.AuditType;
import mvc.Helpers.Audits.AuditService.LogTarget;
import mvc.Http.HttpBase;
import java.util.Map;

public class ApplicationContext implements Application {

    private static Application application = new ApplicationContext();
    private static final String NOT_FOUND_URL = "/web/Views/Error/notFound.jsp";
    private static final String INTERNAL_ERROR_URL = "/web/Views/Error/internalError.jsp";

    public static Application getInstance() {
        return application;
    }

    @Override
    public void initialize(ServletContextEvent sce) {
        // ✅ This runs when the web app starts
        System.out.println("[Startup] MysticHome is initializing...");

        // Register middlewares
        HttpBase.addMiddleware(new AuthorizationHandler());
        HttpBase.addMiddleware(new SyncCacheHandler());
        System.out.println("[Startup] Middleware handlers registered");

        // Restore Redis SignalHub
        Redis.getSignalHub().restore(Redis.getSyncCachePrefix());
        System.out.println("[Startup] Redis SignalHub restored");
    }

    @Override
    public void onError(ServletRequest request, ServletResponse response, FilterChain chain, Throwable t) {
        // log error to file
        AuditService audit = new AuditService();
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();

        // Skip static resources (css, js, images, video, etc.)
        if (uri.matches(".*\\.(css|js|png|jpg|jpeg|gif|svg|ico|woff2?|ttf|eot|mp4|webm|avif)$") || uri
                .startsWith("/web/path_to_") || uri.startsWith("/web/Content/assets/")) {
            return; // Don't log static files
        }

        audit.setSource(uri);
        audit.setMessage(t.getMessage());
        audit.setType(AuditType.ERROR);
        audit.setTarget(LogTarget.FILE);
        audit.log();

        // Redirect to error page based on the Exception type
        try {
            if (t instanceof PageNotFoundException) {
                httpResponse.sendRedirect(NOT_FOUND_URL);

            } else {
                httpResponse.sendRedirect(INTERNAL_ERROR_URL);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

    }

    @Override
    public void onHttpRequest(ServletRequest request, ServletResponse response, FilterChain chain) {
        // Log to Database to replace Watcher
        AuditService audit = new AuditService();
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();

        // Skip static resources (css, js, images, video, etc.)
        if (uri.matches(".*\\.(css|js|png|jpg|jpeg|gif|svg|ico|woff2?|ttf|eot|mp4|webm|avif)$") || uri
                .startsWith("/web/path_to_") || uri.startsWith("/web/Content/assets/")) {
            return; // Don't log static files
        }

        audit.setSource(uri);
        audit.setMessage("API Request");
        audit.setType(AuditType.INFO);
        audit.setTarget(LogTarget.DATABASE);
        audit.log();
    }

    @Override
    public void onDestroy(ServletContextEvent sce) {
        System.out.println("[Shutdown] MysticHome is being undeployed...");

        try {
            System.out.println("[Shutdown] Shutting down Redis threads...");
            Redis.getSignalHub().shutdown();

            // Find and interrupt any remaining Redis threads
            Map<Thread, StackTraceElement[]> threadMap = Thread.getAllStackTraces();
            for (Thread thread : threadMap.keySet()) {
                String threadName = thread.getName();
                if (threadName.contains("Redis") || threadName.contains("Jedis") || threadName.contains("PubSub")) {
                    try {
                        System.out.println("[Shutdown] Interrupting thread: " + threadName);
                        thread.interrupt();
                    } catch (Exception e) {
                        System.err
                                .println("[Shutdown] Error interrupting thread " + threadName + ": " + e.getMessage());
                    }
                }
            }

            System.out.println("[Shutdown] All Redis threads terminated");
        } catch (Exception e) {
            System.err.println("[Shutdown] Error during Redis cleanup: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[Shutdown] MysticHome shutdown completed");
    }
}
