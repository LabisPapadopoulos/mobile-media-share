package gr.uoa.di.std08169.mobile.media.share.shared;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class User_CustomFieldSerializer extends CustomFieldSerializer<User> {

	public static void deserialize(final SerializationStreamReader reader, final User user) throws SerializationException {
//		throw new SerializationException(User.class.getName() + " can not be deserialized");
	}
	
	public static User instantiate(final SerializationStreamReader reader) throws SerializationException {
		final String email = reader.readString();
		final String name = reader.readString();
		final String photo = reader.readString();
		return new User(email, name.isEmpty() ? null : name, photo.isEmpty() ? null : photo);
	}
	
	public static void serialize(final SerializationStreamWriter writer, final User user) throws SerializationException {
		writer.writeString(user.getEmail());
		writer.writeString((user.getName() == null) ? "" : user.getName());
		writer.writeObject((user.getPhoto() == null) ? "" : user.getPhoto());
	}

	@Override
	public void deserializeInstance(final SerializationStreamReader reader, final User user) throws SerializationException {
//		throw new SerializationException(User.class.getName() + " can not be deserialized");
	}
	
	@Override
	public boolean hasCustomInstantiateInstance() {
		return true;
	}
	
	@Override
	public User instantiateInstance(final SerializationStreamReader reader) throws SerializationException {
		final String email = reader.readString();
		final String name = reader.readString();
		final String photo = reader.readString();
		return new User(email, name.isEmpty() ? null : name, photo.isEmpty() ? null : photo);
	}

	@Override
	public void serializeInstance(final SerializationStreamWriter writer, final User user) throws SerializationException {
		writer.writeString(user.getEmail());
		writer.writeString((user.getName() == null) ? "" : user.getName());
		writer.writeObject((user.getPhoto() == null) ? "" : user.getPhoto());
	}
}
