package gr.uoa.di.std08169.mobile.media.share.client.services.download;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

//Serializer gia to DownloadServiceException
public class DownloadServiceException_CustomFieldSerializer extends CustomFieldSerializer<DownloadServiceException> {

	public static void deserialize(final SerializationStreamReader reader, final DownloadServiceException exception) throws SerializationException {}
	
	public static DownloadServiceException instantiate(final SerializationStreamReader reader) throws SerializationException {
		final String message = reader.readString();
		//Diavazei panta ena cause apo to DownloadServiceException
		final Object cause = reader.readObject();
		if (cause instanceof Throwable)
			return new DownloadServiceException(message, (Throwable) cause);
		//diavase kati to opoio den htan Throwable
		throw new SerializationException(DownloadServiceException.class.getName() + " cause must be a throwable");
	}
	
	public static void serialize(final SerializationStreamWriter writer, final DownloadServiceException exception) throws SerializationException {
		writer.writeString(exception.getMessage());
		writer.writeObject(exception.getCause());
	}

	@Override
	public void deserializeInstance(final SerializationStreamReader reader, final DownloadServiceException exception) throws SerializationException {}
	
	
	@Override
	public boolean hasCustomInstantiateInstance() {
		return true;
	}
	
	@Override
	public DownloadServiceException instantiateInstance(final SerializationStreamReader reader) throws SerializationException {
		final String message = reader.readString();
		final Object cause = reader.readObject();
		if (cause instanceof Throwable)
			return new DownloadServiceException(message, (Throwable) cause);
		throw new SerializationException(DownloadServiceException.class.getName() + " cause must be a throwable");
	}
	
	@Override
	public void serializeInstance(final SerializationStreamWriter writer, final DownloadServiceException exception) throws SerializationException {
		writer.writeString(exception.getMessage());
		writer.writeObject(exception.getCause());
	}
}
