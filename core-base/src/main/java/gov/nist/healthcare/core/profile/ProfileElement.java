/*
 * NIST Healthcare Core
 * ProfileElement.java Jun 27, 2006
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.profile;

import gov.nist.healthcare.core.Constants;
import gov.nist.healthcare.core.Constants.ElementType;
import gov.nist.healthcare.generation.message.SequenceNumbersDocument;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * This class represents an element in the profile It can be created using
 * String objects or an XmlObject (XmlBeans)
 * 
 * @author Sydney Henrard (NIST)
 */
public class ProfileElement {

    protected List<String> segmentGroups;
    protected String segment;
    protected String field;
    protected String component;
    protected String subcomponent;

    protected XmlObject xmlObj;

    protected ElementType type;

    /**
     * Construtor
     * 
     * @param segmentGroups
     *        The segment group as a list of String
     * @param segment
     *        the segment name
     * @param field
     *        the field name
     * @param component
     *        the component name
     * @param subcomponent
     *        the subcomponent name
     */
    public ProfileElement(List<String> segmentGroups, String segment,
            String field, String component, String subcomponent) {
        this.segmentGroups = segmentGroups == null ? new ArrayList<String>()
                : segmentGroups;
        this.segment = segment == null ? "" : segment;
        this.field = field == null ? "" : field;
        this.component = component == null ? "" : component;
        this.subcomponent = subcomponent == null ? "" : subcomponent;
        detectType();
    }

    /**
     * Constructor
     * 
     * @param xmlObj
     *        An XmlObject representing this ProfileElement
     */
    public ProfileElement(XmlObject xmlObj) {
        if (!setXmlObject(xmlObj)) {
            throw new IllegalArgumentException(
                    "The XmlObject does not refer to a correct profile element.");
        }
    }

    /**
     * Set the XmlObject using the Profile. The method will try to match the
     * current state (values) of the object to an XmlObject in the Profile.
     * 
     * @param p
     *        the Profile
     */
    public void setXmlObject(Profile p) {
        String xpath = getXPath();
        XmlObject[] rs = p.getDocument().selectPath(xpath);
        // We have a match
        if (rs.length == 1) {
            xmlObj = rs[0];
        } else {
            throw new IllegalArgumentException(
                    "There is no match for the ProfileElement in this profile: "
                            + xpath);
        }
    }

    /**
     * Set the XmlObject. It will rewrite the internal representation.
     * 
     * @param xmlObj
     *        The XmlObject
     * @return A boolean, true if the internal representation had to be
     *         modified; otherwise false, the XmlObject has not been set in that
     *         case.
     */
    public boolean setXmlObject(XmlObject xmlObj) {
        // boolean found = false;
        List<String> segmentGroupsTmp = null;
        String segTmp = null, fieldTmp = null, componentTmp = null, subComponentTmp = null;
        String name = null;
        XmlCursor cursor = xmlObj.newCursor();

        while (!cursor.isStartdoc()) {
            name = cursor.getName().getLocalPart();
            if (name.equals("SegGroup")) {
                if (segmentGroupsTmp == null) {
                    segmentGroupsTmp = new ArrayList<String>();
                }
                segmentGroupsTmp.add(0,
                        cursor.getAttributeText(QName.valueOf("Name")));
            } else if (name.equals("Segment")) {
                segTmp = cursor.getAttributeText(QName.valueOf("Name"));
            } else if (name.equals("Field")) {
                fieldTmp = cursor.getAttributeText(QName.valueOf("Name"));
            } else if (name.equals("Component")) {
                componentTmp = cursor.getAttributeText(QName.valueOf("Name"));
            } else if (name.equals("SubComponent")) {
                subComponentTmp = cursor.getAttributeText(QName.valueOf("Name"));
            }
            cursor.toParent();
        }
        // rewrite = !(segmentGroupsTmp.equals(segmentGroups)
        // && segTmp.equals(segment) && fieldTmp.equals(field)
        // && componentTmp.equals(component) &&
        // subComponentTmp.equals(subcomponent));

        // if (rewrite) {
        // if (found) {
        this.segmentGroups = segmentGroupsTmp == null ? new ArrayList<String>()
                : segmentGroupsTmp;
        this.segment = segTmp == null ? "" : segTmp;
        this.field = fieldTmp == null ? "" : fieldTmp;
        this.component = componentTmp == null ? "" : componentTmp;
        this.subcomponent = subComponentTmp == null ? "" : subComponentTmp;
        // this.segmentGroups = segmentGroupsTmp;
        // this.segment = segTmp;
        // this.field = fieldTmp;
        // this.component = componentTmp;
        // this.subcomponent = subComponentTmp;
        this.xmlObj = xmlObj;
        detectType();
        // }
        // }
        return !"".equals(segTmp);
    }

    /**
     * Get the XmlObject associated to this ProfileElement
     * 
     * @return A XmlObject
     */
    public XmlObject getXmlObject() {
        return xmlObj;
    }

    public List<String> getSegmentGroups() {
        return segmentGroups;
    }

    public void setSegmentGroups(List<String> segmentGroups) {
        this.segmentGroups = segmentGroups;
        detectType();
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
        detectType();
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
        detectType();
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
        detectType();
    }

    public String getSubcomponent() {
        return subcomponent;
    }

    public void setSubcomponent(String subcomponent) {
        this.subcomponent = subcomponent;
        detectType();
    }

    /**
     * Get the value for an attribute
     * 
     * @param attribute
     * @return the value in String format
     */
    public String getAttributeValue(String attribute) {
        if (xmlObj == null) {
            return null;
        }
        return xmlObj.newCursor().getAttributeText(QName.valueOf(attribute));
    }

    // /**
    // * Get the name of this element
    // *
    // * @return The name
    // */
    // public String getName() {
    // String name = null;
    // if (type == ElementType.SEGMENT_GROUP) {
    // name = segGroup;
    // }
    // if (type == ElementType.SEGMENT) {
    // name = segment;
    // }
    // if (type == ElementType.FIELD) {
    // name = field;
    // }
    // if (type == ElementType.COMPONENT) {
    // name = component;
    // }
    // if (type == ElementType.SUBCOMPONENT) {
    // name = subcomponent;
    // }
    // return name;
    // }
    /**
     * Get An XPath expression of this profile element
     * 
     * @return An XPath expression
     */
    public String getXPath() {
        StringBuffer sb = new StringBuffer();
        sb.append("/");
        if (segmentGroups != null) {
            for (String segmentGroup : segmentGroups) {
                sb.append("/SegGroup[@Name='").append(segmentGroup).append("']");
            }
        }
        if (!segment.equals("")) {
            sb.append("/Segment[@Name='").append(segment).append("']");
        }
        if (field != null && !field.equals("")) {
            sb.append("/Field[@Name='").append(escape(field)).append("']");
        }
        if (component != null && !component.equals("")) {
            sb.append("/Component[@Name='").append(escape(component)).append(
                    "']");
        }
        if (subcomponent != null && !subcomponent.equals("")) {
            sb.append("/SubComponent[@Name='").append(escape(subcomponent)).append(
                    "']");
        }
        return sb.toString();
    }

    /**
     * Escape the special character for an XPath Expression
     * 
     * @param xpath
     * @return The escaped xpath
     */
    private String escape(String xpath) {
        Pattern p = Pattern.compile("'");
        Matcher m = p.matcher(xpath);
        return m.replaceAll("''");
    }

    /**
     * Get the corresponding name in the message.
     * 
     * @param version
     *        the version of the profile
     * @return the name in the message
     * @throws IOException
     * @throws XmlException
     */
    public String getNameInMessage(String version) throws XmlException,
            IOException {
        if (xmlObj == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        XmlCursor cursor = xmlObj.newCursor();
        if (type == ElementType.SEGMENT_GROUP || type == ElementType.SEGMENT) {
            sb.append(cursor.getAttributeText(QName.valueOf("Name")));
        } else if (type == ElementType.FIELD) {
            boolean end = false;
            while (!end && cursor.toParent()) {
                if (cursor.getName().getLocalPart().equals("Segment")) {
                    end = true;
                    sb.append(cursor.getAttributeText(QName.valueOf("Name")));
                    sb.append(".");
                    sb.append(getSequenceNumber(version));
                }
            }
        } else if (type == ElementType.COMPONENT) {
            boolean end = false;
            while (!end && cursor.toParent()) {
                if (cursor.getName().getLocalPart().equals("Field")) {
                    end = true;
                    sb.append(cursor.getAttributeText(QName.valueOf("Datatype")));
                    sb.append(".");
                    sb.append(getSequenceNumber(version));
                }
            }
        } else if (type == ElementType.SUBCOMPONENT) {
            if (isLeaf()) {
                boolean end = false;
                while (!end && cursor.toParent()) {
                    if (cursor.getName().getLocalPart().equals("Component")) {
                        end = true;
                        sb.append(cursor.getAttributeText(QName.valueOf("Datatype")));
                        sb.append(".");
                        sb.append(getSequenceNumber(version));
                    }
                }
            }
        }
        cursor.dispose();
        return sb.toString();
    }

    /**
     * Get An XPath expression of this profile element.
     * 
     * @param aProfile
     * @return An XPath expression
     * @throws IOException
     * @throws XmlException
     */
    public String getXPathInMessage(Profile aProfile) throws XmlException,
            IOException {
        ProfileElement pe = new ProfileElement(new ArrayList<String>(), "", "",
                "", "");
        StringBuffer sb = new StringBuffer();
        String messageStructureId = aProfile.getMessageStructureID();
        sb.append("/*:").append(messageStructureId);
        if (segmentGroups != null) {
            for (String segmentGroup : segmentGroups) {
                sb.append("/*:").append(messageStructureId).append(".").append(
                        segmentGroup);
                pe.getSegmentGroups().add(segmentGroup);
            }
        }
        String version = aProfile.getHl7VersionAsString();
        if (!segment.equals("")) {
            pe.setSegment(segment);
            pe.setXmlObject(aProfile);
            sb.append("/*:").append(pe.getNameInMessage(version));
        }
        if (field != null && !field.equals("")) {
            pe.setField(field);
            pe.setXmlObject(aProfile);
            sb.append("/*:").append(pe.getNameInMessage(version));
        }
        if (component != null && !component.equals("")) {
            pe.setComponent(component);
            pe.setXmlObject(aProfile);
            sb.append("/*:").append(pe.getNameInMessage(version));
        }
        if (subcomponent != null && !subcomponent.equals("")) {
            pe.setSubcomponent(subcomponent);
            pe.setXmlObject(aProfile);
            sb.append("/*:").append(pe.getNameInMessage(version));
        }
        return sb.toString();
    }

    /**
     * Get the type
     * 
     * @return The type
     */
    public ElementType getType() {
        return type;
    }

    /**
     * Detect the type of this element
     */
    private void detectType() {
        if (subcomponent != null && !subcomponent.equals("")) {
            type = ElementType.SUBCOMPONENT;
        } else if (component != null && !component.equals("")) {
            type = ElementType.COMPONENT;
        } else if (field != null && !field.equals("")) {
            type = ElementType.FIELD;
        } else if (segment != null && !segment.equals("")) {
            type = ElementType.SEGMENT;
        } else {
            type = ElementType.SEGMENT_GROUP;
        }
    }

    /**
     * Has an X usage
     * 
     * @return true or false
     */
    public boolean hasXUsage() {
        XmlCursor cursor = xmlObj.newCursor();
        String usage = null;
        while (!cursor.isStartdoc()) {
            usage = cursor.getAttributeText(QName.valueOf("Usage"));
            if (usage != null && usage.equals("X")) {
                return true;
            }
            cursor.toParent();
        }
        return false;
    }

    /**
     * Is it a leaf?
     * 
     * @return true or false
     */
    public boolean isLeaf() {
        boolean leaf = false;
        XmlCursor cursor = xmlObj.newCursor();
        if (type == ElementType.SEGMENT) {
            leaf = !cursor.toChild("Field");
        } else if (type == ElementType.FIELD) {
            leaf = !cursor.toChild("Component");
        } else if (type == ElementType.COMPONENT) {
            leaf = !cursor.toChild("SubComponent");
        } else if (type == ElementType.SUBCOMPONENT) {
            leaf = true;
        }
        return leaf;
    }

    /**
     * Is it the last node
     * 
     * @return true or false
     */
    public boolean isLast() {
        XmlCursor cursor = xmlObj.newCursor();
        return !cursor.toNextSibling(cursor.getName());
    }

    /**
     * Get the sequence number
     * 
     * @param version
     *        the version of the profile
     * @return the sequence number; otherwise -1
     * @throws IOException
     * @throws XmlException
     */
    public int getSequenceNumber(String version) throws XmlException,
            IOException {
        int sn = -1;
        if (xmlObj != null) {
            if (type == ElementType.FIELD) {
                SequenceNumbersDocument sequenceNumbersDoc = SequenceNumbersDocument.Factory.parse(this.getClass().getResourceAsStream(
                        Constants.getSequenceNumbers(version)));
                XmlObject[] rs = sequenceNumbersDoc.selectPath(String.format(
                        "/*:SequenceNumbers/Segment[@Name='%s']/Field[@Name='%s']",
                        segment, escape(field)));
                if (rs.length == 1) {
                    XmlObject xmlField = rs[0];
                    XmlCursor cursor = xmlField.newCursor();
                    sn = Integer.parseInt(cursor.getAttributeText(QName.valueOf("Position")));
                    cursor.dispose();
                }
            }
            if (sn == -1 || type == ElementType.COMPONENT
                    || type == ElementType.SUBCOMPONENT) {
                XmlCursor cursor = xmlObj.newCursor();
                String currentElement = cursor.getName().getLocalPart();
                int snTmp = 0;
                while (cursor.toPrevSibling()) {
                    if (cursor.getName().getLocalPart().equals(currentElement)) {
                        snTmp++;
                    }
                }
                sn = snTmp + 1;
                cursor.dispose();
            }
        }
        return sn;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((component == null) ? 0 : component.hashCode());
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        result = prime * result + ((segment == null) ? 0 : segment.hashCode());
        result = prime * result
                + ((subcomponent == null) ? 0 : subcomponent.hashCode());
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
        ProfileElement other = (ProfileElement) obj;
        if (component == null) {
            if (other.component != null) {
                return false;
            }
        } else if (!component.equals(other.component)) {
            return false;
        }
        if (field == null) {
            if (other.field != null) {
                return false;
            }
        } else if (!field.equals(other.field)) {
            return false;
        }
        if (segment == null) {
            if (other.segment != null) {
                return false;
            }
        } else if (!segment.equals(other.segment)) {
            return false;
        }
        if (subcomponent == null) {
            if (other.subcomponent != null) {
                return false;
            }
        } else if (!subcomponent.equals(other.subcomponent)) {
            return false;
        }
        return true;
    }

}
