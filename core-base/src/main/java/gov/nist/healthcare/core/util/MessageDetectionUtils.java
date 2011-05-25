/*
 * NIST Healthcare Core
 * MessageDetectionUtil.java Jan 25, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.util;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * This class contains method about detecting the type of HL7 message.
 * 
 * @author Sydney Henrard (NIST)
 */
public final class MessageDetectionUtils {

    private MessageDetectionUtils() {
    }

    /**
     * Return true if the message as a String is an ER7 message.
     * 
     * @param message
     *        the message as a String
     * @return true if it is an ER7 message
     */
    public static boolean isER7(String message) {
        String firstSegment = message.substring(0, 3).toUpperCase();
        return firstSegment.startsWith("MSH");
    }

    /**
     * Return true if the message as a String is an XML message.
     * 
     * @param message
     *        the message as a String
     * @return true if it is an XML message
     */
    public static boolean isXML(String message) {
        boolean xml = true;
        try {
            // Check that the message is a well-formed XML message.
            XmlObject.Factory.parse(message);
        } catch (XmlException xmle) {
            xml = false;
        }
        return xml;
    }

}
