/*
 * NIST Healthcare Core
 * encodingUtils.java Aug 4, 2006
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.encoding.util;

/**
 * This class contains some utils for the encodinf package
 * 
 * @author Caroline Rosin (NIST)
 */
public final class EncodingUtils {

    private EncodingUtils() {
    }

    /**
     * Converts ASCII code inton unicode.
     * 
     * @param tmpUnicode
     *        the ASCII code of a character
     * @return the unicode code
     */
    public static String getUnicode(int tmpUnicode) {
        String unicode = Integer.toHexString(tmpUnicode);
        // put enough '0' to have 4 characters
        int count = 4 - unicode.length();
        for (int i = count; i > 0; i--) {
            unicode = "0".concat(unicode);
        }
        return unicode;
    }
}
