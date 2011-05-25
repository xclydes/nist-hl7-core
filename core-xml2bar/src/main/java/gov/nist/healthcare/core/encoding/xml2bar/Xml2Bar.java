/*
 * NIST Healthcare Core
 * Xml2bar.java Jun 23, 2006
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.encoding.xml2bar;

import gov.nist.healthcare.core.MalformedMessageException;
import gov.nist.healthcare.core.message.v2.er7.Er7Message;
import gov.nist.healthcare.core.message.v2.xml.XmlMessage;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.xmlbeans.XmlObject;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Provides the transformation of a XML file into an er7 message.
 * 
 * @author Caroline Rosin (NIST)
 */
public final class Xml2Bar {

    /**
     * Constructor.
     */
    private Xml2Bar() {

    }

    /**
     * Transforms a xml message in a ER7 message, using the HL7 standard rules.
     * The segments separator is "\r" as defined in the HL7 standard. To get a
     * printable view of the message, use the getViewableER7(File f) method.
     * 
     * @param f
     *        the File object representing the xml message
     * @return the ER7 as a String
     * @throws MalformedMessageException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static synchronized Er7Message parse(File f)
            throws MalformedMessageException, ParserConfigurationException,
            SAXException, IOException {
        XmlMessage m = new XmlMessage(f);
        return parse(m);

    }

    /**
     * Transforms a xml message in a ER7 message, using the HL7 standard rules.
     * The segments separator is "\r" as defined in the HL7 standard. To get a
     * printable view of the message, use the getViewableER7(Message m) method.
     * 
     * @param xmlMessage
     *        the Message object representing the xml message
     * @return the ER7 as a String
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws MalformedMessageException
     */
    public static synchronized Er7Message parse(XmlMessage xmlMessage)
            throws ParserConfigurationException, SAXException, IOException,
            MalformedMessageException {

        XmlObject bean = xmlMessage.getDocument();

        // creates a new SAX parser
        Xml2barParser parser = new Xml2barParser();
        SAXParserFactory factory = SAXParserFactory.newInstance();

        // Parses the input
        DefaultHandler handler = parser;
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(bean.newInputStream(), handler);

        // gets the ER7 message generated
        return new Er7Message(parser.getER7());
    }

    /**
     * @param f
     *        the File object representing the xml message
     * @return the ER7 as a String, with LF as segment separator
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws MalformedMessageException
     */
    public static synchronized String getViewableER7(File f)
            throws MalformedMessageException, ParserConfigurationException,
            SAXException, IOException {
        String er7 = parse(f).toString();
        er7 = er7.replaceAll("\\r", "\r\n");
        return er7;
    }

    /**
     * @param xmlMessage
     *        the Message object representing the xml message
     * @return the ER7 as a String, with LF as segment separator
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws MalformedMessageException
     */
    public static synchronized String getViewableER7(XmlMessage xmlMessage)
            throws ParserConfigurationException, SAXException, IOException,
            MalformedMessageException {
        String er7 = parse(xmlMessage).toString();
        er7 = er7.replaceAll("\\r", "\r\n");
        return er7;
    }

}
