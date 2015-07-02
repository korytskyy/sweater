package services;

import models.SessionId;
import models.User;

import javax.inject.Singleton;

@Singleton
public class AccountManager {
    /**
     * Register account which will be available for authentication
     *
     * @param accountName
     * @param accountPassword
     * @throws services.AccountManager.RegistrationException with message which contains error key
     */
    public static SessionId registerNewAccount(String accountName, String accountPassword) throws RegistrationException {
        if (User.Dao.find(accountName).isPresent()) {
            throw new RegistrationException("users.signup.usernametaken");
        }

        return User.Dao.save(User.create(accountName, accountPassword));
    }

    public static class RegistrationException extends Exception {
        public RegistrationException() {
        }

        public RegistrationException(String message) {
            super(message);
        }

        public RegistrationException(String message, Throwable cause) {
            super(message, cause);
        }

        public RegistrationException(Throwable cause) {
            super(cause);
        }

        public RegistrationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
