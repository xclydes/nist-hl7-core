/*
 * NIST Healthcare Core
 * MessageTest.java Jul 23, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.junit;

import gov.nist.healthcare.core.MalformedMessageException;
import gov.nist.healthcare.core.message.MessageId;
import gov.nist.healthcare.core.message.MessageLocation;
import gov.nist.healthcare.core.message.Name;
import gov.nist.healthcare.core.message.v2.HL7V2MessageId;
import gov.nist.healthcare.core.message.v2.er7.Er7Message;
import gov.nist.healthcare.core.message.v2.xml.XmlMessage;
import gov.nist.healthcare.core.message.v3.HL7V3Message;
import gov.nist.healthcare.core.message.v3.HL7V3MessageId;
import gov.nist.healthcare.core.message.v3.HL7V3Name;
import java.io.File;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This JUnit tests the Message class
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageTest extends TestCase {

    private static Er7Message er7;
    private static XmlMessage xml;
    private static HL7V3Message v3;

    @Override
    @BeforeClass
    public void setUp() {
        try {
            er7 = new Er7Message(new File(getClass().getResource(
                    "/ValidMessage.er7").getFile()));
            xml = new XmlMessage(new File(getClass().getResource(
                    "/ValidMessage.xml").getFile()));
            v3 = new HL7V3Message(new File(getClass().getResource(
                    "/ValidV3Message.xml").getFile()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @AfterClass
    public void tearDown() {
        er7 = null;
        xml = null;
        v3 = null;
    }

    @Test
    public void testReplacingMethod() {
        Name sending = er7.getSending();
        Name receiving = er7.getReceiving();
        er7.replaceReceiving(sending);
        er7.replaceSending(receiving);
        er7.replaceDateTimeOfMessage("20100101");
        er7.replaceMessageId(new HL7V2MessageId("MessageID"));
        String creationTime = er7.getCreationTime();
        MessageId messageId = er7.getMessageID();
        assertEquals(sending, er7.getReceiving());
        assertEquals(receiving, er7.getSending());
        assertEquals(creationTime, "20100101");
        assertEquals(messageId, new HL7V2MessageId("MessageID"));

        sending = xml.getSending();
        receiving = xml.getReceiving();
        xml.replaceReceiving(sending);
        xml.replaceSending(receiving);
        xml.replaceDateTimeOfMessage("20100101");
        xml.replaceMessageId(new HL7V2MessageId("MessageID"));
        creationTime = xml.getCreationTime();
        messageId = xml.getMessageID();
        assertEquals(sending, xml.getReceiving());
        assertEquals(receiving, xml.getSending());
        assertEquals(creationTime, "20100101");
        assertEquals(messageId, new HL7V2MessageId("MessageID"));

        sending = v3.getSending();
        receiving = v3.getReceiving();
        v3.replaceReceiving(sending);
        v3.replaceSending(receiving);
        v3.replaceDateTimeOfMessage("20100101");
        v3.replaceMessageId(new HL7V3MessageId("root", "extension"));
        creationTime = v3.getCreationTime();
        messageId = v3.getMessageID();
        assertEquals(sending, v3.getReceiving());
        assertEquals(receiving, v3.getSending());
        assertEquals(creationTime, "20100101");
        assertEquals(messageId, new HL7V3MessageId("root", "extension"));
    }

    @Test
    public void testCreatingMessageInDifferentEncoding()
            throws MalformedMessageException {
        Er7Message er7UTF8 = new Er7Message(new File(
                MessageTest.class.getResource("/encoding/utf8.er7").getFile()),
                "UTF-8");
        Er7Message er7UTF16 = new Er7Message(
                new File(
                        MessageTest.class.getResource("/encoding/utf16.er7").getFile()),
                "UTF-16");
        XmlMessage xmlUTF8 = new XmlMessage(new File(
                MessageTest.class.getResource("/encoding/utf8.xml").getFile()),
                "UTF-8");
        XmlMessage xmlUTF16 = new XmlMessage(
                new File(
                        MessageTest.class.getResource("/encoding/utf16.xml").getFile()),
                "UTF-16");
        HL7V3Message xmlv3UTF8 = new HL7V3Message(
                new File(
                        MessageTest.class.getResource("/encoding/utf8v3.xml").getFile()),
                "UTF-8");
        HL7V3Message xmlv3UTF16 = new HL7V3Message(
                new File(
                        MessageTest.class.getResource("/encoding/utf16v3.xml").getFile()),
                "UTF-16");
        assertNotNull(er7UTF8);
        assertNotNull(er7UTF16);
        assertNotNull(xmlUTF8);
        assertNotNull(xmlUTF16);
        assertNotNull(xmlv3UTF8);
        assertNotNull(xmlv3UTF16);
    }

    @Test
    public void testReplaceValue() throws MalformedMessageException {
        Er7Message mergeMessage = new Er7Message(new File(
                getClass().getResource("/ValidMergeMessage.er7").getFile()));
        MessageLocation mrg141 = MessageLocation.getMessageLocation(null,
                "MRG", 1, 1, 1, 4, 1);
        MessageLocation mrg142 = MessageLocation.getMessageLocation(null,
                "MRG", 1, 1, 1, 4, 2);
        MessageLocation mrg143 = MessageLocation.getMessageLocation(null,
                "MRG", 1, 1, 1, 4, 3);
        mergeMessage.replaceValue(mrg141, "NIST2010-2");
        mergeMessage.replaceValue(mrg142, "2.16.840.1.113883.3.72.5.9.2");
        mergeMessage.replaceValue(mrg143, "ISO+");
        assertEquals(mergeMessage.getValue(mrg141), "NIST2010-2");
        assertEquals(mergeMessage.getValue(mrg142),
                "2.16.840.1.113883.3.72.5.9.2");
        assertEquals(mergeMessage.getValue(mrg143), "ISO+");
    }

    @Test
    public void testHL7V3Name() {
        HL7V3Name sender = new HL7V3Name("oid1", "oid2");
        HL7V3Name validReceiver1 = new HL7V3Name("oid1", null);
        HL7V3Name validReceiver2 = new HL7V3Name("oid1", "oid2");
        HL7V3Name validReceiver3 = new HL7V3Name("oid1", "oid3");
        HL7V3Name wrongReceiver1 = new HL7V3Name("oid2", null);
        HL7V3Name wrongReceiver2 = new HL7V3Name("oid2", "oid2");
        HL7V3Name wrongReceiver3 = new HL7V3Name("oid2", "oid3");
        assertEquals(sender, validReceiver1);
        assertEquals(sender, validReceiver2);
        assertEquals(sender, validReceiver3);
        assertNotSame(sender, wrongReceiver1);
        assertNotSame(sender, wrongReceiver2);
        assertNotSame(sender, wrongReceiver3);
    }

}
