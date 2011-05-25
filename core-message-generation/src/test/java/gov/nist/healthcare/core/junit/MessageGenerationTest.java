/*
 * NIST Healthcare Core
 * MessageGenerationTest.java Mar 03, 2010
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
import gov.nist.healthcare.core.generation.MessageGeneration;
import gov.nist.healthcare.core.generation.MessageGenerationResult;
import gov.nist.healthcare.core.profile.Profile;
import gov.nist.healthcare.generation.message.HL7V2MessageGenerationContextDefinitionDocument;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import junit.framework.JUnit4TestAdapter;
import org.apache.xmlbeans.XmlException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * This class is a JUnit test class for message generation
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageGenerationTest {

    private static Profile profile;
    private static MessageGeneration messageGeneration;

    @BeforeClass
    public static void setUp() throws MalformedProfileException {
        profile = new Profile(
                MessageGenerationTest.class.getResourceAsStream("/ORU_R01.xml"));
        messageGeneration = new MessageGeneration();
    }

    private MessageGenerationResult generate(
            HL7V2MessageGenerationContextDefinitionDocument context)
            throws XmlException, IOException, TransformerException,
            MalformedMessageException, ParserConfigurationException,
            SAXException {
        return messageGeneration.generate(profile, context);
    }

    @Test
    public void testXUsageElementLocation() throws XmlException, IOException,
            TransformerException, MalformedMessageException,
            ParserConfigurationException, SAXException {
        HL7V2MessageGenerationContextDefinitionDocument context = HL7V2MessageGenerationContextDefinitionDocument.Factory.parse(new File(
                getClass().getResource("/XUsageElementLocation.xml").getFile()));
        MessageGenerationResult mgr = generate(context);
        List<String> errors = mgr.getErrors();
        assertEquals(errors.size(), 1);
        assertTrue(errors.get(0).contains(
                "The provided location is not present in the message"));
    }

    @Test
    public void testMissingElementLocation() throws XmlException, IOException,
            TransformerException, MalformedMessageException,
            ParserConfigurationException, SAXException {
        HL7V2MessageGenerationContextDefinitionDocument context = HL7V2MessageGenerationContextDefinitionDocument.Factory.parse(new File(
                getClass().getResource("/MissingElementLocation.xml").getFile()));
        MessageGenerationResult mgr = generate(context);
        List<String> errors = mgr.getErrors();
        assertEquals(errors.size(), 1);
        assertTrue(errors.get(0).contains(
                "The provided location is not present in the message"));
    }

    @Test
    public void testSegmentGroupLocation() throws XmlException, IOException,
            TransformerException, MalformedMessageException,
            ParserConfigurationException, SAXException {
        HL7V2MessageGenerationContextDefinitionDocument context = HL7V2MessageGenerationContextDefinitionDocument.Factory.parse(new File(
                getClass().getResource("/SegmentGroupElementLocation.xml").getFile()));
        MessageGenerationResult mgr = generate(context);
        List<String> errors = mgr.getErrors();
        assertEquals(errors.size(), 1);
        assertTrue(errors.get(0).contains(
                "The provided location is not refering to a primitive element"));
    }

    @Test
    public void testSegmentLocation() throws XmlException, IOException,
            TransformerException, MalformedMessageException,
            ParserConfigurationException, SAXException {
        HL7V2MessageGenerationContextDefinitionDocument context = HL7V2MessageGenerationContextDefinitionDocument.Factory.parse(new File(
                getClass().getResource("/SegmentElementLocation.xml").getFile()));
        MessageGenerationResult mgr = generate(context);
        List<String> errors = mgr.getErrors();
        assertEquals(errors.size(), 1);
        assertTrue(errors.get(0).contains(
                "The provided location is not refering to a primitive element"));
    }

    @Test
    public void testNonPrimitiveLocation() {

    }

    @AfterClass
    public static void tearDown() {
        profile = null;
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(MessageGenerationTest.class);
    }

}
