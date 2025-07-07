package mvc.Annotations;

import mvc.Http.HttpContext;

public interface Middleware {
    public void onError(HttpContext context, Exception ex);

    public void executeBeforeAction(HttpContext context);

    public void executeAfterAction(HttpContext context);
}
