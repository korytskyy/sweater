package controllers;

import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Singleton;

@Singleton
public class Application extends Controller {

    public Result index() {
        keepFlashItem(Flash.SUCCESS);
        keepFlashItem(Flash.FAILURE);
        return redirect(routes.SweatController.timelineCometPage());
    }

    /*
    Keep item in flash for next request
     */
    private void keepFlashItem(String item) {
        if (flash().containsKey(item)) {
            flash(item, flash(item));
        }
    }
}
