package services;

import models.SessionId;
import models.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class SecurityService implements PasswordHasher, Authenticator {
    public Optional<? extends SessionId> authenticate(String username, String plainPassword) {
        return User.Dao.find(username).filter(u -> BCrypt.checkpw(plainPassword, u.getPasswordHash()));
    }

    public String generatePasswordHash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }
}
