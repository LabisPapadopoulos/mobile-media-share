package gr.uoa.di.std08169.mobile.media.share.client.services.media;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * Perigrafei sto GWT pws tha stelnei kai pws tha lamvanei MediaServiceExceptions
 * Oi methodoi tou MediaServiceException_CustomFieldSerializer tis kalei mono tou to GWT
 * @author labis
 *
 */
public class MediaServiceException_CustomFieldSerializer extends CustomFieldSerializer<MediaServiceException> {
	// static methodoi giati xreiazontai alla den borei me to extends na tis proteinei gia ulopoihsh

	/**
	 * Petaei panta SerializationException giati ftiaxnei antikeimena adeia kai meta ta gemizei
	 * (mporei na meinoum misa) alla emeis den to theloume.
	 * Prepei to MediaServiceException otan ftiaxtei na einai oloklhro.
	 * @param reader
	 * @param exception
	 * @throws SerializationException
	 */
	public static void deserialize(final SerializationStreamReader reader, final MediaServiceException exception) throws SerializationException {}
	
	/**
	 * Ftiaxnei ena gemato antikeimeno (ena MediaServiceException)
	 * Metaferontai exceptions
	 * @param reader
	 * @return
	 * @throws SerializationException
	 */
	public static MediaServiceException instantiate(final SerializationStreamReader reader) throws SerializationException {
		final String message = reader.readString();
		if (reader.readBoolean()) { // uparxei cause
			final Object cause = reader.readObject();
			if (cause instanceof Throwable)
				return new MediaServiceException(message, (Throwable) cause);
			//diavase kati to opoio den htan Throwable
			throw new SerializationException(MediaServiceException.class.getName() + " cause must be a throwable");
		}
		//an den uparxei cause, gurnaei ena MediaServiceException pou exei mono message
		return new MediaServiceException(message);
	}
	
	/**
	 * Grafei ena antikeimeno ston writer gia na to diavasei enas reader
	 * @param writer
	 */
	public static void serialize(final SerializationStreamWriter writer, final MediaServiceException exception) throws SerializationException {
		writer.writeString(exception.getMessage());
		if (exception.getCause() == null)
			writer.writeBoolean(false);
		else {
			writer.writeBoolean(true);
			writer.writeObject(exception.getCause());
		}
	}

	@Override
	public void deserializeInstance(final SerializationStreamReader reader, final MediaServiceException exception) throws SerializationException {}
	
	/**
	 * Odhgeia pros to GWT gia na xerei na kalei tin instantiate kai oxi tin deserialize (gi' auto uparxei mesa kai to SerializationException)
	 */
	@Override
	public boolean hasCustomInstantiateInstance() {
		return true;
	}
	
	@Override
	public MediaServiceException instantiateInstance(final SerializationStreamReader reader) throws SerializationException {
		final String message = reader.readString();
		if (reader.readBoolean()) {
			final Object cause = reader.readObject();
			if (cause instanceof Throwable)
				return new MediaServiceException(message, (Throwable) cause);
			throw new SerializationException(MediaServiceException.class.getName() + " cause must be a throwable");
		}
		return new MediaServiceException(message);
	}
	
	@Override
	public void serializeInstance(final SerializationStreamWriter writer, final MediaServiceException exception) throws SerializationException {
		writer.writeString(exception.getMessage());
		if (exception.getCause() == null)
			writer.writeBoolean(false);
		else {
			writer.writeBoolean(true);
			writer.writeObject(exception.getCause());
		}
	}
}
