/*
 * NIST Healthcare Core
 * Message.java Jun 3, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message;

import gov.nist.healthcare.core.Constants.MessageEncoding;
import java.io.File;
import java.io.IOException;

/**
 * This interface defines a message.
 * 
 * @author Sydney Henrard (NIST)
 */
public interface Message {

    /**
     * Return a unique identifier for this message.
     * 
     * @return a unique identifier; null otherwise
     */
    MessageId getMessageID();

    /**
     * Get the message encoding of the message.
     * 
     * @return the message encoding
     */
    MessageEncoding getEncoding();

    /**
     * Get the filename of the message if it was created from a file.
     * 
     * @return the filename of the message; null otherwise
     */
    String getFilename();

    /**
     * Get the message content.
     * 
     * @return an Object with the content of the message; null otherwise
     */
    Object getMessage();

    /**
     * Save the message content into a File.
     * 
     * @param file
     *        the destination file
     * @throws IOException
     */
    void save(File file) throws IOException;

    /**
     * Get the version as a String.
     * 
     * @return the version; null otherwise
     */
    String getVersionAsString();

    /**
     * Get the sending application and facility names.
     * 
     * @return the sending application; null otherwise
     */
    Name getSending();

    /**
     * Get the receiving application and facility names.
     * 
     * @return the receiving application; null otherwise
     */
    Name getReceiving();

    /**
     * Get the general message type of this Message (i.e. an HL7 Ack, a Patient
     * Add, etc).
     * 
     * @return A GeneralMessageType enum corresponding to the general message
     *         type.
     * @throws MalformedMessageException
     *         Thrown in the event that the message type is unreadable /
     *         invalid.
     */
    // GeneralMessageType getMessageType() throws MalformedMessageException;
    /**
     * Return this message as a specific message class. Instead of being
     * accessed as a general message, the return of this method will allow the
     * caller to access methods specific to the message class.
     * 
     * @return This message in its specific implementation in the type of
     *         message class that it is.
     * @throws MalformedMessageException
     *         Thrown if the message is not valid / correct and therefore cannot
     *         be instantiated as a MessageClass.
     * @throws UnsupportedMessageClassException
     *         Thrown if a MessageClass cannot be created because we do not have
     *         a MessageClass class to provide support for that particular type
     *         of message.
     * @see MessageClass
     */
    // MessageClass getImplementation() throws MalformedMessageException,
    // UnsupportedMessageClassException;
}
