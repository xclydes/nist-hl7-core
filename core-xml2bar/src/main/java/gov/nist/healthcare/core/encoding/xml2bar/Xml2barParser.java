/*
 * NIST Healthcare Core
 * Xml2barParser.java Jun 23, 2006
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.encoding.xml2bar;

import gov.nist.healthcare.core.encoding.MessageInfo;
import gov.nist.healthcare.core.encoding.util.EncodingUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.LocatorImpl;

/**
 * Parses the XML file using a SAX parser and writes the corresponding ER7 file.
 * 
 * @author Caroline Rosin (NIST)
 */
public class Xml2barParser extends DefaultHandler {

    private Locator locator;
    private int level;

    private final StringBuffer er7;

    private final MessageInfo messageInfo;

    private final StringBuffer segName;
    private String fieldName = "";
    private String componentName = "";
    private String subcomponentName = "";

    private String lastFieldName = "";
    private String lastComponentName = "";
    private String lastSubcomponentName = "";

    private boolean separatorsSet = false;

    private final StringBuffer data;

    /**
     * Constructor.
     */
    public Xml2barParser() {
        super();
        // default locator
        locator = new LocatorImpl();
        messageInfo = new MessageInfo();
        data = new StringBuffer();
        er7 = new StringBuffer();

        segName = new StringBuffer();

    }

    @Override
    public void setDocumentLocator(Locator value) {
        locator = value;
    }

    @Override
    public void startDocument() throws SAXException {
        level = -1;
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    @Override
    public void startElement(String nameSpaceURI, String localName,
            String rawName, Attributes attributs) throws SAXException {

        data.delete(0, data.length());

        level++;

        // ignore groups tags
        Pattern pGroup = Pattern.compile(messageInfo.getMsgStructId() + "\\.");
        Matcher mGroup = pGroup.matcher(rawName);
        boolean bGroup = mGroup.find();

        rawName = rawName.replaceAll(".*:", "");

        if (bGroup) {
            level--;
        } else {
            switch (level) {
            case 0:
                messageInfo.setMsgStructId(rawName);
                break;

            case 1:
                // opening a segment
                segName.append(rawName);

                if (!(segName.length() == 3)) {
                    throw new SAXException("Line " + locator.getLineNumber()
                            + " : Invalid segment name : " + segName.toString()
                            + " ");
                }
                er7.append(rawName);
                break;

            case 2:
                // opening a field
                fieldName = rawName;
                // is the field name like ABC.1 ?
                Pattern p = Pattern.compile("^(([A-Z]|\\d){3})\\.\\d+$");
                Matcher m = p.matcher(fieldName);

                if (!m.find()) {
                    throw new SAXException("Invalid field name : " + fieldName);
                } else if (!m.group(1).equals(segName.toString())) {
                    throw new SAXException("Line " + locator.getLineNumber()
                            + " : Segment and field mismatch : "
                            + segName.toString() + " " + fieldName);
                }

                if (fieldName.equals("MSH.1") || fieldName.equals("MSH.2")) {
                    break;
                }

                try {
                    writeSeparators(fieldName, lastFieldName,
                            messageInfo.getFieldSepChar());
                } catch (Xml2BarException e) {
                    throw new SAXException(e.toString());
                }
                break;

            case 3:
                // opening a component
                componentName = rawName;
                if (!Pattern.matches("^(.)+\\.\\d+$", componentName)) {
                    throw new SAXException("Line " + locator.getLineNumber()
                            + " : Invalid component name : " + componentName);
                }
                try {
                    writeSeparators(componentName, lastComponentName,
                            messageInfo.getCompSepChar());
                } catch (Xml2BarException e) {
                    throw new SAXException(e.toString());
                }
                break;

            case 4:
                // opening a subcomponent
                subcomponentName = rawName;
                if (!Pattern.matches("^(.)+\\.\\d+$", subcomponentName)) {
                    throw new SAXException("Line " + locator.getLineNumber()
                            + " : Invalid component name : " + subcomponentName);
                }
                try {
                    writeSeparators(subcomponentName, lastSubcomponentName,
                            messageInfo.getSubCompChar());
                } catch (Xml2BarException e) {
                    throw new SAXException(e.toString());
                }
                break;

            default:
                throw new SAXException("Line " + locator.getLineNumber()
                        + " : Bad depth : " + level);
            }
        }

    }

    @Override
    public void endElement(String nameSpaceURI, String localName, String rawName)
            throws SAXException {

        // write the data
        writeData();

        // reset the data
        data.delete(0, data.length());

        // ignore groups tags
        Pattern pGroup = Pattern.compile(messageInfo.getMsgStructId() + "\\.");
        Matcher mGroup = pGroup.matcher(rawName);
        boolean bGroup = mGroup.find();

        if (bGroup) {
            level++;
        } else {
            switch (level) {

            case 0:
                break;

            case 1:
                // closing a segment
                er7.append("\r");
                segName.delete(0, segName.length());
                lastFieldName = "";
                lastComponentName = "";
                lastSubcomponentName = "";
                break;

            case 2:
                // closing a field
                lastFieldName = fieldName;
                fieldName = "";
                lastComponentName = "";
                lastSubcomponentName = "";
                break;

            case 3:
                // closing a component
                lastComponentName = componentName;
                componentName = "";
                lastSubcomponentName = "";
                break;

            case 4:
                // closing a subcomponent
                lastSubcomponentName = subcomponentName;
                subcomponentName = "";
                break;

            default:
                break;
            }
        }
        level--;
    }

    @Override
    public void characters(char[] ch, int start, int end) throws SAXException {
        // removing useless whitespace characters
        String donnee = new String(ch, start, end);
        data.append(donnee);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int end)
            throws SAXException {
    }

    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {

    }

    @Override
    public void skippedEntity(String arg0) throws SAXException {

    }

    /**
     * Returns the ER7 message.
     * 
     * @return the ER7 message
     */
    public String getER7() {
        return er7.toString();
    }

    /**
     * Sets the differente separators.
     * 
     * @param data
     *        the list of separators
     * @throws Xml2BarException
     */
    public void setSeparators(String data) throws Xml2BarException {

        if (fieldName.equals("MSH.1") && data.length() == 1) {
            messageInfo.setFieldSepChar(data);
        } else if (fieldName.equals("MSH.2")) {
            switch (data.length()) {
            case 1:
                messageInfo.setCompSepChar(data.substring(0, 1));
                break;
            case 2:
                messageInfo.setCompSepChar(data.substring(0, 1));
                messageInfo.setRepSepChar(data.substring(1, 2));
                break;
            case 3:
                messageInfo.setCompSepChar(data.substring(0, 1));
                messageInfo.setRepSepChar(data.substring(1, 2));
                messageInfo.setEscapeChar(data.substring(2, 3));
                break;
            case 4:
                messageInfo.setCompSepChar(data.substring(0, 1));
                messageInfo.setRepSepChar(data.substring(1, 2));
                messageInfo.setEscapeChar(data.substring(2, 3));
                messageInfo.setSubCompChar(data.substring(3, 4));
                break;
            default:
                throw new Xml2BarException("Line " + locator.getLineNumber()
                        + " : Separators must bet set in MSH.1 or MSH.2 field");
            }
        } else {
            throw new Xml2BarException("Line " + locator.getLineNumber()
                    + " : Separators must bet set in MSH.1 or MSH.2 field");
        }

        // are all separators set ?
        if (!messageInfo.getFieldSepChar().equals("")
                && !messageInfo.getCompSepChar().equals("")
                && !messageInfo.getRepSepChar().equals("")) {
            separatorsSet = true;
        }
    }

    /**
     * Escapes the separators.
     * 
     * @param data
     *        the data
     * @param separator
     *        the separator to escape
     * @param escSeq
     *        the escape sequence
     * @return the data with the escaped separators
     */
    public String ecapeSeparator(String data, String separator, String escSeq) {

        // getting the unicode code
        int tmpUnicode = separator.charAt(0);
        String unicode = EncodingUtils.getUnicode(tmpUnicode);

        // // put enough '0' to have 4 characters
        // int count = 4 - unicode.length();
        // for (int i = count; i > 0; i--) {
        // unicode = "0".concat(unicode);
        // }

        // escape the separator
        Pattern pGroup = Pattern.compile("\\u" + unicode);
        Matcher mGroup = pGroup.matcher(data);

        return mGroup.replaceAll(escSeq);
    }

    /**
     * Writes the good separator in the ER7 message.
     * 
     * @param tag
     *        the current tag
     * @param lastTag
     *        the last tag from the same level
     * @param separator
     *        the separator
     * @throws Xml2BarException
     */
    public void writeSeparators(String tag, String lastTag, String separator)
            throws Xml2BarException {

        // are the separators defined ?
        if (!separatorsSet) {
            throw new Xml2BarException("Line " + locator.getLineNumber()
                    + " : Separators not set");
        }

        // separating the name and the number
        Pattern p = Pattern.compile("^(.+)\\.(.+)$");

        // current tag
        Matcher m1 = p.matcher("");
        m1.reset(tag);
        boolean m1Found = m1.find();

        String tagName = "";
        int tagNumber = 0;

        if (m1Found) {
            try {
                tagName = m1.group(1);
                tagNumber = Integer.parseInt(m1.group(2));
            } catch (IndexOutOfBoundsException e) {
                throw new Xml2BarException("Line " + locator.getLineNumber()
                        + " : Invalid tag name : " + tag);
            } catch (java.lang.NumberFormatException e) {
                throw new Xml2BarException("Line " + locator.getLineNumber()
                        + " : Invalid tag name : " + tag);
            }

        } else {
            throw new Xml2BarException("Line " + locator.getLineNumber()
                    + " : Invalid tag name : " + tag);
        }

        // last tag
        Matcher m2 = p.matcher("");
        m2.reset(lastTag);
        boolean m2Found = m2.find();

        String lastTagName = "";
        int lastTagNumber = 0;

        if (m2Found) {
            try {
                lastTagName = m2.group(1);
                lastTagNumber = Integer.parseInt(m2.group(2));
            } catch (IndexOutOfBoundsException e) {
                throw new Xml2BarException("Line " + locator.getLineNumber()
                        + " : Invalid tag name : " + tag);
            } catch (java.lang.NumberFormatException e) {
                throw new Xml2BarException("Line " + locator.getLineNumber()
                        + " : Invalid tag name : " + tag);
            }
        }

        // part of the same field ?
        if (tagName.equals(lastTagName)) {
            // is it a repetition ?
            if (tagNumber == lastTagNumber) {
                er7.append(messageInfo.getRepSepChar());
                // are there some missing fields ?
            } else if (tagNumber > lastTagNumber + 1) {
                for (int i = lastTagNumber + 1; i < tagNumber; i++) {
                    er7.append(separator);
                }
                er7.append(separator);
            } else {
                er7.append(separator);
            }
        } else {
            // missing the first components ??
            if (tagNumber > 1) {
                for (int i = lastTagNumber + 1; i < tagNumber; i++) {
                    er7.append(separator);
                }
            }
            if (separator.equals(messageInfo.getFieldSepChar())) {
                er7.append(separator);
            }
        }
    }

    public void writeData() throws SAXException {

        String dataSt = data.toString();

        Pattern p = Pattern.compile("^\\s+");
        Matcher m = p.matcher(dataSt);
        dataSt = m.replaceAll("");

        p = Pattern.compile("\\s+$");
        m = p.matcher(dataSt);
        dataSt = m.replaceAll("");

        if (fieldName.equals("MSH.1") || fieldName.equals("MSH.2")) {
            // set the separators
            try {
                setSeparators(dataSt);
            } catch (Xml2BarException e) {
                throw new SAXException(e.toString());
            }

        } else if (separatorsSet) {
            // escape the separators
            if (!messageInfo.getEscapeChar().equals("")) {
                dataSt = ecapeSeparator(dataSt, messageInfo.getEscapeChar(),
                        "\\\\E\\\\");
            }

            dataSt = ecapeSeparator(dataSt, messageInfo.getFieldSepChar(),
                    "\\\\F\\\\");
            dataSt = ecapeSeparator(dataSt, messageInfo.getCompSepChar(),
                    "\\\\S\\\\");
            if (!messageInfo.getSubCompChar().equals("")) {
                dataSt = ecapeSeparator(dataSt, messageInfo.getSubCompChar(),
                        "\\\\T\\\\");
            }
            dataSt = ecapeSeparator(dataSt, messageInfo.getRepSepChar(),
                    "\\\\R\\\\");

        }
        if (!dataSt.equals("")) {
            er7.append(dataSt);
        }
    }
}
