package mvc.Annotations;

import DTO.UserSession;
import mvc.Result;
import mvc.Helpers.SessionHelper;
import mvc.Http.HttpContext;
import mvc.Http.HttpStatusCode;

public class AuthorizationHandler implements Middleware {
    private final String UNAUTHORIZED_PAGE = System.getenv("UNAUTHORIZED_PAGE");
    private String accessUrls;

    @Override
    public void onError(HttpContext context, Exception ex) {
    }

    @Override
    public void executeBeforeAction(HttpContext context) {
        if ("*".equals(accessUrls)) {
            return;
        }
        SessionHelper session = new SessionHelper(context.getRequest().getSession());
        UserSession userSession = session.getUserSession();
        System.out.println(userSession.getRole());
        if (userSession.getAccessUrls() != null) {
            for (String userAccess : userSession.getAccessUrls()) {
                for (String accessUrl : accessUrls.split(",")) {
                    if (userSession.isAuthenticated() && accessUrl.equals(userAccess)) {
                        return;
                    }
                }
            }
        }

        // Unauthorized access
        context.setRequestCancelled(true);
        context.setResult(new Result());
        context.getResult().setStatusCode(HttpStatusCode.UNAUTHORIZED);
        context.getResult().setContentType("text/html");
        context.getResult().setCharset("UTF-8");
        context.getResult().setRedirect(true);
        context.getResult().setPath(UNAUTHORIZED_PAGE);

    }

    @Override
    public void executeAfterAction(HttpContext context) {

    }
}
