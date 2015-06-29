package models;

import com.avaje.ebean.Model;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.*;

@Entity
public class User extends Model {

    @Id
    @GeneratedValue
    public Long id;

    @Column(unique = true)
    private String username;

    private String passwordHash;

    private static Finder<Long, User> find = new Finder<>(User.class);

    public User() {
    }

    private User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public static User create(String username, String plainPassword) {
        return new User(username, generatePasswordHash(plainPassword));
    }

    public static Optional<User> authenticate(String username, String plainPassword) {
        return find(username).filter(u -> BCrypt.checkpw(plainPassword, u.passwordHash));
    }

    public static String generatePasswordHash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static Optional<User> find(String username) {
        return Optional.ofNullable(find.where().eq("username", username).findUnique());
    }

    @Override
    public String toString() {
        return username;
    }
}