package gr.uoa.di.std08169.mobile.media.share.client.i18n;

import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.i18n.client.LocalizableResource;

//interface gia tis glwsses (ola einai strings). Den ulopoieitai pouthena,
//ftiaxnei mono tou to GWT ulopoihsh.
@LocalizableResource.DefaultLocale("en") //oti akolouthei einai agglika
//ConstantsWithLookup: Me anazhthsh mesw tis getString
//px: getString("foo") -> foo()
public interface MediaTypeConstants extends ConstantsWithLookup {
	@DefaultStringValue("Application")
	public String APPLICATION();

	@DefaultStringValue("Audio")
	public String AUDIO();
	
	@DefaultStringValue("Image")
	public String IMAGE();
	
	@DefaultStringValue("Text")
	public String TEXT();
	
	@DefaultStringValue("Video")
	public String VIDEO();
}
