package gr.uoa.di.std08169.mobile.media.share.android.media;

import java.math.BigDecimal;
import java.util.Date;

import gr.uoa.di.std08169.mobile.media.share.android.user.User;

/**
 * Created by labis on 7/14/14.
 */
public class Media {
    private final String id;
	private final String type;
	private final long size;
	private final int duration;
	private final User user;
	private final Date created;
	private Date edited;
	private String title;
	private BigDecimal latitude;
	private BigDecimal longitude;
	private boolean publik;

    public Media(final String id, final String type, final long size, final int duration, final User user,
                 final Date created, final Date edited, final String title, final BigDecimal latitude,
                 final BigDecimal longitude, final boolean publik) {
        if (id == null)
            throw new NullPointerException("ID can not be null");
        if (id.isEmpty())
            throw new IllegalArgumentException("ID can not be empty");
        if (type == null)
            throw new NullPointerException("Type can not be null");
        if (type.isEmpty())
            throw new IllegalArgumentException("Type can not be empty");
        if (size < 0l)
            throw new IllegalArgumentException("Size can not be negative");
        if (duration < 0)
            throw new IllegalArgumentException("Duration can not be negative");
        if (user == null)
            throw new NullPointerException("User can not be null");
        if (created == null)
            throw new NullPointerException("Created can not be null");
        if (edited == null)
            throw new NullPointerException("Edited can not be null");
        if (title == null)
            throw new NullPointerException("Title can not be null");
        if (title.isEmpty())
            throw new IllegalArgumentException("Title can not be empty");
        if (latitude == null)
            throw new IllegalArgumentException("Latitude can not be null");
        if (longitude == null)
            throw new IllegalArgumentException("Longitude can not be null");
        this.id = id;
        this.type = type;
        this.size = size;
        this.duration = duration;
        this.user = user;
        this.created = created;
        this.edited = edited;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.publik = publik;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public int getDuration() {
        return duration;
    }

    public User getUser() {
        return user;
    }

    public Date getCreated() {
        return created;
    }

    public Date getEdited() {
        return edited;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public boolean isPublic() {
        return publik;
    }

    //Override tin equals kai hashCode gia na ginetai swsta h sugrish otan benous se collections
    //gia na mhn berdepsei idia pragmata
    @Override
    public boolean equals(final Object object) {
        return (object instanceof Media) && id.equals(((Media) object).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
