package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Sweat;
import models.User;
import play.Logger;
import play.i18n.Messages;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.*;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Security.Authenticated(Secured.class)
public class SweatController extends Controller {

    public Result timeline() {
        return ok(timeline.render(Sweat.MAX_LENGTH));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result create() {
        JsonNode json = request().body().asJson();
        String sweatContent = json.findPath("content").textValue();
        if (sweatContent == null) {
            return badRequest(Messages.get("sweats.error.missing"));
        }
        return User.find(request().username()).map(user -> {
            Sweat.Builder builder = new Sweat.Builder(user, sweatContent);
            List<String> errorKeys = builder.validate();
            if (!errorKeys.isEmpty()) {
                return badRequest(errorKeys.stream().map(key -> Messages.get(key)).collect(joining(",")));
            }
            Sweat newSweat = builder.build();
            try {
                newSweat.save();
            } catch (Exception e) {
                Logger.error(e.getMessage());
                return internalServerError(e.getMessage());
            }
            return ok(Messages.get("sweats.created"));
        }).orElseGet(() -> {
            session().clear();
            return unauthorized(Messages.get("users.error.notfound"));
        });

        }
    }
