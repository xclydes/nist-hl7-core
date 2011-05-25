/*
 * NIST Healthcare Core
 * Xml2BarTest.java Jul 29, 2008
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.junit;

import gov.nist.healthcare.core.MalformedMessageException;
import gov.nist.healthcare.core.encoding.xml2bar.Xml2Bar;
import gov.nist.healthcare.core.message.v2.er7.Er7Message;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * This class is a JUnit test class for Xml2Bar
 * 
 * @author Caroline Rosin (NIST)
 */
public class Xml2BarTest extends TestCase {

    private static File xmlMessageFile;
    private static File er7MessageFile;

    @Override
    @BeforeClass
    public void setUp() {
        xmlMessageFile = new File(
                getClass().getResource("/Valid.xml").getFile());
        er7MessageFile = new File(
                getClass().getResource("/Valid.er7").getFile());
    }

    @Test
    public void testParse() throws MalformedMessageException,
            ParserConfigurationException, SAXException, IOException {

        Er7Message er7Message = Xml2Bar.parse(xmlMessageFile);
        Er7Message er7MessageBis = new Er7Message(er7MessageFile);

        assertTrue(er7Message.getMessageAsString().equals(
                er7MessageBis.getMessageAsString()));
        er7Message = null;
        er7MessageBis = null;
    }

    @Override
    @AfterClass
    public void tearDown() {
        xmlMessageFile = null;
        er7MessageFile = null;
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(Xml2BarTest.class);
    }

}
