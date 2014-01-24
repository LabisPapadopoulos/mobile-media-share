package gr.uoa.di.std08169.mobile.media.share.shared.user;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class UserResult_CustomFieldSerializer extends CustomFieldSerializer<UserResult> {
	private User_CustomFieldSerializer userSerializer = new User_CustomFieldSerializer();
	
	public static void deserialize(final SerializationStreamReader reader, final UserResult result) throws SerializationException {}
	
	public static UserResult instantiate(final SerializationStreamReader reader) throws SerializationException {
		final int size = reader.readInt();
		final List<User> users = new ArrayList<User>(size);
		for (int i = 0; i < size; i++)
			users.add(User_CustomFieldSerializer.instantiate(reader));
		return new UserResult(users, reader.readInt());
	}
	
	public static void serialize(final SerializationStreamWriter writer, final UserResult result) throws SerializationException {
		//epeidh einai lista apo User, grafoume prwta to megethos kai meta ta stoixeia
		writer.writeInt(result.getUsers().size());
		for (User user : result.getUsers())
			User_CustomFieldSerializer.serialize(writer, user);
		writer.writeInt(result.getTotal());
	}

	@Override
	public void deserializeInstance(final SerializationStreamReader reader, final UserResult result) throws SerializationException {}
	
	@Override
	public boolean hasCustomInstantiateInstance() {
		return true;
	}
	
	@Override
	public UserResult instantiateInstance(final SerializationStreamReader reader) throws SerializationException {
		final int size = reader.readInt();
		final List<User> users = new ArrayList<User>(size);
		for (int i = 0; i < size; i++)
			users.add(userSerializer.instantiateInstance(reader));
		return new UserResult(users, reader.readInt());
	}

	@Override
	public void serializeInstance(final SerializationStreamWriter writer, final UserResult result) throws SerializationException {
		//epeidh einai lista apo User, grafoume prwta to megethos kai meta ta stoixeia
		writer.writeInt(result.getUsers().size());
		for (User user : result.getUsers())
			userSerializer.serializeInstance(writer, user);
		writer.writeInt(result.getTotal());
	}
}
