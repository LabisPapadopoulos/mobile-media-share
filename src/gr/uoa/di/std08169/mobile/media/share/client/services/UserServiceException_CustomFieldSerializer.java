package gr.uoa.di.std08169.mobile.media.share.client.services;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * Perigrafei sto GWT pws tha stelnei kai pws tha lamvanei UserServiceExceptions
 * Oi methodoi tou UserServiceException_CustomFieldSerializer tis kalei mono tou to GWT
 * @author labis
 *
 */
public class UserServiceException_CustomFieldSerializer extends CustomFieldSerializer<UserServiceException> {
	// static methodoi giati xreiazontai alla den borei me to extends na tis proteinei gia ulopoihsh

	/**
	 * Petaei panta SerializationException giati ftiaxnei antikeimena adeia kai meta ta gemizei
	 * (mporei na meinoum misa) alla emeis den to theloume.
	 * Prepei to UserServiceException otan ftiaxtei na einai oloklhro.
	 * @param reader
	 * @param exception
	 * @throws SerializationException
	 */
	public static void deserialize(final SerializationStreamReader reader, final UserServiceException exception) throws SerializationException {}
	
	/**
	 * Ftiaxnei ena gemato antikeimeno (ena UserServiceException)
	 * Metaferontai exceptions
	 * @param reader
	 * @return
	 * @throws SerializationException
	 */
	public static UserServiceException instantiate(final SerializationStreamReader reader) throws SerializationException {
		final String message = reader.readString();
		final Object cause = reader.readObject();
		if (cause instanceof Throwable)
			return new UserServiceException(message, (Throwable) cause);
		throw new SerializationException(UserServiceException.class.getName() + " cause must be a throwable");
	}
	
	/**
	 * Grafei ena antikeimeno ston writer gia na to diavasei enas reader
	 * @param writer
	 */
	public static void serialize(final SerializationStreamWriter writer, final UserServiceException exception) throws SerializationException {
		writer.writeString(exception.getMessage());
		writer.writeObject(exception.getCause());
	}

	@Override
	public void deserializeInstance(final SerializationStreamReader reader, final UserServiceException exception) throws SerializationException {}
	
	/**
	 * Odhgeia pros to GWT gia na xerei na kalei tin instantiate kai oxi tin deserialize (gi' auto uparxei mesa kai to SerializationException)
	 */
	@Override
	public boolean hasCustomInstantiateInstance() {
		return true;
	}
	
	@Override
	public UserServiceException instantiateInstance(final SerializationStreamReader reader) throws SerializationException {
		final String message = reader.readString();
		final Object cause = reader.readObject();
		if (cause instanceof Throwable)
			return new UserServiceException(message, (Throwable) cause);
		throw new SerializationException(UserServiceException.class.getName() + " cause must be a throwable");
	}
	
	@Override
	public void serializeInstance(final SerializationStreamWriter writer, final UserServiceException exception) throws SerializationException {
		writer.writeString(exception.getMessage());
		writer.writeObject(exception.getCause());
	}
}
