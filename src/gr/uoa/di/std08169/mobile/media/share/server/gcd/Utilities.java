package gr.uoa.di.std08169.mobile.media.share.server.gcd;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class Utilities {
	/**
	 * Compile to regular expression pou tairiazei tous tonous, dialitika klp.
	 * \p{...}: propery xarakthra pou tairiazei 
	 * @see http://docs.oracle.com/javase/tutorial/essential/regex/unicode.html#properties
	 * @see http://docs.oracle.com/javase/7/docs/api/java/lang/Character.UnicodeBlock.html#COMBINING_DIACRITICAL_MARKS
	 */
	private static final Pattern COMBINING_DIACRITICAL_MARKS_REGEX = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

	public static String normalize(final String string) {
		//Spaei xarakthres me tonous se xarakthres kai tonous xexwrista (decompose me Canonical decomposition: NFD)
		final String normalized = Normalizer.normalize(string, Normalizer.Form.NFD);
		//diagrafh twn tonwn ston titlo
		final String noCombiningDiacriticalMarks = COMBINING_DIACRITICAL_MARKS_REGEX.matcher(normalized).replaceAll("");
		//Epeidh me tin diplh metatroph ta grammata tairiazoun panta
		return noCombiningDiacriticalMarks.toUpperCase().toLowerCase();
	}
}
//