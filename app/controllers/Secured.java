package controllers;

import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

public class Secured extends Security.Authenticator {
    @Override
    public String getUsername(Http.Context context) {
        return context.session().get(UserController.UserIdentification.FIELD_USERNAME);
    }

    @Override
    public Result onUnauthorized(Http.Context context) {
        return redirect(routes.UserController.loginPage());
    }
}