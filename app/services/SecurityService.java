package services;

import models.SessionId;
import models.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class SecurityService implements PasswordHasher, Authenticator {

    public final static String CONTEXT_USER_KEY = "user";

    public Optional<? extends SessionId> authenticate(String username, String plainPassword) {
        return User.Dao.find(username).filter(u -> BCrypt.checkpw(plainPassword, u.getPasswordHash()));
    }

    public String generatePasswordHash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Check whether user has grants for access to site
     *
     * @param username
     * @return
     * @throws services.SecurityService.AuthorizationException with message which contain error key
     */
    public static User authorizeForSiteAccess(String username) {
        return User.Dao.find(username).orElseThrow(()
                -> new AuthorizationException("users.error.notfound"));
    }

    public static class AuthorizationException extends RuntimeException {
        public AuthorizationException() {
        }

        public AuthorizationException(String message) {
            super(message);
        }

        public AuthorizationException(String message, Throwable cause) {
            super(message, cause);
        }

        public AuthorizationException(Throwable cause) {
            super(cause);
        }

        public AuthorizationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
