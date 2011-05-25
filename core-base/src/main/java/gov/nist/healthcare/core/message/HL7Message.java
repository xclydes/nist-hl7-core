/*
 * NIST Healthcare Core
 * HL7Message.java Jan 20, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message;

import java.util.List;

/**
 * This interface defines an HL7 message.
 * 
 * @author Sydney Henrard (NIST)
 */
public interface HL7Message extends Message {

    /**
     * Get the message content as a String.
     * 
     * @return a String representing the message content
     */
    String getMessageAsString();

    /**
     * Get the values of the element in the message at the specified location.
     * 
     * @param location
     *        the location of the element in the message (XPath or EPath)
     * @return the list of values
     */
    // TODO: see the implementation
    List<String> getValues(String location);

    String getCreationTime();

    /**
     * Replace the receiving application and facility. The receiver should be
     * present in the message to be replaced.
     * 
     * @param name
     *        the name of the receiving application and facility
     * @return true if the sending has been replaced; false otherwise
     */
    boolean replaceReceiving(Name name);

    /**
     * Replace the sending application and facility. The sender should be
     * present in the message to be replaced.
     * 
     * @param name
     *        the name of the sending application and facility
     * @return true if the sending has been replaced; false otherwise
     */
    boolean replaceSending(Name name);

    /**
     * Replace the message id. The message id should be present in the message
     * to be replaced.
     * 
     * @param messageId
     *        the message id
     * @return true if the message id has been replaced; false otherwise
     */
    boolean replaceMessageId(MessageId messageId);

    /**
     * Replace the date and time of message. The date and time of messsage
     * should be present in the message to be replaced.
     * 
     * @param dateTimeOfMessage
     *        the date and time of message
     * @return true if the date and time of message has been replaced; false
     *         otherwise
     */
    boolean replaceDateTimeOfMessage(String dateTimeOfMessage);

}
