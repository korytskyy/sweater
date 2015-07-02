package services;

import com.google.inject.Inject;
import models.Sweat;
import play.Logger;
import play.libs.Comet;
import play.mvc.WebSocket;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class SweatManager {

    private SweatMessenger sweatMessenger;

    @Inject
    public SweatManager(SweatMessenger sweatMessenger) {
        this.sweatMessenger = sweatMessenger;
    }

    /**
     * Store sweat and broadcast it to other clients
     * @param sweat
     * @return the same sweat but with filled persistent attributes
     * @throws services.SweatManager.SweatPersistenceException in case of any IO or other issues
     */
    public Sweat saveNewSweat(Sweat sweat) {
        try {
            // todo potential transation issue
            Sweat.Dao.save(sweat);
            sweatMessenger.save(sweat);
            return sweat;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            throw new SweatPersistenceException(e);
        }
    }

    //todo implement async response
    public List<Sweat> loadAllSweats() {

        return Sweat.Dao.findAll();

    }

    public void subscribe(Comet comet) {
        sweatMessenger.subscribe(comet);
    }

    public void unsubscribe(Comet comet) {
        sweatMessenger.unsubscribe(comet);
    }

    public void subscribe(WebSocket.Out out) {
        sweatMessenger.subscribe(out);
    }

    public void unsubscribe(WebSocket.Out out) {
        sweatMessenger.unsubscribe(out);
    }

    public static class SweatPersistenceException extends RuntimeException {
        public SweatPersistenceException() {
        }

        public SweatPersistenceException(String message) {
            super(message);
        }

        public SweatPersistenceException(String message, Throwable cause) {
            super(message, cause);
        }

        public SweatPersistenceException(Throwable cause) {
            super(cause);
        }

        public SweatPersistenceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

}
