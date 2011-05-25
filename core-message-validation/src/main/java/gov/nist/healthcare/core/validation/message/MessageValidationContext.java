/*
 * NIST Healthcare Core
 * MessageValidationContext.java Oct 28, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message;

import gov.nist.healthcare.validation.AssertionResultConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import org.apache.xmlbeans.StringEnumAbstractBase;
import org.apache.xmlbeans.XmlException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * A parent class for V2 and V3 message validation context
 * 
 * @author Sydney Henrard (NIST)
 */
public abstract class MessageValidationContext {

    protected File contextFile;
    protected Map<StringEnumAbstractBase, AssertionResultConstants.Enum> hFailureResult;
    protected ObjectMapper mapper = new ObjectMapper();

    /**
     * Load a MessageValidationContext as a String
     * 
     * @param xmlMessageValidationContext
     * @throws XmlException
     */
    public abstract void load(String xmlMessageValidationContext)
            throws XmlException;

    /**
     * Load a MessageValidationContext using a File using the platform's default
     * charset.
     * 
     * @param xmlMessageValidationContext
     * @throws IOException
     * @throws XmlException
     */
    public void load(File xmlMessageValidationContext) throws IOException,
            XmlException {
        contextFile = xmlMessageValidationContext;
        StringBuffer sb = new StringBuffer();
        BufferedReader br = new BufferedReader(new FileReader(
                xmlMessageValidationContext));
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        load(sb.toString());
        sb = null;
    }

    /**
     * Load a MessageValidationContext using a File using a specific encoding.
     * 
     * @param xmlMessageValidationContext
     * @param encoding
     * @throws IOException
     * @throws XmlException
     */
    public void load(File xmlMessageValidationContext, String encoding)
            throws IOException, XmlException {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(xmlMessageValidationContext), encoding));
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        String encodedString = new String(sb.toString().getBytes(encoding),
                encoding);
        load(encodedString);
        sb = null;
    }

    /**
     * Return the filename used to create the context
     * 
     * @return the absolute path; null otherwise
     */
    public String getFilename() {
        String filename = null;
        if (contextFile != null) {
            filename = contextFile.getAbsolutePath();
        }
        return filename;
    }

    /**
     * Return the mapping between the assertion types and assertion results.
     * 
     * @return the mapping between the assertion types and assertion results.
     */
    public Map<StringEnumAbstractBase, AssertionResultConstants.Enum> getFailureResults() {
        return hFailureResult;
    }

    /**
     * Get a FailureResult for a MessageFailureType
     * 
     * @param failure
     * @return a AssertionResultConstants
     */
    public AssertionResultConstants.Enum getFailureResult(
            StringEnumAbstractBase failure) {
        return hFailureResult.get(failure);
    }

}
