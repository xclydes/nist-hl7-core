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
import gov.nist.healthcare.core.message.v2.HL7V2Message;
import gov.nist.healthcare.core.message.v2.er7.Er7Message;
import gov.nist.healthcare.core.validation.message.v2.MessageFailureV2;
import gov.nist.healthcare.core.validation.message.v2.MessageValidationContextV2;
import gov.nist.healthcare.core.validation.message.v2.MessageValidationResultV2;
import gov.nist.healthcare.core.validation.message.v2.MessageValidationV2;
import gov.nist.healthcare.validation.AssertionResultConstants;
import gov.nist.healthcare.validation.AssertionTypeV2Constants;
import java.io.File;
import java.io.IOException;
import junit.framework.JUnit4TestAdapter;
import org.apache.xmlbeans.XmlException;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class is a JUnit test class for message content validation
 * 
 * @author Sydney Henrard (NIST)
 */
public class ValueCheckPluginTest {

    private static MessageValidationV2 validator;
    private static MessageValidationContextV2 context;
    // private static XmlMessage xmlValid;
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
        // xmlValid = new XmlMessage(new File(
        // ValueCheckPluginTest.class.getResource(
        // "/content/v2/ValidMessage.xml").getFile()));
        // vc = new ValueCheckPlugin();
        // mapper = new ObjectMapper();
        er7Valid = new Er7Message(
                new File(ValueCheckPluginTest.class.getResource(
                        "/content/valueCheckPlugin/ValidMessage.er7").getFile()));
    }

    private MessageValidationResultV2 validate(HL7V2Message message) {
        MessageValidationResultV2 result = validator.validate(message, context);
        result.getReport();
        return result;
    }

    private void checkResult(MessageValidationResultV2 result, boolean valid,
            int dataPassedCount, int errorCount) {
        assertEquals(result.getAlertCount(), 0);
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
                assertEquals(mf.getFailureType(), AssertionTypeV2Constants.DATA);
            }
        }
    }

    @Test
    public void testPlainTextMatch() throws JsonGenerationException,
            JsonMappingException, IOException, XmlException {
        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/plainTextMatch.xml").getFile()));
        MessageValidationResultV2 result = validate(er7Valid);
        checkResult(result, true, 1, 0);

        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/plainTextNoMatch.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, false, 0, 1);

        context.load(new File(
                getClass().getResource(
                        "/content/valueCheckPlugin/plainTextIgnoreCaseMatch.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, true, 1, 0);

        context.load(new File(
                getClass().getResource(
                        "/content/valueCheckPlugin/plainTextIgnoreCaseNoMatch.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, false, 0, 1);

        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/plainTextNumberMatch.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, true, 1, 0);

        context.load(new File(
                getClass().getResource(
                        "/content/valueCheckPlugin/plainTextNumberNoMatch.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, false, 0, 1);

        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/plainTextRequiredOff.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, true, 0, 0);

        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/plainTextRequiredOn.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, false, 0, 1);

        context.load(new File(
                getClass().getResource(
                        "/content/valueCheckPlugin/plainTextRepetitionRequiredOn.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, false, 0, 6);

        context.load(new File(
                getClass().getResource(
                        "/content/valueCheckPlugin/plainTextRepetitionRequiredOff.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, false, 0, 5);
    }

    @Test
    public void testRegexMatch() throws JsonGenerationException,
            JsonMappingException, IOException, XmlException {
        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/regexMatch.xml").getFile()));
        MessageValidationResultV2 result = validate(er7Valid);
        checkResult(result, true, 1, 0);

        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/regexNoMatch.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, false, 0, 1);
    }

    @Test
    public void testLocationMatch() throws JsonGenerationException,
            JsonMappingException, IOException, XmlException {
        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/locationMatch.xml").getFile()));
        MessageValidationResultV2 result = validate(er7Valid);
        checkResult(result, true, 1, 0);

        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/locationNoMatch.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, false, 0, 1);

        context.load(new File(
                getClass().getResource(
                        "/content/valueCheckPlugin/locationRepetitionMatch.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, true, 1, 0);
    }

    @Test
    public void testAllMatch() throws JsonGenerationException,
            JsonMappingException, IOException, XmlException {
        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/plainTextORMatch.xml").getFile()));
        MessageValidationResultV2 result = validate(er7Valid);
        checkResult(result, true, 1, 0);

        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/plainTextORNoMatch.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, false, 0, 1);

        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/plainTextANDMatch.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, true, 2, 0);

        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/plainTextANDNoMatch.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, false, 0, 1);
    }

    @Test
    public void testMinMaxMatch() throws JsonGenerationException,
            JsonMappingException, IOException, XmlException {
        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/atLeastOneMatch.xml").getFile()));
        MessageValidationResultV2 result = validate(er7Valid);
        checkResult(result, true, 1, 0);

        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/atLeastOneNoMatch.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, false, 0, 1);

        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/atGreatestTwoMatch.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, true, 1, 0);

        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/atGreatestTwoNoMatch.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, false, 0, 1);
    }

    @Test
    public void testPresentEmptyMatch() throws JsonGenerationException,
            JsonMappingException, IOException, XmlException {
        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/presentMatch.xml").getFile()));
        MessageValidationResultV2 result = validate(er7Valid);
        checkResult(result, true, 1, 0);

        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/presentNoMatch.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, false, 0, 1);

        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/emptyMatch.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, true, 1, 0);

        context.load(new File(getClass().getResource(
                "/content/valueCheckPlugin/emptyNoMatch.xml").getFile()));
        result = validate(er7Valid);
        checkResult(result, false, 0, 1);
    }

    @AfterClass
    public static void tearDown() {
        er7Valid = null;
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(ValueCheckPluginTest.class);
    }

}
