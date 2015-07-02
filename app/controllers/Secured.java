package controllers;

import models.SessionId;
import models.Sweat;
import models.User;
import play.Logger;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import services.SecurityService;

import java.util.List;

import static java.util.stream.Collectors.joining;

public class Secured extends Security.Authenticator {
    @Override
    public String getUsername(Http.Context context) {
        String username = context.session().get(SessionId.sessionKey());

        if (username == null) return null;

        User user = SecurityService.authorizeForSiteAccess(username);
        context.args.put(SecurityService.CONTEXT_USER_KEY, user);
        return username;
    }

    @Override
    public Result onUnauthorized(Http.Context context) {
        return redirect(routes.SecurityController.loginPage());
    }
}
