/*
 * NIST Healthcare Core
 * HL7V2MessageImpl.java Jan 20, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message.v2;

import gov.nist.healthcare.core.datatypes.v2.HD;
import gov.nist.healthcare.core.datatypes.v2.MSG;
import gov.nist.healthcare.core.message.MessageId;
import gov.nist.healthcare.core.message.MessageLocation;
import gov.nist.healthcare.core.message.Name;

/**
 * This class defines an HL7 V2 Message.
 * 
 * @author Sydney Henrard (NIST)
 */
public abstract class HL7V2MessageImpl implements HL7V2Message {

    protected Name receiving;
    protected Name sending;
    protected MessageId messageId;
    protected String creationTime;
    protected MSG messageType;

    /**
     * Set the values from the message for the receiving, the sending
     * application, the message id and the creation time.
     */
    protected void parseMessage() {
        // Receiving
        String msh5_1 = getValue(new MessageLocation("MSH", 1, 5, 1, 1));
        String msh5_2 = getValue(new MessageLocation("MSH", 1, 5, 1, 2));
        String msh5_3 = getValue(new MessageLocation("MSH", 1, 5, 1, 3));
        String msh6_1 = getValue(new MessageLocation("MSH", 1, 6, 1, 1));
        String msh6_2 = getValue(new MessageLocation("MSH", 1, 6, 1, 2));
        String msh6_3 = getValue(new MessageLocation("MSH", 1, 6, 1, 3));

        HD receivingApp = new HD(msh5_1, msh5_2, msh5_3);
        HD receivingFac = new HD(msh6_1, msh6_2, msh6_3);

        receiving = new HL7V2Name(receivingApp, receivingFac);

        // Sending
        String msh3_1 = getValue(new MessageLocation("MSH", 1, 3, 1, 1));
        String msh3_2 = getValue(new MessageLocation("MSH", 1, 3, 1, 2));
        String msh3_3 = getValue(new MessageLocation("MSH", 1, 3, 1, 3));
        String msh4_1 = getValue(new MessageLocation("MSH", 1, 4, 1, 1));
        String msh4_2 = getValue(new MessageLocation("MSH", 1, 4, 1, 2));
        String msh4_3 = getValue(new MessageLocation("MSH", 1, 4, 1, 3));

        HD sendingApp = new HD(msh3_1, msh3_2, msh3_3);
        HD sendingFac = new HD(msh4_1, msh4_2, msh4_3);

        sending = new HL7V2Name(sendingApp, sendingFac);

        // Message Id
        MessageLocation msh_10 = new MessageLocation("MSH", 1, 10, 1);
        messageId = new HL7V2MessageId(getValue(msh_10));

        // Creation Time
        MessageLocation msh7_1 = new MessageLocation("MSH", 1, 7, 1, 1);
        creationTime = getValue(msh7_1);

        // Message Type
        MessageLocation msh9_1 = new MessageLocation("MSH", 1, 9, 1, 1);
        MessageLocation msh9_2 = new MessageLocation("MSH", 1, 9, 1, 2);
        MessageLocation msh9_3 = new MessageLocation("MSH", 1, 9, 1, 3);
        messageType = new MSG(getValue(msh9_1), getValue(msh9_2),
                getValue(msh9_3));

    }

    public Object getMessage() {
        return getMessageAsString();
    }

    public MessageId getMessageID() {
        return messageId;
    }

    public Name getReceiving() {
        return receiving;
    }

    public Name getSending() {
        return sending;
    }

    public String getVersionAsString() {
        String version;
        MessageLocation msh12_1 = new MessageLocation("MSH", 1, 12, 1, 1);
        version = getValue(msh12_1);
        return version;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public MSG getMessageType() {
        return messageType;
    }

    public String getMessageCode() {
        String msgType = null;
        MessageLocation msh9_1 = new MessageLocation("MSH", 1, 9, 1, 1);
        msgType = getValue(msh9_1);
        return msgType;
    }

    public String getMessageEvent() {
        String msgEvent = null;
        MessageLocation msh9_2 = new MessageLocation("MSH", 1, 9, 1, 2);
        msgEvent = getValue(msh9_2);
        return msgEvent;
    }

    public String getMessageStructureID() {
        String msgStructID = null;
        MessageLocation msh9_3 = new MessageLocation("MSH", 1, 9, 1, 3);
        msgStructID = getValue(msh9_3);
        return msgStructID;
    }

    public boolean isPresent(MessageLocation location) {
        return getValue(location) != null;
    }

}
