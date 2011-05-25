/*
 * NIST Healthcare Core
 * MessageContentValidationTest.java Aug 17, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.nist.healthcare.core.MalformedMessageException;
import gov.nist.healthcare.core.MalformedProfileException;
import gov.nist.healthcare.core.message.v2.HL7V2Message;
import gov.nist.healthcare.core.message.v2.er7.Er7Message;
import gov.nist.healthcare.core.message.v2.xml.XmlMessage;
import gov.nist.healthcare.core.profile.Profile;
import gov.nist.healthcare.core.validation.message.MissingDependencyException;
import gov.nist.healthcare.core.validation.message.v2.MessageFailureV2;
import gov.nist.healthcare.core.validation.message.v2.MessageValidationContextV2;
import gov.nist.healthcare.core.validation.message.v2.MessageValidationResultV2;
import gov.nist.healthcare.core.validation.message.v2.MessageValidationV2;
import gov.nist.healthcare.data.TableLibraryDocument;
import gov.nist.healthcare.validation.AssertionResultConstants;
import gov.nist.healthcare.validation.AssertionTypeV2Constants;
import gov.nist.healthcare.validation.message.hl7.context.MessageValidationCorrelationDocument;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.JUnit4TestAdapter;
import org.apache.xmlbeans.XmlException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class is a JUnit test class for message content validation
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageContentValidationV2Test {

    private static MessageValidationV2 validator;
    private static MessageValidationContextV2 context;
    private static XmlMessage xmlValid;
    private static Er7Message er7Valid;

    @BeforeClass
    public static void setUp() throws MalformedMessageException {
        // Create a default MessageValidationContext
        context = new MessageValidationContextV2();
        context.setFailureResult(AssertionTypeV2Constants.LENGTH,
                AssertionResultConstants.ERROR);
        // Create the validations
        validator = new MessageValidationV2();
        // Create the valid messages
        xmlValid = new XmlMessage(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/ValidMessage.xml").getFile()));
        er7Valid = new Er7Message(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/ValidMessage.er7").getFile()));
    }

    private MessageValidationResultV2 validate(HL7V2Message message) {
        MessageValidationResultV2 result = validator.validate(message, context);
        result.getReport();
        return result;
    }

    private MessageValidationResultV2 validate(HL7V2Message message,
            List<TableLibraryDocument> tables) {
        MessageValidationResultV2 result = validator.validate(message, context,
                tables);
        result.getReport();
        return result;
    }

    @Test
    public void testXUsageLocation() throws IOException, XmlException {
        context.load(new File(getClass().getResource(
                "/content/v2/MVCXUsage.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlValid);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertTrue(result.isValid());
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);

        result = validate(er7Valid);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertTrue(result.isValid());
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
    }

    @Test
    public void testWrongFieldLocation() throws IOException, XmlException {
        context.load(new File(getClass().getResource(
                "/content/v2/MVCWrongField.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlValid);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertTrue(result.isValid());
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);

        result = validate(er7Valid);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertTrue(result.isValid());
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
    }

    @Test
    public void testWrongComponentLocation() throws IOException, XmlException {
        context.load(new File(getClass().getResource(
                "/content/v2/MVCWrongComponent.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlValid);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertTrue(result.isValid());
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);

        result = validate(er7Valid);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertTrue(result.isValid());
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
    }

    @Test
    public void testWrongSubComponentLocation() throws IOException,
            XmlException {
        context.load(new File(getClass().getResource(
                "/content/v2/MVCWrongSubComponent.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlValid);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertTrue(result.isValid());
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);

        result = validate(er7Valid);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertTrue(result.isValid());
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
    }

    @Test
    public void testCheckField() throws IOException, XmlException {
        context.load(new File(getClass().getResource(
                "/content/v2/MVCCheckField.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlValid);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);

        result = validate(er7Valid);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
    }

    @Test
    public void testNoResult() throws XmlException, IOException {
        context.load(new File(
                getClass().getResource("/content/v2/NoResult.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlValid);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);

        result = validate(er7Valid);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
    }

    @Test
    public void testOneResult() throws XmlException, IOException {
        context.load(new File(getClass().getResource(
                "/content/v2/OneResult.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlValid);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 4);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(1).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(2).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(3).getFailureType(), AssertionTypeV2Constants.DATA);

        result = validate(er7Valid);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 4);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(1).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(2).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(3).getFailureType(), AssertionTypeV2Constants.DATA);
    }

    @Test
    public void testSeveralResult() throws XmlException, IOException {
        context.load(new File(getClass().getResource(
                "/content/v2/SeveralResult.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlValid);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(1).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(2).getFailureType(), AssertionTypeV2Constants.DATA);

        result = validate(er7Valid);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(1).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(2).getFailureType(), AssertionTypeV2Constants.DATA);
    }

    @Test
    public void testCheckAllResult() throws XmlException, IOException {
        context.load(new File(getClass().getResource(
                "/content/v2/CheckAllResult.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlValid);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 0);

        result = validate(er7Valid);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 0);
    }

    @Test
    public void testRepeat() throws IOException, XmlException {
        context.load(new File(getClass().getResource(
                "/content/v2/MVCRepeat.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlValid);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);

        result = validate(er7Valid);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
    }

    @Test
    public void testIfThenElse() throws IOException, XmlException {
        context.load(new File(getClass().getResource(
                "/content/v2/MVCIfThenElse.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlValid);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(1).getFailureType(), AssertionTypeV2Constants.DATA);

        result = validate(er7Valid);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(1).getFailureType(), AssertionTypeV2Constants.DATA);
    }

    @Test
    public void testHDCheck() throws MalformedMessageException, IOException,
            XmlException {
        context.load(new File(getClass().getResource(
                "/content/v2/TestValidHDCheck.xml").getFile()));
        XmlMessage xmlHDCheck = new XmlMessage(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestValidHDCheckMessage.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlHDCheck);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);
        assertEquals(result.isValid(), true);
        Er7Message er7HDCheck = new Er7Message(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestValidHDCheckMessage.er7").getFile()));
        result = validate(er7HDCheck);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);
        assertEquals(result.isValid(), true);

        context.load(new File(getClass().getResource(
                "/content/v2/TestInvalidHDCheck.xml").getFile()));
        xmlHDCheck = new XmlMessage(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidHDCheckMessage.xml").getFile()));
        result = validate(xmlHDCheck);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 4);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(3).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        er7HDCheck = new Er7Message(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidHDCheckMessage.er7").getFile()));
        result = validate(er7HDCheck);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 4);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(3).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
    }

    @Test
    public void testCECheck() throws MalformedMessageException, IOException,
            XmlException {
        List<TableLibraryDocument> tables = new ArrayList<TableLibraryDocument>();
        TableLibraryDocument local = TableLibraryDocument.Factory.parse(getClass().getResourceAsStream(
                "/content/v2/CETable.xml"));
        tables.add(local);
        context.load(new File(getClass().getResource(
                "/content/v2/TestValidCECheck.xml").getFile()));
        XmlMessage xmlCECheck = new XmlMessage(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestValidCECheckMessage.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlCECheck, tables);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);
        assertEquals(result.isValid(), true);
        Er7Message er7CECheck = new Er7Message(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestValidCECheckMessage.er7").getFile()));
        result = validate(er7CECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);
        assertEquals(result.isValid(), true);

        context.load(new File(getClass().getResource(
                "/content/v2/TestInvalidCECheck.xml").getFile()));
        xmlCECheck = new XmlMessage(
                new File(MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidCECheckMessage1.xml").getFile()));
        result = validate(xmlCECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        er7CECheck = new Er7Message(
                new File(MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidCECheckMessage1.er7").getFile()));
        result = validate(er7CECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);

        xmlCECheck = new XmlMessage(
                new File(MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidCECheckMessage2.xml").getFile()));
        result = validate(xmlCECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        er7CECheck = new Er7Message(
                new File(MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidCECheckMessage2.er7").getFile()));
        result = validate(er7CECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);

        xmlCECheck = new XmlMessage(
                new File(MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidCECheckMessage3.xml").getFile()));
        result = validate(xmlCECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(), AssertionTypeV2Constants.DATA);
        er7CECheck = new Er7Message(
                new File(MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidCECheckMessage3.er7").getFile()));
        result = validate(er7CECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(), AssertionTypeV2Constants.DATA);

        xmlCECheck = new XmlMessage(
                new File(MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidCECheckMessage4.xml").getFile()));
        result = validate(xmlCECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(), AssertionTypeV2Constants.DATA);
        er7CECheck = new Er7Message(
                new File(MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidCECheckMessage4.er7").getFile()));
        result = validate(er7CECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(), AssertionTypeV2Constants.DATA);
    }

    @Test
    public void testCWECheck() throws MalformedMessageException, IOException,
            XmlException {
        List<TableLibraryDocument> tables = new ArrayList<TableLibraryDocument>();
        TableLibraryDocument local = TableLibraryDocument.Factory.parse(getClass().getResourceAsStream(
                "/content/v2/CETable.xml"));
        tables.add(local);
        context.load(new File(getClass().getResource(
                "/content/v2/TestValidCWECheck.xml").getFile()));
        XmlMessage xmlCWECheck = new XmlMessage(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestValidCWECheckMessage.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlCWECheck, tables);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);
        assertEquals(result.isValid(), true);
        Er7Message er7CWECheck = new Er7Message(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestValidCWECheckMessage.er7").getFile()));
        result = validate(er7CWECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);
        assertEquals(result.isValid(), true);

        context.load(new File(getClass().getResource(
                "/content/v2/TestInvalidCWECheck1.xml").getFile()));
        xmlCWECheck = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/v2/TestInvalidCWECheckMessage1.xml").getFile()));
        result = validate(xmlCWECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 8);
        for (int i = 0; i < 8; i++) {
            assertEquals(al.get(i).getFailureType(),
                    AssertionTypeV2Constants.DATATYPE);
        }
        er7CWECheck = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/v2/TestInvalidCWECheckMessage1.er7").getFile()));
        result = validate(er7CWECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 8);
        for (int i = 0; i < 8; i++) {
            assertEquals(al.get(i).getFailureType(),
                    AssertionTypeV2Constants.DATATYPE);
        }

        context.load(new File(getClass().getResource(
                "/content/v2/TestInvalidCWECheck2.xml").getFile()));
        xmlCWECheck = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/v2/TestInvalidCWECheckMessage2.xml").getFile()));
        result = validate(xmlCWECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(1).getFailureType(), AssertionTypeV2Constants.DATA);
        er7CWECheck = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/v2/TestInvalidCWECheckMessage2.er7").getFile()));
        result = validate(er7CWECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(1).getFailureType(), AssertionTypeV2Constants.DATA);
    }

    @Test
    public void testCNECheck() throws MalformedMessageException, IOException,
            XmlException {
        List<TableLibraryDocument> tables = new ArrayList<TableLibraryDocument>();
        TableLibraryDocument local = TableLibraryDocument.Factory.parse(getClass().getResourceAsStream(
                "/content/v2/CETable.xml"));
        tables.add(local);
        context.load(new File(getClass().getResource(
                "/content/v2/TestValidCNECheck.xml").getFile()));
        XmlMessage xmlCNECheck = new XmlMessage(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestValidCNECheckMessage.xml").getFile()));
        // MessageValidationResultV2 result = validate(xmlCNECheck, tables);
        // List<MessageFailureV2> al =
        // result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // assertEquals(result.isValid(), true);
        Er7Message er7CNECheck = new Er7Message(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestValidCNECheckMessage.er7").getFile()));
        // result = validate(er7CNECheck, tables);
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // assertEquals(result.isValid(), true);

        context.load(new File(getClass().getResource(
                "/content/v2/TestInvalidCNECheck1.xml").getFile()));
        xmlCNECheck = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/v2/TestInvalidCNECheckMessage1.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlCNECheck, tables);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 11);
        for (int i = 0; i < 11; i++) {
            assertEquals(al.get(i).getFailureType(),
                    AssertionTypeV2Constants.DATATYPE);
        }
        er7CNECheck = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/v2/TestInvalidCNECheckMessage1.er7").getFile()));
        result = validate(er7CNECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 11);
        for (int i = 0; i < 11; i++) {
            assertEquals(al.get(i).getFailureType(),
                    AssertionTypeV2Constants.DATATYPE);
        }

        context.load(new File(getClass().getResource(
                "/content/v2/TestInvalidCNECheck2.xml").getFile()));
        xmlCNECheck = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/v2/TestInvalidCNECheckMessage2.xml").getFile()));
        result = validate(xmlCNECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(1).getFailureType(), AssertionTypeV2Constants.DATA);
        er7CNECheck = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/v2/TestInvalidCNECheckMessage2.er7").getFile()));
        result = validate(er7CNECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(1).getFailureType(), AssertionTypeV2Constants.DATA);
    }

    // @Test
    // public void testXTNCheck() throws MalformedMessageException,
    // IOException,
    // XmlException {
    // context.load(new File(getClass().getResource(
    // "/content/v2/TestValidXTNCheck.xml").getFile()));
    // XmlMessage xmlXTNCheck = new XmlMessage(new File(
    // MessageContentValidationV2Test.class.getResource(
    // "/content/v2/TestValidXTNCheckMessage.xml").getFile()));
    // MessageValidationResultV2 result = validate(xmlXTNCheck);
    // List<MessageFailureV2> al =
    // result.getMessageFailure(AssertionResultConstants.ALERT);
    // assertEquals(al.size(), 0);
    // assertEquals(result.isValid(), true);
    // Er7Message er7XTNCheck = new Er7Message(new File(
    // MessageContentValidationV2Test.class.getResource(
    // "/content/v2/TestValidXTNCheckMessage.er7").getFile()));
    // result = validate(er7XTNCheck);
    // al = result.getMessageFailure(AssertionResultConstants.ALERT);
    // assertEquals(al.size(), 0);
    // assertEquals(result.isValid(), true);
    //
    // context.load(new File(getClass().getResource(
    // "/content/v2/TestInvalidXTNCheck.xml").getFile()));
    // xmlXTNCheck = new XmlMessage(
    // new File(MessageContentValidationV2Test.class.getResource(
    // "/content/v2/TestInvalidXTNCheckMessage.xml").getFile()));
    // result = validate(xmlXTNCheck);
    // al = result.getMessageFailure(AssertionResultConstants.ALERT);
    // assertEquals(al.size(), 3);
    // assertEquals(al.get(0).getFailureType(),
    // AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
    // assertEquals(al.get(1).getFailureType(),
    // AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
    // assertEquals(al.get(2).getFailureType(),
    // AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
    // assertEquals(result.isValid(), false);
    // al = result.getMessageFailure(AssertionResultConstants.ERROR);
    // assertEquals(al.size(), 4);
    // assertEquals(al.get(0).getFailureType(),
    // AssertionTypeV2Constants.DATATYPE);
    // assertEquals(al.get(1).getFailureType(),
    // AssertionTypeV2Constants.DATATYPE);
    // assertEquals(al.get(2).getFailureType(),
    // AssertionTypeV2Constants.DATATYPE);
    // assertEquals(al.get(3).getFailureType(),
    // AssertionTypeV2Constants.DATATYPE);
    // er7XTNCheck = new Er7Message(
    // new File(MessageContentValidationV2Test.class.getResource(
    // "/content/v2/TestInvalidXTNCheckMessage.er7").getFile()));
    // result = validate(er7XTNCheck);
    // al = result.getMessageFailure(AssertionResultConstants.ALERT);
    // assertEquals(al.size(), 3);
    // assertEquals(al.get(0).getFailureType(),
    // AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
    // assertEquals(al.get(1).getFailureType(),
    // AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
    // assertEquals(al.get(2).getFailureType(),
    // AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
    // assertEquals(result.isValid(), false);
    // al = result.getMessageFailure(AssertionResultConstants.ERROR);
    // assertEquals(al.size(), 4);
    // assertEquals(al.get(0).getFailureType(),
    // AssertionTypeV2Constants.DATATYPE);
    // assertEquals(al.get(1).getFailureType(),
    // AssertionTypeV2Constants.DATATYPE);
    // assertEquals(al.get(2).getFailureType(),
    // AssertionTypeV2Constants.DATATYPE);
    // assertEquals(al.get(3).getFailureType(),
    // AssertionTypeV2Constants.DATATYPE);
    // }

    @Test
    public void testPresent() throws XmlException, IOException,
            MalformedMessageException {
        context.load(new File(getClass().getResource(
                "/content/v2/TestPresent.xml").getFile()));
        XmlMessage xmlPresent = new XmlMessage(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestPresentMessage.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlPresent);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);

        Er7Message er7Present = new Er7Message(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestPresentMessage.er7").getFile()));
        result = validate(er7Present);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);
    }

    @Test
    public void testSingleMessageCorrelation() throws IOException,
            XmlException, MalformedMessageException {
        context.load(new File(getClass().getResource(
                "/content/v2/TestSingleMessageCorrelation.xml").getFile()));
        XmlMessage xmlSingleMessageCorrelation = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/v2/TestSingleMessageCorrelationMessage.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlSingleMessageCorrelation);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(1).getFailureType(), AssertionTypeV2Constants.DATA);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);

        Er7Message er7SingleMessageCorrelation = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/v2/TestSingleMessageCorrelationMessage.er7").getFile()));
        result = validate(er7SingleMessageCorrelation);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(1).getFailureType(), AssertionTypeV2Constants.DATA);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 1);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
    }

    @Test
    public void testFineGrainedAssertionResult() throws IOException,
            XmlException, MalformedMessageException {
        context.load(new File(getClass().getResource(
                "/content/v2/TestFineGrainedAssertionResult.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlValid);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.WARNING);
        assertEquals(al.size(), 4);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(1).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(2).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(3).getFailureType(), AssertionTypeV2Constants.DATA);

        result = validate(er7Valid);
        al = result.getMessageFailure(AssertionResultConstants.WARNING);
        assertEquals(al.size(), 4);
        assertEquals(al.get(0).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(1).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(2).getFailureType(), AssertionTypeV2Constants.DATA);
        assertEquals(al.get(3).getFailureType(), AssertionTypeV2Constants.DATA);

        context.load(new File(
                getClass().getResource(
                        "/content/v2/TestFineGrainedAssertionResultInvalidHDCheck.xml").getFile()));
        XmlMessage xmlHDCheck = new XmlMessage(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidHDCheckMessage.xml").getFile()));
        result = validate(xmlHDCheck);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), true);
        al = result.getMessageFailure(AssertionResultConstants.WARNING);
        assertEquals(al.size(), 4);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(3).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        Er7Message er7HDCheck = new Er7Message(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidHDCheckMessage.er7").getFile()));
        result = validate(er7HDCheck);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), true);
        al = result.getMessageFailure(AssertionResultConstants.WARNING);
        assertEquals(al.size(), 4);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(3).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);

        List<TableLibraryDocument> tables = new ArrayList<TableLibraryDocument>();
        TableLibraryDocument local = TableLibraryDocument.Factory.parse(getClass().getResourceAsStream(
                "/content/v2/CETable.xml"));
        tables.add(local);

        context.load(new File(
                getClass().getResource(
                        "/content/v2/TestFineGrainedAssertionResultInvalidCECheck.xml").getFile()));
        XmlMessage xmlCECheck = new XmlMessage(
                new File(MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidCECheckMessage1.xml").getFile()));
        result = validate(xmlCECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), true);
        al = result.getMessageFailure(AssertionResultConstants.WARNING);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        Er7Message er7CECheck = new Er7Message(
                new File(MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidCECheckMessage1.er7").getFile()));
        result = validate(er7CECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), true);
        al = result.getMessageFailure(AssertionResultConstants.WARNING);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);

        xmlCECheck = new XmlMessage(
                new File(MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidCECheckMessage2.xml").getFile()));
        result = validate(xmlCECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), true);
        al = result.getMessageFailure(AssertionResultConstants.WARNING);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        er7CECheck = new Er7Message(
                new File(MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidCECheckMessage2.er7").getFile()));
        result = validate(er7CECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), true);
        al = result.getMessageFailure(AssertionResultConstants.WARNING);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);

        xmlCECheck = new XmlMessage(
                new File(MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidCECheckMessage3.xml").getFile()));
        result = validate(xmlCECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), true);
        al = result.getMessageFailure(AssertionResultConstants.WARNING);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(), AssertionTypeV2Constants.DATA);
        er7CECheck = new Er7Message(
                new File(MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidCECheckMessage3.er7").getFile()));
        result = validate(er7CECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), true);
        al = result.getMessageFailure(AssertionResultConstants.WARNING);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(), AssertionTypeV2Constants.DATA);

        xmlCECheck = new XmlMessage(
                new File(MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidCECheckMessage4.xml").getFile()));
        result = validate(xmlCECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), true);
        al = result.getMessageFailure(AssertionResultConstants.WARNING);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(), AssertionTypeV2Constants.DATA);
        er7CECheck = new Er7Message(
                new File(MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestInvalidCECheckMessage4.er7").getFile()));
        result = validate(er7CECheck, tables);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        assertEquals(result.isValid(), true);
        al = result.getMessageFailure(AssertionResultConstants.WARNING);
        assertEquals(al.size(), 3);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(), AssertionTypeV2Constants.DATA);

        // context.load(new File(
        // getClass().getResource(
        // "/content/v2/TestFineGrainedAssertionResultInvalidXTNCheck.xml").getFile()));
        // XmlMessage xmlXTNCheck = new XmlMessage(
        // new File(MessageContentValidationV2Test.class.getResource(
        // "/content/v2/TestInvalidXTNCheckMessage.xml").getFile()));
        // result = validate(xmlXTNCheck);
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 3);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        // assertEquals(al.get(2).getFailureType(),
        // AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        // assertEquals(result.isValid(), true);
        // al = result.getMessageFailure(AssertionResultConstants.WARNING);
        // assertEquals(al.size(), 4);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(2).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(3).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // Er7Message er7XTNCheck = new Er7Message(
        // new File(MessageContentValidationV2Test.class.getResource(
        // "/content/v2/TestInvalidXTNCheckMessage.er7").getFile()));
        // result = validate(er7XTNCheck);
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 3);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        // assertEquals(al.get(2).getFailureType(),
        // AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        // assertEquals(result.isValid(), true);
        // al = result.getMessageFailure(AssertionResultConstants.WARNING);
        // assertEquals(al.size(), 4);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(2).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(3).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
    }

    @Test
    public void testPlainTextInterpretAsNumber() throws IOException,
            XmlException {
        context.load(new File(getClass().getResource(
                "/content/v2/TestPlainTextInterpretAsNumber.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlValid);
        assertEquals(result.isValid(), true);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);

        result = validate(er7Valid);
        assertEquals(result.isValid(), true);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);
    }

    @Test
    public void testMultiHD() throws MalformedMessageException, IOException,
            XmlException {
        XmlMessage xmlMultiHDMessage = new XmlMessage(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestMultiHDMessage.xml").getFile()));
        context.load(new File(getClass().getResource(
                "/content/v2/TestMultiHDAnySegmentAnyField.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlMultiHDMessage);
        assertEquals(result.isValid(), false);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 4);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(3).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);

        context.load(new File(getClass().getResource(
                "/content/v2/TestMultiHDAnySegment.xml").getFile()));
        result = validate(xmlMultiHDMessage);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 4);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(3).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);

        context.load(new File(getClass().getResource(
                "/content/v2/TestMultiHDAnyField.xml").getFile()));
        result = validate(xmlMultiHDMessage);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);

        Er7Message er7MultiHDMessage = new Er7Message(new File(
                MessageContentValidationV2Test.class.getResource(
                        "/content/v2/TestMultiHDMessage.er7").getFile()));
        context.load(new File(getClass().getResource(
                "/content/v2/TestMultiHDAnySegmentAnyField.xml").getFile()));
        result = validate(er7MultiHDMessage);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 4);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(3).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);

        context.load(new File(getClass().getResource(
                "/content/v2/TestMultiHDAnySegment.xml").getFile()));
        result = validate(er7MultiHDMessage);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 4);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(2).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(3).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);

        context.load(new File(getClass().getResource(
                "/content/v2/TestMultiHDAnyField.xml").getFile()));
        result = validate(er7MultiHDMessage);
        assertEquals(result.isValid(), false);
        al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 2);
        assertEquals(al.get(0).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        assertEquals(al.get(1).getFailureType(),
                AssertionTypeV2Constants.DATATYPE);
        al = result.getMessageFailure(AssertionResultConstants.ALERT);
        assertEquals(al.size(), 0);
    }

    @Test
    public void testMessageCorrelation() throws IOException, XmlException,
            MalformedMessageException, MalformedProfileException,
            MissingDependencyException {
        // Empty Context
        context.load(new File(getClass().getResource(
                "/content/v2/EmptyContext.xml").getFile()));
        // Correlation Document
        MessageValidationCorrelationDocument correlationDoc = MessageValidationCorrelationDocument.Factory.parse(MessageContentValidationV2Test.class.getResourceAsStream("/content/v2/TestMultiMessageCorrelationDocument.xml"));
        // List of messages
        Map<String, HL7V2Message> messages = new HashMap<String, HL7V2Message>();
        Er7Message query = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/v2/TestMultiMessageCorrelationQuery.er7").getFile()));
        Er7Message response = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/v2/TestMultiMessageCorrelationResponse.er7").getFile()));
        messages.put("query", query);
        messages.put("response", response);
        // Profile
        Profile profile = new Profile(getClass().getResourceAsStream(
                "/content/v2/NIST_RSP_K23.xml"));
        context.addValidationChecks(correlationDoc, messages, profile);
        MessageValidationResultV2 result = validate(response);
        assertEquals(result.isValid(), false);
        List<MessageFailureV2> al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 13);
        for (MessageFailureV2 mf : al) {
            assertEquals(mf.getFailureType(), AssertionTypeV2Constants.DATA);
        }
    }

    @AfterClass
    public static void tearDown() {
        validator = null;
        context = null;
        xmlValid = null;
        er7Valid = null;
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(MessageContentValidationV2Test.class);
    }

}
