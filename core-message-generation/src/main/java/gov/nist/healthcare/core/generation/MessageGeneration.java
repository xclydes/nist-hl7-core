/*
 * NIST Healthcare Core
 * MessageGeneration.java May 5, 2008
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.generation;

import gov.nist.healthcare.core.MalformedMessageException;
import gov.nist.healthcare.core.encoding.xml2bar.Xml2Bar;
import gov.nist.healthcare.core.generation.MessageGenerationConstants.GenerationError;
import gov.nist.healthcare.core.message.v2.HL7V2Message;
import gov.nist.healthcare.core.profile.Profile;
import gov.nist.healthcare.core.util.XmlBeansUtils;
import gov.nist.healthcare.generation.DataValueLocationItemGeneration;
import gov.nist.healthcare.generation.MessageInstanceSpecificValuesGeneration;
import gov.nist.healthcare.generation.MessagePopulationModule;
import gov.nist.healthcare.generation.MessagePopulationModule.Resource;
import gov.nist.healthcare.generation.message.HL7V2MessageGenerationContextDefinitionDocument;
import gov.nist.healthcare.message.Component;
import gov.nist.healthcare.message.EncodingConstants;
import gov.nist.healthcare.message.Field;
import gov.nist.healthcare.message.MessageElement;
import gov.nist.healthcare.message.Segment;
import gov.nist.healthcare.message.SegmentGroup;
import gov.nist.healthcare.message.SubComponent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.xml.sax.SAXException;

/**
 * This class is an entry-point for the Message Generation
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageGeneration {

    private final SequenceNumberGenerator sng;
    private Transformer xsltGeneration;
    private MessagePopulation mp;
    private boolean generateTimeOfMessage;

    /**
     * Constructor
     */
    public MessageGeneration() {
        sng = new SequenceNumberGenerator();
        generateTimeOfMessage = true;
    }

    /**
     * Set to true to generate the date and time of message automatically. true
     * by default
     * 
     * @param generate
     */
    public void generateTimeOfMessageAuto(boolean generate) {
        generateTimeOfMessage = generate;
    }

    /**
     * Copy a node
     * 
     * @param fromCursor
     * @param toCursor
     * @param empty
     *        true if the value is set to ""
     */
    public void copyNode(XmlCursor fromCursor, XmlCursor toCursor, boolean empty) {
        fromCursor.push();
        toCursor.beginElement(fromCursor.getName());
        if (!fromCursor.toFirstChild()) {
            String text = fromCursor.getTextValue();
            if (text != null) {
                if (!empty) {
                    toCursor.insertChars(text);
                } else {
                    toCursor.insertChars("");
                }
            }
        }
        toCursor.toNextToken();
        fromCursor.pop();
    }

    /**
     * Generate a message based on a profile and a message generation context
     * 
     * @param profile
     *        the profile to use for generation
     * @param context
     *        the message generation context
     * @return a MessageGenerationResult object
     * @throws TransformerException
     * @throws MalformedMessageException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XmlException
     */
    public MessageGenerationResult generate(Profile profile,
            HL7V2MessageGenerationContextDefinitionDocument context)
            throws TransformerException, MalformedMessageException,
            IOException, ParserConfigurationException, SAXException,
            XmlException {
        // Check the data provided
        try {
            checkGenerationData(context);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException(iae.getMessage());
        }
        List<String> errors = new ArrayList<String>();
        // Modify the profile
        profile = (Profile) profile.clone();
        modifyProfile(profile, context);
        // Prepare the transformation
        if (xsltGeneration == null) {
            // StreamSource xsltStream = new StreamSource(
            // new File(
            // getClass().getResource(
            // MessageGenerationConstants.XSLT_PROFILE_TO_ANNOTATED_MESSAGE).getFile()));
            // xsltGeneration = TransformerFactory.newInstance().newTransformer(
            // xsltStream);
            StreamSource xsltStream = new StreamSource(
                    getClass().getResource(
                            MessageGenerationConstants.XSLT_PROFILE_TO_ANNOTATED_MESSAGE).toString());
            xsltGeneration = TransformerFactory.newInstance().newTransformer(
                    xsltStream);
        }

        // Generate the sequence numbers file
        xsltGeneration.setParameter("sequencenumbers",
                sng.getSequenceNumberFile(profile).toURI());
        // Do the transformation to get the annotated message
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamSource src = new StreamSource(
                profile.getDocument().newInputStream());
        xsltGeneration.transform(src, new StreamResult(out));
        AnnotatedMessage annotatedMessage = new AnnotatedMessage(out.toString());
        // Configure the message population object
        mp = new MessagePopulation();
        mp.setTimeOfMessage(generateTimeOfMessage);
        MessagePopulationModule mpModule = context.getHL7V2MessageGenerationContextDefinition().getMessagePopulationModule();
        if (mpModule != null) {
            for (Resource resource : mpModule.getResourceList()) {
                GenerationError error = mp.addResource(resource);
                if (error != null) {
                    if (error == GenerationError.INVALID_RESOURCE) {
                        errors.add(String.format(
                                "The provided resource %s is not valid. It can be either a missing resource or an invalid format.",
                                resource.getSource()));
                    }
                }
            }
        }
        // Use default TableLibraryDocument
        // TableLibraryDocument tableHL7Values =
        // TableLibraryDocument.Factory.parse(MessageGeneration.class.getResourceAsStream(Constants.getHl7Tables(profile.getHl7VersionAsString())));
        // Create a MessagePopulation object
        // mp = new MessagePopulation(primitiveValues, tableValues,
        // tableHL7Values, defaultValues);
        // Populate the message with random data
        mp.populate(annotatedMessage, profile);
        // Set the message control ID
        mp.setMessageControlId(annotatedMessage, false);
        // Set the message separators
        mp.setMessageSeparators(annotatedMessage, false);
        // Set the user fixed data
        MessageInstanceSpecificValuesGeneration misv = context.getHL7V2MessageGenerationContextDefinition().getMessageInstanceSpecificValues();
        if (misv != null) {
            Iterator<DataValueLocationItemGeneration> itDVLI = misv.getDataValueLocationItemList().iterator();
            while (itDVLI.hasNext()) {
                DataValueLocationItemGeneration dvli = itDVLI.next();
                GenerationError error = mp.populateFixedData(annotatedMessage,
                        dvli, true);
                if (error != null) {
                    if (error == GenerationError.NON_EXISTING) {
                        errors.add(String.format(
                                "The provided location is not present in the message. It can be a non existent element or a X-Usage element.\nThe location is %s",
                                dvli.getLocation().xmlText(new XmlOptions())));
                    } else if (error == GenerationError.NON_PRIMITIVE) {
                        errors.add(String.format(
                                "The provided location is not refering to a primitive element.\nThe location is %s",
                                dvli.getLocation().xmlText(new XmlOptions())));
                    }
                }
            }
        }
        // Populate with data from the the profile
        mp.populateProfileData(annotatedMessage, profile, false);
        // Prune the annotated message
        annotatedMessage.prune();
        // Check the encoding, if it is ER7 we have to convert the messages
        HL7V2Message msg = annotatedMessage;
        if (context.getHL7V2MessageGenerationContextDefinition().getEncoding().toString().equals(
                EncodingConstants.ER_7.toString())) {
            msg = Xml2Bar.parse(annotatedMessage);
        }
        List<HL7V2Message> messages = new ArrayList<HL7V2Message>();
        messages.add(msg);
        src.getInputStream().close();
        out.close();
        src = null;
        out = null;
        return new MessageGenerationResult(messages, errors);
    }

    /**
     * Check the context and the data files for primitive, table and default
     * values.
     * 
     * @param context
     * @throws IllegalArgumentException
     *         if the provided files are not syntaxically correct.
     */
    private void checkGenerationData(
            HL7V2MessageGenerationContextDefinitionDocument context) {
        ArrayList<XmlError> validationErrors = new ArrayList<XmlError>();
        XmlOptions validationOptions = new XmlOptions();
        validationOptions.setErrorListener(validationErrors);
        StringBuffer sb = new StringBuffer();
        boolean dataError = false;
        if (!context.validate(validationOptions)) {
            sb.append("The message generation context file is not valid.\n");
            sb.append(XmlBeansUtils.getValidationMessages(validationErrors));
            dataError = true;
        }
        if (dataError) {
            throw new IllegalArgumentException(sb.toString());
        }
    }

    // private void modifyProfile1(Profile profile,
    // HL7V2SimpleMessageGenerationContextDefinitionDocument context) {
    // MessageInstanceSpecificValuesGeneration misv =
    // context.getHL7V2SimpleMessageGenerationContextDefinition().getMessageInstanceSpecificValues();
    // if (misv != null) {
    // for (DataValueLocationItemGeneration dvli :
    // misv.getDataValueLocationItemList()) {
    // String xpath = getXPath(dvli.getLocation());
    // XmlObject[] rs = profile.getDocument().selectPath(xpath);
    // if (rs.length == 1) {
    // int segmentInstanceNumber = -1;
    // XmlCursor cursor = rs[0].newCursor();
    // do {
    // String elementName = cursor.getName().getLocalPart();
    // if ("Field".equals(elementName)) {
    // int fieldInstanceNumber = getInstanceNumber(dvli,
    // "Field");
    // if (fieldInstanceNumber != -1) {
    // String sMax = cursor.getAttributeText(QName.valueOf("Max"));
    // if (sMax != null) {
    // int max = "*".equals(sMax) ? 1000
    // : Integer.parseInt(sMax);
    // if (fieldInstanceNumber <= max) {
    // cursor.setAttributeText(
    // QName.valueOf("Min"),
    // String.valueOf(fieldInstanceNumber));
    // cursor.setAttributeText(
    // QName.valueOf("Max"),
    // String.valueOf(fieldInstanceNumber));
    // }
    // }
    // }
    // cursor.setAttributeText(QName.valueOf("Usage"), "R");
    // } else if ("Segment".equals(elementName)) {
    // segmentInstanceNumber = getInstanceNumber(dvli,
    // "Segment");
    // if (segmentInstanceNumber != -1) {
    // String sMax = cursor.getAttributeText(QName.valueOf("Max"));
    // if (sMax != null) {
    // int max = "*".equals(sMax) ? 1000
    // : Integer.parseInt(sMax);
    // if (segmentInstanceNumber <= max) {
    // cursor.setAttributeText(
    // QName.valueOf("Min"),
    // String.valueOf(segmentInstanceNumber));
    // cursor.setAttributeText(
    // QName.valueOf("Max"),
    // String.valueOf(segmentInstanceNumber));
    // }
    // }
    // }
    // cursor.setAttributeText(QName.valueOf("Usage"), "R");
    // } else if ("SegGroup".equals(elementName)) {
    // segmentGroupInstanceNumber = getInstanceNumber(
    // dvli, "SegGroup");
    // if (segmentGroupInstanceNumber != -1) {
    // String sMax = cursor.getAttributeText(QName.valueOf("Max"));
    // if (sMax != null) {
    // int max = "*".equals(sMax) ? 1000
    // : Integer.parseInt(sMax);
    // if (segmentGroupInstanceNumber <= max) {
    // cursor.setAttributeText(
    // QName.valueOf("Min"),
    // String.valueOf(segmentInstanceNumber));
    // cursor.setAttributeText(
    // QName.valueOf("Max"),
    // String.valueOf(segmentInstanceNumber));
    // }
    // }
    // }
    // cursor.setAttributeText(QName.valueOf("Usage"), "R");
    // }
    // } while (cursor.toParent());
    //
    // cursor.dispose();
    // }
    // }
    // }
    // }

    // if (getInstanceNumber(xpath, dvli.getLocation()) > -1) {
    // if (modifications.containsKey(xpath)
    // && modifications.get(xpath) < getInstanceNumber(
    // xpath, dvli.getLocation())) {
    // modifications.put(xpath, getInstanceNumber(xpath,
    // dvli.getLocation()));
    // }
    // if (!modifications.containsKey(xpath)) {
    // modifications.put(xpath, getInstanceNumber(xpath,
    // dvli.getLocation()));
    // }
    //
    // }

    // private int getInstanceNumber(DataValueLocationItemGeneration dvli,
    // String elementType) {
    // int instanceNumber = -1;
    //
    // return instanceNumber;
    // }

    /**
     * Modify the profile to match the message generation context
     * 
     * @param profile
     *        the profile to modify
     * @param context
     *        the messsage generation context
     */
    private void modifyProfile(Profile profile,
            HL7V2MessageGenerationContextDefinitionDocument context) {
        // Duplicate the Min and Max cardinality
        duplicateMinMaxCardinality(profile);
        // Duplicate the Usage
        duplicateUsage(profile);
        // Modify the Min and Max cardinality based on the context
        modifyMinMaxCardinality(profile, context);
        // Pre-Process the profile for the tables
        preprocessProfileTable(profile);
        // All R-element with all child are O, the first one is set to R
        // XmlObject[] rs1 = profile.getDocument().selectPath(
        // "//*[@Usage = 'R' and count(child::*[@Usage = 'O' or @Usage='RE']) = count(child::*)]");
        XmlObject[] rs1 = profile.getDocument().selectPath(
                "//*[@Usage = 'R' and count(child::*) > 0 and count(child::*[@Usage = 'R']) = 0]");
        for (XmlObject xmlObj : rs1) {
            XmlCursor cursor = xmlObj.newCursor();
            // System.out.println(cursor.getAttributeText(QName.valueOf("Name")));
            boolean child = toNextOptionalChild(cursor);
            if (child) {
                cursor.setAttributeText(QName.valueOf("Usage"), "R");
                if (!"Component".equals(cursor.getName().getLocalPart())
                        && !"SubComponent".equals(cursor.getName().getLocalPart())) {
                    cursor.setAttributeText(QName.valueOf("Min"), "1");
                }
            }
        }
        // All element that are not R are set to X
        XmlObject[] rs = profile.getDocument().selectPath("//*[@Usage!='R']");
        for (XmlObject xmlObj : rs) {
            XmlCursor cursor = xmlObj.newCursor();
            cursor.setAttributeText(QName.valueOf("Usage"), "X");
        }
    }

    /**
     * Move the cursor to the next optional child
     * 
     * @param cursor
     * @return true if there is a child; false otherwise
     */
    private boolean toNextOptionalChild(XmlCursor cursor) {
        boolean child = false;
        String childElement = null;
        if ("Segment".equals(cursor.getName().getLocalPart())) {
            childElement = "Field";
        } else if ("Field".equals(cursor.getName().getLocalPart())) {
            childElement = "Component";
        } else if ("Component".equals(cursor.getName().getLocalPart())) {
            childElement = "SubComponent";
        }
        if (childElement != null) {
            while (cursor.toChild(childElement)) {
                if (!"X".equals(cursor.getAttributeText(QName.valueOf("Usage")))) {
                    child = true;
                    break;
                }
            }
        }
        return child;
    }

    /**
     * Modify the Min and Max cardinality based on the context.
     * 
     * @param profile
     * @param context
     */
    private void modifyMinMaxCardinality(Profile profile,
            HL7V2MessageGenerationContextDefinitionDocument context) {
        Map<String, Integer> modifications = getProfileModifications(context);
        for (String xpath : modifications.keySet()) {
            // System.out.println(xpath + " " + modifications.get(xpath));
            XmlObject[] rs = profile.getDocument().selectPath(xpath);
            // System.out.println(rs.length);
            if (rs.length == 1) {
                int instanceNumber = modifications.get(xpath);
                XmlCursor cursor = rs[0].newCursor();
                if (instanceNumber > 0) {
                    int min = Integer.parseInt(cursor.getAttributeText(QName.valueOf("Min")));
                    String sMax = cursor.getAttributeText(QName.valueOf("Max"));
                    if (sMax != null) {
                        int max = "*".equals(sMax) ? 1000
                                : Integer.parseInt(sMax);
                        if (instanceNumber >= min && instanceNumber <= max) {
                            cursor.setAttributeText(QName.valueOf("Min"),
                                    String.valueOf(instanceNumber));
                            cursor.setAttributeText(QName.valueOf("Max"),
                                    String.valueOf(instanceNumber));
                        } else if (instanceNumber < min) {
                            cursor.setAttributeText(QName.valueOf("Max"),
                                    String.valueOf(min));
                        } else if (instanceNumber > max) {
                            cursor.setAttributeText(QName.valueOf("Min"),
                                    String.valueOf(max));
                        }
                    }
                }
                if (!"X".equals(cursor.getAttributeText(QName.valueOf("Usage")))) {
                    cursor.setAttributeText(QName.valueOf("Usage"), "R");
                }
            }
        }
    }

    /**
     * Duplicate the Min and Max cardinality.
     * 
     * @param profile
     */
    private void duplicateMinMaxCardinality(Profile profile) {
        String xpath = "//.[@Min and @Max]";
        XmlObject[] rs = profile.getDocument().selectPath(xpath);
        for (XmlObject element : rs) {
            XmlCursor cursor = element.newCursor();
            String min = cursor.getAttributeText(QName.valueOf("Min"));
            String max = cursor.getAttributeText(QName.valueOf("Max"));
            cursor.setAttributeText(QName.valueOf("OriginalMin"), min);
            cursor.setAttributeText(QName.valueOf("OriginalMax"), max);
            cursor.dispose();
        }
    }

    /**
     * Duplicate the Min and Max cardinality.
     * 
     * @param profile
     */
    private void duplicateUsage(Profile profile) {
        String xpath = "//.[@Usage]";
        XmlObject[] rs = profile.getDocument().selectPath(xpath);
        for (XmlObject element : rs) {
            XmlCursor cursor = element.newCursor();
            String usage = cursor.getAttributeText(QName.valueOf("Usage"));
            cursor.setAttributeText(QName.valueOf("OriginalUsage"), usage);
            cursor.dispose();
        }
    }

    /**
     * Pre-Process the profile to fix the table express at a field level whereas
     * the field is not primitives.
     */
    private void preprocessProfileTable(Profile profile) {
        // Look for Field with a CE datatype that has components with no table
        String ceXPath = "//.[@Datatype = 'CE' and @Table and count(child::*[position() = 1 and @Table]) = 0]";
        XmlObject[] rs = profile.getDocument().selectPath(ceXPath);
        for (XmlObject ceField : rs) {
            XmlCursor cursor = ceField.newCursor();
            String table = cursor.getAttributeText(QName.valueOf("Table"));
            cursor.toChild("Component");
            cursor.setAttributeText(QName.valueOf("Table"), table);
        }
    }

    /**
     * Get all the modifications to be done to the profile
     * 
     * @param context
     *        a message generation context
     * @return a list of xpath expression
     */
    private Map<String, Integer> getProfileModifications(
            HL7V2MessageGenerationContextDefinitionDocument context) {
        Map<String, Integer> modifications = new HashMap<String, Integer>();
        MessageInstanceSpecificValuesGeneration misv = context.getHL7V2MessageGenerationContextDefinition().getMessageInstanceSpecificValues();
        if (misv != null) {
            for (DataValueLocationItemGeneration dvli : misv.getDataValueLocationItemList()) {
                String xpath = getXPath(dvli.getLocation());
                int instanceNumber = getInstanceNumber(xpath,
                        dvli.getLocation());
                // System.out.println(xpath + " " + instanceNumber);
                if (instanceNumber > -1) {
                    if (modifications.containsKey(xpath)
                            && modifications.get(xpath) < instanceNumber) {
                        modifications.put(xpath, instanceNumber);
                    }
                    if (!modifications.containsKey(xpath)) {
                        modifications.put(xpath, instanceNumber);
                    }
                }
                int idxSlash = 1;
                while ((idxSlash = xpath.indexOf("/", idxSlash + 1)) != -1) {
                    String subXPath = xpath.substring(0, idxSlash);
                    instanceNumber = getInstanceNumber(subXPath,
                            dvli.getLocation());
                    if (instanceNumber > -1) {
                        if (modifications.containsKey(subXPath)
                                && modifications.get(subXPath) < instanceNumber) {
                            modifications.put(subXPath, instanceNumber);
                        }
                        if (!modifications.containsKey(subXPath)) {
                            modifications.put(subXPath, instanceNumber);
                        }
                    }
                }
            }
        }
        return modifications;
    }

    /**
     * Get the xpath expression from a MessageElement
     * 
     * @param location
     *        the location as a MessageElement
     * @return the xpath expression
     */
    private String getXPath(MessageElement location) {
        StringBuffer sb = new StringBuffer("/");
        // SegGroup
        SegmentGroup sg = location.getSegmentGroup();
        Segment s = null;
        while (sg != null) {
            sb.append("/SegGroup[@Name='").append(sg.getName()).append("']");
            if (sg != null) {
                s = sg.getSegment();
            }
            sg = sg.getSegmentGroup();
        }
        if (s == null) {
            s = location.getSegment();
        }
        if (s != null) {
            sb.append("/Segment[@Name='").append(s.getName()).append("']");
            Field f = s.getField();
            if (f != null) {
                sb.append("/Field[").append(f.getPosition()).append("]");
                Component c = f.getComponent();
                if (c != null) {
                    sb.append("/Component[").append(c.getPosition()).append("]");
                    SubComponent sc = c.getSubComponent();
                    if (sc != null) {
                        sb.append("/SubComponent[").append(sc.getPosition()).append(
                                "]");
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Return the instance number
     * 
     * @param xpath
     * @param location
     *        the location as a MessageElement
     * @return the instance number
     */
    private int getInstanceNumber(String xpath, MessageElement location) {
        StringBuffer sb = new StringBuffer();
        int instanceNumber = 1;
        int idxSlash = xpath.lastIndexOf("/");
        String level = xpath.substring(idxSlash + 1);
        Pattern p = Pattern.compile("(\\w*)\\[(.*)\\]");
        Pattern p1 = Pattern.compile("@Name='(\\w*)'");
        Matcher m = p.matcher(level);
        if (m.matches()) {
            String elementType = m.group(1);
            if ("SegGroup".equals(elementType) || "Segment".equals(elementType)) {
                Matcher m1 = p1.matcher(m.group(2));
                if (m1.matches()) {
                    if ("SegGroup".equals(elementType)) {
                        elementType = "SegmentGroup";
                    }
                    sb.delete(0, sb.length());
                    sb.append(".//*:").append(elementType).append("[@Name='").append(
                            m1.group(1)).append("']");
                    XmlObject[] rs = location.selectPath(sb.toString());
                    if (rs.length == 1) {
                        XmlCursor cursor = rs[0].newCursor();
                        String instanceNumberAsString = cursor.getAttributeText(QName.valueOf("InstanceNumber"));
                        if (instanceNumberAsString != null) {
                            instanceNumber = Integer.parseInt(instanceNumberAsString);
                        }
                    }
                }
            } else if ("Field".equals(elementType)) {
                sb.delete(0, sb.length());
                // sb.append("//").append(level);
                sb.append(".//*:").append(elementType);
                XmlObject[] rs = location.selectPath(sb.toString());
                if (rs.length == 1) {
                    XmlCursor cursor = rs[0].newCursor();
                    String instanceNumberAsString = cursor.getAttributeText(QName.valueOf("InstanceNumber"));
                    if (instanceNumberAsString != null) {
                        instanceNumber = Integer.parseInt(instanceNumberAsString);
                    }
                }
            } else {
                instanceNumber = 0;
            }
        }
        return instanceNumber;
    }

}
