package Controllers;

import mvc.ControllerBase;
import mvc.Result;

public class LandingController extends ControllerBase {

    public Result index() throws Exception {
        return page();
    }
}
