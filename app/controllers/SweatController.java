package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Sweat;
import models.User;
import play.Logger;
import play.i18n.Messages;
import play.libs.Comet;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import services.SweatMessenger;
import views.html.*;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.joining;


@Security.Authenticated(SecuredWithCookies.class)
public class SweatController extends Controller {

    private final SweatMessenger sweatMessenger;

    @Inject
    public SweatController(SweatMessenger sweatMessenger) {
        Logger.debug("Sweat controller construcotr");
        this.sweatMessenger = sweatMessenger;
    }

    public Result timeline() {
        Logger.debug("Sweat controller timeline");
        return ok(timeline.render(Sweat.Dao.findAll(), Sweat.MAX_LENGTH));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result create() {
        Logger.debug("Sweat controller create");
        JsonNode json = request().body().asJson();
        String sweatContent = json.findPath("content").textValue();
        if (sweatContent == null) {
            return badRequest(Messages.get("sweats.error.missing"));
        }
        return User.Dao.find(request().username()).map(user -> {
            Sweat.Builder builder = new Sweat.Builder(user, sweatContent);
            List<String> errorKeys = builder.validate();
            if (!errorKeys.isEmpty()) {
                return badRequest(errorKeys.stream().map(key -> Messages.get(key)).collect(joining(",")));
            }
            Sweat newSweat = builder.build();
            try {
                Sweat.Dao.save(newSweat);
                sweatMessenger.save(newSweat);
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

    public Result stream() {
        Logger.debug("Sweat controller stream");

        return ok(new Comet("parent.cometMessage") {
            @Override
            public void onConnected() {
                SweatMessenger.registerChunkOut(this);
            }
        });
    }
}
