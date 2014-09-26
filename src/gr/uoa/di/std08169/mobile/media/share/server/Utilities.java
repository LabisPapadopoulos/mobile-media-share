package gr.uoa.di.std08169.mobile.media.share.server;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;

public class Utilities {
	/**
	 * Compile to regular expression pou tairiazei tous tonous, dialitika klp.
	 * \p{...}: propery xarakthra pou tairiazei 
	 * @see http://docs.oracle.com/javase/tutorial/essential/regex/unicode.html#properties
	 * @see http://docs.oracle.com/javase/7/docs/api/java/lang/Character.UnicodeBlock.html#COMBINING_DIACRITICAL_MARKS
	 */
	private static final Pattern COMBINING_DIACRITICAL_MARKS_REGEX = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	private static final String DIGEST = "MD5";
	private static final Charset UTF_8 = Charset.forName("UTF-8");

	public static String normalize(final String string) {
		//Spaei xarakthres me tonous se xarakthres kai tonous xexwrista (decompose me Canonical decomposition: NFD)
		final String normalized = Normalizer.normalize(string, Normalizer.Form.NFD);
		//diagrafh twn tonwn ston titlo
		final String noCombiningDiacriticalMarks = COMBINING_DIACRITICAL_MARKS_REGEX.matcher(normalized).replaceAll("");
		//Epeidh me tin diplh metatroph ta grammata tairiazoun panta
		return noCombiningDiacriticalMarks.toUpperCase().toLowerCase();
	}
	
	public static String generateToken(final String email, final Date date) throws NoSuchAlgorithmException {
		/**
		 * @see http://docs.oracle.com/javase/7/docs/api/java/security/MessageDigest.html
		 */
		//Upologismos MD5
		final MessageDigest digest = MessageDigest.getInstance(DIGEST);
		//Desmeush buffer gia na graftei enas long (to  date) kai na diavastei san bytes
		final ByteBuffer registrationBytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
		registrationBytes.putLong(date.getTime());
		//Me update prostithentai dedomena
		digest.update(registrationBytes.array());
		digest.update(email.getBytes(UTF_8));
		//me to digest vgainei to teliko apotelesma kai kodikopoieitai se 16adikh morfh (apo xuma bytes)
		//gia na borei na stalthei ws link
		return Hex.encodeHexString(digest.digest());
	}
	
	public static String generateToken(final String media, final String user, final String client, final Date timestamp) throws NoSuchAlgorithmException {
		final MessageDigest digest = MessageDigest.getInstance(DIGEST);
		digest.update(media.getBytes(UTF_8));
		digest.update(user.getBytes(UTF_8));
		digest.update(client.getBytes(UTF_8));
		final ByteBuffer timestampBytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
		timestampBytes.putLong(timestamp.getTime());
		digest.update(timestampBytes.array());
		return Hex.encodeHexString(digest.digest());
	}
}
