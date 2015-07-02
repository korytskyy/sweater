package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Sweat;
import models.User;
import play.Logger;
import play.i18n.Messages;
import play.libs.Comet;
import play.libs.Json;
import play.mvc.*;
import services.SecurityService;
import services.SweatManager;
import views.html.timelineComet;
import views.html.timelineWs;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.joining;

@Security.Authenticated(Secured.class)
public class SweatController extends Controller {

    private final SweatManager sweatManager;

    @Inject
    public SweatController(SweatManager sweatManager) {
        Logger.debug("Sweat controller construcotr");
        this.sweatManager = sweatManager;
    }

    public Result timelineCometPage() {
        Logger.debug("Sweat controller timelineCometPage");
        // todo implement pagination or analogue feature
        // todo implement additional loading of sweats which were created after this request completion but before WS subscription
        return ok(timelineComet.render(SweatManager.loadAllSweats(), Sweat.MAX_LENGTH));
    }

    public Result timelineWsPage() {
        return ok(timelineWs.render(SweatManager.loadAllSweats(), Sweat.MAX_LENGTH));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result create() {
        // todo refactor logic duplication with websocket implementation
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
                sweatManager.saveNewSweat(newSweat);
            } catch (SweatManager.SweatPersistenceException e) {
                Logger.error(e.getMessage());

                return internalServerError(e.getMessage());
            }
            return ok(Messages.get("sweats.created"));
        }).orElseGet(() -> {
            session().clear();
            return unauthorized(Messages.get("users.error.notfound"));
        });
    }

    // todo fix CSRF issue with ajax
    public Result streamComet() {
        Logger.debug("Sweat controller streamComet");

        return ok(new Comet("parent.message") {
            @Override
            public void onConnected() {
                SweatManager.subscribe(this);
            }
        });
    }

    public WebSocket<String> streamWs() {
        return new WebSocket<String>() {
            @Override
            public void onReady(In<String> in, Out<String> out) {

                in.onMessage((String msg) -> {
                    try {
                        //todo make automatical binding
                        JsonNode sweatJson = Json.parse(msg);
                        String content = sweatJson.findPath("content").textValue();
                        String username = sweatJson.findPath("owner").findPath("username").textValue();
                        //todo new token based authentication for ws/comet should be implemented
                        User user = SecurityService.authorizeForSiteAccess(username);
                        Sweat.Builder builder = new Sweat.Builder(user, content);
                        //todo refactor
                        List<String> errorKeys = builder.validate();
                        if (!errorKeys.isEmpty()) {
                            Logger.error(errorKeys.toString());
                            // todo send error to client
                        } else {
                            Sweat newSweat = builder.build();
                            try {
                                sweatManager.saveNewSweat(newSweat);
                            } catch (SweatManager.SweatPersistenceException e) {
                                Logger.error(e.getMessage());
                                // todo send error to client
                            }
                            // todo send success to client return Messages.get("sweats.created")
                        }
                    } catch (Exception e) {
                        Logger.error("something wrong " + e.getMessage());
                    }

                });

                in.onClose(() -> {
                    Logger.debug("ws onclose");
                    try {
                        sweatManager.unsubscribe(out);
                    } catch (Exception e) {
                        Logger.error("Unexpected during unsubscribe");
                    }
                });

                try {
                    sweatManager.subscribe(out);
                } catch (Exception e) {
                    Logger.error("Unexpected during subscribe");
                }
            }
        };
    }
}
