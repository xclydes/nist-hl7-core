/*
 * NIST Healthcare Core
 * HL7V2MessageId.java Jan 20, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message.v2;

import gov.nist.healthcare.core.message.MessageId;

/**
 * This class represents the message id for an HL7V2 message.
 * 
 * @author Sydney Henrard (NIST)
 */
public class HL7V2MessageId implements MessageId {

    private String messageId;

    /**
     * Constructor.
     * 
     * @param messageId
     *        the message id
     */
    public HL7V2MessageId(String messageId) {
        this.messageId = messageId;

    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((messageId == null) ? 0 : messageId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HL7V2MessageId other = (HL7V2MessageId) obj;
        if (messageId == null) {
            if (other.messageId != null) {
                return false;
            }
        } else if (!messageId.equals(other.messageId)) {
            return false;
        }
        return true;
    }

}
