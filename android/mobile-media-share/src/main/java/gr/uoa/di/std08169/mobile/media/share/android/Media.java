package gr.uoa.di.std08169.mobile.media.share.android;

/**
 * Created by labis on 7/14/14.
 */
public class Media {
    private final String id;
    private final String title;
    private final String type;
    private final String user;
    private final double latitude;
    private final double longitude;

    public Media(final String id, final String title, final String type, final String user,
                 final double latitude, final double longitude) {
        if (id == null)
            throw new NullPointerException("ID can not be null");
        if (id.isEmpty())
            throw new IllegalArgumentException("ID can not be empty");
        if (title == null)
            throw new NullPointerException("Title can not be null");
        if (title.isEmpty())
            throw new IllegalArgumentException("Title can not be empty");
        if (type == null)
            throw new NullPointerException("Type can not be null");
        if (type.isEmpty())
            throw new IllegalArgumentException("Type can not be empty");
        if (user == null)
            throw new NullPointerException("User can not be null");
        if (user.isEmpty())
            throw new IllegalArgumentException("User can not be empty");
        this.id = id;
        this.title = title;
        this.type = type;
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getUser() {
        return user;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
