package gr.uoa.di.std08169.mobile.media.share.shared;

import java.math.BigDecimal;
import java.util.Date;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class Media_CustomFieldSerializer extends CustomFieldSerializer<Media> {
	private User_CustomFieldSerializer userSerializer = new User_CustomFieldSerializer();
	
	public static void deserialize(final SerializationStreamReader reader, final Media media) throws SerializationException {
//		throw new SerializationException(Media.class.getName() + " can not be deserialized");
	}
	
	public static Media instantiate(final SerializationStreamReader reader) throws SerializationException {
		//Ta thelei me tin seira o reader gia na mhn paralhfthei kapoio stoixeio
		final String id = reader.readString();
		final String type = reader.readString();
		final long size = reader.readLong();
		final int duration = reader.readInt();
		//Xrhsh allou CustomFieldSerializer gia na diavasei tupo User
		final User user = User_CustomFieldSerializer.instantiate(reader);
		//Diavazei to long pou antistoixei se timestamp kai gurnaei se Date
//		final Date created = new Date(reader.readLong()); TODO
//		reader.readLong();
		final Date created = new Date();
//		reader.readLong();
		final Date edited = new Date();
		final String title = reader.readString();
		final String latitude = reader.readString();
		final String longitude = reader.readString();
		final boolean publik = reader.readBoolean();
		return new Media(id, type, size, duration, user, created, edited, title, new BigDecimal(latitude), new BigDecimal(longitude), publik);
	}
	
	public static void serialize(final SerializationStreamWriter writer, final Media media) throws SerializationException {
		writer.writeString(media.getId());
		writer.writeString(media.getType());
		writer.writeLong(media.getSize());
		writer.writeInt(media.getDuration());
		User_CustomFieldSerializer.serialize(writer, media.getUser());
//		writer.writeLong(media.getCreated().getTime());
//		writer.writeLong(media.getEdited().getTime());
		writer.writeString(media.getTitle());
		writer.writeString(media.getLatitude().toString());
		writer.writeString(media.getLongitude().toString());
		writer.writeBoolean(media.isPublic());
	}

	@Override
	public void deserializeInstance(final SerializationStreamReader reader, final Media media) throws SerializationException {
//		throw new SerializationException(Media.class.getName() + " can not be deserialized");
	}
	
	@Override
	public boolean hasCustomInstantiateInstance() {
		return true;
	}
	
	@Override
	public Media instantiateInstance(final SerializationStreamReader reader) throws SerializationException {
		//Ta thelei me tin seira o reader gia na mhn paralhfthei kapoio stoixeio
		final String id = reader.readString();
		final String type = reader.readString();
		final long size = reader.readLong();
		final int duration = reader.readInt();
		//Xrhsh allou CustomFieldSerializer gia na diavasei tupo User
		final User user = userSerializer.instantiateInstance(reader);
		//Diavazei to long pou antistoixei se timestamp kai gurnaei se Date
		final Date created = new Date(/*reader.readLong() TODO */);
		final Date edited = new Date(/*reader.readLong() TODO */);
		final String title = reader.readString();
		final String latitude = reader.readString();
		final String longitude = reader.readString();
		final boolean publik = reader.readBoolean();
		return new Media(id, type, size, duration, user, created, edited, title, new BigDecimal(latitude), new BigDecimal(longitude), publik);		
	}

	@Override
	public void serializeInstance(final SerializationStreamWriter writer, final Media media) throws SerializationException {
		writer.writeString(media.getId());
		writer.writeString(media.getType());
		writer.writeLong(media.getSize());
		writer.writeInt(media.getDuration());
		userSerializer.serializeInstance(writer, media.getUser());
//		writer.writeLong(media.getCreated().getTime());
//		writer.writeLong(media.getEdited().getTime());
		writer.writeString(media.getTitle());
		writer.writeString(media.getLatitude().toString());
		writer.writeString(media.getLongitude().toString());
		writer.writeBoolean(media.isPublic());
	}
}
