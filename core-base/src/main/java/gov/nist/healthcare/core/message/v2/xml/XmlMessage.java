/*
 * NIST Healthcare Core
 * XmlMessage.java Jun 8, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message.v2.xml;

import gov.nist.healthcare.core.MalformedMessageException;
import gov.nist.healthcare.core.Constants.ElementType;
import gov.nist.healthcare.core.Constants.MessageEncoding;
import gov.nist.healthcare.core.datatypes.v2.MSG;
import gov.nist.healthcare.core.message.MessageId;
import gov.nist.healthcare.core.message.MessageLocation;
import gov.nist.healthcare.core.message.Name;
import gov.nist.healthcare.core.message.SegmentGroupInstanceNumber;
import gov.nist.healthcare.core.message.ValuedMessageLocation;
import gov.nist.healthcare.core.message.v2.HL7V2MessageId;
import gov.nist.healthcare.core.message.v2.HL7V2MessageImpl;
import gov.nist.healthcare.core.message.v2.HL7V2Name;
import gov.nist.healthcare.core.profile.Profile;
import gov.nist.healthcare.core.util.XmlBeansUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlCursor.TokenType;

/**
 * This class represents an XML V2 message.
 * 
 * @author Sydney Henrard (NIST)
 */
public class XmlMessage extends HL7V2MessageImpl {

    protected XmlObject messageDoc;
    private File messageFile;

    /**
     * Default Constructor.
     */
    protected XmlMessage() {
    }

    /**
     * Create a Message using a File object using the platform's default
     * charset.
     * 
     * @param messageFile
     * @throws MalformedMessageException
     */
    public XmlMessage(File messageFile) throws MalformedMessageException {
        try {
            messageDoc = XmlObject.Factory.parse(messageFile,
                    (new XmlOptions()).setLoadLineNumbers());
            this.messageFile = messageFile;
            parseMessage();
        } catch (Exception e) {
            throw new MalformedMessageException(e.getMessage());
        }
    }

    /**
     * Create a Message using a File object using a specific encoding.
     * 
     * @param messageFile
     * @param encoding
     * @throws MalformedMessageException
     */
    public XmlMessage(File messageFile, String encoding)
            throws MalformedMessageException {
        try {
            StringBuffer sb = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(messageFile), encoding));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String encodedString = new String(sb.toString().getBytes(encoding),
                    encoding);
            messageDoc = XmlObject.Factory.parse(encodedString,
                    (new XmlOptions()).setLoadLineNumbers());
            this.messageFile = messageFile;
            parseMessage();
        } catch (Exception e) {
            throw new MalformedMessageException(e.getMessage());
        }
    }

    /**
     * Create a Message using a String object.
     * 
     * @param messageString
     * @throws MalformedMessageException
     */
    public XmlMessage(String messageString) throws MalformedMessageException {
        try {
            messageDoc = XmlObject.Factory.parse(messageString,
                    (new XmlOptions()).setLoadLineNumbers());
            parseMessage();
        } catch (Exception e) {
            throw new MalformedMessageException(e.getMessage());
        }
    }

    /**
     * Create a Message using an XmlObject.
     * 
     * @param xmlObj
     */
    public XmlMessage(XmlObject xmlObj) {
        messageDoc = xmlObj;
        parseMessage();
    }

    public MessageEncoding getEncoding() {
        return MessageEncoding.V2_XML;
    }

    public String getFilename() {
        String filename = "";
        if (messageFile != null) {
            filename = messageFile.getAbsolutePath();
        }
        return filename;
    }

    public String getMessageAsString() {
        return messageDoc.toString();
    }

    public String getValue(MessageLocation location) {
        String value = null;
        value = getValue(location.getXPath());
        return value;
    }

    public boolean hasGroups() {
        boolean hasGroups = false;
        MSG messageType = getMessageType();
        if (messageType != null) {
            String messageStructureId = messageType.getMessageStructure();
            if (messageStructureId != null) {
                XmlCursor xc = messageDoc.newCursor();
                while (xc.hasNextToken() && !hasGroups) {
                    TokenType type = xc.toNextToken();
                    if (type.equals(TokenType.START)) {
                        if (xc.getName().getLocalPart().startsWith(
                                messageStructureId + ".")) {
                            hasGroups = true;
                        }
                    }
                }
                xc.dispose();
            }
        }
        return hasGroups;
    }

    public boolean replaceValue(MessageLocation location, String oldValue,
            String newValue) {
        boolean success = true;
        String xpath = location.getXPath();
        String value = getValue(xpath);
        if (oldValue.equals(value)) {
            XmlObject[] rs = null;
            rs = messageDoc.selectPath(xpath);
            if (rs.length == 1) {
                // Get the value
                XmlCursor cursor = rs[0].newCursor();
                cursor.setTextValue(newValue);
            }
        } else {
            success = false;
        }
        return success;
    }

    public void save(File file) throws IOException {
        messageDoc.save(file, new XmlOptions().setSavePrettyPrint());
    }

    /**
     * Get the document root of the XML representation of the message.
     * 
     * @return the document root of the XML representatin of the message
     */
    public XmlObject getDocument() {
        return messageDoc;
    }

    /**
     * Get the value of the element in the message at the specified location
     * (XPath).
     * 
     * @param location
     *        the location of the element in the message
     * @return the value or an XML fragment; null otherwise
     */
    public String getValue(String location) {
        String value = null;
        XmlObject[] rs = null;
        rs = messageDoc.selectPath(location);
        if (rs.length == 1) {
            value = XmlBeansUtils.getValueFromXmlObject(rs[0]);
        }
        return value;
    }

    /**
     * Get the values in the message at the specified location (XPath).
     * 
     * @param location
     *        the location of the element in the message
     * @return a list of values or XML fragments
     */
    public List<String> getValues(String location) {
        ArrayList<String> values = new ArrayList<String>();
        XmlObject[] rs = null;
        rs = messageDoc.selectPath(location);
        for (XmlObject xmlObj : rs) {
            values.add(XmlBeansUtils.getValueFromXmlObject(xmlObj));
        }
        return values;
    }

    @Override
    public synchronized Object clone() {
        XmlMessage m = null;
        try {
            m = new XmlMessage(messageDoc.copy());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return m;
    }

    // public MessageClass getImplementation() throws MalformedMessageException,
    // UnsupportedMessageClassException {
    // MessageClass messageClass = null;
    // GeneralMessageType type = null;
    // type = this.getMessageType();
    // switch (type) {
    // case UNSUPPORTED:
    // throw new UnsupportedMessageClassException(
    // "The message type is UNSUPPORTED.");
    // case HL7_ACKNOWLEDGMENT:
    // messageClass = new AcknowledgmentV2HL7Xml(this);
    // break;
    // case PATIENT_ADD:
    // messageClass = new PatientAddV2HL7Xml(this);
    // break;
    // case PATIENT_UPDATE:
    // messageClass = new PatientUpdateV2HL7Xml(this);
    // break;
    // case PATIENT_MERGE_DUPLICATE:
    // messageClass = new PatientMergeDuplicateV2HL7Xml(this);
    // break;
    // case GET_IDENTIFIERS_QUERY:
    // messageClass = new GetIdentifiersQueryV2HL7Xml(this);
    // break;
    // case GET_IDENTIFIERS_QUERY_RESPONSE:
    // messageClass = new GetIdentifiersQueryResponseV2HL7Xml(this);
    // break;
    // case PDQ_QUERY:
    // messageClass = new PatientDemographicsQueryV2HL7Xml(this);
    // break;
    // case PDQ_QUERY_RESPONSE:
    // messageClass = new PatientDemographicsQueryResponseV2HL7Xml(this);
    // break;
    // case PDQ_QUERY_CANCEL:
    // messageClass = new PatientDemographicsQueryCancelV2HL7Xml(this);
    // break;
    // }
    // return messageClass;
    // }

    public int getSegmentCount(String segmentName) {
        int segmentCount = 0;
        StringBuffer sb = new StringBuffer();
        sb.append("count(//*:").append(segmentName).append(")");
        XmlObject[] rs = null;
        rs = messageDoc.selectPath(sb.toString());
        if (rs.length == 1) {
            segmentCount = XmlBeansUtils.getCountValueFromXmlObject(rs[0]);
            if (segmentCount == -1) {
                segmentCount = 0;
            }
        }
        // XmlObject[] rs = messageDoc.selectPath(sb.toString());
        // if (rs.length == 1) {
        // XmlCursor cursor = rs[0].newCursor();
        // // Check if it a terminal element
        // if (!cursor.toFirstChild()) {
        // XmlObjectBase n = (XmlObjectBase) rs[0];
        // segmentCount = Integer.parseInt(n.getStringValue());
        // }
        // }
        return segmentCount;
    }

    public int getFieldCount(String segmentName, int segmentInstanceNumber,
            int fieldPosition) {
        int fieldCount = 0;
        StringBuffer sb = new StringBuffer();
        sb.append("count(//*:").append(segmentName).append("[").append(
                segmentInstanceNumber).append("]/*[ends-with(name(), '.").append(
                fieldPosition).append("')])");
        XmlObject[] rs = null;
        rs = messageDoc.selectPath(sb.toString());
        if (rs.length == 1) {
            fieldCount = XmlBeansUtils.getCountValueFromXmlObject(rs[0]);
            if (fieldCount == -1) {
                fieldCount = 0;
            }
        }
        // XmlObject[] rs = messageDoc.selectPath(sb.toString());
        // if (rs.length == 1) {
        // XmlCursor cursor = rs[0].newCursor();
        // // Check if it a terminal element
        // if (!cursor.toFirstChild()) {
        // XmlObjectBase n = (XmlObjectBase) rs[0];
        // fieldCount = Integer.parseInt(n.getStringValue());
        // }
        // }
        return fieldCount;
    }

    public boolean replaceSending(Name name) {
        HL7V2Name sending = (HL7V2Name) name;
        boolean msh3Replaced = false;
        boolean msh4Replaced = false;
        if (sending != null) {
            XmlObject[] rs = messageDoc.selectPath("//*:MSH/*");
            for (int i = 0; i < rs.length; i++) {
                XmlCursor cursor = rs[i].newCursor();
                if ("MSH.3".equals(cursor.getName().getLocalPart())) {
                    cursor.removeXmlContents();
                    cursor.toEndToken();
                    if (sending.getApplicationName().getNamespaceId() != null) {
                        cursor.insertElementWithText("HD.1",
                                "urn:hl7-org:v2xml",
                                sending.getApplicationName().getNamespaceId());
                    }
                    if (sending.getApplicationName().getUniversalId() != null) {
                        cursor.insertElementWithText("HD.2",
                                "urn:hl7-org:v2xml",
                                sending.getApplicationName().getUniversalId());
                    }
                    if (sending.getApplicationName().getUniversalIdType() != null) {
                        cursor.insertElementWithText(
                                "HD.3",
                                "urn:hl7-org:v2xml",
                                sending.getApplicationName().getUniversalIdType());
                    }
                    msh3Replaced = true;
                } else if ("MSH.4".equals(cursor.getName().getLocalPart())) {
                    cursor.removeXmlContents();
                    cursor.toEndToken();
                    if (sending.getFacilityName().getNamespaceId() != null) {
                        cursor.insertElementWithText("HD.1",
                                "urn:hl7-org:v2xml",
                                sending.getFacilityName().getNamespaceId());
                    }
                    if (sending.getFacilityName().getUniversalId() != null) {
                        cursor.insertElementWithText("HD.2",
                                "urn:hl7-org:v2xml",
                                sending.getFacilityName().getUniversalId());
                    }
                    if (sending.getFacilityName().getUniversalIdType() != null) {
                        cursor.insertElementWithText("HD.3",
                                "urn:hl7-org:v2xml",
                                sending.getFacilityName().getUniversalIdType());
                    }
                    msh4Replaced = true;
                }
            }
            if (msh3Replaced && msh4Replaced) {
                this.sending = sending;
            }
        }
        return msh3Replaced && msh4Replaced;
    }

    public boolean replaceReceiving(Name name) {
        HL7V2Name receiving = (HL7V2Name) name;
        boolean msh5Replaced = false;
        boolean msh6Replaced = false;
        if (receiving != null) {
            XmlObject[] rs = messageDoc.selectPath("//*:MSH/*");
            for (int i = 0; i < rs.length; i++) {
                XmlCursor cursor = rs[i].newCursor();
                if ("MSH.5".equals(cursor.getName().getLocalPart())) {
                    cursor.removeXmlContents();
                    cursor.toEndToken();
                    if (receiving.getApplicationName().getNamespaceId() != null) {
                        cursor.insertElementWithText("HD.1",
                                "urn:hl7-org:v2xml",
                                receiving.getApplicationName().getNamespaceId());
                    }
                    if (receiving.getApplicationName().getUniversalId() != null) {
                        cursor.insertElementWithText("HD.2",
                                "urn:hl7-org:v2xml",
                                receiving.getApplicationName().getUniversalId());
                    }
                    if (receiving.getApplicationName().getUniversalIdType() != null) {
                        cursor.insertElementWithText(
                                "HD.3",
                                "urn:hl7-org:v2xml",
                                receiving.getApplicationName().getUniversalIdType());
                    }
                    msh5Replaced = true;
                } else if ("MSH.6".equals(cursor.getName().getLocalPart())) {
                    cursor.removeXmlContents();
                    cursor.toEndToken();
                    if (receiving.getFacilityName().getNamespaceId() != null) {
                        cursor.insertElementWithText("HD.1",
                                "urn:hl7-org:v2xml",
                                receiving.getFacilityName().getNamespaceId());
                    }
                    if (receiving.getFacilityName().getUniversalId() != null) {
                        cursor.insertElementWithText("HD.2",
                                "urn:hl7-org:v2xml",
                                receiving.getFacilityName().getUniversalId());
                    }
                    if (receiving.getFacilityName().getUniversalIdType() != null) {
                        cursor.insertElementWithText(
                                "HD.3",
                                "urn:hl7-org:v2xml",
                                receiving.getFacilityName().getUniversalIdType());
                    }
                    msh6Replaced = true;
                }
            }
            if (msh5Replaced && msh6Replaced) {
                this.receiving = receiving;
            }
        }
        return msh5Replaced && msh6Replaced;
    }

    public boolean replaceDateTimeOfMessage(String dateTimeOfMessage) {
        boolean replaced = false;
        if (dateTimeOfMessage != null) {
            XmlObject[] rs = messageDoc.selectPath("//*:MSH/*");
            for (int i = 0; i < rs.length; i++) {
                XmlCursor cursor = rs[i].newCursor();
                if ("MSH.7".equals(cursor.getName().getLocalPart())) {
                    cursor.removeXmlContents();
                    cursor.toEndToken();
                    cursor.insertElementWithText("TS.1", "urn:hl7-org:v2xml",
                            dateTimeOfMessage);
                    replaced = true;
                }
            }
            if (replaced) {
                creationTime = dateTimeOfMessage;
            }
        }
        return replaced;
    }

    public boolean replaceMessageId(MessageId messageId) {
        HL7V2MessageId newMessageId = (HL7V2MessageId) messageId;
        boolean replaced = false;
        if (newMessageId != null) {
            XmlObject[] rs = messageDoc.selectPath("//*:MSH/*");
            for (int i = 0; i < rs.length; i++) {
                XmlCursor cursor = rs[i].newCursor();
                if ("MSH.10".equals(cursor.getName().getLocalPart())) {
                    cursor.removeXmlContents();
                    cursor.setTextValue(newMessageId.getMessageId());
                    replaced = true;
                }
            }
            if (replaced) {
                this.messageId = newMessageId;
            }
        }
        return replaced;
    }

    public List<ValuedMessageLocation> getLocations(Profile p,
            MessageLocation start) {
        List<ValuedMessageLocation> locations = new ArrayList<ValuedMessageLocation>();
        String xpath = start.getXPath();
        XmlObject[] rs = messageDoc.selectPath(xpath);
        if (rs.length == 1) {
            locations.addAll(getLocationsRecursive(rs[0].newCursor(), start));
        }
        return locations;
    }

    /**
     * The recursive call for getLocations
     * 
     * @param cursor
     * @param location
     * @return a list of valued location
     */
    private List<ValuedMessageLocation> getLocationsRecursive(XmlCursor cursor,
            MessageLocation location) {
        List<ValuedMessageLocation> locations = new ArrayList<ValuedMessageLocation>();
        cursor.push();
        if (cursor.toFirstChild()) {
            locations.addAll(getLocationsRecursive(cursor, getNewLocation(
                    cursor, location)));
            while (cursor.toNextSibling()) {
                locations.addAll(getLocationsRecursive(cursor, getNewLocation(
                        cursor, location)));
            }
        } else {
            ValuedMessageLocation vml = null;
            if (location.getComponentPosition() <= 0) {
                vml = new ValuedMessageLocation(location.getSegmentGroups(),
                        location.getSegmentName(),
                        location.getSegmentInstanceNumber(),
                        location.getFieldPosition(),
                        location.getFieldInstanceNumber(),
                        cursor.getTextValue());
            } else if (location.getSubComponentPosition() <= 0) {
                vml = new ValuedMessageLocation(location.getSegmentGroups(),
                        location.getSegmentName(),
                        location.getSegmentInstanceNumber(),
                        location.getFieldPosition(),
                        location.getFieldInstanceNumber(),
                        location.getComponentPosition(), cursor.getTextValue());
            } else if (location.getSubComponentPosition() > 0) {
                vml = new ValuedMessageLocation(location.getSegmentGroups(),
                        location.getSegmentName(),
                        location.getSegmentInstanceNumber(),
                        location.getFieldPosition(),
                        location.getFieldInstanceNumber(),
                        location.getComponentPosition(),
                        location.getSubComponentPosition(),
                        cursor.getTextValue());
            }
            locations.add(vml);
        }
        cursor.pop();
        return locations;
    }

    /**
     * Get the new location
     * 
     * @param cursor
     * @param location
     * @return a list of valued location
     */
    private MessageLocation getNewLocation(XmlCursor cursor,
            MessageLocation location) {
        MessageLocation newLocation = null;
        QName currentName = cursor.getName();
        String elementName = cursor.getName().getLocalPart();
        ElementType elementType = location.getElementType();
        if (elementType == ElementType.SEGMENT_GROUP) {
            int idx = elementName.indexOf(".");
            if (idx == -1) {
                // Segment
                cursor.push();
                int segmentInstanceNumber = 1;
                while (cursor.toPrevSibling()) {
                    if (cursor.getName().equals(currentName)) {
                        segmentInstanceNumber++;
                    } else {
                        break;
                    }
                }
                cursor.pop();
                newLocation = new MessageLocation(location.getSegmentGroups(),
                        elementName, segmentInstanceNumber);
            } else {
                // Segment Group
                List<SegmentGroupInstanceNumber> segmentGroups = new ArrayList<SegmentGroupInstanceNumber>();
                segmentGroups.addAll(location.getSegmentGroups());
                SegmentGroupInstanceNumber sgin = new SegmentGroupInstanceNumber();
                sgin.setName(elementName.substring(idx + 1));
                cursor.push();
                int segmentGroupInstanceNumber = 1;
                while (cursor.toPrevSibling()) {
                    if (cursor.getName().equals(currentName)) {
                        segmentGroupInstanceNumber++;
                    } else {
                        break;
                    }
                }
                sgin.setInstanceNumber(segmentGroupInstanceNumber);
                cursor.pop();
                segmentGroups.add(sgin);
                newLocation = new MessageLocation(segmentGroups);
            }
        } else if (elementType == ElementType.SEGMENT) {
            // Field
            int fieldPosition = Integer.parseInt(elementName.substring(elementName.indexOf(".") + 1));
            cursor.push();
            int fieldInstanceNumber = 1;
            while (cursor.toPrevSibling()) {
                if (cursor.getName().equals(currentName)) {
                    fieldInstanceNumber++;
                } else {
                    break;
                }
            }
            cursor.pop();
            newLocation = new MessageLocation(location.getSegmentGroups(),
                    location.getSegmentName(),
                    location.getSegmentInstanceNumber(), fieldPosition,
                    fieldInstanceNumber);
        } else if (elementType == ElementType.FIELD) {
            // Component
            int componentPosition = Integer.parseInt(elementName.substring(elementName.indexOf(".") + 1));
            newLocation = new MessageLocation(location.getSegmentGroups(),
                    location.getSegmentName(),
                    location.getSegmentInstanceNumber(),
                    location.getFieldPosition(),
                    location.getFieldInstanceNumber(), componentPosition);
        } else if (elementType == ElementType.COMPONENT) {
            // SubComponent
            int subComponentPosition = Integer.parseInt(elementName.substring(elementName.indexOf(".") + 1));
            newLocation = new MessageLocation(location.getSegmentGroups(),
                    location.getSegmentName(),
                    location.getSegmentInstanceNumber(),
                    location.getFieldPosition(),
                    location.getFieldInstanceNumber(),
                    location.getComponentPosition(), subComponentPosition);
        }
        return newLocation;
    }

}
