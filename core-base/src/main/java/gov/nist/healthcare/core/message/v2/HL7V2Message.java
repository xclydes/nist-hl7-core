/*
 * NIST Healthcare Core
 * HL7V2Message.java Jan 20, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message.v2;

import gov.nist.healthcare.core.message.HL7Message;
import gov.nist.healthcare.core.message.MessageLocation;
import gov.nist.healthcare.core.message.ValuedMessageLocation;
import gov.nist.healthcare.core.profile.Profile;
import java.util.List;

/**
 * This interface defines an HL7V2Message
 * 
 * @author Sydney Henrard (NIST)
 */
public interface HL7V2Message extends HL7Message {

    /**
     * Gets the value at the specified location.
     * 
     * @param location
     *        the location in the message
     * @return the value at the location; null otherwise
     */
    String getValue(MessageLocation location);

    /**
     * Return the number of segment that has the name in parameter
     * 
     * @param segmentName
     *        the name of the segment
     * @return the number of segment
     */
    int getSegmentCount(String segmentName);

    /**
     * Return the number of field in the instance of the segment in parameter
     * 
     * @param segmentName
     *        the name of the segment
     * @param segmentInstanceNumber
     *        the instance number of the segment
     * @param fieldPosition
     *        the position of the field
     * @return the number of field
     */
    int getFieldCount(String segmentName, int segmentInstanceNumber,
            int fieldPosition);

    /**
     * Returns if the message contains segment groups.
     * 
     * @return true if the message contains segment groups; false otherwise
     */
    boolean hasGroups();

    /**
     * Get the message code (MSH.9.1).
     * 
     * @return the message code
     */
    String getMessageCode();

    /**
     * Get the message event (MSH.9.2).
     * 
     * @return the message event
     */
    String getMessageEvent();

    /**
     * Get the message structure id (MSH.9.3).
     * 
     * @return the message structure id
     */
    String getMessageStructureID();

    /**
     * Returns true if the element is present in the message.
     * 
     * @param location
     *        the location in the message
     * @return a boolean that returns the presence of an element in the message
     */
    boolean isPresent(MessageLocation location);

    /**
     * Get all the locations (children) with value in the message from the start
     * position. It contains only the primitive locations.
     * 
     * @param profile
     * @param start
     * @return a list of valued location
     */
    List<ValuedMessageLocation> getLocations(Profile profile,
            MessageLocation start);

}
