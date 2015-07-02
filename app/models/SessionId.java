package models;

import play.Play;

import java.util.Objects;

public interface SessionId {

    public static String sessionKey() {
        String key = Play.application().configuration().getString("session.key");
        if (key == null || "".equals(key)) {
            key = "sessionKey";
        }
        return key;
    }

    public String sessionId();
}
