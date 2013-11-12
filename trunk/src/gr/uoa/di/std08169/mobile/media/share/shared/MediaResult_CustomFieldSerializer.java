package gr.uoa.di.std08169.mobile.media.share.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class MediaResult_CustomFieldSerializer extends CustomFieldSerializer<MediaResult> {
	private Media_CustomFieldSerializer mediaSerializer = new Media_CustomFieldSerializer();
	
	public static void deserialize(final SerializationStreamReader reader, final MediaResult result) throws SerializationException {
		throw new SerializationException(MediaResult.class.getName() + " can not be deserialized");
	}
	
	public static MediaResult instantiate(final SerializationStreamReader reader) throws SerializationException {
		final int size = reader.readInt();
		final List<Media> media = new ArrayList<Media>(size);
		for (int i = 0; i < size; i++)
			media.add(Media_CustomFieldSerializer.instantiate(reader));
		return new MediaResult(media, reader.readInt());
	}
	
	public static void serialize(final SerializationStreamWriter writer, final MediaResult result) throws SerializationException {
		//epeidh einai lista apo Media, grafoume prwta to megethos kai meta ta stoixeia
		writer.writeInt(result.getMedia().size());
		for (Media media : result.getMedia())
			Media_CustomFieldSerializer.serialize(writer, media);
		writer.writeInt(result.getTotal());
	}

	@Override
	public void deserializeInstance(final SerializationStreamReader reader, final MediaResult result) throws SerializationException {
		throw new SerializationException(MediaResult.class.getName() + " can not be deserialized");
	}
	
	@Override
	public boolean hasCustomInstantiateInstance() {
		return true;
	}
	
	@Override
	public MediaResult instantiateInstance(final SerializationStreamReader reader) throws SerializationException {
		final int size = reader.readInt();
		final List<Media> media = new ArrayList<Media>(size);
		for (int i = 0; i < size; i++)
			media.add(mediaSerializer.instantiateInstance(reader));
		return new MediaResult(media, reader.readInt());
	}

	@Override
	public void serializeInstance(final SerializationStreamWriter writer, final MediaResult result) throws SerializationException {
		//epeidh einai lista apo Media, grafoume prwta to megethos kai meta ta stoixeia
		writer.writeInt(result.getMedia().size());
		for (Media media : result.getMedia())
			mediaSerializer.serializeInstance(writer, media);
		writer.writeInt(result.getTotal());
	}
}
