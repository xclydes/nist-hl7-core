/*
 * NIST Healthcare Core
 * MessageContext.java Oct 4, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.structure.v2.xml;

import gov.nist.healthcare.core.Constants.ElementType;
import org.apache.xmlbeans.XmlObject;

/**
 * This class represents the message context during the message validation
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageContext {

    private XmlObject object;
    private String nameInMessage;
    private ElementType type;
    private String value;
    private boolean primitive;

    /**
     * Constructor
     * 
     * @param object
     */
    public MessageContext(XmlObject object) {
        this.object = object;
    }

    public XmlObject getObject() {
        return object;
    }

    public void setObject(XmlObject object) {
        this.object = object;
    }

    public String getNameInMessage() {
        return nameInMessage;
    }

    public void setNameInMessage(String nameInMessage) {
        this.nameInMessage = nameInMessage;
    }

    public ElementType getType() {
        return type;
    }

    public void setType(ElementType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    public void setPrimitive(boolean primitive) {
        this.primitive = primitive;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((object == null) ? 0 : object.hashCode());
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
        MessageContext other = (MessageContext) obj;
        if (object == null) {
            if (other.object != null) {
                return false;
            }
        } else if (!object.equals(other.object)) {
            return false;
        }
        return true;
    }

}
