package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Entity
public class Sweat extends Model {

    public static final int MAX_LENGTH = 140;

    @Id
    @GeneratedValue
    private Long id;

    private String content;

    @Column(updatable = false, insertable = false)
    @GeneratedValue
    @CreatedTimestamp
    private OffsetDateTime creationTime;

    @ManyToOne
    private User owner;

    private static Finder<Long, Sweat> find = new Finder<Long, Sweat>(Sweat.class);

    /**
     * Purpose of this class is gathering Sweat properties for further validation before instantiation
     */
    public static class Builder {
        private String content;
        private User owner;

        /**
         *
         * @param owner User
         * @param content of potential Sweat
         */
        public Builder(User owner, String content) {
            this.owner = owner;
            this.content = content;
        }

        /**
         * Expose invalid state of object properties.
         * @return list of validation error keys for further mapping to user friendly descriptions
         */
        public List<String> validate() {
            ArrayList<String> validationErrorList = new ArrayList<>();
            if (content == null || content.isEmpty()) {
                validationErrorList.add("sweats.error.missing");
            }
            if (Objects.toString(content, "").length() > MAX_LENGTH) {
                validationErrorList.add("sweats.error.sizelimit");
            }
            return validationErrorList;
        }

        /**
         * Create Sweat object if state is valid
         * @return new Sweat object
         * @throws IllegalStateException in case of invalid state
         * @see Builder#validate()
         */
        public Sweat build() {
            if (!validate().isEmpty()) {
                throw new IllegalStateException("State of object properties is invlaid");
            }
            return new Sweat(owner, content);
        }
    }


    private Sweat(User owner, String content) {
        this.content = content;
        this.owner = owner;
    }

//    public Sweat() {
//    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public OffsetDateTime getCreationTime() {
        return creationTime;
    }

//    public void setCreationTime(OffsetDateTime creationTime) {
//        this.creationTime = creationTime;
//    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public static List<Sweat> findAll() {
        return find.findList();
    }

    @Override
    public String toString() {
        return String.format("%tF '%s' created by %s", creationTime, content, owner);
    }
}
