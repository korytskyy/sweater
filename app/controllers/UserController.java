package controllers;

import models.User;
import play.data.Form;
import play.data.validation.Constraints;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

import java.util.Optional;

import static play.data.Form.form;

public class UserController extends Controller {

    // workaround for bug in java controllers which must pass optional parameters
    private static final String NO_MESSAGE = null;

    public Result loginPage() {
        return ok(
                login.render(form(UserIdentification.class), NO_MESSAGE)
        );
    }

    public Result loginProcessing() {
        Form<UserIdentification> boundLoginForm = form(UserIdentification.class).bindFromRequest();

        if (boundLoginForm.hasErrors()) {
            return badRequest(login.render(boundLoginForm, Messages.get("users.error.correct-form")));
        }

        UserIdentification userIdentification = boundLoginForm.get();

        return userIdentification.authenticateUser().map(u -> {
            setUpSession(u);
            flashSuccess(Messages.get("users.login.success"));
            return redirect(routes.Application.index());
        }).orElseGet(() -> {
            return forbidden(login.render(boundLoginForm, Messages.get("users.login.invalid")));
        });
    }

    public Result logoutProcessing() {
        session().clear();
        return redirect(request().getHeader("referer"));
    }

    public Result signUpPage() {
        return ok(
                signup.render(form(UserIdentification.class), NO_MESSAGE)
        );
    }

    public Result signUpProcessing() {
        Form<UserIdentification> boundSignUpForm = form(UserIdentification.class).bindFromRequest();

        if (boundSignUpForm.hasErrors()) {
            return badRequest(signup.render(boundSignUpForm, Messages.get("users.error.correct-form")));
        }

        UserIdentification userIdentification = boundSignUpForm.get();

        if (User.find(userIdentification.username).isPresent()) {
            return badRequest(signup.render(boundSignUpForm, Messages.get("users.signup.usernametaken")));
        }

        User user = userIdentification.createUser();
        user.save();

        setUpSession(user);
        flashSuccess(Messages.get("users.signup.success"));

        return redirect(routes.Application.index());
    }

    private void flashSuccess(String message) {
        flash(Flash.SUCCESS, message);
    }

    private void setUpSession(User u) {
        session().clear();
        session(UserIdentification.FIELD_USERNAME, u.getUsername());
    }

    public static class UserIdentification {
        public static String FIELD_USERNAME = "username";
        public static String FIELD_PASSWORD = "password";

        @Constraints.Required
        public String username;

        @Constraints.Required
        public String password;

        public User createUser() {
            return User.create(username, password);
        }

        public Optional<User> authenticateUser() {
            return User.authenticate(username, password);
        }
    }
}
