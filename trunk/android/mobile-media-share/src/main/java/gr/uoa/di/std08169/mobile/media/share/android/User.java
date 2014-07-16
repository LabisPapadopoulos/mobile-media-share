package gr.uoa.di.std08169.mobile.media.share.android;

/**
 * Created by labis on 7/16/14.
 */
public class User {
    private final String email;
    private String name;
    private String photo;
    private UserStatus status;
    //To password prepei na phgainei pros th vash opote xreiazetai, alla pote na erxetai

    public User(final String email, final UserStatus status, final String name, final String photo) {
        if (email == null)
            //exception gia asfaleia se periptwsh null
            throw new IllegalArgumentException(User.class.getName() + " email can not be null");
        if (email.isEmpty())
            throw new IllegalArgumentException(User.class.getName() + " email can not be empty");
        if (status == null)
            throw new IllegalArgumentException(User.class.getName() + " status can not be null");
        this.email = email;
        this.status = status;
        this.name = ((name != null) && name.isEmpty()) ? null : name;
        this.photo = ((photo != null) && photo.isEmpty()) ? null : photo;
    }

    public String getEmail() {
        return email;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getPhoto() {
        return photo;
    }

    @Override
    public boolean equals(final Object object) {
        return (object instanceof User) && email.equals(((User) object).email);
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }

    @Override
    public String toString() {
        return email;
    }
}