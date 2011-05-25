/*
 * NIST Healthcare Core
 * MessageStructureV3Validator.java Sep 12, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.structure.v3;

import gov.nist.healthcare.core.validation.message.MessageValidationConstants;
import gov.nist.healthcare.core.validation.message.MessageValidationException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author mccaffrey
 */
public class MessageStructureV3Validator {

    public XmlObject validate(String message, String schemaLocation)
            throws MessageValidationException {
        XmlObject res = null;
        try {
            SchemaValidationErrorHandler errorHandler = new SchemaValidationErrorHandler();
            Document doc = validateWithSchema(message, errorHandler,
                    schemaLocation);
            Document result = generateReport(doc, errorHandler, null);
            res = XmlObject.Factory.parse(result);
        } catch (XmlException e) {
            throw new MessageValidationException(e.getMessage());
        }
        return res;
    }

    public XmlObject validate(String message, String schemaLocation,
            String schematronLocation) throws MessageValidationException {
        XmlObject res = null;
        try {
            SchemaValidationErrorHandler errorHandler = new SchemaValidationErrorHandler();
            Document doc = validateWithSchema(message, errorHandler,
                    schemaLocation);
            String schematronResult = validateWithSchematron(doc,
                    schematronLocation, "errors");
            Node schematronResultNode = null;
            schematronResultNode = stringToDom(schematronResult);
            Node[] messageList = { schematronResultNode };
            Document result = generateReport(doc, errorHandler, messageList);
            res = XmlObject.Factory.parse(result);
        } catch (SAXException e) {
            throw new MessageValidationException(e.getMessage());
        } catch (ParserConfigurationException e) {
            throw new MessageValidationException(e.getMessage());
        } catch (IOException e) {
            throw new MessageValidationException(e.getMessage());
        } catch (XmlException e) {
            throw new MessageValidationException(e.getMessage());
        }
        return res;
    }

    public boolean isReportSuccess(XmlObject report) {
        XmlObject[] resultsOfTest = report.selectPath("/Report/ReportHeader/ResultOfTest");
        if (!(resultsOfTest.length > 0)) {
            return false;
        }
        XmlObject resultOfTest = resultsOfTest[0];
        if ("Passed".equalsIgnoreCase(resultOfTest.newCursor().getTextValue())) {
            return true;
        }
        return false;
    }

    public String readFile(String path) throws IOException {
        BufferedReader input = new BufferedReader(
                new FileReader(new File(path)));
        String line = null;
        StringBuilder xml = new StringBuilder();
        while ((line = input.readLine()) != null) {
            xml.append(line);
        }
        return xml.toString();
    }

    private Document generateReport(Document doc,
            SchemaValidationErrorHandler errorHandler, Node[] messages) {
        Document result = null;
        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            result = builder.newDocument();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        Element report = result.createElement("Report");
        result.appendChild(report);
        int errorCount = errorHandler.getNumberErrors()
                + getMessageCount(messages);
        // int warningCount =
        // warnings.getFirstChild().getChildNodes().getLength();
        Element header = createHeader(result, errorCount);
        report.appendChild(header);
        // if(errorHandler.hasErrors()) {
        Element schemaErrorReport = createSchemaErrorReport(result,
                errorHandler);
        report.appendChild(schemaErrorReport);
        // }
        if (messages != null) {
            for (int i = 0; i < messages.length; i++) {
                Node message = messages[i];
                report.appendChild(result.importNode(message.getFirstChild(),
                        true));
            }
        }
        /*
         * Element testObject = result.createElement("TestObject"); if(doc !=
         * null) {
         * testObject.appendChild(result.importNode(doc.getDocumentElement(),
         * true)); } else { testObject.setTextContent(
         * "Error: Could not read file to generate test object.  Verify it is valid XML."
         * ); } report.appendChild(testObject);
         */
        return result;
    }

    private Element createHeader(Document result, int errorCountInt) {
        Element reportHeader = result.createElement("ReportHeader");
        Element validationStatus = result.createElement("ValidationStatus");
        validationStatus.setTextContent("Complete");
        reportHeader.appendChild(validationStatus);
        Element serviceName = result.createElement("ServiceName");
        serviceName.setTextContent("NIST PIX/PDQ");
        reportHeader.appendChild(serviceName);
        Element dateOfTest = result.createElement("DateOfTest");
        dateOfTest.setTextContent(createDateOfTest());
        reportHeader.appendChild(dateOfTest);
        Element timeOfTest = result.createElement("TimeOfTest");
        timeOfTest.setTextContent(createTimeOfTest());
        reportHeader.appendChild(timeOfTest);
        Element resultOfTest = result.createElement("ResultOfTest");
        if (errorCountInt == 0) {
            resultOfTest.setTextContent("Passed");
        } else {
            resultOfTest.setTextContent("Failed");
        }
        reportHeader.appendChild(resultOfTest);
        Element errorCount = result.createElement("ErrorCount");
        errorCount.setTextContent(String.valueOf(errorCountInt));
        reportHeader.appendChild(errorCount);
        return reportHeader;
    }

    private int getMessageCount(Node[] messages) {
        if (messages == null || messages.length == 0) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < messages.length; i++) {
            Node message = messages[i];
            count += message.getFirstChild().getChildNodes().getLength();
        }
        return count;
    }

    private Element createSchemaErrorReport(Document doc,
            SchemaValidationErrorHandler errorHandler) {
        Element result = doc.createElement("Results");
        result.setAttribute("severity", "schemaViolation");
        if (errorHandler.hasErrors()) {
            Iterator<String> it = errorHandler.getErrors().iterator();
            while (it.hasNext()) {
                Element issue = doc.createElement("issue");
                result.appendChild(issue);
                Element message = doc.createElement("message");
                message.setTextContent(it.next());
                issue.appendChild(message);
            }
        }
        return result;
    }

    private String createDateOfTest() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private String createTimeOfTest() {
        DateFormat dateFormat = new SimpleDateFormat("HHmmss.SSSS ZZZZ");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private Document validateWithSchema(String xml,
            SchemaValidationErrorHandler handler, String schemaLocation) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        factory.setAttribute(
                "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema");
        factory.setAttribute(
                "http://java.sun.com/xml/jaxp/properties/schemaSource",
                schemaLocation);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            return null;
        }
        builder.setErrorHandler(handler);
        Document doc = null;
        StringReader stringReader = new StringReader(xml);
        InputSource inputSource = new InputSource(stringReader);
        try {
            doc = builder.parse(inputSource);
        } catch (SAXException e) {
            System.out.println("Message is not valid XML.");
            handler.addError("Message is not valid XML.", null);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Message is not valid XML.  Possible empty message.");
            handler.addError(
                    "Message is not valid XML.  Possible empty message.", null);
            e.printStackTrace();
        }
        stringReader.close();
        return doc;
    }

    // validateWithSchematron( ... ) does schematron validation, but not in the
    // most efficient way. For stable schematron, it would be more efficient
    // to run the schematron through the skeleton transform once, save that
    // transformation to a file and then simply reuse that transform rather than
    // generating it on every run. That is left as an exercise for the
    // implementor.

    private String validateWithSchematron(Document xml,
            String schematronLocation, String phase) {

        StringBuilder result = new StringBuilder();
        StreamSource schematron = new StreamSource(schematronLocation);
        StreamSource skeleton = new StreamSource(
                MessageStructureV3Validator.class.getClassLoader().getResourceAsStream(
                        MessageValidationConstants.XSLT_SKELETON));
        Node schematronTransform = doTransform(schematron, skeleton, phase);
        result.append(doTransform(xml, schematronTransform));
        return result.toString();
    }

    private Node doTransform(StreamSource xmlSource, StreamSource xsltSource,
            String phase) {
        System.setProperty("javax.xml.transform.TransformerFactory",
                "net.sf.saxon.TransformerFactoryImpl");
        DOMResult result = new DOMResult();
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            URIResolver resolver = new SkeletonURIResolver();
            tFactory.setURIResolver(resolver);
            Transformer transformer = tFactory.newTransformer(xsltSource);
            transformer.setParameter("phase", phase);
            transformer.transform(xmlSource, result);
        } catch (TransformerConfigurationException tce) {
            tce.printStackTrace();
            return null;
        } catch (TransformerException te) {
            te.printStackTrace();
            return null;
        } finally {
            System.clearProperty("javax.xml.transform.TransformerFactory");
        }
        return result.getNode();
    }

    private String doTransform(Document originalXml, Node transform) {
        System.setProperty("javax.xml.transform.TransformerFactory",
                "net.sf.saxon.TransformerFactoryImpl");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(os);
        try {
            Source xmlSource = new DOMSource(originalXml);
            Source xsltSource = new DOMSource(transform);
            Transformer transformer = TransformerFactory.newInstance().newTransformer(
                    xsltSource);
            transformer.transform(xmlSource, result);
        } catch (TransformerConfigurationException tce) {
            tce.printStackTrace();
            return null;
        } catch (TransformerException te) {
            te.printStackTrace();
            return null;
        } finally {
            System.clearProperty("javax.xml.transform.TransformerFactory");
        }
        return os.toString();
    }

    private Document stringToDom(String xmlSource) throws SAXException,
            ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlSource)));
    }

}
