/*
 * NIST Healthcare Core
 * ProfileContext.java Oct 4, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.structure.v2.xml;

import gov.nist.healthcare.core.Constants.ElementType;
import gov.nist.healthcare.core.profile.ProfileElement;
import java.io.IOException;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * This class represents the profile context during the message validation
 * 
 * @author Sydney Henrard (NIST)
 */
public class ProfileContext {

    private XmlObject object;
    private int min;
    private int max;
    private int current;
    private int messageInstance;
    private ProfileElement profileElement;
    private String nameInMessage;
    private String usage;
    private String table;
    private int maxLength;
    private String datatype;
    private String constant;
    private boolean primitive;

    /**
     * Constructor
     * 
     * @param object
     */
    public ProfileContext(XmlObject object) {
        setObject(object);
    }

    public XmlObject getObject() {
        return object;
    }

    public void setObject(XmlObject object) {
        this.object = object;
        profileElement = new ProfileElement(object);
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getMessageInstance() {
        return messageInstance;
    }

    public void setMessageInstance(int messageInstance) {
        this.messageInstance = messageInstance;
    }

    /**
     * Get the type of the ProfileElement
     * 
     * @return an ElementType constant
     */
    public ElementType getType() {
        return profileElement.getType();
    }

    /**
     * Get the name of the ProfileElement in the message
     * 
     * @param version
     *        the profile version
     * @return the message name
     * @throws IOException
     * @throws XmlException
     */
    public String getNameInMessage(String version) throws XmlException,
            IOException {
        nameInMessage = profileElement.getNameInMessage(version);
        return nameInMessage;
    }

    public String getNameInMessage() {
        return nameInMessage;
    }

    public void setNameInMessage(String nameInMessage) {
        this.nameInMessage = nameInMessage;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getConstant() {
        return constant;
    }

    public void setConstant(String constant) {
        this.constant = constant;
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
        ProfileContext other = (ProfileContext) obj;
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
