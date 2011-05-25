/*
 * NIST Healthcare Core
 * MessageStructureValidationV2Xml.java Aug 18, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.structure.v2.xml;

import gov.nist.healthcare.core.Constants.ElementType;
import gov.nist.healthcare.core.message.v2.xml.XmlMessage;
import gov.nist.healthcare.core.profile.Profile;
import gov.nist.healthcare.core.validation.message.MessageFailure;
import gov.nist.healthcare.core.validation.message.MessageValidationConstants;
import gov.nist.healthcare.core.validation.message.MessageValidationConstants.ValidationState;
import gov.nist.healthcare.core.validation.message.MessageValidationException;
import gov.nist.healthcare.core.validation.message.structure.v2.FiniteStateMachine;
import gov.nist.healthcare.core.validation.message.structure.v2.MessageStructureValidationV2;
import gov.nist.healthcare.core.validation.message.v2.MessageFailureV2;
import gov.nist.healthcare.validation.AssertionTypeV2Constants;
import gov.nist.healthcare.validation.ErrorSeverityConstants;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlLineNumber;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;

/**
 * This class validates the message structure of an Xml V2 message.
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageStructureValidationV2Xml extends
        MessageStructureValidationV2 {

    private List<ProfileContext> alPC;
    private List<MessageContext> alMC;
    private boolean err = false;
    private List<ProfileMessageLink> alTmp;

    @Override
    protected void checkBasic() throws MessageValidationException {
        try {
            StringBuffer sb = new StringBuffer();
            // Check the version
            String pVersion = profile.getHl7VersionAsString();
            String mVersion = message.getVersionAsString();
            if (mVersion == null) {
                mVersion = "";
            }
            if (!pVersion.equals(mVersion)) {
                MessageFailureV2 mf = new MessageFailureV2(
                        message.getEncoding());
                sb.delete(0, sb.length());
                sb.append("The HL7 version '").append(pVersion);
                sb.append("' specified in the profile does not match the HL7 version '");
                sb.append(mVersion).append(
                        "' specified in the message instance.");
                mf.setDescription(sb.toString());
                mf.setFailureSeverity(ErrorSeverityConstants.FATAL);
                mf.setFailureType(AssertionTypeV2Constants.VERSION);
                XmlCursor xmlCursor = getMessageLocation((XmlMessage) message,
                        "$this//*:MSH.12/*:VID.1");
                if (xmlCursor != null) {
                    setMessageLocation(mf, xmlCursor);
                }
                messageFailures.add(mf);
            } else {
                MessageFailureV2 mf = new MessageFailureV2(
                        message.getEncoding());
                mf.setDescription("The version is correct.");
                mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                mf.setFailureType(AssertionTypeV2Constants.CHECKED);
                messageFailures.add(mf);
            }
            boolean messageStructureIdFailures = false;
            // Check the message type
            String pMessageType = profile.getMessageType();
            String mMessageType = message.getMessageCode();
            if (mMessageType == null) {
                mMessageType = "";
            }
            if (!pMessageType.equals(mMessageType)) {
                MessageFailureV2 mf = new MessageFailureV2(
                        message.getEncoding());
                sb.delete(0, sb.length());
                sb.append("The Message Type '").append(pMessageType);
                sb.append("' specified in the profile does not match the Message Type '");
                sb.append(mMessageType).append(
                        "' specified in the message instance.");
                mf.setDescription(sb.toString());
                mf.setFailureSeverity(ErrorSeverityConstants.FATAL);
                mf.setFailureType(AssertionTypeV2Constants.MESSAGE_STRUCTURE_ID);
                XmlCursor xmlCursor = getMessageLocation((XmlMessage) message,
                        "$this//*:MSH.9/*:MSG.1");
                if (xmlCursor != null) {
                    setMessageLocation(mf, xmlCursor);
                }
                messageFailures.add(mf);
                messageStructureIdFailures = true;
            }
            // Check the event ID
            String pEventID = profile.getMessageEvent();
            String mEventID = message.getMessageEvent();
            if (mEventID == null) {
                mEventID = "";
            }
            // 2.3.1 ACK empty no check
            if (!("ACK".equals(pMessageType) && "2.3.1".equals(pVersion) && "".equals(mEventID))) {
                if (!pEventID.equals(mEventID)) {
                    MessageFailureV2 mf = new MessageFailureV2(
                            message.getEncoding());
                    sb.delete(0, sb.length());
                    sb.append("The Event ID '").append(pEventID);
                    sb.append("' specified in the profile does not match the Event ID '");
                    sb.append(mEventID).append(
                            "' specified in the message instance.");
                    mf.setDescription(sb.toString());
                    // It is not a fatal error for ACK
                    if ("ACK".equals(pMessageType)) {
                        mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                    } else {
                        mf.setFailureSeverity(ErrorSeverityConstants.FATAL);
                    }
                    mf.setFailureType(AssertionTypeV2Constants.MESSAGE_STRUCTURE_ID);
                    XmlCursor xmlCursor = getMessageLocation(
                            (XmlMessage) message, "$this//*:MSH.9/*:MSG.2");
                    if (xmlCursor != null) {
                        setMessageLocation(mf, xmlCursor);
                    }
                    messageFailures.add(mf);
                    messageStructureIdFailures = true;
                }
            }
            // Check the message structure id
            String pMessageStructureID = profile.getMessageStructureID();
            String mMessageStructureID = message.getMessageStructureID();
            if (mMessageStructureID == null) {
                mMessageStructureID = "";
            }
            // 2.3.1 empty no check
            if (!("2.3.1".equals(pVersion) && "".equals(mMessageStructureID))) {
                if (!pMessageStructureID.equals(mMessageStructureID)) {
                    MessageFailureV2 mf = new MessageFailureV2(
                            message.getEncoding());
                    sb.delete(0, sb.length());
                    sb.append("The Message Structure ID '").append(
                            pMessageStructureID);
                    sb.append("' specified in the profile does not match the Message Structure ID '");
                    sb.append(mMessageStructureID).append(
                            "' specified in the message instance.");
                    mf.setDescription(sb.toString());
                    mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                    mf.setFailureType(AssertionTypeV2Constants.MESSAGE_STRUCTURE_ID);
                    XmlCursor xmlCursor = getMessageLocation(
                            (XmlMessage) message, "$this//*:MSH.9/*:MSG.3");
                    if (xmlCursor != null) {
                        setMessageLocation(mf, xmlCursor);
                    }
                    messageFailures.add(mf);
                    messageStructureIdFailures = true;
                }
            }
            if (!messageStructureIdFailures) {
                MessageFailureV2 mf = new MessageFailureV2(
                        message.getEncoding());
                mf.setDescription("The message type (MSH.9) is correct.");
                mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                mf.setFailureType(AssertionTypeV2Constants.CHECKED);
                messageFailures.add(mf);
            }
        } catch (Exception e) {
            throw new MessageValidationException(e.getMessage());
        }
    }

    /**
     * Set the message location given by an XPath of a MessageFailure
     * 
     * @param aMessage
     * @param anXPath
     */
    private XmlCursor getMessageLocation(XmlMessage aMessage, String anXPath) {
        XmlObject[] rs = null;
        rs = aMessage.getDocument().selectPath(anXPath);
        if (rs.length == 1) {
            XmlCursor cursor = rs[0].newCursor();
            return cursor;
        }
        return null;
    }

    /**
     * Set the message location of a MessageFailure
     * 
     * @param aMessageFailure
     * @param xmlCursor
     */
    private void setMessageLocation(MessageFailure aMessageFailure,
            XmlCursor xmlCursor) {
        XmlLineNumber xln = getLineColumn(xmlCursor);
        if (xln != null) {
            aMessageFailure.setLine(xln.getLine());
            aMessageFailure.setColumn(xln.getColumn());
        }
        aMessageFailure.setPath(getMessageLocationAsXPath(xmlCursor));
    }

    /**
     * Get the line and the column from an XmlCursor
     * 
     * @param messageLocation
     *        an XmlCursor
     * @return an XmlLineNumber
     */
    private XmlLineNumber getLineColumn(XmlCursor messageLocation) {
        XmlLineNumber bm = (XmlLineNumber) messageLocation.getBookmark(XmlLineNumber.class);
        return bm;
    }

    /**
     * Get the message location as an XPath expression
     * 
     * @param messageLocation
     *        an XmlCursor
     * @return a XPath expression representing the message location
     */
    private String getMessageLocationAsXPath(XmlCursor messageLocation) {
        if (messageLocation == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        messageLocation.push();
        int pos;
        String elementName = null;
        do {
            if (!messageLocation.isStart()) {
                messageLocation.toParent();
            }
            if (messageLocation.getName() != null) {
                elementName = messageLocation.getName().getLocalPart();
                // Count the position of this element
                pos = 1;
                while (messageLocation.toPrevSibling()) {
                    if (messageLocation.getName().getLocalPart().equals(
                            elementName)) {
                        pos++;
                    }
                }
                sb.insert(0, "]").insert(0, pos).insert(0, "[");
                sb.insert(0, elementName).insert(0, "/");
            }
            messageLocation.toParent();
        } while (!messageLocation.isStartdoc());
        // Remove a / at the end
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '/') {
            sb.delete(sb.length() - 1, sb.length());
        }
        messageLocation.pop();
        return sb.toString();
    }

    @Override
    protected void checkMessageStructure() throws MessageValidationException {
        boolean messageStructureFailure = false;
        try {
            // Create a schema
            StreamSource xsltStream = new StreamSource(
                    MessageStructureValidationV2Xml.class.getClassLoader().getResourceAsStream(
                            MessageValidationConstants.XSLT_CHECK_STRUCTURE));
            Transformer t = TransformerFactory.newInstance().newTransformer(
                    xsltStream);
            XmlObject[] rs = profile.getDocument().selectPath(
                    "//SegGroup[@Name]");
            if (rs.length > 0) {
                // if (message.hasGroups()) {
                t.setParameter("groups", "true");
            } else {
                t.setParameter("groups", "false");
            }
            t.setParameter("xml", "true");
            StreamSource src = new StreamSource(
                    profile.getDocument().newInputStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            t.transform(src, new StreamResult(out));
            XmlObject schemaDoc = XmlObject.Factory.parse(
                    new ByteArrayInputStream(out.toByteArray()),
                    (new XmlOptions()).setLoadLineNumbers());

            // Load the schema
            SchemaTypeLoader sLoader = null;
            Collection<Object> compErrors = new ArrayList<Object>();
            XmlOptions schemaOptions = new XmlOptions();
            schemaOptions.setErrorListener(compErrors);
            XmlObject[] schemas = new XmlObject[1];
            schemas[0] = schemaDoc;
            sLoader = XmlBeans.compileXsd(schemas, sLoader, schemaOptions);

            // Remove all Z-Segments
            XmlMessage messageWithoutZSegment = (XmlMessage) ((XmlMessage) message).clone();
            rs = messageWithoutZSegment.getDocument().selectPath(
                    "//*[starts-with(name(), 'Z')]");
            for (int i = rs.length - 1; i >= 0; i--) {
                XmlObject zSegment = rs[i];
                zSegment.newCursor().removeXml();
            }
            // Load the Message
            XmlObject xobj = sLoader.parse(
                    messageWithoutZSegment.getDocument().toString(), null,
                    (new XmlOptions()).setLoadLineNumbers());

            // Validate the Message against the schema
            Collection<XmlValidationError> errors = new ArrayList<XmlValidationError>();
            xobj.validate(new XmlOptions().setErrorListener(errors));
            Iterator<XmlValidationError> it = errors.iterator();
            while (it.hasNext()) {
                XmlValidationError xve = it.next();
                messageFailures.add(interpretSchemaError(xve));
                messageStructureFailure = true;
            }
        } catch (XmlException xmle) {
            // This type of exception is thrown when the generated schema is
            // ambiguous
            MessageFailureV2 mf = new MessageFailureV2(message.getEncoding());
            mf.setDescription("The message validation can't be performed because the profile is ambiguous."
                    + " Possible reasons for this problem include an ambiguous message definition"
                    + " specified in the standard or an ambiguous message definition caused by the"
                    + " user changing the Usage settings for segments during profile creation."
                    + " Remember that a segment with the same name MUST be separated by at least one"
                    + " non-optional segment with a different name.");
            mf.setFailureSeverity(ErrorSeverityConstants.FATAL);
            mf.setFailureType(AssertionTypeV2Constants.AMBIGUOUS_PROFILE);
            messageFailures.add(mf);
        } catch (Exception e) {
            throw new MessageValidationException(e.getMessage());
        } finally {
            if (!messageStructureFailure) {
                MessageFailureV2 mf = new MessageFailureV2(
                        message.getEncoding());
                mf.setDescription("The message structure at the segment level is correct.");
                mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                mf.setFailureType(AssertionTypeV2Constants.CHECKED);
                messageFailures.add(mf);
            }
        }
    }

    /**
     * Interpret XML Schemas errors
     * 
     * @param xve
     *        anXmlValidationError
     * @return a MessageFailure
     */
    private MessageFailureV2 interpretSchemaError(XmlValidationError xve) {
        MessageFailureV2 mf = new MessageFailureV2(message.getEncoding());
        String errorMessage = xve.getMessage();

        if (errorMessage.startsWith("Expected elements")) {
            Pattern pattern1 = Pattern.compile("Expected elements '(.+)' instead of '(.+)' here in element (.+)$");
            Pattern pattern2 = Pattern.compile("Expected elements '(.+)' before the end of the content in element (.+)$");

            Matcher m1 = pattern1.matcher(errorMessage);
            Matcher m2 = pattern2.matcher(errorMessage);

            if (m1.matches()) {
                String expected = m1.group(1).replaceAll("@urn:hl7-org:v2xml",
                        "");
                String actual = m1.group(2).replaceAll("@urn:hl7-org:v2xml", "");
                String position = m1.group(3);

                StringBuffer sb = new StringBuffer();
                sb.append("The element '").append(actual);
                sb.append("' does not match one of the expected elements '").append(
                        expected);
                sb.append("' in element '").append(position).append("'.");

                mf.setDescription(sb.toString());
                mf.setFailureSeverity(ErrorSeverityConstants.FATAL);
                mf.setFailureType(AssertionTypeV2Constants.MESSAGE_STRUCTURE);
            } else if (m2.matches()) {
                String expected = m2.group(1).replaceAll("@urn:hl7-org:v2xml",
                        "");
                String position = m2.group(2).replaceAll("@urn:hl7-org:v2xml",
                        "");

                StringBuffer sb = new StringBuffer();
                sb.append("Unexpected element before the end of the content (");
                sb.append(position).append("); '").append(expected).append(
                        "' expected.");

                mf.setDescription(sb.toString());
                mf.setFailureSeverity(ErrorSeverityConstants.FATAL);
                mf.setFailureType(AssertionTypeV2Constants.MESSAGE_STRUCTURE);
            } else {
                System.err.println("Unprocessed error message: " + errorMessage);
            }
        } else if (errorMessage.startsWith("Expected element")) {
            Pattern pattern1 = Pattern.compile("Expected element '(.*)' instead of '(.*)' here in element (.*)$");
            Pattern pattern2 = Pattern.compile("Expected element '(.*)' before the end of the content in element (.*)$");

            Matcher m1 = pattern1.matcher(errorMessage);
            Matcher m2 = pattern2.matcher(errorMessage);

            if (m1.matches()) {
                StringBuffer sb = new StringBuffer();
                String value = m1.group(1).substring(0,
                        m1.group(1).indexOf("@"));
                String str = m1.group(2).substring(0, m1.group(2).indexOf("@"));

                sb.append("The element '").append(str).append(
                        "' does not match the expected element '").append(value).append(
                        "'.");
                mf.setDescription(sb.toString());
                mf.setFailureSeverity(ErrorSeverityConstants.FATAL);
                mf.setFailureType(AssertionTypeV2Constants.MESSAGE_STRUCTURE);
            } else if (m2.matches()) {
                StringBuffer sb = new StringBuffer();
                String value = m2.group(1).substring(0,
                        m2.group(1).indexOf("@"));
                String str = m2.group(2).substring(0, m2.group(2).indexOf("@"));

                sb.append("Unexpected element before the end of the content (").append(
                        str).append("); '").append(value).append("' expected.");
                mf.setDescription(sb.toString());
                mf.setFailureSeverity(ErrorSeverityConstants.FATAL);
                mf.setFailureType(AssertionTypeV2Constants.MESSAGE_STRUCTURE);
            } else {
                System.err.println("Unprocessed error message: " + errorMessage);
            }
        } else if (errorMessage.startsWith("Element not allowed")) {
            Pattern pattern = Pattern.compile("Element not allowed: (.*) in element (.*)");
            Matcher m = pattern.matcher(errorMessage);

            if (m.matches()) {
                StringBuffer sb = new StringBuffer();
                String value = m.group(1).substring(0, m.group(1).indexOf("@"));

                sb.append("Element ").append(value).append(" is not allowed.");
                mf.setDescription(sb.toString());
                mf.setFailureSeverity(ErrorSeverityConstants.FATAL);
                mf.setFailureType(AssertionTypeV2Constants.MESSAGE_STRUCTURE);
            }
        } else if (errorMessage.equalsIgnoreCase("Invalid Type")) {
            mf.setDescription("The root element in the message is not the message structure id specified in the profile. It could also be a namespace error in the message.");
            mf.setFailureSeverity(ErrorSeverityConstants.FATAL);
            mf.setFailureType(AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        } else if (errorMessage.startsWith("Element")
                && errorMessage.endsWith("cannot have text content.")) {
            Pattern pattern = Pattern.compile("Element '(.+)'.*");
            Matcher m = pattern.matcher(errorMessage);
            if (m.matches()) {
                StringBuffer sb = new StringBuffer();
                String value = m.group(1).substring(0, m.group(1).indexOf("@"));
                sb.append("Element '").append(value).append(
                        "' with element-only content type cannot have text content.");
                mf.setDescription(sb.toString());
                mf.setFailureSeverity(ErrorSeverityConstants.FATAL);
                mf.setFailureType(AssertionTypeV2Constants.MESSAGE_STRUCTURE);
            } else {
                System.err.println("Unprocessed error message: " + errorMessage);
            }
        } else {
            System.err.println("Unprocessed error message: " + errorMessage);
        }
        XmlCursor xmlCursor = xve.getCursorLocation();
        setMessageLocation(mf, xmlCursor);

        return mf;
    }

    @Override
    protected void mapAndCheckElements(Profile profile)
            throws MessageValidationException {
        try {
            FiniteStateMachine fsm = new FiniteStateMachine(profile,
                    message.hasGroups());
            HashMap<XmlObject, ArrayList<XmlObject>> hMap = fsm.mapSegmentElements(
                    profile, (XmlMessage) message);

            alTmp = new ArrayList<ProfileMessageLink>();
            Iterator<XmlObject> itXmlObj = hMap.keySet().iterator();
            while (itXmlObj.hasNext()) {
                XmlObject pObj = itXmlObj.next();
                ArrayList<XmlObject> mObj = hMap.get(pObj);
                XmlCursor pCursor = pObj.newCursor();
                alPC = new ArrayList<ProfileContext>();
                alMC = new ArrayList<MessageContext>();
                for (int i = 0; i < mObj.size(); i++) {
                    XmlCursor mCursor = mObj.get(i).newCursor();
                    // System.out.println("checkElements "
                    // + mCursor.getName().getLocalPart());
                    pCursor.push();
                    checkElements(pCursor, mCursor);
                    pCursor.pop();
                    mCursor.dispose();
                }
                ProfileContext pc = getProfileContext(pCursor);
                if (pc.getType() == ElementType.SEGMENT_GROUP) {
                    if (mObj.size() > pc.getMax()) {
                        MessageFailureV2 mf = new MessageFailureV2(
                                message.getEncoding());
                        StringBuffer sb = new StringBuffer();
                        sb.append(pc.getNameInMessage()).append(" is present ").append(
                                mObj.size());
                        sb.append(" times whereas it is only allowed ").append(
                                pc.getMax()).append(" times");
                        mf.setDescription(sb.toString());
                        mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                        mf.setFailureType(AssertionTypeV2Constants.CARDINALITY);
                        XmlCursor xmlCursor = mObj.get(pc.getMax()).newCursor();
                        setMessageLocation(mf, xmlCursor);
                        messageFailures.add(mf);
                    } else if (mObj.size() < pc.getMin()) {
                        MessageFailureV2 mf = new MessageFailureV2(
                                message.getEncoding());
                        StringBuffer sb = new StringBuffer();
                        sb.append(pc.getNameInMessage()).append(" is present ").append(
                                mObj.size());
                        sb.append(" times whereas it must be present at least ").append(
                                pc.getMin()).append(" times");
                        mf.setDescription(sb.toString());
                        mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                        mf.setFailureType(AssertionTypeV2Constants.CARDINALITY);
                        if (mObj.size() > 0) {
                            XmlCursor xmlCursor = mObj.get(mObj.size() - 1).newCursor();
                            setMessageLocation(mf, xmlCursor);
                        }
                        messageFailures.add(mf);
                    }
                }
                pCursor.dispose();
            }
        } catch (Exception e) {
            throw new MessageValidationException(e.getMessage());
        }
    }

    /**
     * Check the Segment, Field, Component and SubComponent
     * 
     * @param pCursor
     * @param mCursor
     * @throws IOException
     * @throws XmlException
     */
    private void checkElements(XmlCursor pCursor, XmlCursor mCursor)
            throws XmlException, IOException {
        int level = 0;
        HashMap<ElementType, SimpleBookmark> bookmarks = new HashMap<ElementType, SimpleBookmark>();
        ValidationState vState = null;
        ProfileContext pc = null;
        MessageContext mc = null, oldMC = null;
        ProfileContext pcStart = getProfileContext(pCursor);
        boolean goon = true;
        boolean finishprofile = false;
        boolean finishmessage = false;

        do {
            pc = getProfileContext(pCursor);
            oldMC = mc;
            mc = getMessageContext(mCursor);
            if (pc == null) {
                // We reached the end of the profile
                goon = false;
            } else if (pc.getType() == ElementType.SEGMENT_GROUP) {
                // We reached a SegmentGroup in the Profile
                goon = false;
            } else if (mc.getNameInMessage() == null) {
                // We reached the end of the message
                goon = false;
                finishprofile = true;
            } else if (pc.getType() == pcStart.getType()) {
                // We reached a Segment in the Profile, so we check the segment
                // name
                if (!pc.getNameInMessage().equals(pcStart.getNameInMessage())) {
                    goon = false;
                }
            } else if (mc.getType() == ElementType.SEGMENT_GROUP) {
                // We reached a SegmentGroup in the Message
                goon = false;
                finishprofile = true;
            } else if (mc.getType() == ElementType.SEGMENT) {
                // We reached a Segment in the Message but we are not at a
                // Segment level in the Profile
                if (!mc.getNameInMessage().equals(pcStart.getNameInMessage())) {
                    goon = false;
                    finishprofile = true;
                }
            } else if (pc.getNameInMessage() == null) {
                // We finished the profile
                goon = false;
                finishmessage = true;
            }
            if (goon) {
                // System.out.print(level + " Profile " + pc.getNameInMessage()
                // + " " + pc.getType());
                // System.out.print(" Message " + mc.getNameInMessage() + " "
                // + mc.getType());
                // System.out.println();
                int levelComparison = compareLevel(pc, mc);
                if (levelComparison == 0
                        && pc.getNameInMessage().equals(mc.getNameInMessage())) {
                    // Check the value
                    checkValue(pc, mc);
                    // Update the current message instance of the profile
                    // element
                    pc.setCurrent(pc.getCurrent() + 1);
                    // Count the number of instance in the message
                    if (pc.getCurrent() == 1) {
                        pc.setMessageInstance(countMessageInstance(mCursor,
                                pc.getNameInMessage()));
                    }
                    if (pc.getUsage().equals("X")) {
                        // The usage in the profile is X
                        // So the element should not be present in the message
                        vState = ValidationState.XERR;
                    } else if (pc.getCurrent() == pc.getMessageInstance()) {
                        // This is the last element of this type in the message
                        vState = ValidationState.MAX;
                    } else {
                        // Normal State
                        vState = ValidationState.NORMAL;
                    }
                } else if (levelComparison < 0) {
                    // The level are different (a segment is compared to a
                    // field)
                    // It means it is an extra element
                    vState = ValidationState.XTRA;
                } else if (levelComparison > 0) {
                    // The level are different (a field is compared to a
                    // segment)
                    // It means an element could be missing
                    vState = ValidationState.UNMATCHING;
                } else if (pc.getNameInMessage().equals("")
                        || mc.getNameInMessage() == null) {
                    // Special Case
                    vState = ValidationState.SKIP;
                } else {
                    // All other cases are errors
                    vState = ValidationState.ERR;
                }
                // System.out.println(" " + vState);
                // Set the profile and message cursors
                setCursors(level, vState, pCursor, mCursor, bookmarks, pc, mc);
            }
        } while (goon);
        // Check we have to process the end of the profile
        if (finishprofile) {
            goon = true;
            do {
                pc = getProfileContext(pCursor);
                if (pc == null) {
                    goon = false;
                } else if (pc.getType() == ElementType.SEGMENT_GROUP
                        || pc.getType() == ElementType.SEGMENT) {
                    // We reached a SegmentGroup or Segment in the Profile
                    goon = false;
                }
                if (goon) {
                    // System.out.println("Profile " + pc.getNameInMessage() +
                    // " " + pc.getType());
                    if (pc.getUsage().equals("R")) {
                        MessageFailureV2 mf = new MessageFailureV2(
                                message.getEncoding());
                        StringBuffer sb = new StringBuffer();
                        sb.append(pc.getNameInMessage());
                        sb.append(" is missing");
                        mf.setDescription(sb.toString());
                        mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                        mf.setFailureType(AssertionTypeV2Constants.USAGE);
                        XmlCursor xmlCursor = oldMC.getObject().newCursor();
                        setMessageLocation(mf, xmlCursor);
                        messageFailures.add(mf);
                    }
                    // Advance in the profile
                    advanceProfileToSibling(level, pCursor, null);
                }

            } while (goon);
        }
        // Check if we have to process the end of the message
        if (finishmessage) {
            goon = true;
            do {
                mc = getMessageContext(mCursor);
                // We reached the end of the message
                if (mc.getNameInMessage() == null) {
                    goon = false;
                } else {
                    MessageFailureV2 mf = new MessageFailureV2(
                            message.getEncoding());
                    StringBuffer sb = new StringBuffer();
                    sb.append(mc.getNameInMessage());
                    sb.append(" is an extra element");
                    mf.setDescription(sb.toString());
                    mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                    mf.setFailureType(AssertionTypeV2Constants.XTRA);
                    setMessageLocation(mf, mCursor);
                    messageFailures.add(mf);
                    // Advance in the message
                    advanceMessage(mCursor);
                }
            } while (goon);
        }
        alPC.clear();
        alMC.clear();
    }

    /**
     * Set the cursors in the profile and the message
     * 
     * @param level
     * @param vState
     * @param pCursor
     * @param mCursor
     * @param bookmarks
     * @param pc
     * @param mc
     */
    private void setCursors(int level, ValidationState vState,
            XmlCursor pCursor, XmlCursor mCursor,
            HashMap<ElementType, SimpleBookmark> bookmarks, ProfileContext pc,
            MessageContext mc) {
        if (vState == ValidationState.SKIP) {
            // Do Nothing
        } else if (vState == ValidationState.MAX) {
            processErrors(vState);
            // Generate the cardinality errors
            if (pc.getCurrent() > pc.getMax()) {
                MessageFailureV2 mf = new MessageFailureV2(
                        message.getEncoding());
                StringBuffer sb = new StringBuffer();
                sb.append(pc.getNameInMessage()).append(" is present ").append(
                        pc.getCurrent());
                sb.append(" times whereas it is only allowed ").append(
                        pc.getMax()).append(" times");
                mf.setDescription(sb.toString());
                mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                mf.setFailureType(AssertionTypeV2Constants.CARDINALITY);
                setMessageLocation(mf, mCursor);
                messageFailures.add(mf);
            } else if (pc.getCurrent() < pc.getMin()) {
                MessageFailureV2 mf = new MessageFailureV2(
                        message.getEncoding());
                StringBuffer sb = new StringBuffer();
                sb.append(pc.getNameInMessage()).append(" is present ").append(
                        pc.getCurrent());
                sb.append(" times whereas it must be present at least ").append(
                        pc.getMin()).append(" times");
                mf.setDescription(sb.toString());
                mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                mf.setFailureType(AssertionTypeV2Constants.CARDINALITY);
                setMessageLocation(mf, mCursor);
                messageFailures.add(mf);
            }
            ElementType et = pc.getType();
            SimpleBookmark bookmark = null;
            if (et == ElementType.FIELD) {
                bookmark = bookmarks.get(ElementType.SEGMENT);
            } else if (et == ElementType.COMPONENT) {
                bookmark = bookmarks.get(ElementType.FIELD);
            } else if (et == ElementType.SUBCOMPONENT) {
                bookmark = bookmarks.get(ElementType.FIELD);
            }
            bookmarks.put(et, null);
            level = advanceProfile(level, pCursor, bookmark);
            // Reset the counter
            pc.setCurrent(0);
            // Advance the message
            advanceMessage(mCursor);
        } else if (vState == ValidationState.XTRA) {
            MessageFailureV2 mf = new MessageFailureV2(message.getEncoding());
            StringBuffer sb = new StringBuffer();
            sb.append(mc.getNameInMessage()).append(" is an extra element");
            mf.setDescription(sb.toString());
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            // TODO: Cardinality or Usage
            mf.setFailureType(AssertionTypeV2Constants.XTRA);
            setMessageLocation(mf, mCursor);
            messageFailures.add(mf);
            // Advance in the message
            advanceMessage(mCursor);
        } else if (vState == ValidationState.UNMATCHING) {
            ProfileMessageLink pml = new ProfileMessageLink(pc, mc);
            alTmp.add(pml);
            processErrors(vState);
            // Advance in the profile
            ElementType et = pc.getType();
            SimpleBookmark bookmark = null;
            if (et == ElementType.FIELD) {
                bookmark = bookmarks.get(ElementType.SEGMENT);
                et = ElementType.SEGMENT;
            } else if (et == ElementType.COMPONENT) {
                bookmark = bookmarks.get(ElementType.FIELD);
                et = ElementType.FIELD;
            } else if (et == ElementType.SUBCOMPONENT) {
                bookmark = bookmarks.get(ElementType.FIELD);
                et = ElementType.COMPONENT;
            }
            advanceProfileToSibling(level, pCursor, bookmark);
        } else if (vState == ValidationState.NORMAL) {
            // Save the position at this level
            ElementType et = pc.getType();
            SimpleBookmark bookmark = new SimpleBookmark();
            pCursor.setBookmark(bookmark);
            if (et == ElementType.SEGMENT) {
                bookmarks.put(ElementType.SEGMENT, bookmark);
            } else if (et == ElementType.FIELD) {
                bookmarks.put(ElementType.FIELD, bookmark);
            } else if (et == ElementType.COMPONENT) {
                bookmarks.put(ElementType.COMPONENT, bookmark);
            } else if (et == ElementType.SUBCOMPONENT) {
                bookmarks.put(ElementType.SUBCOMPONENT, bookmark);
            }
            // Advance in the profile
            level = advanceProfileToChild(level, pCursor);
            // Advance in the message
            advanceMessage(mCursor);
        } else if (vState == ValidationState.ERR) {
            ProfileMessageLink pml = new ProfileMessageLink(pc, mc);
            alTmp.add(pml);
            if (!err) {
                err = true;
                // Save the position in the message
                mCursor.push();
            }
            // Advance in the message
            if (mCursor.toNextSibling()) {
                // Do Nothing
            } else {
                // Put the position in the message back
                mCursor.pop();
                processErrors(vState);
                // Advance in the profile
                int oldlevel = level;
                level = advanceProfileToSibling(level, pCursor, null);
                if (level - oldlevel != 0) {
                    MessageFailureV2 mf = new MessageFailureV2(
                            message.getEncoding());
                    StringBuffer sb = new StringBuffer();
                    sb.append(mc.getNameInMessage()).append(
                            " is an extra element");
                    mf.setDescription(sb.toString());
                    mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                    // TODO: Cardinality or Usage
                    mf.setFailureType(AssertionTypeV2Constants.XTRA);
                    setMessageLocation(mf, mCursor);
                    messageFailures.add(mf);
                    advanceMessage(mCursor);
                }
            }
        } else if (vState == ValidationState.XERR) {
            MessageFailureV2 mf = new MessageFailureV2(message.getEncoding());
            StringBuffer sb = new StringBuffer();
            sb.append(pc.getNameInMessage()).append(
                    " is present whereas it is an X-Usage element");
            mf.setDescription(sb.toString());
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            // TODO: Usage
            mf.setFailureType(AssertionTypeV2Constants.X_USAGE);
            setMessageLocation(mf, mCursor);
            messageFailures.add(mf);
            // Advance in the message
            advanceMessage(mCursor);
        }
    }

    /**
     * Count the number of messageName instance in the message
     * 
     * @param mCursor
     * @param messageName
     */
    private int countMessageInstance(XmlCursor mCursor, String messageName) {
        int nInstance = 1;
        mCursor.push();
        boolean end = false;
        while (!end && mCursor.toNextSibling()) {
            if (mCursor.getName().getLocalPart().equals(messageName)) {
                nInstance++;
            } else {
                end = true;
            }
        }
        mCursor.pop();
        return nInstance;
    }

    /**
     * Compare the level in the profile and in the message. Return an integer
     * that is the difference between the levels Ex: Profile is at a Field
     * level, Message is at a Component level: The result is 1. Ex: Profile is
     * at a SubComponent level, Message is at a Field level: The result is -2.
     * 
     * @param pc
     *        a ProfileContext
     * @param mc
     *        a MessageContext
     * @return an integer
     */
    private int compareLevel(ProfileContext pc, MessageContext mc) {
        int profileLevel = 0;
        int messageLevel = 0;

        // Profile Level
        ElementType profileType = pc.getType();
        if (profileType.equals(ElementType.SEGMENT_GROUP)) {
            profileLevel = 0;
        }
        if (profileType.equals(ElementType.SEGMENT)) {
            profileLevel = 1;
        }
        if (profileType.equals(ElementType.FIELD)) {
            profileLevel = 2;
        }
        if (profileType.equals(ElementType.COMPONENT)) {
            profileLevel = 3;
        }
        if (profileType.equals(ElementType.SUBCOMPONENT)) {
            profileLevel = 4;
        }
        // Message Level
        ElementType messageType = mc.getType();
        if (messageType.equals(ElementType.SEGMENT_GROUP)) {
            messageLevel = 0;
        }
        if (messageType.equals(ElementType.SEGMENT)) {
            messageLevel = 1;
        }
        if (messageType.equals(ElementType.FIELD)) {
            messageLevel = 2;
        }
        if (messageType.equals(ElementType.COMPONENT)) {
            messageLevel = 3;
        }
        if (messageType.equals(ElementType.SUBCOMPONENT)) {
            messageLevel = 4;
        }

        return profileLevel - messageLevel;
    }

    /**
     * Advance in the profile
     * 
     * @param level
     * @param xc
     * @param sb
     * @return the new level
     */
    private int advanceProfile(int level, XmlCursor xc, SimpleBookmark sb) {
        // Figure out the next child
        String nextChild = null;
        String name = xc.getName().getLocalPart();
        if (name.equals("SegGroup")) {
            nextChild = "Segment";
        } else if (name.equals("Segment")) {
            nextChild = "Field";
        } else if (name.equals("Field")) {
            nextChild = "Component";
        } else if (name.equals("Component")) {
            nextChild = "SubComponent";
        } else if (name.equals("SubComponent")) {
            nextChild = "NotPossible";
        }
        // Try the first child
        if (!xc.toChild(nextChild)) {
            level = advanceProfileToSibling(level, xc, sb);
        } else {
            level++;
        }
        return level;
    }

    /**
     * Advance in the profile to the first sibling
     * 
     * @param level
     * @param xc
     * @param sb
     * @return the new level
     */
    private int advanceProfileToSibling(int level, XmlCursor xc,
            SimpleBookmark sb) {
        boolean end = false;
        int toParent = 0;
        ElementType initialElementType = ElementType.valueOf(xc.getName().getLocalPart().toUpperCase());
        while (!end && !xc.toNextSibling()) {
            end = !xc.toParent();
            toParent++;
            // No sibling has been found so we moved to the parent
            // if a position has been saved in the bookmark
            // we should move the profile to that position
            if ((toParent == 1 && initialElementType == ElementType.COMPONENT && sb != null)
                    || (toParent == 2
                            && initialElementType == ElementType.SUBCOMPONENT && sb != null)) {
                xc.toBookmark(sb);
                end = true;
                level--;
            }
            if (level != -1) {
                level--;
            }
        }
        return level;
    }

    /**
     * Advance in the profile to the first child
     * 
     * @param level
     * @param xc
     * @return the new level
     */
    private int advanceProfileToChild(int level, XmlCursor xc) {
        // Figure out the next child
        String nextChild = null;
        String name = xc.getName().getLocalPart();
        if (name.equals("SegGroup")) {
            nextChild = "Segment";
        } else if (name.equals("Segment")) {
            nextChild = "Field";
        } else if (name.equals("Field")) {
            nextChild = "Component";
        } else if (name.equals("Component")) {
            nextChild = "SubComponent";
        } else if (name.equals("SubComponent")) {
            nextChild = "NotPossible";
        }
        // Try the first child
        if (xc.toChild(nextChild)) {
            level++;
        }
        return level;
    }

    /**
     * Advance in the message
     * 
     * @param xc
     */
    private void advanceMessage(XmlCursor xc) {
        boolean end = false;
        if (!xc.toChild(0)) {
            while (!end && !xc.toNextSibling()) {
                end = !xc.toParent();
            }
        }
    }

    /**
     * Generate the errors depending on the ValidationState
     * 
     * @param vs
     */
    private void processErrors(ValidationState vs) {
        err = false;
        if (alTmp.size() > 0) {
            // Get the last ProfileMessageLink
            ProfileMessageLink pml = alTmp.get(alTmp.size() - 1);
            if (vs == ValidationState.ERR
                    && pml.getProfileContext().getUsage().equals("R")) {
                XmlCursor xc = pml.getMessageContext().getObject().newCursor();
                boolean last = !xc.toNextSibling();
                if (!last) {
                    xc.toPrevSibling();
                }
                xc.dispose();

                if (last) {
                    MessageFailureV2 mf = new MessageFailureV2(
                            message.getEncoding());
                    StringBuffer sb = new StringBuffer();
                    sb.append(pml.getProfileContext().getNameInMessage()).append(
                            " element is missing");
                    mf.setDescription(sb.toString());
                    mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                    mf.setFailureType(AssertionTypeV2Constants.USAGE);
                    pml = alTmp.get(0);
                    XmlCursor xmlCursor = pml.getMessageContext().getObject().newCursor();
                    setMessageLocation(mf, xmlCursor);
                    messageFailures.add(mf);
                } else {
                    MessageFailureV2 mf = new MessageFailureV2(
                            message.getEncoding());
                    StringBuffer sb = new StringBuffer();
                    sb.append(pml.getProfileContext().getNameInMessage()).append(
                            " is an extra element");
                    mf.setDescription(sb.toString());
                    mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                    // TODO: Cardinality or Usage
                    mf.setFailureType(AssertionTypeV2Constants.XTRA);
                    XmlCursor xmlCursor = pml.getMessageContext().getObject().newCursor();
                    setMessageLocation(mf, xmlCursor);
                    messageFailures.add(mf);
                }
            } else if (vs == ValidationState.UNMATCHING
                    && pml.getProfileContext().getUsage().equals("R")) {
                MessageFailureV2 mf = new MessageFailureV2(
                        message.getEncoding());
                StringBuffer sb = new StringBuffer();
                sb.append(pml.getProfileContext().getNameInMessage()).append(
                        " element is missing");
                mf.setDescription(sb.toString());
                mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                mf.setFailureType(AssertionTypeV2Constants.USAGE);
                pml = alTmp.get(0);
                XmlCursor xcTmp = pml.getMessageContext().getObject().newCursor();
                xcTmp.toPrevSibling();
                XmlCursor xmlCursor = xcTmp;
                setMessageLocation(mf, xmlCursor);
                messageFailures.add(mf);
            } else if (vs == ValidationState.MAX) {
                for (int i = 0; i < alTmp.size(); i++) {
                    pml = alTmp.get(i);
                    MessageFailureV2 mf = new MessageFailureV2(
                            message.getEncoding());
                    StringBuffer sb = new StringBuffer();
                    sb.append(pml.getMessageContext().getNameInMessage()).append(
                            " is an extra element");
                    mf.setDescription(sb.toString());
                    mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                    mf.setFailureType(AssertionTypeV2Constants.XTRA);
                    XmlCursor xmlCursor = pml.getMessageContext().getObject().newCursor();
                    setMessageLocation(mf, xmlCursor);
                    messageFailures.add(mf);
                }
            }
            alTmp.clear();
        }
    }

    /**
     * Get the ProfileContext for the given XmlCursor
     * 
     * @return a ProfileContext
     * @throws IOException
     * @throws XmlException
     */
    private ProfileContext getProfileContext(XmlCursor pCursor)
            throws XmlException, IOException {
        ProfileContext pc = null;
        if (pCursor.getName() != null
                && !pCursor.getName().getLocalPart().equals(
                        "HL7v2xConformanceProfile")
                && !pCursor.getName().getLocalPart().equals("HL7v2xStaticDef")) {
            pc = new ProfileContext(pCursor.getObject());
            // Check if a ProfileContext already exists
            int idx = alPC.indexOf(pc);
            if (idx == -1) {
                pc.setCurrent(0);
                pc.setMessageInstance(0);
                // Min and Max
                String type = pCursor.getName().getLocalPart();
                int min = -1, max = -1;
                if (type.equals("Component") || type.equals("SubComponent")) {
                    min = 1;
                    max = 1;
                } else if (type.equals("SegGroup") || type.equals("Segment")
                        || type.equals("Field")) {
                    min = Integer.parseInt(pCursor.getAttributeText(QName.valueOf("Min")));
                    String sMax = pCursor.getAttributeText(QName.valueOf("Max"));
                    max = 1000;
                    if (!sMax.equals("*")) {
                        max = Integer.parseInt(sMax);
                    }
                }
                pc.setMin(min);
                pc.setMax(max);
                // Usage
                pc.setUsage(pCursor.getAttributeText(QName.valueOf("Usage")));
                // Table
                pc.setTable(pCursor.getAttributeText(QName.valueOf("Table")));
                // Max Length
                String length = pCursor.getAttributeText(QName.valueOf("Length"));
                if (length != null) {
                    pc.setMaxLength(Integer.parseInt(length));
                }
                // Datatype
                pc.setDatatype(pCursor.getAttributeText(QName.valueOf("Datatype")));
                // Constant
                pc.setConstant(pCursor.getAttributeText(QName.valueOf("ConstantValue")));
                // Is it a primitive element?
                pCursor.push();
                String nextChild = null;
                if (type.equals("SegGroup")) {
                    nextChild = "Segment";
                } else if (type.equals("Segment")) {
                    nextChild = "Field";
                } else if (type.equals("Field")) {
                    nextChild = "Component";
                } else if (type.equals("Component")) {
                    nextChild = "SubComponent";
                } else if (type.equals("SubComponent")) {
                    nextChild = "NotPossible";
                }
                boolean primitive = !pCursor.toChild(nextChild);
                pCursor.pop();
                pc.setPrimitive(primitive);
                // Message Name, the get method also set the message name
                pc.getNameInMessage(profile.getHl7VersionAsString());
                alPC.add(pc);
            } else {
                pc = alPC.get(idx);
            }
        }
        return pc;
    }

    /**
     * Get the MessageContext for the given XmlCursor
     * 
     * @return a MessageContext
     */
    private MessageContext getMessageContext(XmlCursor mCursor) {
        MessageContext mc = new MessageContext(mCursor.getObject());
        // Check if a MessageContext already exists
        int idx = alMC.indexOf(mc);
        if (idx == -1) {
            String name = mCursor.getName() != null ? mCursor.getName().getLocalPart()
                    : null;
            // Name
            mc.setNameInMessage(name);
            if (name != null) {
                // Type
                int l = 0;
                boolean end = false;
                boolean segment = name.length() == 3;
                if (segment) {
                    mc.setType(ElementType.SEGMENT);
                } else {
                    mCursor.push();
                    while (!end && !segment) {
                        l++;
                        end = !mCursor.toParent() || mCursor.getName() == null;
                        if (mCursor.getName() != null) {
                            segment = mCursor.getName().getLocalPart().length() == 3;
                        }
                    }
                    mCursor.pop();
                    if (!end) {
                        if (l == 1) {
                            mc.setType(ElementType.FIELD);
                        } else if (l == 2) {
                            mc.setType(ElementType.COMPONENT);
                        } else if (l == 3) {
                            mc.setType(ElementType.SUBCOMPONENT);
                        }
                    } else {
                        mc.setType(ElementType.SEGMENT_GROUP);
                    }
                }
                // Value
                mCursor.push();
                end = false;
                StringBuffer sb = new StringBuffer();
                while (!end) {
                    end = mCursor.toNextToken().isNone();
                    if (mCursor.isText()) {
                        sb.append(mCursor.getTextValue());
                    } else if (mCursor.isStart()) {
                        end = true;
                    }
                }
                mCursor.pop();
                mc.setValue(sb.toString().trim());
                // Is it a primitive element?
                mCursor.push();
                mc.setPrimitive(!mCursor.toFirstChild());
                mCursor.pop();
            }
            alMC.add(mc);
        } else {
            mc = alMC.get(idx);
        }
        return mc;
    }

    /**
     * Check the value of the primitive element
     * 
     * @param pc
     *        a ProfileContext
     * @param mc
     *        a MessageContext
     */
    private void checkValue(ProfileContext pc, MessageContext mc) {
        boolean primitiveProfile = pc.isPrimitive();
        boolean primitiveMessage = mc.isPrimitive();
        String value = mc.getValue();
        if (!primitiveProfile && !value.equals("")) {
            StringBuffer sb = new StringBuffer();
            MessageFailureV2 mf = new MessageFailureV2(message.getEncoding());
            sb.append("The element is not a primitive and it has a value '").append(
                    value).append("'");
            mf.setDescription(sb.toString());
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            mf.setFailureType(AssertionTypeV2Constants.DATA);
            XmlCursor xmlCursor = mc.getObject().newCursor();
            setMessageLocation(mf, xmlCursor);
            messageFailures.add(mf);
            sb = null;
        } else if (primitiveProfile && value.equals("")) {
            StringBuffer sb = new StringBuffer();
            MessageFailureV2 mf = new MessageFailureV2(message.getEncoding());
            sb.append("The value is empty");
            mf.setDescription(sb.toString());
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            mf.setFailureType(AssertionTypeV2Constants.DATA);
            XmlCursor xmlCursor = mc.getObject().newCursor();
            setMessageLocation(mf, xmlCursor);
            messageFailures.add(mf);
            sb = null;
        } else if (primitiveProfile) {
            MessageFailureV2 mf = null;
            XmlCursor xmlCursor = mc.getObject().newCursor();
            // Check the length
            int maxLength = pc.getMaxLength();
            mf = checkLength(value, maxLength);
            if (mf != null) {
                setMessageLocation(mf, xmlCursor);
                messageFailures.add(mf);
            }
            // Check the table
            String table = pc.getTable();
            if (table != null) {
                mf = checkTable(value, table);
                if (mf != null) {
                    setMessageLocation(mf, xmlCursor);
                    messageFailures.add(mf);
                }
            }
            // Check the constant
            String constant = pc.getConstant();
            if (constant != null) {
                mf = checkConstant(value, constant);
                if (mf != null) {
                    setMessageLocation(mf, xmlCursor);
                    messageFailures.add(mf);
                }
            }
            // Check the format depending on the datatype
            String datatype = pc.getDatatype();
            if (datatype != null) {
                mf = checkDatatype(value, datatype);
                if (mf != null) {
                    setMessageLocation(mf, xmlCursor);
                    messageFailures.add(mf);
                }
            }
        } else if (primitiveMessage && !primitiveProfile) {
            if ("R".equals(pc.getUsage())) {
                StringBuffer sb = new StringBuffer();
                MessageFailureV2 mf = new MessageFailureV2(
                        message.getEncoding());
                sb.append("The element is missing at least one of its children");
                mf.setDescription(sb.toString());
                mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                mf.setFailureType(AssertionTypeV2Constants.MESSAGE_STRUCTURE);
                XmlCursor xmlCursor = mc.getObject().newCursor();
                setMessageLocation(mf, xmlCursor);
                messageFailures.add(mf);
                sb = null;
            }
        }
    }

    /*
     * The SimpleBookmark class is used to save a position in a cursor
     */
    class SimpleBookmark extends XmlCursor.XmlBookmark {
        public SimpleBookmark() {
            super();
        }
    }

}
