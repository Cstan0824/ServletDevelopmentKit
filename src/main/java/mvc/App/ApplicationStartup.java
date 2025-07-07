package mvc.App;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import mvc.ApplicationContext;

@WebListener
public class ApplicationStartup implements ServletContextListener {

    public Application application = ApplicationContext.getInstance();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        application.initialize(sce);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        application.onDestroy(sce);
    }
}
