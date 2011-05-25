/*
 * NIST Healthcare Core
 * XmlBeansUtils.java Jan 21, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.values.XmlObjectBase;

/**
 * This class contains methods specific for processing XmlBeans.
 * 
 * @author Sydney Henrard (NIST)
 */
public final class XmlBeansUtils {

    private XmlBeansUtils() {
    }

    /**
     * Get a value from an XmlObject. It returns the String or the XML fragment.
     * 
     * @param xmlObj
     *        the XmlObject
     * @return the value or the XML fragment as a String; null otherwise
     */
    public static String getValueFromXmlObject(XmlObject xmlObj) {
        String value = null;
        XmlCursor cursor = xmlObj.newCursor();
        if (cursor.isAttr() || (cursor.isStart() && !cursor.toFirstChild())
                || (cursor.isStartdoc() && !cursor.toFirstChild())) {
            // Return the value
            XmlObjectBase n = (XmlObjectBase) xmlObj;
            value = n.getStringValue();
        } else {
            // Return the XML fragment
            value = xmlObj.xmlText();
        }
        return value;
    }

    /**
     * Get the validation messages.
     * 
     * @param validationErrors
     * @return the validation messages
     */
    public static String getValidationMessages(
            ArrayList<XmlError> validationErrors) {
        StringBuffer sb = new StringBuffer();
        Iterator<XmlError> it = validationErrors.iterator();
        while (it.hasNext()) {
            XmlError err = it.next();
            sb.append("[").append(err.getLine()).append(",").append(
                    err.getColumn()).append("] ");
            sb.append(err.getMessage()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Get a value at the specified location in an XmlObject.
     * 
     * @param xmlObj
     * @param location
     * @return the value; null otherwise
     */
    public static String getValue(XmlObject xmlObj, String location) {
        String value = null;
        String[] values = getValues(xmlObj, location);
        if (values.length > 1) {
            value = values[0];
        }
        return value;
    }

    /**
     * Get a list of values at the specified location in an XmlObject.
     * 
     * @param xmlObj
     * @param location
     * @return a list if values; null otherwise
     */
    public static String[] getValues(XmlObject xmlObj, String location) {
        List<String> values = new ArrayList<String>();
        XmlObject[] rs = null;
        rs = xmlObj.selectPath(location);
        for (XmlObject currentRS : rs) {
            String value = XmlBeansUtils.getValueFromXmlObject(currentRS);
            if (value != null) {
                values.add(value);
            }
        }
        return values.toArray(new String[values.size()]);
    }

    /**
     * Get the result of a count XPath expression from an XmlObject..
     * 
     * @param xmlObj
     *        the XmlObject
     * @return the result; -1 otherwise
     */
    public static int getCountValueFromXmlObject(XmlObject xmlObj) {
        int count = -1;
        XmlCursor cursor = xmlObj.newCursor();
        if (cursor.isStartdoc()) {
            XmlObjectBase n = (XmlObjectBase) xmlObj;
            count = n.getIntValue();
        }
        return count;
    }

    /**
     * Validate an XMLObject instance
     * 
     * @param doc
     * @return a list of XmlError
     */
    public static List<XmlError> validate(XmlObject doc) {
        XmlOptions options = new XmlOptions();
        List<XmlError> validationErrors = new ArrayList<XmlError>();
        options.setErrorListener(validationErrors);
        doc.validate(options);
        return validationErrors;
    }

}
