package gr.uoa.di.std08169.mobile.media.share.shared.user;

/**
 * Auth h klash antiproswpeuei ena xrhsth kai einai sto 
 * shared giati tha phgenoerxetai metaxu java - javascript 
 * @author labis
 *
 */
public class User {
	private final String email;
	private final String name;
	private final String photo;
	
	public User(final String email, final String name, final String photo) {
		if (email == null)
			//exception gia asfaleia se periptwsh null
			throw new IllegalArgumentException(User.class.getName() + " email can not be null");
		if (email.isEmpty())
			throw new IllegalArgumentException(User.class.getName() + " email can not be empty");
		this.email = email;
		this.name = ((name != null) && name.isEmpty()) ? null : name;
		this.photo = ((photo != null) && photo.isEmpty()) ? null : photo;
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	public String getPhoto() {
		return photo;
	}
	
	@Override
	public boolean equals(final Object object) {
		return (object instanceof User) ? email.equals(((User) object).email) : false;
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
