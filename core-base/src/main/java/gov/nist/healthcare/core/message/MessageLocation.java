/*
 * NIST Healthcare Core
 * MessageLocation.java Feb 26, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message;

import gov.nist.healthcare.core.Constants.ElementType;
import gov.nist.healthcare.core.profile.Profile;
import gov.nist.healthcare.message.MessageElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

/**
 * @author Caroline Rosin (NIST)
 */
public class MessageLocation {

    private final List<SegmentGroupInstanceNumber> segmentGroups;
    private final String segmentName;
    private final int segmentInstanceNumber;
    private final int fieldPosition;
    private final int fieldInstanceNumber;
    private final int componentPosition;
    private final int subComponentPosition;

    /**
     * Constructor
     */
    protected MessageLocation() {
        segmentGroups = null;
        segmentName = null;
        segmentInstanceNumber = -1;
        fieldPosition = -1;
        fieldInstanceNumber = -1;
        componentPosition = -1;
        subComponentPosition = -1;
    }

    /**
     * Constructor for a String that represents an er7 message location
     * 
     * @param path
     *        the message location (ex: MSH[1].9[2].3)
     * @throws IllegalArgumentException
     *         if the path is not a valid massage location
     */
    public MessageLocation(String path) {

        this.segmentGroups = null;
        Pattern p = Pattern.compile("" + "" + "([A-Z0-9]{3})\\[(?:(\\d+))\\]"
                + "(?:\\.(\\d+)\\[(?:(\\d+))\\]" + "(?:\\.(\\d+)"
                + "(?:\\.(\\d+))?)?)?");
        Matcher m = p.matcher(path);
        if (m.matches()) {
            String sName = m.group(1);
            String sInstanceNumber = m.group(2);
            String fPosition = m.group(3);
            String fInstanceNumber = m.group(4);
            String cPosition = m.group(5);
            String scPosition = m.group(6);
            this.segmentName = sName;
            if (sInstanceNumber != null) {
                this.segmentInstanceNumber = Integer.parseInt(sInstanceNumber);
            } else {
                this.segmentInstanceNumber = 1;
            }
            if (fPosition != null) {
                this.fieldPosition = Integer.parseInt(fPosition);
                if (fInstanceNumber != null) {
                    this.fieldInstanceNumber = Integer.parseInt(fInstanceNumber);
                } else {
                    this.fieldInstanceNumber = 1;
                }
                if (cPosition != null) {
                    this.componentPosition = Integer.parseInt(cPosition);
                    if (scPosition != null) {
                        this.subComponentPosition = Integer.parseInt(scPosition);
                    } else {
                        this.subComponentPosition = 0;
                    }
                } else {
                    this.componentPosition = 0;
                    this.subComponentPosition = 0;
                }
            } else {
                this.fieldPosition = 0;
                this.fieldInstanceNumber = 0;
                this.componentPosition = 0;
                this.subComponentPosition = 0;
            }
        } else {
            throw new IllegalArgumentException(path
                    + "is not a valid message location");
        }
    }

    /**
     * Constructor for a SubComponent with segment groups.
     * 
     * @param segmentGroups
     * @param segmentName
     * @param segmentInstanceNumber
     * @param fieldPosition
     * @param fieldInstanceNumber
     * @param componentPosition
     * @param subComponentPosition
     */
    public MessageLocation(List<SegmentGroupInstanceNumber> segmentGroups,
            String segmentName, int segmentInstanceNumber, int fieldPosition,
            int fieldInstanceNumber, int componentPosition,
            int subComponentPosition) {
        this.segmentGroups = segmentGroups;
        if (segmentName.length() != 3) {
            throw new IllegalArgumentException(
                    "Segment name must be 3 characters");
        }
        if (segmentInstanceNumber <= 0) {
            throw new IllegalArgumentException(
                    "Segment instance number must be positive");
        }
        if (fieldPosition <= 0) {
            throw new IllegalArgumentException(
                    "Field position must be positive");
        }
        if (fieldInstanceNumber <= 0) {
            throw new IllegalArgumentException(
                    "Field instance number must be positive");
        }
        if (componentPosition <= 0) {
            throw new IllegalArgumentException(
                    "Component position must be positive");
        }
        if (subComponentPosition <= 0) {
            throw new IllegalArgumentException(
                    "SubComponent position must be positive");
        }
        this.segmentName = segmentName;
        this.segmentInstanceNumber = segmentInstanceNumber;
        this.fieldPosition = fieldPosition;
        this.fieldInstanceNumber = fieldInstanceNumber;
        this.componentPosition = componentPosition;
        this.subComponentPosition = subComponentPosition;
    }

    /**
     * Constructor for a Component with segment groups.
     * 
     * @param segmentGroups
     * @param segmentName
     * @param segmentInstanceNumber
     * @param fieldPosition
     * @param fieldInstanceNumber
     * @param componentPosition
     */
    public MessageLocation(List<SegmentGroupInstanceNumber> segmentGroups,
            String segmentName, int segmentInstanceNumber, int fieldPosition,
            int fieldInstanceNumber, int componentPosition) {
        this.segmentGroups = segmentGroups;
        if (segmentName.length() != 3) {
            throw new IllegalArgumentException(
                    "Segment name must be 3 characters");
        }
        if (segmentInstanceNumber <= 0) {
            throw new IllegalArgumentException(
                    "Segment instance number must be positive");
        }
        if (fieldPosition <= 0) {
            throw new IllegalArgumentException(
                    "Field position must be positive");
        }
        if (fieldInstanceNumber <= 0) {
            throw new IllegalArgumentException(
                    "Field instance number must be positive");
        }
        if (componentPosition <= 0) {
            throw new IllegalArgumentException(
                    "Component position must be positive");
        }
        this.segmentName = segmentName;
        this.segmentInstanceNumber = segmentInstanceNumber;
        this.fieldPosition = fieldPosition;
        this.fieldInstanceNumber = fieldInstanceNumber;
        this.componentPosition = componentPosition;
        this.subComponentPosition = 0;
    }

    /**
     * Constructor for a Field with segment groups.
     * 
     * @param segmentGroups
     * @param segmentName
     * @param segmentInstanceNumber
     * @param fieldPosition
     * @param fieldInstanceNumber
     */
    public MessageLocation(List<SegmentGroupInstanceNumber> segmentGroups,
            String segmentName, int segmentInstanceNumber, int fieldPosition,
            int fieldInstanceNumber) {
        this.segmentGroups = segmentGroups;
        if (segmentName.length() != 3) {
            throw new IllegalArgumentException(
                    "Segment name must be 3 characters");
        }
        if (segmentInstanceNumber <= 0) {
            throw new IllegalArgumentException(
                    "Segment instance number must be positive");
        }
        if (fieldPosition <= 0) {
            throw new IllegalArgumentException(
                    "Field position must be positive");
        }
        if (fieldInstanceNumber <= 0) {
            throw new IllegalArgumentException(
                    "Field instance number must be positive");
        }

        this.segmentName = segmentName;
        this.segmentInstanceNumber = segmentInstanceNumber;
        this.fieldPosition = fieldPosition;
        this.fieldInstanceNumber = fieldInstanceNumber;
        this.componentPosition = 0;
        this.subComponentPosition = 0;
    }

    /**
     * Constructor for a Segment with segment groups.
     * 
     * @param segmentGroups
     * @param segmentName
     * @param segmentInstanceNumber
     */
    public MessageLocation(List<SegmentGroupInstanceNumber> segmentGroups,
            String segmentName, int segmentInstanceNumber) {
        this.segmentGroups = segmentGroups;
        if (segmentName.length() != 3) {
            throw new IllegalArgumentException(
                    "Segment name must be 3 characters");
        }
        if (segmentInstanceNumber <= 0) {
            throw new IllegalArgumentException(
                    "Segment instance number must be positive");
        }
        this.segmentName = segmentName;
        this.segmentInstanceNumber = segmentInstanceNumber;
        this.fieldPosition = 0;
        this.fieldInstanceNumber = 0;
        this.componentPosition = 0;
        this.subComponentPosition = 0;
    }

    /**
     * Constructor for a SegmentGroup.
     * 
     * @param segmentGroups
     */
    public MessageLocation(List<SegmentGroupInstanceNumber> segmentGroups) {
        this.segmentGroups = segmentGroups;
        this.segmentName = null;
        this.segmentInstanceNumber = 0;
        this.fieldPosition = 0;
        this.fieldInstanceNumber = 0;
        this.componentPosition = 0;
        this.subComponentPosition = 0;
    }

    /**
     * Constructor for a SubComponent.
     * 
     * @param segmentName
     * @param segmentInstanceNumber
     * @param fieldPosition
     * @param fieldInstanceNumber
     * @param componentPosition
     * @param subComponentPosition
     */
    public MessageLocation(String segmentName, int segmentInstanceNumber,
            int fieldPosition, int fieldInstanceNumber, int componentPosition,
            int subComponentPosition) {
        this(null, segmentName, segmentInstanceNumber, fieldPosition,
                fieldInstanceNumber, componentPosition, subComponentPosition);
    }

    /**
     * Constructor for a Component.
     * 
     * @param segmentName
     * @param segmentInstanceNumber
     * @param fieldPosition
     * @param fieldInstanceNumber
     * @param componentPosition
     */
    public MessageLocation(String segmentName, int segmentInstanceNumber,
            int fieldPosition, int fieldInstanceNumber, int componentPosition) {
        this(null, segmentName, segmentInstanceNumber, fieldPosition,
                fieldInstanceNumber, componentPosition);
    }

    /**
     * Constructor for a Field.
     * 
     * @param segmentName
     * @param segmentInstanceNumber
     * @param fieldPosition
     * @param fieldInstanceNumber
     */
    public MessageLocation(String segmentName, int segmentInstanceNumber,
            int fieldPosition, int fieldInstanceNumber) {
        this(null, segmentName, segmentInstanceNumber, fieldPosition,
                fieldInstanceNumber);
    }

    /**
     * Constructor for a Segment.
     * 
     * @param segmentName
     * @param segmentInstanceNumber
     */
    public MessageLocation(String segmentName, int segmentInstanceNumber) {
        this(null, segmentName, segmentInstanceNumber);
    }

    /**
     * Constructor.
     * 
     * @param messageElement
     */
    public MessageLocation(MessageElement messageElement) {
        this.segmentGroups = new ArrayList<SegmentGroupInstanceNumber>();
        String segmentName = null;
        int segmentInstanceNumber = 0;
        int fieldPosition = 0;
        int fieldInstanceNumber = 0;
        int componentPosition = 0;
        int subComponentPosition = 0;
        // SegGroup
        gov.nist.healthcare.message.SegmentGroup sg = messageElement.getSegmentGroup();
        gov.nist.healthcare.message.Segment s = null;
        while (sg != null) {
            int segmentGroupInstanceNumber = sg.getInstanceNumber() == 0 ? 1
                    : sg.getInstanceNumber();
            SegmentGroupInstanceNumber sgin = new SegmentGroupInstanceNumber();
            sgin.setName(sg.getName());
            sgin.setInstanceNumber(segmentGroupInstanceNumber);
            this.segmentGroups.add(sgin);
            if (sg != null) {
                s = sg.getSegment();
            }
            sg = sg.getSegmentGroup();
        }
        if (s == null) {
            s = messageElement.getSegment();
        }
        if (s != null) {
            segmentName = s.getName();
            segmentInstanceNumber = s.getInstanceNumber() == 0 ? 1
                    : s.getInstanceNumber();
            gov.nist.healthcare.message.Field f = s.getField();
            if (f != null) {
                fieldPosition = f.getPosition();
                fieldInstanceNumber = f.getInstanceNumber() == 0 ? 1
                        : f.getInstanceNumber();
                gov.nist.healthcare.message.Component c = f.getComponent();
                if (c != null) {
                    componentPosition = c.getPosition();
                    gov.nist.healthcare.message.SubComponent sc = c.getSubComponent();
                    if (sc != null) {
                        subComponentPosition = sc.getPosition();
                    }
                }
            }
        }
        this.segmentName = segmentName;
        this.segmentInstanceNumber = segmentInstanceNumber;
        this.fieldPosition = fieldPosition;
        this.fieldInstanceNumber = fieldInstanceNumber;
        this.componentPosition = componentPosition;
        this.subComponentPosition = subComponentPosition;
    }

    public List<SegmentGroupInstanceNumber> getSegmentGroups() {
        return segmentGroups;
    }

    public String getSegmentName() {
        return segmentName;
    }

    public int getSegmentInstanceNumber() {
        return segmentInstanceNumber;
    }

    public int getFieldPosition() {
        return fieldPosition;
    }

    public int getFieldInstanceNumber() {
        return fieldInstanceNumber;
    }

    public int getComponentPosition() {
        return componentPosition;
    }

    public int getSubComponentPosition() {
        return subComponentPosition;
    }

    /**
     * Get an XPath expression from the MessageLocation
     * 
     * @return an XPath expression
     */
    public String getXPath() {
        StringBuffer sb = new StringBuffer("/");
        // Segment Group
        if (segmentGroups != null) {
            for (SegmentGroupInstanceNumber segmentGroup : segmentGroups) {
                sb.append("/*:").append(segmentGroup.getName()).append("[").append(
                        segmentGroup.getInstanceNumber()).append("]");
            }
        }
        // Segment
        if (segmentName != null) {
            sb.append("/*:").append(segmentName).append("[").append(
                    segmentInstanceNumber).append("]");
            // Field
            if (fieldPosition > 0) {
                sb.append("/*[ends-with(name(), '.").append(fieldPosition).append(
                        "')][").append(fieldInstanceNumber).append("]");
                // Component
                if (componentPosition > 0) {
                    sb.append("/*[ends-with(name(), '.").append(
                            componentPosition).append("')]");
                    // SubComponent
                    if (subComponentPosition > 0) {
                        sb.append("/*[ends-with(name(), '.").append(
                                subComponentPosition).append("')]");
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Get an EPath expression from the MessageLocation
     * 
     * @return an EPath expression
     */
    public String getEPath() {
        StringBuffer sb = new StringBuffer("");
        // Segment
        sb.append(segmentName).append("[").append(segmentInstanceNumber).append(
                "]");
        // Field
        if (fieldPosition > 0) {
            sb.append(".").append(fieldPosition).append("[").append(
                    fieldInstanceNumber).append("]");
            // Component
            if (componentPosition > 0) {
                sb.append(".").append(componentPosition);
                // SubComponent
                if (subComponentPosition > 0) {
                    sb.append(".").append(subComponentPosition);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Get the ElementType
     * 
     * @return the ElementType
     */
    public ElementType getElementType() {
        ElementType et = ElementType.SEGMENT_GROUP;
        if (subComponentPosition > 0) {
            et = ElementType.SUBCOMPONENT;
        } else if (componentPosition > 0) {
            et = ElementType.COMPONENT;
        } else if (fieldPosition > 0) {
            et = ElementType.FIELD;
        } else if (segmentName != null) {
            et = ElementType.SEGMENT;
        }
        return et;
    }

    // TODO: See the factory pattern, style getSegment, getField that would
    // return a MessageLocation
    /**
     * Get a message location by providing all the parameters,
     * 
     * @param segmentGroups
     * @param segmentName
     * @param segmentInstanceNumber
     * @param fieldPosition
     * @param fieldInstanceNumber
     * @param componentPosition
     * @param subComponentPosition
     * @return
     */
    public static MessageLocation getMessageLocation(
            List<SegmentGroupInstanceNumber> segmentGroups, String segmentName,
            int segmentInstanceNumber, int fieldPosition,
            int fieldInstanceNumber, int componentPosition,
            int subComponentPosition) {
        MessageLocation location = null;
        if (fieldPosition <= 0) {
            location = new MessageLocation(segmentGroups, segmentName,
                    segmentInstanceNumber);
        } else if (componentPosition <= 0) {
            location = new MessageLocation(segmentGroups, segmentName,
                    segmentInstanceNumber, fieldPosition, fieldInstanceNumber);
        } else if (subComponentPosition <= 0) {
            location = new MessageLocation(segmentGroups, segmentName,
                    segmentInstanceNumber, fieldPosition, fieldInstanceNumber,
                    componentPosition);

        } else {
            location = new MessageLocation(segmentGroups, segmentName,
                    segmentInstanceNumber, fieldPosition, fieldInstanceNumber,
                    componentPosition, subComponentPosition);
        }
        return location;
    }

    @Override
    public String toString() {
        return getEPath();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + componentPosition;
        result = prime * result + fieldInstanceNumber;
        result = prime * result + fieldPosition;
        result = prime * result
                + ((segmentGroups == null) ? 0 : segmentGroups.hashCode());
        result = prime * result + segmentInstanceNumber;
        result = prime * result
                + ((segmentName == null) ? 0 : segmentName.hashCode());
        result = prime * result + subComponentPosition;
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
        MessageLocation other = (MessageLocation) obj;
        if (componentPosition != other.componentPosition) {
            return false;
        }
        if (fieldInstanceNumber != other.fieldInstanceNumber) {
            return false;
        }
        if (fieldPosition != other.fieldPosition) {
            return false;
        }
        if (segmentGroups == null) {
            if (other.segmentGroups != null) {
                return false;
            }
        } else if (!segmentGroups.equals(other.segmentGroups)) {
            return false;
        }
        if (segmentInstanceNumber != other.segmentInstanceNumber) {
            return false;
        }
        if (segmentName == null) {
            if (other.segmentName != null) {
                return false;
            }
        } else if (!segmentName.equals(other.segmentName)) {
            return false;
        }
        if (subComponentPosition != other.subComponentPosition) {
            return false;
        }
        return true;
    }

    /**
     * Check if the location is a possible location from the profile.
     * 
     * @param profile
     *        the profile
     * @return a boolean set to true if the location is possible
     */
    public boolean isExistInProfile(Profile profile) {
        boolean exist = false;
        if (profile != null) {
            XmlObject xmlProfile = profile.getDocument();
            XmlObject start = null;
            XmlObject[] rs = xmlProfile.selectPath("/HL7v2xConformanceProfile/HL7v2xStaticDef");
            if (rs.length == 1) {
                start = rs[0];
            }
            // Segment Group
            boolean segmentGroupFound = false;
            String s = "";
            if (segmentGroups != null && segmentGroups.size() > 0) {
                Iterator<SegmentGroupInstanceNumber> sgIt = segmentGroups.iterator();
                SegmentGroupInstanceNumber sg = null;
                while (sgIt.hasNext()) {
                    sg = sgIt.next();
                    segmentGroupFound = false;
                    rs = start.selectPath("SegGroup[@Name='" + sg.getName()
                            + "']");
                    if (rs.length > 0) {
                        start = checkUsageCardinality(rs,
                                sg.getInstanceNumber());
                        segmentGroupFound = (start != null);
                        s = segmentName;
                    } else {
                        sg = null;
                    }
                }
            } else {
                segmentGroupFound = true;
                s = segmentName;
            }
            if (segmentGroupFound) {
                // Segment
                boolean segmentFound = false;
                rs = start.selectPath("Segment[@Name='" + s + "']");
                if (rs.length > 0) {
                    start = checkUsageCardinality(rs, segmentInstanceNumber);
                    segmentFound = (start != null);
                }
                if (segmentFound) {
                    int f = fieldPosition;
                    if (f > 0) {
                        boolean fieldFound = false;
                        rs = start.selectPath("Field[" + fieldPosition + "]");
                        if (rs.length == 1) {
                            start = checkUsageCardinality(rs,
                                    fieldInstanceNumber);
                            fieldFound = (start != null);
                        }
                        if (fieldFound) {
                            int c = componentPosition;
                            if (c <= 0) {
                                exist = true;
                            } else {
                                boolean componentFound = false;
                                rs = start.selectPath("Component["
                                        + componentPosition + "]");
                                if (rs.length == 1) {
                                    start = rs[0];
                                    componentFound = true;
                                }
                                if (componentFound) {
                                    int sc = subComponentPosition;
                                    if (sc <= 0) {
                                        exist = true;
                                    } else {
                                        rs = start.selectPath("SubComponent["
                                                + subComponentPosition + "]");
                                        if (rs.length == 1) {
                                            exist = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return exist;
    }

    /**
     * Check if the location is a primitive element.
     * 
     * @param profile
     *        the profile
     * @return a boolean set to true if the location is a primitive element.
     */
    public boolean isPrimitive(Profile profile) {
        boolean primitive = false;
        if (profile != null) {
            XmlObject xmlProfile = profile.getDocument();
            XmlObject start = null;
            XmlObject[] rs = xmlProfile.selectPath("/HL7v2xConformanceProfile/HL7v2xStaticDef");
            if (rs.length == 1) {
                start = rs[0];
            }
            // Segment Group
            boolean segmentGroupFound = false;
            String s = "";
            if (segmentGroups != null && segmentGroups.size() > 0) {
                Iterator<SegmentGroupInstanceNumber> sgIt = segmentGroups.iterator();
                SegmentGroupInstanceNumber sg = null;
                while (sgIt.hasNext()) {
                    sg = sgIt.next();
                    segmentGroupFound = false;
                    rs = start.selectPath("SegGroup[@Name='" + sg.getName()
                            + "']");
                    if (rs.length > 0) {
                        start = checkUsageCardinality(rs,
                                sg.getInstanceNumber());
                        segmentGroupFound = (start != null);
                        s = segmentName;
                    } else {
                        sg = null;
                    }
                }
            } else {
                segmentGroupFound = true;
                s = segmentName;
            }
            if (segmentGroupFound) {
                // Segment
                boolean segmentFound = false;
                rs = start.selectPath("Segment[@Name='" + s + "']");
                if (rs.length > 0) {
                    start = checkUsageCardinality(rs, segmentInstanceNumber);
                    segmentFound = (start != null);
                }
                if (segmentFound) {
                    int f = fieldPosition;
                    if (f > 0) {
                        boolean fieldFound = false;
                        rs = start.selectPath("Field[" + fieldPosition + "]");
                        if (rs.length == 1) {
                            start = checkUsageCardinality(rs,
                                    fieldInstanceNumber);
                            fieldFound = (start != null);
                        }
                        if (fieldFound) {
                            int c = componentPosition;
                            if (c <= 0) {
                                rs = start.selectPath("Component");
                                primitive = rs.length == 0;
                            } else {
                                boolean componentFound = false;
                                rs = start.selectPath("Component["
                                        + componentPosition + "]");
                                if (rs.length == 1) {
                                    start = rs[0];
                                    componentFound = true;
                                }
                                if (componentFound) {
                                    int sc = subComponentPosition;
                                    if (sc <= 0) {
                                        rs = start.selectPath("SubComponent");
                                        primitive = rs.length == 0;
                                    } else {
                                        rs = start.selectPath("SubComponent["
                                                + subComponentPosition + "]");
                                        if (rs.length == 1) {
                                            primitive = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return primitive;
    }

    /**
     * Check the usage and cardinality in the profile to the location in the
     * context.
     * 
     * @param rs
     * @param instanceNumberContext
     * @return the location in the profile if everything went fine; null
     *         otherwise
     */
    private XmlObject checkUsageCardinality(XmlObject[] rs,
            int instanceNumberContext) {
        XmlObject xmlObj = null;
        boolean end = false;
        int i = 0;
        while (!end && i < rs.length) {
            XmlCursor cursor = rs[i].newCursor();
            // Check the usage
            String usage = cursor.getAttributeText(QName.valueOf("Usage"));
            if (!"X".equals(usage)) {
                // Check the cardinality
                String sInstanceNumberProfile = cursor.getAttributeText(QName.valueOf("Max"));
                if (!"*".equals(sInstanceNumberProfile)) {
                    int instanceNumberProfile = Integer.parseInt(sInstanceNumberProfile);
                    if (instanceNumberContext <= instanceNumberProfile) {
                        end = true;
                        xmlObj = rs[i];
                    }
                } else {
                    end = true;
                    xmlObj = rs[i];
                }
            }
            i++;
        }
        return xmlObj;
    }

}
