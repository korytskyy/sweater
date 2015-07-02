package services;

import com.google.inject.ImplementedBy;
import models.SessionId;

import java.util.Optional;

@ImplementedBy(SecurityService.class)
public interface Authenticator {
    public Optional<? extends SessionId> authenticate(String userid, String plainPassword);

}

