/*
 * NIST Healthcare Core
 * MessageValidationTest.java Jul 25, 2008
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.junit;

import static org.junit.Assert.assertEquals;
import gov.nist.healthcare.core.Constants;
import gov.nist.healthcare.core.MalformedMessageException;
import gov.nist.healthcare.core.MalformedProfileException;
import gov.nist.healthcare.core.message.v2.HL7V2Message;
import gov.nist.healthcare.core.message.v2.er7.Er7Message;
import gov.nist.healthcare.core.message.v2.xml.XmlMessage;
import gov.nist.healthcare.core.profile.Profile;
import gov.nist.healthcare.core.validation.message.MessageValidationException;
import gov.nist.healthcare.core.validation.message.v2.MessageFailureV2;
import gov.nist.healthcare.core.validation.message.v2.MessageValidationContextV2;
import gov.nist.healthcare.core.validation.message.v2.MessageValidationResultV2;
import gov.nist.healthcare.core.validation.message.v2.MessageValidationV2;
import gov.nist.healthcare.data.TableLibraryDocument;
import gov.nist.healthcare.validation.AssertionResultConstants;
import gov.nist.healthcare.validation.AssertionTypeV2Constants;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.apache.xmlbeans.XmlException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class is a JUnit test class for MessageValidation
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageStructureValidationV2Test {

    private static Profile profile;
    private static Profile profileForSegmentGroupXUsage;
    private static Profile profileForSegmentXUsage;
    private static Profile profileForMissingChild;
    private static MessageValidationContextV2 context;
    private static TableLibraryDocument tableLibraryDocument;
    private static MessageValidationV2 validator;

    @BeforeClass
    public static void setUp() throws MalformedProfileException, XmlException,
            IOException {
        // Create the profiles
        profile = new Profile(
                MessageStructureValidationV2Test.class.getResourceAsStream("/structure/v2/RSP_K22.xml"));
        profileForSegmentGroupXUsage = new Profile(
                MessageStructureValidationV2Test.class.getResourceAsStream("/structure/v2/RSP_K22SegmentGroup.xml"));
        profileForSegmentXUsage = new Profile(
                MessageStructureValidationV2Test.class.getResourceAsStream("/structure/v2/RSP_K22Segment.xml"));
        profileForMissingChild = new Profile(
                MessageStructureValidationV2Test.class.getResourceAsStream("/structure/v2/RSP_K22MissingChild.xml"));
        // Create a default MessageValidationContext
        context = new MessageValidationContextV2();
        context.setFailureResult(AssertionTypeV2Constants.LENGTH,
                AssertionResultConstants.ERROR);
        // Load local table file
        tableLibraryDocument = TableLibraryDocument.Factory.parse(new File(
                MessageStructureValidationV2Test.class.getResource(
                        "/structure/v2/LocalTable.xml").getFile()));
        // Create the validations
        validator = new MessageValidationV2();
    }

    private MessageValidationResultV2 validate(HL7V2Message message)
            throws MessageValidationException, XmlException, IOException {
        List<TableLibraryDocument> tableLibraryDocuments = new ArrayList<TableLibraryDocument>();
        tableLibraryDocuments.add(tableLibraryDocument);
        tableLibraryDocuments.add(TableLibraryDocument.Factory.parse(MessageStructureValidationV2Test.class.getResourceAsStream(Constants.getHl7Tables(profile.getHl7VersionAsString()))));
        MessageValidationResultV2 result = validator.validate(message, profile,
                context, tableLibraryDocuments);

        return result;
    }

    private MessageValidationResultV2 validateWithOtherProfile(Profile p,
            HL7V2Message message) throws MessageValidationException,
            XmlException, IOException {
        List<TableLibraryDocument> tableLibraryDocuments = new ArrayList<TableLibraryDocument>();
        tableLibraryDocuments.add(tableLibraryDocument);
        tableLibraryDocuments.add(TableLibraryDocument.Factory.parse(MessageStructureValidationV2Test.class.getResourceAsStream(Constants.getHl7Tables(profile.getHl7VersionAsString()))));
        MessageValidationResultV2 result = validator.validate(message, p,
                context, tableLibraryDocuments);
        result.getReport();
        return result;
    }

    private MessageValidationResultV2 validateWithOtherTable(
            HL7V2Message message, TableLibraryDocument tableValues)
            throws MessageValidationException, XmlException, IOException {
        List<TableLibraryDocument> tableLibraryDocuments = new ArrayList<TableLibraryDocument>();
        tableLibraryDocuments.add(tableValues);
        tableLibraryDocuments.add(TableLibraryDocument.Factory.parse(MessageStructureValidationV2Test.class.getResourceAsStream(Constants.getHl7Tables(profile.getHl7VersionAsString()))));
        MessageValidationResultV2 result = validator.validate(message, profile,
                context, tableLibraryDocuments);
        result.getReport();
        return result;
    }

    private MessageValidationResultV2 validateWithOtherContext(
            HL7V2Message message, MessageValidationContextV2 context)
            throws MessageValidationException, XmlException, IOException {
        List<TableLibraryDocument> tableLibraryDocuments = new ArrayList<TableLibraryDocument>();
        tableLibraryDocuments.add(tableLibraryDocument);
        tableLibraryDocuments.add(TableLibraryDocument.Factory.parse(MessageStructureValidationV2Test.class.getResourceAsStream(Constants.getHl7Tables(profile.getHl7VersionAsString()))));
        MessageValidationResultV2 result = validator.validate(message, profile,
                context, tableLibraryDocuments);
        result.getReport();
        return result;
    }

    private MessageValidationResultV2 validateWithOtherContextAndTable(
            HL7V2Message message, MessageValidationContextV2 context,
            TableLibraryDocument tableValues)
            throws MessageValidationException, XmlException, IOException {
        List<TableLibraryDocument> tableLibraryDocuments = new ArrayList<TableLibraryDocument>();
        tableLibraryDocuments.add(tableValues);
        tableLibraryDocuments.add(TableLibraryDocument.Factory.parse(MessageStructureValidationV2Test.class.getResourceAsStream(Constants.getHl7Tables(profile.getHl7VersionAsString()))));
        MessageValidationResultV2 result = validator.validate(message, profile,
                context, tableLibraryDocuments);
        result.getReport();
        return result;
    }

    @Test
    public void testXMLValid() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        XmlMessage xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestValid.xml").getFile()));
        MessageValidationResultV2 mvr = validate(xml);
        // Iterator<MessageFailureV2> errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        assertEquals(mvr.isValid(), true);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testXMLVersion() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        XmlMessage xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestVersion.xml").getFile()));
        MessageValidationResultV2 mvr = validate(xml);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.VERSION);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testXMLMessageType() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        XmlMessage xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestMessageCode.xml").getFile()));
        MessageValidationResultV2 mvr = validate(xml);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE_ID);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestTriggerEvent.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE_ID);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestMessageStructureId.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE_ID);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testXMLRUsage() throws MessageValidationException,
            XmlException, IOException, MalformedMessageException {
        XmlMessage xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestMissingRequiredSegmentGroup.xml").getFile()));
        MessageValidationResultV2 mvr = validate(xml);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestMissingRequiredSegment.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 4);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(3).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestMissingRequiredSegmentWithGroup.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestMissingRequiredField.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestMissingRequiredFieldWithGroup.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestMissingRequiredComponent.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestMissingRequiredComponentWithGroup.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestMissingRequiredSubComponent.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestMissingRequiredSubComponentWithGroup.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(1).getFailureType(), AssertionTypeV2Constants.USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testXMLXUsage() throws MessageValidationException,
            XmlException, IOException, MalformedMessageException {
        XmlMessage xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestPresentUnsupportedSegmentGroup.xml").getFile()));
        MessageValidationResultV2 mvr = validateWithOtherProfile(
                profileForSegmentGroupXUsage, xml);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestPresentUnsupportedSegment.xml").getFile()));
        mvr = validateWithOtherProfile(profileForSegmentXUsage, xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestPresentUnsupportedSegmentWithGroup.xml").getFile()));
        mvr = validateWithOtherProfile(profileForSegmentXUsage, xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestPresentUnsupportedField.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.X_USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestPresentUnsupportedFieldWithGroup.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.X_USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestPresentUnsupportedComponent.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.X_USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestPresentUnsupportedComponentWithGroup.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.X_USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestPresentUnsupportedSubComponent.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.X_USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestPresentUnsupportedSubComponentWithGroup.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.X_USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

    }

    @Test
    public void testXMLCardinality() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        XmlMessage xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestMinCardinality.xml").getFile()));
        MessageValidationResultV2 mvr = validate(xml);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.CARDINALITY);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestMaxCardinality.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.CARDINALITY);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestRECardinality.xml").getFile()));
        mvr = validate(xml);
        assertEquals(mvr.isValid(), true);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testXMLLength() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        XmlMessage xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestLength.xml").getFile()));
        MessageValidationResultV2 mvr = validate(xml);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.LENGTH);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testXMLTable() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        XmlMessage xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestTable.xml").getFile()));
        MessageValidationResultV2 mvr = validate(xml);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testXMLConstant() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        XmlMessage xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestConstant.xml").getFile()));
        MessageValidationResultV2 mvr = validate(xml);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testXMLDatatype() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        XmlMessage xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestDTMDatatype.xml").getFile()));
        MessageValidationResultV2 mvr = validate(xml);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestTMDatatype.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestDTDatatype.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestSIDatatype.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestNMDatatype.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestTNDatatype.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testXMLNonPrimitiveElement() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        XmlMessage xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestValueInNonPrimitiveElementSegmentGroup.xml").getFile()));
        MessageValidationResultV2 mvr = validate(xml);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestValueInNonPrimitiveElementSegment.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestValueInNonPrimitiveElementSegmentWithGroup.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestValueInNonPrimitiveElementField.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestValueInNonPrimitiveElementFieldWithGroup.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestValueInNonPrimitiveElementComponent.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestValueInNonPrimitiveElementComponentWithGroup.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testXMLRootElement() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        XmlMessage xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestRootElement.xml").getFile()));
        MessageValidationResultV2 mvr = validate(xml);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testXMLEmptyValue() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        XmlMessage xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestEmptyValueInField.xml").getFile()));
        MessageValidationResultV2 mvr = validate(xml);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestEmptyValueInFieldWithGroup.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestEmptyValueInComponent.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(
                new File(
                        getClass().getResource(
                                "/structure/v2/xml/TestEmptyValueInComponentWithGroup.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testXMLMissingChild() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        XmlMessage xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestMissingChildSegment.xml").getFile()));
        MessageValidationResultV2 mvr = validateWithOtherProfile(
                profileForMissingChild, xml);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestMissingChildField.xml").getFile()));
        mvr = validateWithOtherProfile(profileForMissingChild, xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestMissingChildComponent.xml").getFile()));
        mvr = validateWithOtherProfile(profileForMissingChild, xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testXMLExtraElement() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        XmlMessage xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestExtraSegmentGroup.xml").getFile()));
        MessageValidationResultV2 mvr = validate(xml);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestExtraSegment.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestExtraField.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.XTRA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestExtraComponent.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.XTRA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestExtraSubComponent.xml").getFile()));
        mvr = validate(xml);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.XTRA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testXMLCETable() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        XmlMessage xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestCETable.xml").getFile()));
        MessageValidationResultV2 mvr = validate(xml);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testXMLUserTable() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        XmlMessage xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestValid.xml").getFile()));
        TableLibraryDocument emptyUserTable = TableLibraryDocument.Factory.parse(new File(
                getClass().getResource("/structure/v2/EmptyUserTable.xml").getFile()));
        MessageValidationResultV2 mvr = validateWithOtherTable(xml,
                emptyUserTable);
        assertEquals(mvr.isValid(), true);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testXMLUserTableInContext() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        MessageValidationContextV2 contextUserTable = new MessageValidationContextV2();
        XmlMessage xml = new XmlMessage(new File(getClass().getResource(
                "/structure/v2/xml/TestUserTableMessage.xml").getFile()));
        contextUserTable.load(new File(getClass().getResource(
                "/structure/v2/TestUserTable0300.xml").getFile()));
        MessageValidationResultV2 mvr = validateWithOtherContext(xml,
                contextUserTable);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        contextUserTable.load(new File(getClass().getResource(
                "/structure/v2/TestUserTableAll.xml").getFile()));
        mvr = validateWithOtherContext(xml, contextUserTable);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        contextUserTable.load(new File(getClass().getResource(
                "/structure/v2/TestUserTableNone.xml").getFile()));
        mvr = validateWithOtherContext(xml, contextUserTable);
        assertEquals(mvr.isValid(), true);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        TableLibraryDocument emptyUserTable = TableLibraryDocument.Factory.parse(new File(
                getClass().getResource("/structure/v2/EmptyUserTable.xml").getFile()));
        contextUserTable.load(new File(getClass().getResource(
                "/structure/v2/TestUserTable0300.xml").getFile()));
        mvr = validateWithOtherContextAndTable(xml, contextUserTable,
                emptyUserTable);
        assertEquals(mvr.isValid(), true);
        al = mvr.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 6);
        for (int i = 0; i < al.size(); i++) {
            assertEquals(al.get(i).getFailureType(),
                    AssertionTypeV2Constants.VALIDATION_CONFIGURATION);
        }

        contextUserTable.load(new File(getClass().getResource(
                "/structure/v2/TestUserTableAll.xml").getFile()));
        mvr = validateWithOtherContextAndTable(xml, contextUserTable,
                emptyUserTable);
        assertEquals(mvr.isValid(), true);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        contextUserTable.load(new File(getClass().getResource(
                "/structure/v2/TestUserTableNone.xml").getFile()));
        mvr = validateWithOtherContextAndTable(xml, contextUserTable,
                emptyUserTable);
        assertEquals(mvr.isValid(), true);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testER7Valid() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        Er7Message er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestValid.er7").getFile()));
        MessageValidationResultV2 mvr = validate(er7);
        // Iterator<MessageFailureV2> errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        assertEquals(mvr.isValid(), true);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testER7Version() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        Er7Message er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestVersion.er7").getFile()));
        MessageValidationResultV2 mvr = validate(er7);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.VERSION);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testER7MessageType() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        Er7Message er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestMessageCode.er7").getFile()));
        MessageValidationResultV2 mvr = validate(er7);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE_ID);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestTriggerEvent.er7").getFile()));
        mvr = validate(er7);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE_ID);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestMessageStructureId.er7").getFile()));
        mvr = validate(er7);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE_ID);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testER7RUsage() throws MessageValidationException,
            XmlException, IOException, MalformedMessageException {
        Er7Message er7 = new Er7Message(
                new File(
                        getClass().getResource(
                                "/structure/v2/er7/TestMissingRequiredSegmentGroup.er7").getFile()));
        MessageValidationResultV2 mvr = validate(er7);
        // Iterator<MessageFailureV2> errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestMissingRequiredSegment.er7").getFile()));
        mvr = validate(er7);
        // Iterator<MessageFailureV2> errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 4);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(3).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(
                new File(
                        getClass().getResource(
                                "/structure/v2/er7/TestMissingRequiredSegmentWithGroup.er7").getFile()));
        mvr = validate(er7);
        // errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestMissingRequiredField.er7").getFile()));
        mvr = validate(er7);
        // errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(
                new File(
                        getClass().getResource(
                                "/structure/v2/er7/TestMissingRequiredFieldWithGroup.er7").getFile()));
        mvr = validate(er7);
        // errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(
                new File(
                        getClass().getResource(
                                "/structure/v2/er7/TestMissingRequiredComponent.er7").getFile()));
        mvr = validate(er7);
        // errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(
                new File(
                        getClass().getResource(
                                "/structure/v2/er7/TestMissingRequiredComponentWithGroup.er7").getFile()));
        mvr = validate(er7);
        // errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(
                new File(
                        getClass().getResource(
                                "/structure/v2/er7/TestMissingRequiredSubComponent.er7").getFile()));
        mvr = validate(er7);
        // errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(
                new File(
                        getClass().getResource(
                                "/structure/v2/er7/TestMissingRequiredSubComponentWithGroup.er7").getFile()));
        mvr = validate(er7);
        // errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.USAGE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testER7XUsage() throws MessageValidationException,
            XmlException, IOException, MalformedMessageException {
        Er7Message er7 = new Er7Message(
                new File(
                        getClass().getResource(
                                "/structure/v2/er7/TestPresentUnsupportedSegmentGroup.er7").getFile()));
        MessageValidationResultV2 mvr = validateWithOtherProfile(
                profileForSegmentGroupXUsage, er7);
        // Iterator<MessageFailureV2> errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(
                new File(
                        getClass().getResource(
                                "/structure/v2/er7/TestPresentUnsupportedSegment.er7").getFile()));
        mvr = validateWithOtherProfile(profileForSegmentXUsage, er7);
        // Iterator<MessageFailureV2> errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(
                new File(
                        getClass().getResource(
                                "/structure/v2/er7/TestPresentUnsupportedSegmentWithGroup.er7").getFile()));
        mvr = validateWithOtherProfile(profileForSegmentXUsage, er7);
        // Iterator<MessageFailureV2> errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestPresentUnsupportedField.er7").getFile()));
        mvr = validate(er7);
        // Iterator<MessageFailureV2> errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.X_USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(
                new File(
                        getClass().getResource(
                                "/structure/v2/er7/TestPresentUnsupportedFieldWithGroup.er7").getFile()));
        mvr = validate(er7);
        // Iterator<MessageFailureV2> errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.X_USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(
                new File(
                        getClass().getResource(
                                "/structure/v2/er7/TestPresentUnsupportedComponent.er7").getFile()));
        mvr = validate(er7);
        // Iterator<MessageFailureV2> errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.X_USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(
                new File(
                        getClass().getResource(
                                "/structure/v2/er7/TestPresentUnsupportedComponentWithGroup.er7").getFile()));
        mvr = validate(er7);
        // Iterator<MessageFailureV2> errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.X_USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(
                new File(
                        getClass().getResource(
                                "/structure/v2/er7/TestPresentUnsupportedSubComponent.er7").getFile()));
        mvr = validate(er7);
        // Iterator<MessageFailureV2> errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.X_USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(
                new File(
                        getClass().getResource(
                                "/structure/v2/er7/TestPresentUnsupportedSubComponentWithGroup.er7").getFile()));
        mvr = validate(er7);
        // Iterator<MessageFailureV2> errors = mvr.getErrors();
        // while (errors.hasNext()) {
        // MessageFailureV2 mf = errors.next();
        // System.out.println(mf.getLocationForReport());
        // System.out.println(mf.getDescription());
        // }
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.X_USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

    }

    @Test
    public void testER7Cardinality() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        Er7Message er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestMinCardinality.er7").getFile()));
        MessageValidationResultV2 mvr = validate(er7);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.CARDINALITY);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestMaxCardinality.er7").getFile()));
        mvr = validate(er7);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.CARDINALITY);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestRECardinality.er7").getFile()));
        mvr = validate(er7);
        assertEquals(mvr.isValid(), true);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testER7Length() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        Er7Message er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestLength.er7").getFile()));
        MessageValidationResultV2 mvr = validate(er7);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.LENGTH);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testER7Table() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        Er7Message er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestTable.er7").getFile()));
        MessageValidationResultV2 mvr = validate(er7);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testER7Constant() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        Er7Message er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestConstant.er7").getFile()));
        MessageValidationResultV2 mvr = validate(er7);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testER7Datatype() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        Er7Message er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestDTMDatatype.er7").getFile()));
        MessageValidationResultV2 mvr = validate(er7);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestTMDatatype.er7").getFile()));
        mvr = validate(er7);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestDTDatatype.er7").getFile()));
        mvr = validate(er7);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestSIDatatype.er7").getFile()));
        mvr = validate(er7);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestNMDatatype.er7").getFile()));
        mvr = validate(er7);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestTNDatatype.er7").getFile()));
        mvr = validate(er7);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testER7MissingChild() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        Er7Message er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestMissingChildSegment.er7").getFile()));
        MessageValidationResultV2 mvr = validateWithOtherProfile(
                profileForMissingChild, er7);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestMissingChildField.er7").getFile()));
        mvr = validateWithOtherProfile(profileForMissingChild, er7);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestMissingChildComponent.er7").getFile()));
        mvr = validateWithOtherProfile(profileForMissingChild, er7);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.USAGE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testER7ExtraElement() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        Er7Message er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestExtraSegment.er7").getFile()));
        MessageValidationResultV2 mvr = validate(er7);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestExtraField.er7").getFile()));
        mvr = validate(er7);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.XTRA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestExtraComponent.er7").getFile()));
        mvr = validate(er7);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.XTRA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestExtraSubComponent.er7").getFile()));
        mvr = validate(er7);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.XTRA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testER7CETable() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        Er7Message er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestCETable.er7").getFile()));
        MessageValidationResultV2 mvr = validate(er7);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testER7UserTable() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        Er7Message er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestValid.er7").getFile()));
        TableLibraryDocument emptyUserTable = TableLibraryDocument.Factory.parse(new File(
                getClass().getResource("/structure/v2/EmptyUserTable.xml").getFile()));
        MessageValidationResultV2 mvr = validateWithOtherTable(er7,
                emptyUserTable);
        assertEquals(mvr.isValid(), true);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @Test
    public void testER7UserTableInContext() throws MalformedMessageException,
            MessageValidationException, XmlException, IOException {
        MessageValidationContextV2 contextUserTable = new MessageValidationContextV2();
        Er7Message er7 = new Er7Message(new File(getClass().getResource(
                "/structure/v2/er7/TestUserTableMessage.er7").getFile()));
        contextUserTable.load(new File(getClass().getResource(
                "/structure/v2/TestUserTable0300.xml").getFile()));
        MessageValidationResultV2 mvr = validateWithOtherContext(er7,
                contextUserTable);
        List<MessageFailureV2> al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        contextUserTable.load(new File(getClass().getResource(
                "/structure/v2/TestUserTableAll.xml").getFile()));
        mvr = validateWithOtherContext(er7, contextUserTable);
        al = mvr.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(mvr.isValid(), false);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        contextUserTable.load(new File(getClass().getResource(
                "/structure/v2/TestUserTableNone.xml").getFile()));
        mvr = validateWithOtherContext(er7, contextUserTable);
        assertEquals(mvr.isValid(), true);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        TableLibraryDocument emptyUserTable = TableLibraryDocument.Factory.parse(new File(
                getClass().getResource("/structure/v2/EmptyUserTable.xml").getFile()));
        contextUserTable.load(new File(getClass().getResource(
                "/structure/v2/TestUserTable0300.xml").getFile()));
        mvr = validateWithOtherContextAndTable(er7, contextUserTable,
                emptyUserTable);
        assertEquals(mvr.isValid(), true);
        al = mvr.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 6);
        for (int i = 0; i < al.size(); i++) {
            assertEquals(al.get(i).getFailureType(),
                    AssertionTypeV2Constants.VALIDATION_CONFIGURATION);
        }

        contextUserTable.load(new File(getClass().getResource(
                "/structure/v2/TestUserTableAll.xml").getFile()));
        mvr = validateWithOtherContextAndTable(er7, contextUserTable,
                emptyUserTable);
        assertEquals(mvr.isValid(), true);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);

        contextUserTable.load(new File(getClass().getResource(
                "/structure/v2/TestUserTableNone.xml").getFile()));
        mvr = validateWithOtherContextAndTable(er7, contextUserTable,
                emptyUserTable);
        assertEquals(mvr.isValid(), true);
        assertEquals(
                mvr.getMessageFailure(AssertionResultConstants.ALERT).size(), 0);
    }

    @AfterClass
    public static void tearDown() {
        profile = null;
        profileForSegmentGroupXUsage = null;
        profileForSegmentXUsage = null;
        profileForMissingChild = null;
        context = null;
        tableLibraryDocument = null;
        validator = null;
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(MessageStructureValidationV2Test.class);
    }

}
