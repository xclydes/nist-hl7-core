/*
 * NIST Healthcare Core
 * HL7V3Message.java Jan 21, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message.v3;

import gov.nist.healthcare.core.Constants.MessageEncoding;
import gov.nist.healthcare.core.MalformedMessageException;
import gov.nist.healthcare.core.message.HL7Message;
import gov.nist.healthcare.core.message.MessageId;
import gov.nist.healthcare.core.message.Name;
import gov.nist.healthcare.core.util.XmlBeansUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * This class defines an HL7 V3 Message.
 * 
 * @author mccaffrey (NIST)
 */
public class HL7V3Message implements HL7Message {

    public static final String ITS_VERSION = "XML_1.0";
    // final public static String SENDER_ID_XPATH =
    // "./*/v3:sender/v3:device/v3:id";
    // final public static String RECEIVER_ID_XPATH =
    // "./*/v3:receiver/v3:device/v3:id";
    public static final String SENDER_APPLICATION_NAME_IDENTIFIER_XPATH = "./*/v3:sender/v3:device/v3:id/@root";
    public static final String SENDER_FACILITY_NAME_IDENTIFIER_XPATH = "./*/v3:sender/v3:device/v3:asAgent/v3:representedOrganization/v3:id/@root";
    public static final String RECEIVER_APPLICATION_NAME_IDENTIFIER_XPATH = "./*/v3:receiver/v3:device/v3:id/@root";
    public static final String RECEIVER_FACILITY_NAME_IDENTIFIER_XPATH = "./*/v3:receiver/v3:device/v3:asAgent/v3:representedOrganization/v3:id/@root";
    public static final String INTERACTION_ID_XPATH = "./*/v3:interactionId/@extension";
    public static final String MESSAGE_ID_XPATH = "./*/v3:id";
    public static final String CREATION_TIME_XPATH = "./*/v3:creationTime/@value";
    public static final String V3_NAMESPACE = "urn:hl7-org:v3";
    public static final String V3_NAMESPACE_DECLARATION = "declare namespace v3=\'"
            + V3_NAMESPACE + "\' ";
    // final public static String SSN_OID = "2.16.840.1.113883.4.1";
    // this
    // final public static String DRIVERS_LICENSE_NUMBER_OID =
    // "1.2.840.114350.1.13.99997.2.3412"; // TODO:
    // check
    // this
    private File messageFile;
    protected XmlObject messageDoc;
    private Name sending;
    private Name receiving;
    private String creationTime;
    private MessageId messageId;
    private String interactionId;

    // messageId is an array of Strings. The first element shall be the root.
    // If it exists, then the second element shall be the extension.

    // Note that most of the File stuff is copied from Er7Message.
    // Not yet tested.
    public HL7V3Message() {
    }

    // public CopyOfHL7V3Message(XmlObject doc) throws MalformedMessageException
    // {
    // this.setByDocument(doc);
    // }
    //
    // public CopyOfHL7V3Message(String xmlString)
    // throws MalformedMessageException {
    // this.setByXmlString(xmlString);
    // }
    //
    // /**
    // * Create a Message using a File object.
    // *
    // * @param messageFile
    // * @throws MalformedMessageException
    // */
    // public CopyOfHL7V3Message(File messageFile)
    // throws MalformedMessageException {
    // try {
    // BufferedReader br = new BufferedReader(new FileReader(messageFile));
    // StringBuffer sb = new StringBuffer();
    // String line = null;
    // while ((line = br.readLine()) != null) {
    // sb.append(line);
    // sb.append("\r");
    // }
    // this.messageFile = messageFile;
    // br.close();
    // this.setByXmlString(sb.toString());
    // } catch (Exception e) {
    // throw new MalformedMessageException(e.getMessage());
    // }
    // }
    //
    // public void setByXmlString(String xml) throws MalformedMessageException {
    // try {
    // XmlObject doc = CopyOfHL7V3Message.stringToXmlObject(xml);
    // this.setByDocument(doc);
    // } catch (XmlException ex) {
    // ex.printStackTrace();
    // throw new MalformedMessageException("Not valid XML: XmlException: "
    // + ex.getMessage());
    // }
    //
    // }
    //
    // // All constructors with content and setBy... methods must eventually
    // lead
    // // here.
    // public void setByDocument(XmlObject doc) throws MalformedMessageException
    // {
    // XmlObject[] rs = doc.selectPath("/");
    // XmlCursor cursor = rs[0].newCursor();
    // XmlLineNumber bm = (XmlLineNumber)
    // cursor.getBookmark(XmlLineNumber.class);
    //
    // this.message = doc;
    // // this.parseSenderReceiver();
    // this.parseMessageId();
    // this.parseCreationTime();
    // }

    /**
     * Create a Message using a File object using the platform's default
     * charset.
     * 
     * @param messageFile
     * @throws MalformedMessageException
     */
    public HL7V3Message(File messageFile) throws MalformedMessageException {
        try {
            setByDocument(XmlObject.Factory.parse(messageFile,
                    (new XmlOptions()).setLoadLineNumbers()));
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
    public HL7V3Message(File messageFile, String encoding)
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
            setByDocument(XmlObject.Factory.parse(encodedString,
                    (new XmlOptions()).setLoadLineNumbers()));
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
    public HL7V3Message(String messageString) throws MalformedMessageException {
        try {
            setByDocument(XmlObject.Factory.parse(messageString,
                    (new XmlOptions()).setLoadLineNumbers()));
        } catch (Exception e) {
            throw new MalformedMessageException(e.getMessage());
        }
    }

    /**
     * Create a Message using an XmlObject.
     * 
     * @param xmlObj
     */
    public HL7V3Message(XmlObject xmlObj) {
        setByDocument(xmlObj);
    }

    protected void setByDocument(XmlObject doc) {
        messageDoc = doc;
        parseMessage();
    }

    protected void parseMessage() {
        this.parseSender();
        this.parseReceiver();
        this.parseMessageId();
        this.parseCreationTime();
        this.parseInteractionId();
    }

    protected void parseSender() {
        String sendingApplicationNameIdentifier = null;
        String sendingFacilityNameIdentifier = null;

        sendingApplicationNameIdentifier = this.getValue(formatV3XPath(SENDER_APPLICATION_NAME_IDENTIFIER_XPATH));
        sendingFacilityNameIdentifier = this.getValue(formatV3XPath(SENDER_FACILITY_NAME_IDENTIFIER_XPATH));

        sending = new HL7V3Name(sendingApplicationNameIdentifier,
                sendingFacilityNameIdentifier);
    }

    protected void parseReceiver() {
        String receivingApplicationNameIdentifier = null;
        String receivingFacilityNameIdentifier = null;

        receivingApplicationNameIdentifier = this.getValue(formatV3XPath(RECEIVER_APPLICATION_NAME_IDENTIFIER_XPATH));
        receivingFacilityNameIdentifier = this.getValue(formatV3XPath(RECEIVER_FACILITY_NAME_IDENTIFIER_XPATH));

        receiving = new HL7V3Name(receivingApplicationNameIdentifier,
                receivingFacilityNameIdentifier);
    }

    // I think we get away with this because Name is abstract and therefore we
    // would
    // only ever be passed a HL7V3Name or HL7V2Name. HL7V2Name does not apply,
    // so the only reasonable object passed here is HL7V3Name... 07/23/2009

    // public void setSending(Name sending) {
    // this.sending = (HL7V3Name) sending;
    // }
    //
    //
    // public void setReceiving(Name receiving) {
    // this.receiving = (HL7V3Name) receiving;
    // }

    protected void parseCreationTime() {
        this.creationTime = this.getValue(formatV3XPath(CREATION_TIME_XPATH));
    }

    protected void parseMessageId() {
        XmlObject[] rs = null;
        rs = messageDoc.selectPath(HL7V3Message.V3_NAMESPACE_DECLARATION + " "
                + MESSAGE_ID_XPATH);
        // Document.id has 1...1 cardinality
        if (rs == null || rs.length == 0) {
            return;
        }
        XmlObject id = rs[0];
        String root = null;
        String extension = null;
        XmlObject rootXmlObject = id.selectAttribute("", "root");
        XmlObject extensionXmlObject = id.selectAttribute("", "extension");
        if (rootXmlObject != null) {
            root = rootXmlObject.newCursor().getTextValue();
        }
        if (extensionXmlObject != null) {
            extension = extensionXmlObject.newCursor().getTextValue();
        }
        messageId = new HL7V3MessageId(root, extension);
    }

    protected void parseInteractionId() {
        interactionId = getValue(formatV3XPath(INTERACTION_ID_XPATH));
    }

    // I think we get away with this because Name is abstract and therefore we
    // would
    // only ever be passed a HL7V3Name or HL7V2Name. HL7V2Name does not apply,
    // so the only reasonable object passed here is HL7V3Name... 07/23/2009

    // public void setSending(Name sending) {
    // this.sending = (HL7V3Name) sending;
    // }
    //
    //
    // public void setReceiving(Name receiving) {
    // this.receiving = (HL7V3Name) receiving;
    // }

    public Name getSending() {
        // if (this.sending == null)
        // this.parseSender();
        return sending;

    }

    // I think we get away with this because Name is abstract and therefore we
    // would
    // only ever be passed a HL7V3Name or HL7V2Name. HL7V2Name does not apply,
    // so the only reasonable object passed here is HL7V3Name... 07/23/2009

    // public void setSending(Name sending) {
    // this.sending = (HL7V3Name) sending;
    // }
    //
    //
    // public void setReceiving(Name receiving) {
    // this.receiving = (HL7V3Name) receiving;
    // }

    public Name getReceiving() {
        // if (this.receiving == null)
        // this.parseReceiver();
        return receiving;

    }

    public MessageId getMessageID() {
        return messageId;
        // return this.getValue(INTERACTION_ID_XPATH);
    }

    public String getCreationTime() {
        return creationTime;
    }

    // Returns the first language labelled with the preferenceInd or null if
    // none found.
    // Only will return the first.

    // static public PRPAMT201301UV02LanguageCommunication
    // findPrefered201301Language(
    // List<PRPAMT201301UV02LanguageCommunication> languages) {
    // Iterator<PRPAMT201301UV02LanguageCommunication> it =
    // languages.iterator();
    // while (it.hasNext()) {
    // PRPAMT201301UV02LanguageCommunication language = it.next();
    // if (language.isSetPreferenceInd())
    // return language;
    // }
    // return null;
    // }

    // Returns the first language labelled with the preferenceInd or null if
    // none found.
    // Only will return the first.
    // static public PRPAMT201302UV02LanguageCommunication
    // findPrefered201302Language(
    // List<PRPAMT201302UV02LanguageCommunication> languages) {
    // Iterator<PRPAMT201302UV02LanguageCommunication> it =
    // languages.iterator();
    // while (it.hasNext()) {
    // PRPAMT201302UV02LanguageCommunication language = it.next();
    // if (language.isSetPreferenceInd())
    // return language;
    // }
    // return null;
    // }

    // Given a list of AsOtherIds and an II's root, get the extension of that
    // root...
    // static public String process201301AsOtherIds(
    // List<PRPAMT201301UV02OtherIDs> asOtherIds, String root) {
    // Iterator<PRPAMT201301UV02OtherIDs> it = asOtherIds.iterator();
    // while (it.hasNext()) {
    // PRPAMT201301UV02OtherIDs asOtherId = it.next();
    // List<II> ids = asOtherId.getIdList();
    // Iterator<II> it2 = ids.iterator();
    // while (it2.hasNext()) {
    // II ii = it2.next();
    // if (root.equalsIgnoreCase(ii.getRoot()))
    // return ii.getExtension();
    // }
    // }
    // return null;
    // }

    // static public String process201302AsOtherIds(
    // List<PRPAMT201302UV02OtherIDs> asOtherIds, String root) {
    // Iterator<PRPAMT201302UV02OtherIDs> it = asOtherIds.iterator();
    // while (it.hasNext()) {
    // PRPAMT201302UV02OtherIDs asOtherId = it.next();
    // List<PRPAMT201302UV02OtherIDsId> ids = asOtherId.getIdList();
    // Iterator<PRPAMT201302UV02OtherIDsId> it2 = ids.iterator();
    // while (it2.hasNext()) {
    // PRPAMT201302UV02OtherIDsId ii = it2.next();
    // if (root.equalsIgnoreCase(ii.getRoot()))
    // return ii.getExtension();
    // }
    // }
    // return null;
    // }

    public String getInteractionId() {
        // if (this.interactionId == null) {
        // }
        // return this.interactionId;
        return interactionId;
    }

    public String getMessageAsString() {
        return messageDoc.toString();
        // if (message == null)
        // return "";
        // // return message.xmlText();
        // return message.toString();
    }

    /**
     * Get the document root of the XML representation of the message.
     * 
     * @return the document root of the XML representatin of the message
     */
    public XmlObject getDocument() {
        return messageDoc;
    }

    // Note that there's a fair amount of duplication in the
    // generateMCCIMT000X00UV01Xxxxx methods. Can't think of any
    // way to get around this at the moment. Just make sure that a change in one
    // is reflected in the others if needed.
    // public MCCIMT000100UV01Receiver generateMCCIMT000100UV01Receiver() {
    //
    // MCCIMT000100UV01Receiver receiver =
    // MCCIMT000100UV01Receiver.Factory.newInstance();
    // receiver.setTypeCode(CommunicationFunctionType.RCV);
    //
    // MCCIMT000100UV01Device receiverDevice = receiver.addNewDevice();
    // receiverDevice.setClassCode(EntityClassDevice.DEV);
    // receiverDevice.setDeterminerCode("INSTANCE");
    // receiverDevice.addNewId().setRoot(
    // this.getReceiving().getApplicationName());
    //
    // if (this.getReceiving().getFacilityName() != null
    // && !this.getReceiving().getFacilityName().equals("")) {
    // MCCIMT000100UV01Agent asAgent = receiverDevice.addNewAsAgent();
    // asAgent.setClassCode("AGNT");
    //
    // MCCIMT000100UV01Organization representedOrganization =
    // asAgent.addNewRepresentedOrganization();
    // representedOrganization.setDeterminerCode("INSTANCE");
    // representedOrganization.setClassCode("ORG");
    // representedOrganization.addNewId().setRoot(
    // this.getReceiving().getFacilityName());
    // }
    // return receiver;
    // }
    //
    // public MCCIMT000100UV01Sender generateMCCIMT000100UV01Sender() {
    //
    // MCCIMT000100UV01Sender sender =
    // MCCIMT000100UV01Sender.Factory.newInstance();
    // sender.setTypeCode(CommunicationFunctionType.SND);
    // MCCIMT000100UV01Device senderDevice = sender.addNewDevice();
    // senderDevice.setClassCode(EntityClassDevice.DEV);
    // senderDevice.setDeterminerCode("INSTANCE");
    // senderDevice.addNewId().setRoot(this.getSending().getApplicationName());
    //
    // if (this.getSending().getFacilityName() != null
    // && !this.getSending().getFacilityName().equals("")) {
    // MCCIMT000100UV01Agent asAgent = senderDevice.addNewAsAgent();
    // asAgent.setClassCode("AGNT");
    //
    // MCCIMT000100UV01Organization representedOrganization =
    // asAgent.addNewRepresentedOrganization();
    // representedOrganization.setDeterminerCode("INSTANCE");
    // representedOrganization.setClassCode("ORG");
    // representedOrganization.addNewId().setRoot(
    // this.getSending().getFacilityName());
    // }
    // return sender;
    // }
    //
    // public MCCIMT000200UV01Receiver generateMCCIMT000200UV01Receiver() {
    //
    // MCCIMT000200UV01Receiver receiver =
    // MCCIMT000200UV01Receiver.Factory.newInstance();
    // receiver.setTypeCode(CommunicationFunctionType.RCV);
    //
    // MCCIMT000200UV01Device receiverDevice = receiver.addNewDevice();
    // receiverDevice.setClassCode(EntityClassDevice.DEV);
    // receiverDevice.setDeterminerCode("INSTANCE");
    // receiverDevice.addNewId().setRoot(
    // this.getReceiving().getApplicationName());
    //
    // if (this.getReceiving().getFacilityName() != null
    // && !this.getReceiving().getFacilityName().equals("")) {
    // MCCIMT000200UV01Agent asAgent = receiverDevice.addNewAsAgent();
    // asAgent.setClassCode("AGNT");
    //
    // MCCIMT000200UV01Organization representedOrganization =
    // asAgent.addNewRepresentedOrganization();
    // representedOrganization.setDeterminerCode("INSTANCE");
    // representedOrganization.setClassCode("ORG");
    // representedOrganization.addNewId().setRoot(
    // this.getReceiving().getFacilityName());
    // }
    // return receiver;
    // }
    //
    // public MCCIMT000200UV01Sender generateMCCIMT000200UV01Sender() {
    //
    // MCCIMT000200UV01Sender sender =
    // MCCIMT000200UV01Sender.Factory.newInstance();
    // sender.setTypeCode(CommunicationFunctionType.SND);
    // MCCIMT000200UV01Device senderDevice = sender.addNewDevice();
    // senderDevice.setClassCode(EntityClassDevice.DEV);
    // senderDevice.setDeterminerCode("INSTANCE");
    // senderDevice.addNewId().setRoot(this.getSending().getApplicationName());
    //
    // if (this.getSending().getFacilityName() != null
    // && !this.getSending().getFacilityName().equals("")) {
    // MCCIMT000200UV01Agent asAgent = senderDevice.addNewAsAgent();
    // asAgent.setClassCode("AGNT");
    //
    // MCCIMT000200UV01Organization representedOrganization =
    // asAgent.addNewRepresentedOrganization();
    // representedOrganization.setDeterminerCode("INSTANCE");
    // representedOrganization.setClassCode("ORG");
    // representedOrganization.addNewId().setRoot(
    // this.getSending().getFacilityName());
    // }
    // return sender;
    // }
    //
    // public MCCIMT000300UV01Receiver generateMCCIMT000300UV01Receiver() {
    //
    // MCCIMT000300UV01Receiver receiver =
    // MCCIMT000300UV01Receiver.Factory.newInstance();
    // receiver.setTypeCode(CommunicationFunctionType.RCV);
    //
    // MCCIMT000300UV01Device receiverDevice = receiver.addNewDevice();
    // receiverDevice.setClassCode(EntityClassDevice.DEV);
    // receiverDevice.setDeterminerCode("INSTANCE");
    // receiverDevice.addNewId().setRoot(
    // this.getReceiving().getApplicationName());
    //
    // if (this.getReceiving().getFacilityName() != null
    // && !this.getReceiving().getFacilityName().equals("")) {
    // MCCIMT000300UV01Agent asAgent = receiverDevice.addNewAsAgent();
    // asAgent.setClassCode("AGNT");
    //
    // MCCIMT000300UV01Organization representedOrganization =
    // asAgent.addNewRepresentedOrganization();
    // representedOrganization.setDeterminerCode("INSTANCE");
    // representedOrganization.setClassCode("ORG");
    // representedOrganization.addNewId().setRoot(
    // this.getReceiving().getFacilityName());
    // }
    // return receiver;
    // }
    //
    // public MCCIMT000300UV01Sender generateMCCIMT000300UV01Sender() {
    //
    // MCCIMT000300UV01Sender sender =
    // MCCIMT000300UV01Sender.Factory.newInstance();
    // sender.setTypeCode(CommunicationFunctionType.SND);
    // MCCIMT000300UV01Device senderDevice = sender.addNewDevice();
    // senderDevice.setClassCode(EntityClassDevice.DEV);
    // senderDevice.setDeterminerCode("INSTANCE");
    // senderDevice.addNewId().setRoot(this.getSending().getApplicationName());
    //
    // if (this.getSending().getFacilityName() != null
    // && !this.getSending().getFacilityName().equals("")) {
    // MCCIMT000300UV01Agent asAgent = senderDevice.addNewAsAgent();
    // asAgent.setClassCode("AGNT");
    //
    // MCCIMT000300UV01Organization representedOrganization =
    // asAgent.addNewRepresentedOrganization();
    // representedOrganization.setDeterminerCode("INSTANCE");
    // representedOrganization.setClassCode("ORG");
    // representedOrganization.addNewId().setRoot(
    // this.getSending().getFacilityName());
    // }
    // return sender;
    // }

    public String getVersionAsString() {
        return "3";
    }

    // I think we get away with this because Name is abstract and therefore we
    // would
    // only ever be passed a HL7V3Name or HL7V2Name. HL7V2Name does not apply,
    // so the only reasonable object passed here is HL7V3Name... 07/23/2009

    // public void setSending(Name sending) {
    // this.sending = (HL7V3Name) sending;
    // }
    //
    //
    // public void setReceiving(Name receiving) {
    // this.receiving = (HL7V3Name) receiving;
    // }

    public void save(File file) throws IOException {
        // BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        // bw.write(this.getMessageAsString());
        // bw.close();

        messageDoc.save(file, new XmlOptions().setSavePrettyPrint());
    }

    public String getFilename() {
        String filename = "";
        if (messageFile != null) {
            filename = messageFile.getAbsolutePath();
        }
        return filename;
    }

    /*
     * public String getMessageControlID() { return
     * this.getValue(MESSAGE_ID_XPATH); }
     */

    public MessageEncoding getEncoding() {
        return MessageEncoding.V3;
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
            // } else if (rs.length == 0) {
            // throw new IllegalArgumentException("The XPath " + location
            // + " expression returns no result");
            // } else {
            // throw new IllegalArgumentException("The XPath " + location
            // + " expression returns several nodes");
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

    // TODO: Not sure if this should be here or elsewhere. Keeping it here for
    // now.

    // public static String generateCurrentTimestamp() {
    //
    // Calendar calendar = Calendar.getInstance();
    //
    // StringBuffer sb = new StringBuffer();
    // sb.append(String.valueOf(calendar.get(Calendar.YEAR)));
    // sb.append(CopyOfHL7V3Message.affirmTwoDigits(String.valueOf(calendar.get(Calendar.MONTH)
    // + 1)));
    // sb.append(CopyOfHL7V3Message.affirmTwoDigits(String.valueOf(calendar.get(Calendar.DATE))));
    // sb.append(CopyOfHL7V3Message.affirmTwoDigits(String.valueOf(calendar.get(Calendar.HOUR_OF_DAY))));
    // sb.append(CopyOfHL7V3Message.affirmTwoDigits(String.valueOf(calendar.get(Calendar.MINUTE))));
    // sb.append(CopyOfHL7V3Message.affirmTwoDigits(String.valueOf(calendar.get(Calendar.SECOND))));
    //
    // return sb.toString();
    // }

    // Only send this zero, one or two digit number
    // private static String affirmTwoDigits(String number) {
    //
    // switch (number.length()) {
    // case 0:
    // return "00";
    // case 1:
    // return "0" + number;
    // case 2:
    // return number;
    // }
    //
    // throw new IllegalArgumentException("Your number was too big.");
    // }

    // Returns the first language labelled with the preferenceInd or null if
    // none found.
    // Only will return the first.

    // static public PRPAMT201301UV02LanguageCommunication
    // findPrefered201301Language(
    // List<PRPAMT201301UV02LanguageCommunication> languages) {
    // Iterator<PRPAMT201301UV02LanguageCommunication> it =
    // languages.iterator();
    // while (it.hasNext()) {
    // PRPAMT201301UV02LanguageCommunication language = it.next();
    // if (language.isSetPreferenceInd())
    // return language;
    // }
    // return null;
    // }

    // Returns the first language labelled with the preferenceInd or null if
    // none found.
    // Only will return the first.
    // static public PRPAMT201302UV02LanguageCommunication
    // findPrefered201302Language(
    // List<PRPAMT201302UV02LanguageCommunication> languages) {
    // Iterator<PRPAMT201302UV02LanguageCommunication> it =
    // languages.iterator();
    // while (it.hasNext()) {
    // PRPAMT201302UV02LanguageCommunication language = it.next();
    // if (language.isSetPreferenceInd())
    // return language;
    // }
    // return null;
    // }

    // Given a list of AsOtherIds and an II's root, get the extension of that
    // root...
    // static public String process201301AsOtherIds(
    // List<PRPAMT201301UV02OtherIDs> asOtherIds, String root) {
    // Iterator<PRPAMT201301UV02OtherIDs> it = asOtherIds.iterator();
    // while (it.hasNext()) {
    // PRPAMT201301UV02OtherIDs asOtherId = it.next();
    // List<II> ids = asOtherId.getIdList();
    // Iterator<II> it2 = ids.iterator();
    // while (it2.hasNext()) {
    // II ii = it2.next();
    // if (root.equalsIgnoreCase(ii.getRoot()))
    // return ii.getExtension();
    // }
    // }
    // return null;
    // }

    // static public String process201302AsOtherIds(
    // List<PRPAMT201302UV02OtherIDs> asOtherIds, String root) {
    // Iterator<PRPAMT201302UV02OtherIDs> it = asOtherIds.iterator();
    // while (it.hasNext()) {
    // PRPAMT201302UV02OtherIDs asOtherId = it.next();
    // List<PRPAMT201302UV02OtherIDsId> ids = asOtherId.getIdList();
    // Iterator<PRPAMT201302UV02OtherIDsId> it2 = ids.iterator();
    // while (it2.hasNext()) {
    // PRPAMT201302UV02OtherIDsId ii = it2.next();
    // if (root.equalsIgnoreCase(ii.getRoot()))
    // return ii.getExtension();
    // }
    // }
    // return null;
    // }

    // public GeneralMessageType getMessageType() {
    //
    // String interactionId = this.getInteractionId();
    // if (Constants.V3_ACKNOWLEDGMENT_INTERACTION_ID.equals(interactionId))
    // return GeneralMessageType.HL7_ACKNOWLEDGMENT;
    // if (Constants.V3_PATIENT_ADD_INTERACTION_ID.equals(interactionId))
    // return GeneralMessageType.PATIENT_ADD;
    // if (Constants.V3_PATIENT_UPDATE_INTERACTION_ID.equals(interactionId))
    // return GeneralMessageType.PATIENT_UPDATE;
    // if
    // (Constants.V3_PATIENT_MERGE_DUPLICATES_INTERACTION_ID.equals(interactionId))
    // return GeneralMessageType.PATIENT_MERGE_DUPLICATE;
    // if
    // (Constants.V3_QUERY_BY_IDENTIFIER_INTERACTION_ID.equals(interactionId))
    // return GeneralMessageType.GET_IDENTIFIERS_QUERY;
    // if
    // (Constants.V3_QUERY_BY_IDENTIFIER_RESPONSE_INTERACTION_ID.equals(interactionId))
    // return GeneralMessageType.GET_IDENTIFIERS_QUERY_RESPONSE;
    // if (Constants.V3_PDQ_QUERY_INTERACTION_ID.equals(interactionId))
    // return GeneralMessageType.PDQ_QUERY;
    // if (Constants.V3_PDQ_QUERY_RESPONSE_INTERACTION_ID.equals(interactionId))
    // return GeneralMessageType.PDQ_QUERY_RESPONSE;
    // if
    // (Constants.V3_PDQ_GENERAL_QUERY_ACTIVATE_QUERY_CONTINUE.equals(interactionId))
    // {
    // return GeneralMessageType.PDQ_QUERY_CONTINUATION;
    // }
    //
    // return GeneralMessageType.UNSUPPORTED;
    // }

    // public MessageClass getImplementation()
    // throws UnsupportedMessageClassException {
    //
    // MessageClass message = null;
    // GeneralMessageType type = this.getMessageType();
    // switch (type) {
    // case HL7_ACKNOWLEDGMENT:
    // try {
    // message = new AcknowledgmentV3Impl(this);
    // } catch (MalformedMessageException mme) {
    // mme.printStackTrace();
    // return null;
    // }
    // break;
    // case PATIENT_ADD:
    // try {
    // message = new PatientAddV3Impl(this);
    // } catch (MalformedMessageException mme) {
    // mme.printStackTrace();
    // return null;
    // }
    // break;
    // case PATIENT_UPDATE:
    // try {
    // message = new PatientUpdateV3Impl(this);
    // } catch (MalformedMessageException mme) {
    // mme.printStackTrace();
    // return null;
    // }
    // break;
    //
    // case PATIENT_MERGE_DUPLICATE:
    // try {
    // message = new PatientMergeDuplicateV3Impl(this);
    // } catch (MalformedMessageException mme) {
    // mme.printStackTrace();
    // return null;
    // }
    // break;
    // case GET_IDENTIFIERS_QUERY:
    // try {
    // message = new GetIdentifiersQueryV3Impl(this);
    // } catch (MalformedMessageException mme) {
    // mme.printStackTrace();
    // return null;
    // }
    // break;
    // case GET_IDENTIFIERS_QUERY_RESPONSE:
    // try {
    // message = new GetIdentifiersQueryResponseV3Impl(this);
    // } catch (MalformedMessageException mme) {
    // mme.printStackTrace();
    // return null;
    // }
    // break;
    //
    // case PDQ_QUERY:
    // try {
    // message = new PatientDemographicsQueryV3Impl(this);
    // } catch (MalformedMessageException mme) {
    // mme.printStackTrace();
    // return null;
    // }
    // break;
    // case PDQ_QUERY_RESPONSE:
    // try {
    // message = new PatientDemographicsQueryResponseV3Impl(this);
    // } catch (MalformedMessageException mme) {
    // mme.printStackTrace();
    // return null;
    // }
    // break;
    // case PDQ_QUERY_CONTINUATION:
    // try {
    // message = new GeneralQueryActivateQueryContinueV3Impl(this);
    // } catch (MalformedMessageException mme) {
    // mme.printStackTrace();
    // return null;
    // }
    // break;
    // default:
    // throw new UnsupportedMessageClassException(type
    // + " is unsupported.");
    // }
    // return message;
    // }

    // public static XmlObject stringToXmlObject(String xmlSource)
    // throws XmlException {
    // return XmlObject.Factory.parse(xmlSource,
    // new XmlOptions().setLoadLineNumbers());
    // }
    //
    // public static String getStringValue(XmlObject xmlObject,
    // String xPathExpression) {
    //
    // XmlObject value = null;
    // XmlObject[] valueArray =
    // xmlObject.selectPath(CopyOfHL7V3Message.V3_NAMESPACE_DECLARATION
    // + " " + xPathExpression);
    //
    // if (valueArray == null || valueArray.length == 0)
    // return null;
    // value = valueArray[0];
    // return value.newCursor().getTextValue();
    // }

    // public static String[] getStringMultipleValues(XmlObject xmlObject,
    // String xPathExpression) {
    // ArrayList<String> array = new ArrayList<String>();
    // XmlObject[] valueArray =
    // xmlObject.selectPath(CopyOfHL7V3Message.V3_NAMESPACE_DECLARATION
    // + " " + xPathExpression);
    //
    // if (valueArray == null || valueArray.length == 0)
    // return null;
    //
    // for (int i = 0; i < valueArray.length; i++) {
    // array.add(valueArray[i].newCursor().getTextValue());
    // }
    //
    // return array.toArray(new String[array.size()]);
    //
    // }

    /*
     * public void initCreationTime() {
     * this.setCreationTime(HL7V3Message.generateCurrentTimestamp()); }
     */

    // public void setCreationTime(String creationTime) {
    // this.creationTime = creationTime;
    // }
    // public void setMessageID(String root, String extension) {
    // if (extension != null)
    // this.messageId = new String[2];
    // else
    // this.messageId = new String[1];
    // this.messageId[0] = root;
    // if (extension != null)
    // this.messageId[1] = extension;
    // }
    // public void setMessageID(String[] messageId) {
    // this.messageId = messageId;
    // }
    //
    // public void setMessageID(String messageId) {
    // this.messageId = new String[1];
    // this.messageId[0] = messageId;
    // }
    //
    // public void setMessageID(II messageId) {
    // if (messageId.getExtension() != null)
    // this.messageId = new String[2];
    // else
    // this.messageId = new String[1];
    // if (messageId.getRoot() != null)
    // this.messageId[0] = messageId.getRoot().toLowerCase();
    // if (messageId.getExtension() != null)
    // this.messageId[1] = messageId.getExtension();
    // }
    // public II getMessageIDAsIi() {
    // // String id = this.getMessageID();
    // // CX_II cxIi = CX_II.generateFromV3String(id);
    // CX_II cxIi = CX_II.generate(this.getMessageID());
    // return CX_II.convert(cxIi);
    //
    // }
    //
    /*
     * public static II getIdStringAsIi(String id) { CX_II cxIi =
     * CX_II.generateFromV3String(id); return CX_II.convert(cxIi); }
     */

    // public static String[] convertId(II id) {
    // String root = null;
    // if (id.getRoot() != null)
    // root = id.getRoot().toLowerCase();
    // String extension = null;
    // if (id.getExtension() != null)
    // extension = id.getExtension();
    // String[] converted = null;
    // if (extension != null)
    // converted = new String[2];
    // else
    // converted = new String[1];
    // converted[0] = root;
    // if (extension != null)
    // converted[1] = extension;
    // return converted;
    // }
    //
    // public static II convertId(String[] id) {
    // II ii = II.Factory.newInstance();
    // if (id == null)
    // return ii;
    // if (id.length > 0)
    // ii.setRoot(id[0]);
    // if (id.length > 1)
    // ii.setExtension(id[1]);
    // return ii;
    // }
    public boolean replaceSending(Name name) {
        HL7V3Name sending = (HL7V3Name) name;
        boolean replacedApplication = false;
        boolean replacedFacility = false;
        if (sending != null) {
            if (sending.getApplicationName() != null) {
                XmlObject[] rs = messageDoc.selectPath(formatV3XPath(".//v3:sender"));
                if (rs.length == 1) {
                    XmlCursor cursor = rs[0].newCursor();
                    cursor.removeXmlContents();
                    cursor.toEndToken();
                    cursor.beginElement("device", "urn:hl7-org:v3");
                    cursor.insertAttributeWithValue("classCode", "DEV");
                    cursor.insertAttributeWithValue("determinerCode",
                            "INSTANCE");
                    cursor.beginElement("id", "urn:hl7-org:v3");
                    cursor.insertAttributeWithValue("root",
                            sending.getApplicationName());
                    if (sending.getFacilityName() != null) {
                        cursor.toParent();
                        cursor.toParent();
                        cursor.toEndToken();
                        cursor.beginElement("asAgent", "urn:hl7-org:v3");
                        cursor.insertAttributeWithValue("classCode", "AGNT");
                        cursor.beginElement("representedOrganization",
                                "urn:hl7-org:v3");
                        cursor.insertAttributeWithValue("classCode", "ORG");
                        cursor.insertAttributeWithValue("determinerCode",
                                "INSTANCE");
                        cursor.beginElement("id", "urn:hl7-org:v3");
                        cursor.insertAttributeWithValue("root",
                                sending.getFacilityName());
                        replacedFacility = true;
                    } else {
                        replacedFacility = true;
                    }
                    replacedApplication = true;
                }
            }
            if (replacedApplication && replacedFacility) {
                this.sending = sending;
            }
        }
        return replacedApplication && replacedFacility;
    }

    public boolean replaceReceiving(Name name) {
        HL7V3Name receiving = (HL7V3Name) name;
        boolean replacedApplication = false;
        boolean replacedFacility = false;
        if (receiving != null) {
            if (receiving.getApplicationName() != null) {
                XmlObject[] rs = messageDoc.selectPath(formatV3XPath(".//v3:receiver"));
                if (rs.length == 1) {
                    XmlCursor cursor = rs[0].newCursor();
                    cursor.removeXmlContents();
                    cursor.toEndToken();
                    cursor.beginElement("device", "urn:hl7-org:v3");
                    cursor.insertAttributeWithValue("classCode", "DEV");
                    cursor.insertAttributeWithValue("determinerCode",
                            "INSTANCE");
                    cursor.beginElement("id", "urn:hl7-org:v3");
                    cursor.insertAttributeWithValue("root",
                            receiving.getApplicationName());
                    if (receiving.getFacilityName() != null) {
                        cursor.toParent();
                        cursor.toParent();
                        cursor.toEndToken();
                        cursor.beginElement("asAgent", "urn:hl7-org:v3");
                        cursor.insertAttributeWithValue("classCode", "AGNT");
                        cursor.beginElement("representedOrganization",
                                "urn:hl7-org:v3");
                        cursor.insertAttributeWithValue("classCode", "ORG");
                        cursor.insertAttributeWithValue("determinerCode",
                                "INSTANCE");
                        cursor.beginElement("id", "urn:hl7-org:v3");
                        cursor.insertAttributeWithValue("root",
                                receiving.getFacilityName());
                        replacedFacility = true;
                    } else {
                        replacedFacility = true;
                    }
                    replacedApplication = true;
                }
            }
            if (replacedApplication && replacedFacility) {
                this.receiving = receiving;
            }
        }
        return replacedApplication && replacedFacility;
    }

    public boolean replaceDateTimeOfMessage(String dateTimeOfMessage) {
        boolean replaced = false;
        if (dateTimeOfMessage != null) {
            XmlObject[] rs = messageDoc.selectPath(formatV3XPath(".//v3:creationTime"));
            if (rs.length == 1) {
                XmlCursor cursor = rs[0].newCursor();
                cursor.setAttributeText(new QName("", "value"),
                        dateTimeOfMessage);
                replaced = true;
            }
            if (replaced) {
                creationTime = dateTimeOfMessage;
            }
        }
        return replaced;
    }

    public boolean replaceMessageId(MessageId messageId) {
        HL7V3MessageId newMessageId = (HL7V3MessageId) messageId;
        boolean replaced = false;
        if (newMessageId != null) {
            XmlObject[] rs = messageDoc.selectPath(formatV3XPath("./*/v3:id"));
            if (rs.length == 1) {
                XmlCursor cursor = rs[0].newCursor();
                if (newMessageId.getRoot() != null) {
                    cursor.setAttributeText(new QName("", "root"),
                            newMessageId.getRoot());
                }
                if (newMessageId.getExtension() != null) {
                    cursor.setAttributeText(new QName("", "extension"),
                            newMessageId.getExtension());
                }
                replaced = true;
            }
            if (replaced) {
                this.messageId = newMessageId;
            }
        }
        return replaced;
    }

    public Object getMessage() {
        return getMessageAsString();
    }

    /**
     * Add the declaration of the V3 namespace to the XPath
     * 
     * @param xpath
     * @return the modified xpath
     */
    protected String formatV3XPath(String xpath) {
        return String.format("%s %s", HL7V3Message.V3_NAMESPACE_DECLARATION,
                xpath);
    }

    public List<String> getLocations(String parentXpath) {
        ArrayList<String> locations = new ArrayList<String>();
        XmlObject[] rs = null;
        rs = messageDoc.selectPath(parentXpath);
        if (rs.length > 1) {
            throw new IllegalArgumentException(
                    "Xpath must point to a unique location");
        }
        if (rs.length == 1) {
            XmlCursor cursor = rs[0].newCursor();
            /* Attributes */
            cursor.push();
            if (cursor.toFirstAttribute()) {
                do {
                    StringBuffer b = new StringBuffer();
                    b.append(parentXpath);
                    String attrName = cursor.getName().getLocalPart();
                    b.append("/@");
                    if (cursor.getName().getPrefix() != null
                            && !"".equals(cursor.getName().getPrefix())) {
                        b.append("*:");
                    }
                    b.append(attrName);
                    locations.add(b.toString());
                } while (cursor.toNextAttribute());
            }
            cursor.pop();
            /* Children */
            Map<String, Integer> childCount = new HashMap<String, Integer>();
            if (cursor.toFirstChild()) {
                do {
                    int cnt = 0;
                    StringBuffer b = new StringBuffer();
                    b.append(parentXpath);
                    String elementName = cursor.getName().getLocalPart();
                    if (childCount.containsKey(elementName)) {
                        cnt = childCount.get(elementName);
                    }
                    cnt++;
                    childCount.put(elementName, cnt);
                    b.append("/*:").append(elementName);
                    b.append("[").append(cnt).append("]");
                    cursor.push();
                    /* Add the element if it contains text */
                    cursor.toNextToken();
                    if (cursor.isText() && cursor.getTextValue() != null
                            && !"".equals(cursor.getTextValue())
                            && !cursor.getTextValue().matches("\\s+")) {
                        locations.add(b.toString());
                    }
                    locations.addAll(getLocations(b.toString()));
                    cnt++;
                    cursor.pop();
                } while (cursor.toNextSibling());
            }
        }
        return locations;
    }
}
