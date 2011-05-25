/*
 * NIST Healthcare Core
 * MessageElement.java May 5, 2008
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.datatypes.v2;

/**
 * This class is for representing a value of type MSG (Message Type -- Chapter
 * 2, Data Types).
 * 
 * @author Leonard Gebase (NIST)
 */
public class MSG {
    private String messageCode = null;
    private String triggerEvent = null;
    private String messageStructure = null;

    public MSG() {
    }

    /**
     * Creates a new HD using an old one. Does not copy the old one, just
     * references its components.
     * 
     * @param msgType
     */
    public MSG(MSG msgType) {
        if (msgType != null) {
            this.messageCode = msgType.messageCode;
            this.triggerEvent = msgType.triggerEvent;
            this.messageStructure = msgType.messageStructure;
        }
    }

    /**
     * Constructor.
     * 
     * @param code
     *        the message code
     * @param trigger
     *        the message event trigger
     * @param struct
     *        the message structure id
     */
    public MSG(String code, String trigger, String struct) {
        this.messageCode = code;
        this.triggerEvent = trigger;
        this.messageStructure = struct;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public String getTriggerEvent() {
        return triggerEvent;
    }

    public void setTriggerEvent(String triggerEvent) {
        this.triggerEvent = triggerEvent;
    }

    public String getMessageStructure() {
        return messageStructure;
    }

    public void setMessageStructure(String messageStructure) {
        this.messageStructure = messageStructure;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((messageCode == null) ? 0 : messageCode.hashCode());
        result = prime
                * result
                + ((messageStructure == null) ? 0 : messageStructure.hashCode());
        result = prime * result
                + ((triggerEvent == null) ? 0 : triggerEvent.hashCode());
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
        MSG other = (MSG) obj;
        if (messageCode == null) {
            if (other.messageCode != null) {
                return false;
            }
        } else if (!messageCode.equalsIgnoreCase(other.messageCode)) {
            return false;
        }
        // Modification from the generated eclipse methods
        // No need to test if one of the messageStructure is null
        if (messageStructure != null) {
            if (!messageStructure.equalsIgnoreCase(other.messageStructure)) {
                return false;
            }
        }
        if (triggerEvent == null) {
            if (other.triggerEvent != null) {
                return false;
            }
        } else if (!triggerEvent.equalsIgnoreCase(other.triggerEvent)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String msgCode = messageCode == null || "".equals(messageCode) ? ""
                : messageCode;
        String trigger = triggerEvent == null || "".equals(triggerEvent) ? ""
                : " " + triggerEvent;
        String struct = messageStructure == null || "".equals(messageStructure) ? ""
                : " " + messageStructure;
        return msgCode + trigger + struct;
    }

}
