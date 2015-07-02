package services;

import com.google.inject.ImplementedBy;

@ImplementedBy(SecurityService.class)
public interface PasswordHasher {
    public String generatePasswordHash(String plainPassword);
}
