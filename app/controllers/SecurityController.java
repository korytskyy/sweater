package controllers;

import models.SessionId;
import models.User;
import play.data.Form;
import play.data.validation.Constraints;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.AccountManager;
import services.Authenticator;
import services.SecurityService;
import views.html.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import static play.data.Form.form;

@Singleton
public class SecurityController extends Controller {

    // workaround for bug in java controllers which must pass optional parameters
    private static final String NO_MESSAGE = null;

    private Authenticator authenticator;

    public static User getCurrentUser() {
        return (User) Http.Context.current().args.get(SecurityService.CONTEXT_USER_KEY);
    }

    @Inject
    public SecurityController(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public Result loginPage() {
        return ok(login.render(form(UserId.class), NO_MESSAGE));
    }

    public Result loginProcessing() {
        try {
            Form<UserId> boundLoginForm = UserId.bindAndValidateForm();
            UserId userId = boundLoginForm.get();

            return authenticator.authenticate(userId.username, userId.password).map(u -> {
                setUpSession(u, "users.login.success");
                return redirect(routes.Application.index());
            }).orElse(
                    forbidden(login.render(boundLoginForm, Messages.get("users.login.invalid")))
            );
        } catch (UserId.FormValidationException e) {
            return e.getErrorResult();
        }
    }

    public Result logoutProcessing() {
        session().clear();
        return redirect(request().getHeader("referer"));
    }

    public Result signUpPage() {
        return ok(signup.render(form(UserId.class), NO_MESSAGE));
    }

    public Result signUpProcessing() {
        Form<UserId> boundSignUpForm = form(UserId.class).bindFromRequest();
        if (boundSignUpForm.hasErrors()) {
            return badRequest(signup.render(boundSignUpForm, Messages.get("users.error.correct-form")));
        }
        UserId userId = boundSignUpForm.get();

        try {
            setUpSession(AccountManager.registerNewAccount(userId.username, userId.password), "users.signup.success");
            return redirect(routes.Application.index());
        } catch (AccountManager.
                RegistrationException e) {
            return badRequest(signup.render(boundSignUpForm, Messages.get(e.getMessage())));
        }
    }

    private void setUpSession(SessionId sessionId, String successMessageKey) {
        session().clear();
        session(SessionId.sessionKey(), sessionId.sessionValue());
        flash(Flash.SUCCESS, Messages.get(successMessageKey));
    }

    public static class UserId {
        public static String FIELD_USERNAME = "username";
        public static String FIELD_PASSWORD = "password";

        @Constraints.Required
        public String username;

        @Constraints.Required
        public String password;

        public User createUser() {
            return User.create(username, password);
        }

        public static Form<UserId> bindAndValidateForm() throws FormValidationException {
            Form<UserId> boundLoginForm = form(UserId.class).bindFromRequest();
            if (boundLoginForm.hasErrors()) {
                throw new FormValidationException(badRequest(login.render(boundLoginForm, Messages.get("users.error.correct-form"))));
            }
            return boundLoginForm;
        }

        public static class FormValidationException extends Exception {
            Result errorResult;

            FormValidationException(Result errorResult) {
                this.errorResult = errorResult;
            }

            public Result getErrorResult() {
                return errorResult;
            }
        }
    }
}
