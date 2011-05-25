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
import gov.nist.healthcare.core.message.v2.HL7V2Message;
import gov.nist.healthcare.core.message.v2.er7.Er7Message;
import gov.nist.healthcare.core.message.v2.xml.XmlMessage;
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
 * This class is a JUnit test class for message content validation
 * 
 * @author Sydney Henrard (NIST)
 */
public class DatatypeCheckPluginTest {

    private static MessageValidationV2 validator;
    private static MessageValidationContextV2 context;

    @BeforeClass
    public static void setUp() {
        // Create a default MessageValidationContext
        context = new MessageValidationContextV2();
        context.setFailureResult(AssertionTypeV2Constants.LENGTH,
                AssertionResultConstants.ERROR);
        // Create the validations
        validator = new MessageValidationV2();
        // Create the valid messages
        // xmlValid = new XmlMessage(new File(
        // ValueCheckPluginTest.class.getResource(
        // "/content/datatypeCheckPlugin/ValidMessage.xml").getFile()));
        // vc = new ValueCheckPlugin();
        // mapper = new ObjectMapper();
        // er7Valid = new Er7Message(
        // new File(DatatypeCheckPluginTest.class.getResource(
        // "/content/valueCheckPlugin/ValidMessage.er7").getFile()));
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

    private void checkResult(MessageValidationResultV2 result, boolean valid,
            int dataPassedCount, int errorCount, int alertCount) {
        assertEquals(result.getAlertCount(), alertCount);
        assertEquals(result.getIgnoreCount(), dataPassedCount);
        assertEquals(result.getWarningCount(), 0);
        assertEquals(result.isValid(), valid);
        assertEquals(result.getErrorCount(), errorCount);
        for (MessageFailureV2 mf : result.getMessageFailure(AssertionResultConstants.AFFIRMATIVE)) {
            assertEquals(mf.getFailureType(), AssertionTypeV2Constants.CHECKED);
        }
        if (result.getIgnoreCount() > 0) {
            for (MessageFailureV2 mf : result.getMessageFailure(AssertionResultConstants.IGNORE)) {
                assertEquals(mf.getFailureType(),
                        AssertionTypeV2Constants.DATA_PASSED);
            }
        }
        if (result.getErrorCount() > 0) {
            for (MessageFailureV2 mf : result.getMessageFailure(AssertionResultConstants.ERROR)) {
                assertTrue(mf.getFailureType() == AssertionTypeV2Constants.DATATYPE
                        || mf.getFailureType() == AssertionTypeV2Constants.DATA);
            }
        }
        if (result.getAlertCount() > 0) {
            for (MessageFailureV2 mf : result.getMessageFailure(AssertionResultConstants.ALERT)) {
                assertEquals(mf.getFailureType(),
                        AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
            }
        }
    }

    @Test
    public void testHDCheck() throws MalformedMessageException, IOException,
            XmlException {
        context.load(new File(getClass().getResource(
                "/content/datatypeCheckPlugin/TestValidHDCheck.xml").getFile()));
        XmlMessage xmlHDCheck = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestValidHDCheckMessage.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlHDCheck);
        checkResult(result, true, 0, 0, 0);

        // List<MessageFailureV2> al =
        // result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // assertEquals(result.isValid(), true);
        Er7Message er7HDCheck = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestValidHDCheckMessage.er7").getFile()));
        result = validate(er7HDCheck);
        checkResult(result, true, 0, 0, 0);
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // assertEquals(result.isValid(), true);

        context.load(new File(
                getClass().getResource(
                        "/content/datatypeCheckPlugin/TestInvalidHDCheck.xml").getFile()));
        xmlHDCheck = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidHDCheckMessage.xml").getFile()));
        result = validate(xmlHDCheck);
        checkResult(result, false, 0, 4, 3);
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
        er7HDCheck = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidHDCheckMessage.er7").getFile()));
        result = validate(er7HDCheck);
        checkResult(result, false, 0, 4, 3);
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
    }

    @Test
    public void testCECheck() throws MalformedMessageException, IOException,
            XmlException {
        List<TableLibraryDocument> tables = new ArrayList<TableLibraryDocument>();
        TableLibraryDocument local = TableLibraryDocument.Factory.parse(getClass().getResourceAsStream(
                "/content/datatypeCheckPlugin/CETable.xml"));
        tables.add(local);
        context.load(new File(getClass().getResource(
                "/content/datatypeCheckPlugin/TestValidCECheck.xml").getFile()));
        XmlMessage xmlCECheck = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestValidCECheckMessage.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlCECheck, tables);
        checkResult(result, true, 0, 0, 0);
        // List<MessageFailureV2> al =
        // result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // assertEquals(result.isValid(), true);
        Er7Message er7CECheck = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestValidCECheckMessage.er7").getFile()));
        result = validate(er7CECheck, tables);
        checkResult(result, true, 0, 0, 0);
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // assertEquals(result.isValid(), true);

        context.load(new File(
                getClass().getResource(
                        "/content/datatypeCheckPlugin/TestInvalidCECheck.xml").getFile()));
        xmlCECheck = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidCECheckMessage1.xml").getFile()));
        result = validate(xmlCECheck, tables);
        checkResult(result, false, 0, 3, 3);
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
        // assertEquals(al.size(), 3);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(2).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        er7CECheck = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidCECheckMessage1.er7").getFile()));
        result = validate(er7CECheck, tables);
        checkResult(result, false, 0, 3, 3);
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
        // assertEquals(al.size(), 3);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(2).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);

        xmlCECheck = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidCECheckMessage2.xml").getFile()));
        result = validate(xmlCECheck, tables);
        checkResult(result, false, 0, 3, 3);
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
        // assertEquals(al.size(), 3);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(2).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        er7CECheck = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidCECheckMessage2.er7").getFile()));
        result = validate(er7CECheck, tables);
        checkResult(result, false, 0, 3, 3);
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
        // assertEquals(al.size(), 3);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(2).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);

        xmlCECheck = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidCECheckMessage3.xml").getFile()));
        result = validate(xmlCECheck, tables);
        checkResult(result, false, 0, 3, 3);
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
        // assertEquals(al.size(), 3);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(2).getFailureType(),
        // AssertionTypeV2Constants.DATA);
        er7CECheck = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidCECheckMessage3.er7").getFile()));
        result = validate(er7CECheck, tables);
        checkResult(result, false, 0, 3, 3);
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
        // assertEquals(al.size(), 3);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(2).getFailureType(),
        // AssertionTypeV2Constants.DATA);

        xmlCECheck = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidCECheckMessage4.xml").getFile()));
        result = validate(xmlCECheck, tables);
        checkResult(result, false, 0, 3, 3);
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
        // assertEquals(al.size(), 3);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(2).getFailureType(),
        // AssertionTypeV2Constants.DATA);
        er7CECheck = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidCECheckMessage4.er7").getFile()));
        result = validate(er7CECheck, tables);
        checkResult(result, false, 0, 3, 3);
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
        // assertEquals(al.size(), 3);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(2).getFailureType(),
        // AssertionTypeV2Constants.DATA);
    }

    @Test
    public void testCWECheck() throws MalformedMessageException, IOException,
            XmlException {
        List<TableLibraryDocument> tables = new ArrayList<TableLibraryDocument>();
        TableLibraryDocument local = TableLibraryDocument.Factory.parse(getClass().getResourceAsStream(
                "/content/datatypeCheckPlugin/CETable.xml"));
        tables.add(local);
        context.load(new File(getClass().getResource(
                "/content/datatypeCheckPlugin/TestValidCWECheck.xml").getFile()));
        XmlMessage xmlCWECheck = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestValidCWECheckMessage.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlCWECheck, tables);
        checkResult(result, true, 0, 0, 0);
        // List<MessageFailureV2> al =
        // result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // assertEquals(result.isValid(), true);
        Er7Message er7CWECheck = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestValidCWECheckMessage.er7").getFile()));
        result = validate(er7CWECheck, tables);
        checkResult(result, true, 0, 0, 0);
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // assertEquals(result.isValid(), true);

        context.load(new File(
                getClass().getResource(
                        "/content/datatypeCheckPlugin/TestInvalidCWECheck1.xml").getFile()));
        xmlCWECheck = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidCWECheckMessage1.xml").getFile()));
        result = validate(xmlCWECheck, tables);
        checkResult(result, false, 0, 8, 0);
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // al = result.getMessageFailure(AssertionResultConstants.ERROR);
        // assertEquals(al.size(), 8);
        // for (int i = 0; i < 8; i++) {
        // assertEquals(al.get(i).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // }
        er7CWECheck = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidCWECheckMessage1.er7").getFile()));
        result = validate(er7CWECheck, tables);
        checkResult(result, false, 0, 8, 0);
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // assertEquals(result.isValid(), false);
        // al = result.getMessageFailure(AssertionResultConstants.ERROR);
        // assertEquals(al.size(), 8);
        // for (int i = 0; i < 8; i++) {
        // assertEquals(al.get(i).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // }

        context.load(new File(
                getClass().getResource(
                        "/content/datatypeCheckPlugin/TestInvalidCWECheck2.xml").getFile()));
        xmlCWECheck = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidCWECheckMessage2.xml").getFile()));
        result = validate(xmlCWECheck, tables);
        checkResult(result, false, 0, 2, 0);
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // assertEquals(result.isValid(), false);
        // al = result.getMessageFailure(AssertionResultConstants.ERROR);
        // assertEquals(al.size(), 2);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.DATA);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.DATA);
        er7CWECheck = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidCWECheckMessage2.er7").getFile()));
        result = validate(er7CWECheck, tables);
        checkResult(result, false, 0, 2, 0);
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // assertEquals(result.isValid(), false);
        // al = result.getMessageFailure(AssertionResultConstants.ERROR);
        // assertEquals(al.size(), 2);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.DATA);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.DATA);
    }

    @Test
    public void testCNECheck() throws MalformedMessageException, IOException,
            XmlException {
        List<TableLibraryDocument> tables = new ArrayList<TableLibraryDocument>();
        TableLibraryDocument local = TableLibraryDocument.Factory.parse(getClass().getResourceAsStream(
                "/content/datatypeCheckPlugin/CETable.xml"));
        tables.add(local);
        context.load(new File(getClass().getResource(
                "/content/datatypeCheckPlugin/TestValidCNECheck.xml").getFile()));
        XmlMessage xmlCNECheck = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestValidCNECheckMessage.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlCNECheck, tables);
        checkResult(result, true, 0, 0, 0);
        // List<MessageFailureV2> al =
        // result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // assertEquals(result.isValid(), true);
        Er7Message er7CNECheck = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestValidCNECheckMessage.er7").getFile()));
        result = validate(er7CNECheck, tables);
        checkResult(result, true, 0, 0, 0);
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // assertEquals(result.isValid(), true);

        context.load(new File(
                getClass().getResource(
                        "/content/datatypeCheckPlugin/TestInvalidCNECheck1.xml").getFile()));
        xmlCNECheck = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidCNECheckMessage1.xml").getFile()));
        result = validate(xmlCNECheck, tables);
        checkResult(result, false, 0, 11, 0);
        // List<MessageFailureV2> al =
        // result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // al = result.getMessageFailure(AssertionResultConstants.ERROR);
        // assertEquals(al.size(), 11);
        // for (int i = 0; i < 11; i++) {
        // assertEquals(al.get(i).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // }
        er7CNECheck = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidCNECheckMessage1.er7").getFile()));
        result = validate(er7CNECheck, tables);
        checkResult(result, false, 0, 11, 0);
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // assertEquals(result.isValid(), false);
        // al = result.getMessageFailure(AssertionResultConstants.ERROR);
        // assertEquals(al.size(), 11);
        // for (int i = 0; i < 11; i++) {
        // assertEquals(al.get(i).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // }

        context.load(new File(
                getClass().getResource(
                        "/content/datatypeCheckPlugin/TestInvalidCNECheck2.xml").getFile()));
        xmlCNECheck = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidCNECheckMessage2.xml").getFile()));
        result = validate(xmlCNECheck, tables);
        checkResult(result, false, 0, 2, 0);
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // assertEquals(result.isValid(), false);
        // al = result.getMessageFailure(AssertionResultConstants.ERROR);
        // assertEquals(al.size(), 2);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.DATA);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.DATA);
        er7CNECheck = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestInvalidCNECheckMessage2.er7").getFile()));
        result = validate(er7CNECheck, tables);
        checkResult(result, false, 0, 2, 0);
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
        // assertEquals(result.isValid(), false);
        // al = result.getMessageFailure(AssertionResultConstants.ERROR);
        // assertEquals(al.size(), 2);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.DATA);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.DATA);
    }

    @Test
    public void testMultiHD() throws MalformedMessageException, IOException,
            XmlException {
        XmlMessage xmlMultiHDMessage = new XmlMessage(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestMultiHDMessage.xml").getFile()));
        context.load(new File(
                getClass().getResource(
                        "/content/datatypeCheckPlugin/TestMultiHDAnySegmentAnyField.xml").getFile()));
        MessageValidationResultV2 result = validate(xmlMultiHDMessage);
        checkResult(result, false, 0, 1, 0);
        // assertEquals(result.isValid(), false);
        // List<MessageFailureV2> al =
        // result.getMessageFailure(AssertionResultConstants.ERROR);
        // assertEquals(al.size(), 4);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(2).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(3).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);

        context.load(new File(
                getClass().getResource(
                        "/content/datatypeCheckPlugin/TestMultiHDAnySegment.xml").getFile()));
        result = validate(xmlMultiHDMessage);
        checkResult(result, false, 0, 2, 0);
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
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);

        context.load(new File(
                getClass().getResource(
                        "/content/datatypeCheckPlugin/TestMultiHDAnyField.xml").getFile()));
        result = validate(xmlMultiHDMessage);
        checkResult(result, false, 0, 1, 0);
        // assertEquals(result.isValid(), false);
        // al = result.getMessageFailure(AssertionResultConstants.ERROR);
        // assertEquals(al.size(), 2);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);

        Er7Message er7MultiHDMessage = new Er7Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/datatypeCheckPlugin/TestMultiHDMessage.er7").getFile()));
        context.load(new File(
                getClass().getResource(
                        "/content/datatypeCheckPlugin/TestMultiHDAnySegmentAnyField.xml").getFile()));
        result = validate(er7MultiHDMessage);
        checkResult(result, false, 0, 1, 0);
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
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);

        context.load(new File(
                getClass().getResource(
                        "/content/datatypeCheckPlugin/TestMultiHDAnySegment.xml").getFile()));
        result = validate(er7MultiHDMessage);
        checkResult(result, false, 0, 2, 0);
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
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);

        context.load(new File(
                getClass().getResource(
                        "/content/datatypeCheckPlugin/TestMultiHDAnyField.xml").getFile()));
        result = validate(er7MultiHDMessage);
        checkResult(result, false, 0, 1, 0);
        // assertEquals(result.isValid(), false);
        // al = result.getMessageFailure(AssertionResultConstants.ERROR);
        // assertEquals(al.size(), 2);
        // assertEquals(al.get(0).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // assertEquals(al.get(1).getFailureType(),
        // AssertionTypeV2Constants.DATATYPE);
        // al = result.getMessageFailure(AssertionResultConstants.ALERT);
        // assertEquals(al.size(), 0);
    }

    @AfterClass
    public static void tearDown() {
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(DatatypeCheckPluginTest.class);
    }

}
