/*
 * NIST Healthcare Core
 * MessageStructureValidationV2Er7.java Aug 18, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.structure.v2.er7;

import gov.nist.healthcare.core.message.MessageLocation;
import gov.nist.healthcare.core.message.v2.er7.Er7Message;
import gov.nist.healthcare.core.profile.Profile;
import gov.nist.healthcare.core.validation.message.MessageValidationConstants;
import gov.nist.healthcare.core.validation.message.MessageValidationException;
import gov.nist.healthcare.core.validation.message.structure.v2.FiniteStateMachine;
import gov.nist.healthcare.core.validation.message.structure.v2.MessageStructureValidationV2;
import gov.nist.healthcare.core.validation.message.v2.MessageFailureV2;
import gov.nist.healthcare.validation.AssertionTypeV2Constants;
import gov.nist.healthcare.validation.ErrorSeverityConstants;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;

/**
 * This class validates the message structure of an Er7 V2 message.
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageStructureValidationV2Er7 extends
        MessageStructureValidationV2 {

    private Map<XmlObject, ArrayList<Integer>> profileMapping;
    private Map<Integer, String> er7Mapping;
    private Map<String, Integer> segInstances;
    private int lineNumber;
    private String locSegName;
    private int locSegInstanceNumber;
    private int locFieldPosition;
    private int locFieldInstanceNumber;
    private int locComponentPosition;
    private int locSubComponentPosition;
    private int fieldInstance;

    @Override
    protected void checkBasic() throws MessageValidationException {
        setEr7Mapping();

        locSegName = "MSH";
        locSegInstanceNumber = 1;
        lineNumber = ((Er7Message) message).getLine(getCurrentLocation());

        StringBuffer sb = new StringBuffer();
        /* Check the version */
        String pVersion = profile.getHl7VersionAsString();
        String mVersion = message.getVersionAsString();
        if (mVersion == null) {
            mVersion = "";
        }
        if (!pVersion.equals(mVersion)) {
            locFieldPosition = 12;
            locFieldInstanceNumber = 1;

            MessageFailureV2 mf = new MessageFailureV2(message.getEncoding());
            sb.delete(0, sb.length());
            sb.append("The HL7 version '").append(pVersion);
            sb.append("' specified in the profile does not match the HL7 version '");
            sb.append(mVersion).append("' specified in the message instance.");
            mf.setDescription(sb.toString());
            mf.setFailureSeverity(ErrorSeverityConstants.FATAL);
            mf.setFailureType(AssertionTypeV2Constants.VERSION);
            mf.setLine(((Er7Message) message).getLine(getCurrentLocation()));
            mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
            mf.setPath(getCurrentLocation().toString());

            messageFailures.add(mf);
        } else {
            MessageFailureV2 mf = new MessageFailureV2(message.getEncoding());
            mf.setDescription("The version is correct.");
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            mf.setFailureType(AssertionTypeV2Constants.CHECKED);
            messageFailures.add(mf);
        }
        boolean messageStructureIdFailures = false;
        /* Check the message type */
        String pMessageType = profile.getMessageType();
        String mMessageType = message.getMessageCode();
        if (mMessageType == null) {
            mMessageType = "";
        }
        if (!pMessageType.equals(mMessageType)) {
            locFieldPosition = 9;
            locFieldInstanceNumber = 1;
            locComponentPosition = 1;

            MessageFailureV2 mf = new MessageFailureV2(message.getEncoding());
            sb.delete(0, sb.length());
            sb.append("The Message Type '").append(pMessageType);
            sb.append("' specified in the profile does not match the Message Type '");
            sb.append(mMessageType).append(
                    "' specified in the message instance.");
            mf.setDescription(sb.toString());
            mf.setFailureSeverity(ErrorSeverityConstants.FATAL);
            mf.setFailureType(AssertionTypeV2Constants.MESSAGE_STRUCTURE_ID);
            mf.setLine(((Er7Message) message).getLine(getCurrentLocation()));
            mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
            mf.setPath(getCurrentLocation().toString());

            messageFailures.add(mf);
            messageStructureIdFailures = true;
        }
        /* Check the event ID */
        String pEventID = profile.getMessageEvent();
        String mEventID = message.getMessageEvent();
        if (mEventID == null) {
            mEventID = "";
        }
        // 2.3.1 ACK empty no check
        if (!("ACK".equals(pMessageType) && "2.3.1".equals(pVersion) && "".equals(mEventID))) {
            if (!pEventID.equals(mEventID)) {
                locFieldPosition = 9;
                locFieldInstanceNumber = 1;
                locComponentPosition = 2;

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
                mf.setLine(((Er7Message) message).getLine(getCurrentLocation()));
                mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
                mf.setPath(getCurrentLocation().toString());

                messageFailures.add(mf);
                messageStructureIdFailures = true;
            }
        }
        /* Check the message structure id */
        String pMessageStructureID = profile.getMessageStructureID();
        String mMessageStructureID = message.getMessageStructureID();
        if (mMessageStructureID == null) {
            mMessageStructureID = "";
        }
        // 2.3.1 empty no check
        if (!("2.3.1".equals(pVersion) && "".equals(mMessageStructureID))) {
            if (!pMessageStructureID.equals(mMessageStructureID)) {
                locFieldPosition = 9;
                locFieldInstanceNumber = 1;
                locComponentPosition = 3;

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
                mf.setLine(((Er7Message) message).getLine(getCurrentLocation()));
                mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
                mf.setPath(getCurrentLocation().toString());

                messageFailures.add(mf);
                messageStructureIdFailures = true;
            }
        }
        if (!messageStructureIdFailures) {
            MessageFailureV2 mf = new MessageFailureV2(message.getEncoding());
            mf.setDescription("The message type (MSH.9) is correct.");
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            mf.setFailureType(AssertionTypeV2Constants.CHECKED);
            messageFailures.add(mf);
        }
    }

    /**
     * Set the ER7 mapping : line number - ER7 segment.
     * 
     * @throws MessageValidationException
     */
    private void setEr7Mapping() throws MessageValidationException {
        BufferedReader er7 = null;
        er7Mapping = new HashMap<Integer, String>();
        er7 = new BufferedReader(new StringReader(message.getMessageAsString()));
        String tmp;
        int lineNr = 0;
        try {
            while (((tmp = er7.readLine()) != null)) {
                lineNr++;
                if (!tmp.matches("^[A-Z0-9]{3}(\\|.*)*$")) {
                    MessageFailureV2 mf = new MessageFailureV2(
                            message.getEncoding());
                    StringBuffer sb = new StringBuffer();
                    sb.delete(0, sb.length());
                    sb.append("The line '").append(tmp).append(
                            "' is not a valid segment");
                    mf.setDescription(sb.toString());
                    mf.setFailureSeverity(ErrorSeverityConstants.FATAL);
                    mf.setFailureType(AssertionTypeV2Constants.MESSAGE_STRUCTURE);
                    mf.setLine(lineNr);
                    mf.setColumn(1);
                    // mf.setEPath(getCurrentLocation().toString());
                    messageFailures.add(mf);
                }
                er7Mapping.put(lineNr, tmp);
            }
        } catch (IOException e) {
            throw new MessageValidationException(e.getMessage());
        }
    }

    private MessageLocation getCurrentLocation() {
        MessageLocation location = null;
        if (!"".equals(locSegName)) {
            if (locFieldPosition > 0) {
                if (locComponentPosition > 0) {
                    if (locSubComponentPosition > 0) {
                        location = new MessageLocation(locSegName,
                                locSegInstanceNumber, locFieldPosition,
                                locFieldInstanceNumber, locComponentPosition,
                                locSubComponentPosition);
                    } else {
                        location = new MessageLocation(locSegName,
                                locSegInstanceNumber, locFieldPosition,
                                locFieldInstanceNumber, locComponentPosition);
                    }

                } else {
                    location = new MessageLocation(locSegName,
                            locSegInstanceNumber, locFieldPosition,
                            locFieldInstanceNumber);
                }
            } else {
                location = new MessageLocation(locSegName, locSegInstanceNumber);
            }
        }
        return location;
    }

    @Override
    protected void checkMessageStructure() throws MessageValidationException {
        boolean messageStructureFailure = false;
        try {
            // Create a pseudo XML message
            XmlObject pseudoMessage = XmlObject.Factory.newInstance();
            XmlCursor pmCursor = pseudoMessage.newCursor();
            pmCursor.toNextToken();
            pmCursor.beginElement(profile.getMessageStructureID(),
                    "urn:hl7-org:v2xml");
            BufferedReader br = new BufferedReader(new StringReader(
                    message.getMessageAsString()));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (!line.matches("\\s*")) {
                    String fieldSep = ((Er7Message) message).getFieldSeparatorChar();
                    try {
                        int idx = line.indexOf(fieldSep);
                        if (idx == -1 && line.length() <= 3) {
                            idx = 3;
                        }
                        line = line.substring(0, idx);
                        if (!line.startsWith("Z")) {
                            pmCursor.beginElement(line, "urn:hl7-org:v2xml");
                            pmCursor.toNextToken();
                        }
                    } catch (StringIndexOutOfBoundsException e) {
                        if (line.length() > 3) {
                            System.out.println(line);
                        }
                    }
                }
            }
            pmCursor.dispose();

            // Create a schema
            StreamSource xsltStream = new StreamSource(
                    MessageStructureValidationV2Er7.class.getClassLoader().getResourceAsStream(
                            MessageValidationConstants.XSLT_CHECK_STRUCTURE));
            Transformer t = TransformerFactory.newInstance().newTransformer(
                    xsltStream);
            t.setParameter("groups", "false");
            t.setParameter("xml", "false");

            StreamSource src = new StreamSource(
                    profile.getDocument().newInputStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            t.transform(src, new StreamResult(out));
            XmlObject schemaDoc = XmlObject.Factory.parse(
                    new ByteArrayInputStream(out.toByteArray()),
                    (new XmlOptions()).setLoadLineNumbers());
            // pseudoMessage.save(new File("tmp/mu/PseudoMessage.xml"));
            // schemaDoc.save(new File("tmp/mu/Schema.xsd"));
            // Load the schema
            SchemaTypeLoader sLoader = null;
            Collection<Object> compErrors = new ArrayList<Object>();
            XmlOptions schemaOptions = new XmlOptions();
            schemaOptions.setErrorListener(compErrors);
            XmlObject[] schemas = new XmlObject[1];

            schemas[0] = schemaDoc;
            sLoader = XmlBeans.compileXsd(schemas, sLoader, schemaOptions);

            // Load the Message
            XmlObject xobj = sLoader.parse(pseudoMessage.toString(), null,
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
     * Interpret XML Schemas errors.
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
                String position = m1.group(3).replaceAll("@urn:hl7-org:v2xml",
                        "");

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
        } else if (errorMessage.equalsIgnoreCase("Invalid Type")) {
            mf.setDescription("The root element in the message is not the message structure id specified in the profile. It could also be a namespace error in the message.");
            mf.setFailureSeverity(ErrorSeverityConstants.FATAL);
            mf.setFailureType(AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        } else {
            System.err.println("Unprocessed error message: " + errorMessage);
        }

        return mf;
    }

    @Override
    protected void mapAndCheckElements(Profile profile)
            throws MessageValidationException {
        try {
            /* Set profile mapping */
            FiniteStateMachine fsm = new FiniteStateMachine(profile, false);
            profileMapping = fsm.mapSegmentElements(profile,
                    (Er7Message) message);

            /* Parse */
            parse();
        } catch (Exception e) {
            e.printStackTrace();
            throw new MessageValidationException(e.getMessage());
        }
    }

    /**
     * Parse the profile to check the segments
     */
    private void parse() {
        lineNumber = -1;
        segInstances = new HashMap<String, Integer>();
        /* Validate segments cardinalities */
        Set<XmlObject> keySet = profileMapping.keySet();
        for (XmlObject key : keySet) {
            if (key != null) {
                checkSegmentCardinalities(key);
            }
        }
        /* Reverse profileMapping */
        ArrayList<XmlObject> reverseProfileMapping = new ArrayList<XmlObject>();
        keySet = profileMapping.keySet();
        for (XmlObject key : keySet) {
            if (key != null) {
                ArrayList<Integer> values = profileMapping.get(key);
                if (values != null) {
                    for (Integer value : values) {
                        if (reverseProfileMapping.size() < value + 1) {
                            for (int i = reverseProfileMapping.size(); i < value + 1; i++) {
                                reverseProfileMapping.add(null);
                            }
                        }
                        reverseProfileMapping.set(value, key);
                    }
                }
            }
        }
        /* Validate Segment */
        for (int idx = 0; idx < reverseProfileMapping.size(); idx++) {
            XmlObject xmlSegment = reverseProfileMapping.get(idx);
            if (xmlSegment != null) {
                int lineNr = idx;
                int instance = 1;
                String segment = er7Mapping.get(lineNr);
                String segName = segment.substring(0, 3);
                if (segInstances.containsKey(segName)) {
                    instance = segInstances.get(segName);
                    instance++;
                }
                segInstances.put(segName, instance);
                locSegName = segName;
                locSegInstanceNumber = segInstances.get(segName);
                lineNumber = lineNr;
                checkSegment(xmlSegment, segment);
                lineNumber = -1;
                /* Reset message location */
                locSegName = "";
                locSegInstanceNumber = -1;
                locFieldPosition = -1;
                locFieldInstanceNumber = -1;
                locComponentPosition = -1;
                locSubComponentPosition = -1;
            }
        }
    }

    /**
     * Check the segments cardinalities
     * 
     * @param segObj
     *        the Segment object
     */
    private void checkSegmentCardinalities(XmlObject segObj) {

        XmlCursor cur = segObj.newCursor();
        String usage = cur.getAttributeText(QName.valueOf("Usage"));
        String segName = cur.getAttributeText(QName.valueOf("Name"));
        if (!usage.equals("X")) {
            String min = cur.getAttributeText(QName.valueOf("Min"));
            String max = cur.getAttributeText(QName.valueOf("Max"));
            if (cur.toParent()) {
                if (cur.getName().toString().equals("SegGroup")) {
                    ArrayList<Integer> list = profileMapping.get(segObj);
                    if (list != null) {
                        Iterator<Integer> it = list.iterator();
                        ArrayList<ArrayList<Integer>> groups = new ArrayList<ArrayList<Integer>>();
                        ArrayList<Integer> currentGroup = null;
                        int previous = 0;
                        while (it.hasNext()) {
                            int lineNr = it.next();
                            /* init */
                            if (currentGroup == null) {
                                ArrayList<Integer> group = new ArrayList<Integer>();
                                currentGroup = group;
                                currentGroup.add(lineNr);
                            } else {
                                /* does the line belong to the current group ? */
                                boolean b = true;
                                int i = previous + 1;
                                while (i < lineNr && b) {
                                    String line = er7Mapping.get(i);
                                    if (!line.matches("\\s*")) {
                                        b = false;
                                    }
                                }
                                if (b) {
                                    currentGroup.add(lineNr);
                                } else {
                                    groups.add(currentGroup);
                                    ArrayList<Integer> group = new ArrayList<Integer>();
                                    currentGroup = group;
                                    currentGroup.add(lineNr);
                                }
                            }
                            previous = lineNr;
                        }
                    }
                } else {
                    ArrayList<Integer> list = profileMapping.get(segObj);
                    if (list != null) {
                        int occurrences = list.size();
                        if (occurrences < Integer.parseInt(min)) {
                            MessageFailureV2 mf = new MessageFailureV2(
                                    message.getEncoding());
                            mf.setFailureType(AssertionTypeV2Constants.CARDINALITY);
                            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                            mf.setDescription(segName
                                    + " is present "
                                    + occurrences
                                    + " times whereas it must be present at least "
                                    + min + " times");
                            mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
                            mf.setLine(lineNumber);
                            mf.setPath(getCurrentLocation().toString());
                            messageFailures.add(mf);
                        }
                        if (!max.equals("*")
                                && occurrences > Integer.parseInt(max)) {
                            MessageFailureV2 mf = new MessageFailureV2(
                                    message.getEncoding());
                            mf.setFailureType(AssertionTypeV2Constants.CARDINALITY);
                            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                            mf.setDescription(segName + " is present "
                                    + occurrences
                                    + " times whereas it is only allowed "
                                    + max + " times");
                            mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
                            mf.setLine(lineNumber);
                            mf.setPath(getCurrentLocation().toString());

                            messageFailures.add(mf);
                        }
                    }
                }
            }
        }
    }

    /**
     * Check a segment
     * 
     * @param segmentObj
     *        the segment object (from profile)
     * @param segment
     *        the segment string (from ER7)
     */
    private void checkSegment(XmlObject segmentObj, String segment) {
        locFieldInstanceNumber = -1;
        String segmentName = segmentObj.newCursor().getAttributeText(
                QName.valueOf("Name"));
        String segmentUsage = segmentObj.newCursor().getAttributeText(
                QName.valueOf("Usage"));
        XmlObject[] fieldsObj = segmentObj.selectChildren(QName.valueOf("Field"));

        if (fieldsObj.length > 0) {
            String[] fields = segment.split(((Er7Message) message).getFieldSeparator());
            if (fields.length - 1 > fieldsObj.length) {
                /* Extra fields */
                MessageFailureV2 mf = new MessageFailureV2(
                        message.getEncoding());
                mf.setFailureType(AssertionTypeV2Constants.XTRA);
                mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                mf.setDescription("Extra fields for segment " + segmentName);
                mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
                mf.setLine(lineNumber);
                mf.setPath(getCurrentLocation().toString());

                messageFailures.add(mf);
            } else {
                int fieldsIdx = 1;
                if (fields[0].equals("MSH")) {
                    fieldsIdx = 0;
                }
                for (int i = 0; i < fieldsObj.length; i++, fieldsIdx++) {
                    if (fieldsIdx == 0) {
                        continue;
                    }
                    locFieldPosition = i + 1;
                    locFieldInstanceNumber = 1;
                    locComponentPosition = -1;
                    if (fieldsIdx < fields.length) {
                        checkField(fieldsObj[i], fields[fieldsIdx]);
                    } else {
                        /* Check Usage */
                        checkUsage(fieldsObj[i], "");
                    }
                }
            }
            // No Fields
            if ("R".equals(segmentUsage) && fields.length - 1 == 0) {
                locFieldPosition = -1;
                StringBuffer sb = new StringBuffer();
                MessageFailureV2 mf = new MessageFailureV2(
                        message.getEncoding());
                sb.append("The element is missing at least one of its children");
                mf.setDescription(sb.toString());
                mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                mf.setFailureType(AssertionTypeV2Constants.MESSAGE_STRUCTURE);
                mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
                mf.setLine(lineNumber);
                mf.setPath(getCurrentLocation().toString());
                messageFailures.add(mf);
                sb = null;
            }
        }
    }

    /**
     * Check a field
     * 
     * @param fieldObj
     *        the field object (from profile)
     * @param field
     *        the field string (from ER7)
     */
    private void checkField(XmlObject fieldObj, String field) {
        fieldInstance = 1;
        locComponentPosition = -1;

        XmlCursor cur = fieldObj.newCursor();

        /* Check usage */
        String usage = cur.getAttributeText(QName.valueOf("Usage"));
        boolean usageError = checkUsage(fieldObj, field);

        if (!usageError) {
            if (usage.equals("R") || !field.equals("")) {
                /* Check cardinalities */
                String min = cur.getAttributeText(QName.valueOf("Min"));
                String max = cur.getAttributeText(QName.valueOf("Max"));

                if (getCurrentLocation().getSegmentName().equals("MSH")
                        && getCurrentLocation().getFieldPosition() == 2) {
                    /* MSH.2 */
                    checkValue(fieldObj, field);
                } else {

                    String[] repetitions = field.split(((Er7Message) message).getRepetitionSeparator());
                    int occurences = repetitions.length;
                    if (field.equals("")) {
                        occurences = 0;
                    }
                    checkCardinalities(fieldObj, usage, min, max, occurences);

                    XmlObject[] componentsObj = fieldObj.selectChildren(QName.valueOf("Component"));
                    if (componentsObj.length > 0) {
                        for (int i = 0; i < repetitions.length; i++) {
                            locFieldInstanceNumber = i + 1;
                            String[] components = repetitions[i].split(((Er7Message) message).getComponentSeparator());
                            if (components.length > componentsObj.length) {
                                /* Extra components */
                                MessageFailureV2 mf = new MessageFailureV2(
                                        message.getEncoding());
                                mf.setFailureType(AssertionTypeV2Constants.XTRA);
                                mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                                mf.setDescription("Extra components for field "
                                        + getCurrentLocation().toString());
                                mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
                                mf.setLine(lineNumber);
                                mf.setPath(getCurrentLocation().toString());

                                messageFailures.add(mf);
                            } else {
                                for (int j = 0; j < componentsObj.length; j++) {
                                    locComponentPosition = j + 1;
                                    locSubComponentPosition = -1;
                                    if (j < components.length) {
                                        checkComponent(componentsObj[j],
                                                components[j]);
                                    } else {
                                        /* check Usage */
                                        checkUsage(componentsObj[j], "");
                                    }
                                }
                            }
                            fieldInstance++;
                        }
                    } else {
                        for (int i = 0; i < repetitions.length; i++) {
                            locFieldInstanceNumber = i + 1;
                            if (!repetitions[i].matches("\\s*")) {
                                /* Check value */
                                checkValue(fieldObj, repetitions[i]);
                            }
                            fieldInstance++;
                        }
                    }
                }
            }
        }
        // No Components
        if ("R".equals(usage)
                && "".equals(field)
                && fieldObj.selectChildren(QName.valueOf("Component")).length > 0) {
            locComponentPosition = -1;
            StringBuffer sb = new StringBuffer();
            MessageFailureV2 mf = new MessageFailureV2(message.getEncoding());
            sb.append("The element is missing at least one of its children");
            mf.setDescription(sb.toString());
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            mf.setFailureType(AssertionTypeV2Constants.MESSAGE_STRUCTURE);
            mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
            mf.setLine(lineNumber);
            mf.setPath(getCurrentLocation().toString());
            messageFailures.add(mf);
            sb = null;
        }
    }

    /**
     * Check the component
     * 
     * @param componentObj
     *        the component object (from profile)
     * @param component
     *        the component string (from ER7)
     */
    private void checkComponent(XmlObject componentObj, String component) {
        locSubComponentPosition = -1;
        XmlCursor cur = componentObj.newCursor();

        /* Check usage */
        String usage = cur.getAttributeText(QName.valueOf("Usage"));
        boolean usageError = checkUsage(componentObj, component);

        if (!usageError) {
            if (usage.equals("R") || !component.equals("")) {
                XmlObject[] subcomponentsObj = componentObj.selectChildren(QName.valueOf("SubComponent"));
                if (subcomponentsObj.length > 0) {
                    if (((Er7Message) message).getSubComponentSeparatorChar().equals(
                            "")) {
                        MessageFailureV2 mf = new MessageFailureV2(
                                message.getEncoding());
                        mf.setFailureType(AssertionTypeV2Constants.DATA);
                        mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                        mf.setDescription("The subcomponent separator is not set. Check MSH.1");
                        mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
                        mf.setLine(lineNumber);
                        mf.setPath(getCurrentLocation().toString());

                        messageFailures.add(mf);
                    } else {
                        String[] subcomponents = component.split(((Er7Message) message).getSubComponentSeparator());
                        if (subcomponents.length > subcomponentsObj.length) {
                            /* Extra subcomponents */
                            MessageFailureV2 mf = new MessageFailureV2(
                                    message.getEncoding());
                            mf.setFailureType(AssertionTypeV2Constants.XTRA);
                            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                            mf.setDescription("Extra subcomponents for component "
                                    + getCurrentLocation().toString());
                            mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
                            mf.setLine(lineNumber);
                            mf.setPath(getCurrentLocation().toString());

                            messageFailures.add(mf);
                        } else {
                            for (int i = 0; i < subcomponentsObj.length; i++) {
                                locSubComponentPosition = i + 1;
                                // mLocation.setSubcompNumber(i + 1);
                                if (i < subcomponents.length) {
                                    checkSubComponent(subcomponentsObj[i],
                                            subcomponents[i]);
                                } else {
                                    /* Check Usage */
                                    checkUsage(subcomponentsObj[i], "");
                                }
                            }
                        }
                    }
                } else {
                    /* Check value */
                    checkValue(componentObj, component);
                }
            }
        }
        // No SubComponents
        if ("R".equals(usage)
                && "".equals(component)
                && componentObj.selectChildren(QName.valueOf("SubComponent")).length > 0) {
            locSubComponentPosition = -1;
            StringBuffer sb = new StringBuffer();
            MessageFailureV2 mf = new MessageFailureV2(message.getEncoding());
            sb.append("The element is missing at least one of its children");
            mf.setDescription(sb.toString());
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            mf.setFailureType(AssertionTypeV2Constants.MESSAGE_STRUCTURE);
            mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
            mf.setLine(lineNumber);
            mf.setPath(getCurrentLocation().toString());
            messageFailures.add(mf);
            sb = null;
        }
    }

    /**
     * Check the subcomponent
     * 
     * @param subcomponentObj
     *        the subcomponent object (from profile)
     * @param subcomponent
     *        the subcomponent string (from ER7)
     */
    private void checkSubComponent(XmlObject subcomponentObj,
            String subcomponent) {
        XmlCursor cur = subcomponentObj.newCursor();
        /* Check usage */
        String usage = cur.getAttributeText(QName.valueOf("Usage"));
        boolean usageError = checkUsage(subcomponentObj, subcomponent);

        if (!usageError) {
            if (usage.equals("R") || !subcomponent.equals("")) {
                /* Check value */
                checkValue(subcomponentObj, subcomponent);
            }
        }
    }

    /**
     * Check the element usage
     * 
     * @param usage
     *        the usage
     * @param value
     *        the value
     * @return true if there is a usage error; false otherwise
     */
    private boolean checkUsage(XmlObject obj, String value) {
        boolean usageError = false;
        MessageFailureV2 mf = null;
        String usage = obj.newCursor().getAttributeText(QName.valueOf("Usage"));

        if (usage.equals("R") && value.matches("\\s*")) {
            /* A required element is empty */
            mf = new MessageFailureV2(message.getEncoding());
            mf.setFailureType(AssertionTypeV2Constants.USAGE);
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            mf.setDescription(getCurrentLocation().toString() + " is missing");
            mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
            mf.setLine(lineNumber);
            mf.setPath(getCurrentLocation().toString());
            messageFailures.add(mf);
            usageError = true;
        } else if (usage.equals("X") && value.matches(".+")) {
            /* A X element has a value */
            mf = new MessageFailureV2(message.getEncoding());
            mf.setFailureType(AssertionTypeV2Constants.X_USAGE);
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            mf.setDescription(getCurrentLocation().toString()
                    + " is present whereas it is an X-Usage element");
            mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
            mf.setLine(lineNumber);
            mf.setPath(getCurrentLocation().toString());
            messageFailures.add(mf);
            usageError = true;
        }
        return usageError;
    }

    /**
     * Check a value
     * 
     * @param obj
     *        the object associated (from profile)
     * @param value
     *        the value from ER7
     */
    private void checkValue(XmlObject obj, String value) {

        XmlCursor cur = obj.newCursor();

        /* Check X usage */
        String usage = cur.getAttributeText(QName.valueOf("Usage"));
        if (!usage.equals("X")) {
            if (!(usage.equals("O") && value.equals(""))) {
                /* Check extra separators */
                if (getCurrentLocation().getSegmentName().equals("MSH")
                        && getCurrentLocation().getFieldPosition() == 2) {
                    /* MSH.2 */
                    checkSeparators(obj, value);
                }
                /* Check length */
                String maxLength = cur.getAttributeText(QName.valueOf("Length"));
                if (maxLength != null) {
                    MessageFailureV2 mf = checkLength(value,
                            Integer.parseInt(maxLength));
                    if (mf != null) {
                        mf.setLine(lineNumber);
                        mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
                        mf.setPath(getCurrentLocation().toString());

                        messageFailures.add(mf);
                    }
                }
                /* Check table */
                String table = cur.getAttributeText(QName.valueOf("Table"));
                if (table != null) {
                    MessageFailureV2 mf = checkTable(value, table);
                    if (mf != null) {
                        mf.setLine(lineNumber);
                        mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
                        mf.setPath(getCurrentLocation().toString());

                        messageFailures.add(mf);
                    }
                }
                /* Check constant value */
                String constant = cur.getAttributeText(QName.valueOf("ConstantValue"));
                if (constant != null) {
                    MessageFailureV2 mf = checkConstant(value, constant);
                    if (mf != null) {
                        mf.setLine(lineNumber);
                        mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
                        mf.setPath(getCurrentLocation().toString());

                        messageFailures.add(mf);
                    }
                }
                /* Check datatype */
                String datatype = cur.getAttributeText(QName.valueOf("Datatype"));
                MessageFailureV2 mf = checkDatatype(value, datatype);
                if (mf != null) {
                    mf.setLine(lineNumber);
                    mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
                    mf.setPath(getCurrentLocation().toString());

                    messageFailures.add(mf);
                }
            }
        }
    }

    /**
     * Check the cardinalities of an element
     * 
     * @param fieldObj
     * @param usage
     *        the usage
     * @param min
     *        the minimum
     * @param max
     *        the maximum
     * @param occurrences
     *        number of occurences of the element
     */
    private void checkCardinalities(XmlObject fieldObj, String usage,
            String min, String max, int occurrences) {

        MessageFailureV2 mf = null;

        if (!usage.equals("X")) {
            int minimum = Integer.parseInt(min);
            if (occurrences < minimum) {
                if (!(usage.equals("RE") && occurrences == 0)) {
                    mf = new MessageFailureV2(message.getEncoding());
                    mf.setFailureType(AssertionTypeV2Constants.CARDINALITY);
                    mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                    mf.setDescription(getCurrentLocation().toString()
                            + " is present " + occurrences
                            + " times whereas it must be present at least "
                            + minimum + " times");
                    mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
                    mf.setLine(lineNumber);
                    mf.setPath(getCurrentLocation().toString());

                    messageFailures.add(mf);
                }
            }
            if (!max.equals("*")) {
                int maximum = Integer.parseInt(max);
                if (occurrences > maximum) {
                    mf = new MessageFailureV2(message.getEncoding());
                    mf.setFailureType(AssertionTypeV2Constants.CARDINALITY);
                    mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                    mf.setDescription(getCurrentLocation().toString()
                            + " is present " + occurrences
                            + " times whereas it is only allowed " + maximum
                            + " times");
                    mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
                    mf.setLine(lineNumber);
                    mf.setPath(getCurrentLocation().toString());

                    messageFailures.add(mf);
                }
            }
        }
    }

    /**
     * Check if there are some extra separators
     * 
     * @param value
     *        the value
     */
    private void checkSeparators(XmlObject obj, String value) {

        MessageFailureV2 mf = null;

        if (!((Er7Message) message).getSubComponentSeparatorChar().equals("")
                && value.contains(((Er7Message) message).getSubComponentSeparatorChar())
                && !(getCurrentLocation().getSegmentName().equals("MSH") && getCurrentLocation().getFieldPosition() == 2)) {

            mf = new MessageFailureV2(message.getEncoding());
            mf.setFailureType(AssertionTypeV2Constants.DATA);
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            mf.setDescription(" Extra separator in value : " + value);
            mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
            mf.setLine(lineNumber);
            mf.setPath(getCurrentLocation().toString());

            messageFailures.add(mf);
        } else if ((value.contains(((Er7Message) message).getComponentSeparatorChar())
                || value.contains(((Er7Message) message).getFieldSeparatorChar()) || value.contains(((Er7Message) message).getRepetitionSeparatorChar()))
                && !(getCurrentLocation().getSegmentName().equals("MSH") && getCurrentLocation().getFieldPosition() == 2)) {
            mf = new MessageFailureV2(message.getEncoding());
            mf.setFailureType(AssertionTypeV2Constants.DATA);
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            mf.setDescription(" Extra separator in value : " + value);
            mf.setColumn(((Er7Message) inputMessage).getColumn(getCurrentLocation()));
            mf.setLine(lineNumber);
            mf.setPath(getCurrentLocation().toString());

            messageFailures.add(mf);
        }
    }

}
