package gr.uoa.di.std08169.mobile.media.share.shared.download;

import java.util.Date;

public class Download {
	private final String token;
	private final String media;
	private final String user;
	private final String client; //Ip tou user
	private final Date timestamp;
	
	public Download(final String token, final String media, final String user, final String client, final Date timestamp) {
		super();
		if (token == null)
			throw new IllegalArgumentException(Download.class.getName() + " token can not be null");
		if (token.isEmpty())
			throw new IllegalArgumentException(Download.class.getName() + " token can not be empty");
		if (media == null)
			throw new IllegalArgumentException(Download.class.getName() + " media can not be null");
		if (media.isEmpty())
			throw new IllegalArgumentException(Download.class.getName() + " media can not be empty");
		if (user == null)
			throw new IllegalArgumentException(Download.class.getName() + " user can not be null");
		if (user.isEmpty())
			throw new IllegalArgumentException(Download.class.getName() + " user can not be empty");
		if (client == null)
			throw new IllegalArgumentException(Download.class.getName() + " client can not be null");
		if (client.isEmpty())
			throw new IllegalArgumentException(Download.class.getName() + " client can not be empty");
		if (timestamp == null)
			throw new IllegalArgumentException(Download.class.getName() + " timestamp can not be null");
		this.token = token;
		this.media = media;
		this.user = user;
		this.client = client;
		this.timestamp = timestamp;
	}
	
	public String getToken() {
		return token;
	}
	
	public String getMedia() {
		return media;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getClient() {
		return client;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	@Override
	public boolean equals(final Object object) {
		return ((object instanceof Download) && token.equals(((Download) object).token)); 
	}
	
	@Override
	public int hashCode() {
		return token.hashCode();
	}
	
	@Override
	public String toString() {
		return token;
	}
}
