package gr.uoa.di.std08169.mobile.media.share.shared;

import java.math.BigDecimal;
import java.util.Date;

public class Media {
	//Geografiko platos (voreia/noteia)
	private static final BigDecimal MIN_LATITUDE = new BigDecimal(-90);
	private static final BigDecimal MAX_LATITUDE = new BigDecimal(90);
	//Geografiko Mhkos (anatolika/dutika)
	private static final BigDecimal MIN_LONGITUDE = new BigDecimal(0);//GMT (Londino)
	private static final BigDecimal MAX_LONGITUDE = new BigDecimal(360);
	
	private final String id;
	private final String type;
	private final long size;
	private final int duration;
	private final User user;
	private final Date created;
	//Metadedomena
	private Date edited;
	private String title;
	private BigDecimal latitude;
	private BigDecimal longitude;
	private boolean publik;
	
	public Media(final String id, final String type, final long size, final int duration, final User user, 
			final Date created, final Date edited, final String title, final BigDecimal latitude, 
			final BigDecimal longitude, final boolean publik) {
		
		if (id == null)
			throw new IllegalArgumentException(Media.class.getName() + " id can not be null");
		if (id.isEmpty())
			throw new IllegalArgumentException(Media.class.getName() + " id can not be empty");
		if (type == null)
			throw new IllegalArgumentException(Media.class.getName() + " type can not be null");
		if (type.isEmpty())
			throw new IllegalArgumentException(Media.class.getName() + " type can not be empty");
		if (size <= 0)
			throw new IllegalArgumentException(Media.class.getName() + " size must be positive");
		if (duration < 0)
			throw new IllegalArgumentException(Media.class.getName() + " duration can not be negative");
		if (user == null)
			throw new IllegalArgumentException(Media.class.getName() + " user can not be null");
		if (created == null)
			throw new IllegalArgumentException(Media.class.getName() + " created can not be null");
		if (edited == null)
			throw new IllegalArgumentException(Media.class.getName() + " edited can not be null");
		if (title == null)
			throw new IllegalArgumentException(Media.class.getName() + " title can not be null");
		if (title.isEmpty())
			throw new IllegalArgumentException(Media.class.getName() + " title can not be empty");
		if (latitude == null)
			throw new IllegalArgumentException(Media.class.getName() + " latitude can not be null");
		if (longitude == null)
			throw new IllegalArgumentException(Media.class.getName() + " longitude can not be null");
		this.id = id;
		this.type = type;
		this.size = size;
		this.duration = duration;
		this.user = user;
		this.created = created;
		this.edited = edited;
		this.title = title;
		//[-90, 90]
		if (latitude.compareTo(MIN_LATITUDE) < 0) //comapreTo(): kanei afairesh 
			this.latitude = MIN_LATITUDE;
		else if (latitude.compareTo(MAX_LATITUDE) > 0)
			this.latitude = MAX_LATITUDE;
		else
			this.latitude = latitude;
		//[0, 360)
		if (longitude.compareTo(MIN_LONGITUDE) < 0)//gia p.x -2
			//longitude = 360 + longitude; 
			this.longitude = MAX_LONGITUDE.add(longitude);
		else if (longitude.compareTo(MAX_LONGITUDE) >= 0)//gia >= 360
			//longitude = longitude - 360;
			this.longitude = longitude.subtract(MAX_LONGITUDE);
		else
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

	public void setEdited(final Date edited) {
		this.edited = edited;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public void setLatitude(final BigDecimal latitude) {
		this.latitude = latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public void setLongitude(final BigDecimal longitude) {
		this.longitude = longitude;
	}

	public boolean isPublic() {
		return publik;
	}

	public void setPublic(final boolean publik) {
		this.publik = publik;
	}
	
	@Override
	public boolean equals(final Object object) {
		return (object instanceof Media) ? id.equals(((Media) object).id) : false;
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
