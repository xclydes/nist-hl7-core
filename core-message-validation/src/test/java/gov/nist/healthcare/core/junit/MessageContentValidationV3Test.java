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
import gov.nist.healthcare.core.MalformedMessageException;
import gov.nist.healthcare.core.message.v3.HL7V3Message;
import gov.nist.healthcare.core.validation.message.MissingDependencyException;
import gov.nist.healthcare.core.validation.message.v3.MessageFailureV3;
import gov.nist.healthcare.core.validation.message.v3.MessageValidationContextV3;
import gov.nist.healthcare.core.validation.message.v3.MessageValidationResultV3;
import gov.nist.healthcare.core.validation.message.v3.MessageValidationV3;
import gov.nist.healthcare.validation.AssertionResultConstants;
import gov.nist.healthcare.validation.AssertionTypeV3Constants;
import gov.nist.healthcare.validation.message.hl7.context.MessageValidationCorrelationDocument;
import java.io.File;
import java.io.IOException;
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
public class MessageContentValidationV3Test {

    private static MessageValidationV3 validator;
    private static MessageValidationContextV3 context;
    private static HL7V3Message v3Valid;

    @BeforeClass
    public static void setUp() throws MalformedMessageException {
        // Create a default MessageValidationContext
        context = new MessageValidationContextV3();
        // Create the validation
        validator = new MessageValidationV3();
        // Create the valid message
        v3Valid = new HL7V3Message(new File(
                MessageContentValidationV3Test.class.getResource(
                        "/content/v3/ValidV3Message.xml").getFile()));
    }

    private MessageValidationResultV3 validate(HL7V3Message message) {
        return validator.validate(message, context);
    }

    @Test
    public void testNoResult() throws XmlException, IOException {
        context.load(new File(getClass().getResource(
                "/content/v3/NoResultV3.xml").getFile()));
        MessageValidationResultV3 resultV3 = validate(v3Valid);
        List<MessageFailureV3> alV3 = resultV3.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(alV3.size(), 2);
        assertEquals(alV3.get(0).getFailureType(),
                AssertionTypeV3Constants.DATA);
        assertEquals(alV3.get(1).getFailureType(),
                AssertionTypeV3Constants.DATA);
    }

    @Test
    public void testOneResult() throws XmlException, IOException {
        context.load(new File(getClass().getResource(
                "/content/v3/OneResultV3.xml").getFile()));
        MessageValidationResultV3 resultV3 = validate(v3Valid);
        List<MessageFailureV3> alV3 = resultV3.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(alV3.size(), 3);
        assertEquals(alV3.get(0).getFailureType(),
                AssertionTypeV3Constants.DATA);
        assertEquals(alV3.get(1).getFailureType(),
                AssertionTypeV3Constants.DATA);
        assertEquals(alV3.get(2).getFailureType(),
                AssertionTypeV3Constants.DATA);
    }

    @Test
    public void testSeveralResult() throws XmlException, IOException {
        context.load(new File(getClass().getResource(
                "/content/v3/SeveralResultV3.xml").getFile()));
        MessageValidationResultV3 resultV3 = validate(v3Valid);
        List<MessageFailureV3> alV3 = resultV3.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(alV3.size(), 3);
        assertEquals(alV3.get(0).getFailureType(),
                AssertionTypeV3Constants.DATA);
        assertEquals(alV3.get(1).getFailureType(),
                AssertionTypeV3Constants.DATA);
        assertEquals(alV3.get(2).getFailureType(),
                AssertionTypeV3Constants.DATA);
    }

    @Test
    public void testCheckAllResult() throws XmlException, IOException {
        context.load(new File(getClass().getResource(
                "/content/v3/CheckAllResultV3.xml").getFile()));
        MessageValidationResultV3 resultV3 = validate(v3Valid);
        List<MessageFailureV3> alV3 = resultV3.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(alV3.size(), 0);
    }

    @Test
    public void testIfThenElse() throws IOException, XmlException {
        context.load(new File(getClass().getResource(
                "/content/v3/IfThenElseV3.xml").getFile()));
        MessageValidationResultV3 resultV3 = validate(v3Valid);
        List<MessageFailureV3> alV3 = resultV3.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(alV3.size(), 4);
        assertEquals(alV3.get(0).getFailureType(),
                AssertionTypeV3Constants.DATA);
        assertEquals(alV3.get(1).getFailureType(),
                AssertionTypeV3Constants.DATA);
        assertEquals(alV3.get(2).getFailureType(),
                AssertionTypeV3Constants.DATA);
        assertEquals(alV3.get(3).getFailureType(),
                AssertionTypeV3Constants.DATA);
    }

    @Test
    public void testCount() throws IOException, XmlException {
        context.load(new File(getClass().getResource(
                "/content/v3/countV3mvc.xml").getFile()));
        MessageValidationResultV3 resultV3 = validate(v3Valid);
        List<MessageFailureV3> alV3 = resultV3.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(alV3.size(), 1);
    }

    @Test
    public void testMessageCorrelation() throws IOException, XmlException,
            MalformedMessageException, MissingDependencyException {
        // Empty Context
        context.load(new File(getClass().getResource(
                "/content/v3/EmptyContext.xml").getFile()));
        // Correlation Document
        MessageValidationCorrelationDocument correlationDoc = MessageValidationCorrelationDocument.Factory.parse(MessageContentValidationV2Test.class.getResourceAsStream("/content/v3/TestMultiMessageCorrelationDocument.xml"));
        // List of messages
        Map<String, HL7V3Message> messages = new HashMap<String, HL7V3Message>();
        HL7V3Message query = new HL7V3Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/v3/TestMultiMessageCorrelationQuery.xml").getFile()));
        HL7V3Message response = new HL7V3Message(
                new File(
                        MessageContentValidationV2Test.class.getResource(
                                "/content/v3/TestMultiMessageCorrelationResponse.xml").getFile()));
        messages.put("query", query);
        messages.put("response", response);
        context.addValidationChecks(correlationDoc, messages);
        MessageValidationResultV3 result = validate(response);
        assertEquals(result.isValid(), false);
        List<MessageFailureV3> al = result.getMessageFailure(AssertionResultConstants.ERROR);
        assertEquals(al.size(), 13);
        for (MessageFailureV3 mf : al) {
            assertEquals(mf.getFailureType(), AssertionTypeV3Constants.DATA);
        }
    }

    @AfterClass
    public static void tearDown() {
        validator = null;
        context = null;
        v3Valid = null;
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(MessageContentValidationV3Test.class);
    }

}
