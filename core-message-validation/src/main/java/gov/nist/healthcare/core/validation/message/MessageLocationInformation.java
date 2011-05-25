/*
 * NIST Healthcare Core
 * MessageLocationInformation.java Jul 18, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message;

import gov.nist.healthcare.core.Constants.ElementType;

/**
 * This class contains textual information about a message location It also maps
 * to a profile location
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageLocationInformation {

    private ElementType elementType;
    private String elementName;
    private int occurrence;
    private int sequenceNumber;

    public ElementType getElementType() {
        return elementType;
    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public int getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(int occurrence) {
        this.occurrence = occurrence;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

}
