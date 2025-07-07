package Controllers;

import mvc.ControllerBase;
import mvc.Result;
import mvc.Annotations.HttpRequest;

public class SampleController extends ControllerBase {
    
    @HttpRequest(HttpMethod.GET)
    public Result index() throws Exception {
        return page();
    }
}
