/*
 * NIST Healthcare Core
 * SchemaValidationErrorHandler.java Sep 12, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.structure.v3;

import java.util.Iterator;
import java.util.Vector;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author andrew.mccaffrey
 */
public class SchemaValidationErrorHandler implements ErrorHandler {

    private Vector<String> warnings = null;
    private Vector<String> linesWarnings = null;
    private Vector<String> errors = null;
    private Vector<String> linesErrors = null;
    private Vector<String> fatalErrors = null;
    private Vector<String> linesFatalErrors = null;

    /**
     * Creates a new instance of SchemaValidationErrorHandler
     */
    public SchemaValidationErrorHandler() {
    }

    public void warning(SAXParseException exception) throws SAXException {
        this.addWarning(exception.getMessage(),
                Integer.toString(exception.getLineNumber()));
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        this.addFatalError(exception.getMessage(),
                Integer.toString(exception.getLineNumber()));
    }

    public void error(SAXParseException exception) throws SAXException {
        this.addError(exception.getMessage(),
                Integer.toString(exception.getLineNumber()));
    }

    public boolean hasWarnings() {
        return warnings != null && warnings.size() > 0;
    }

    public boolean hasErrors() {
        return errors != null && errors.size() > 0;
    }

    public boolean hasFatalErrors() {
        return fatalErrors != null && fatalErrors.size() > 0;
    }

    public String getPrintableWarnings() {
        StringBuffer sb = new StringBuffer();
        if (warnings != null) {
            Iterator<String> it = getWarnings().iterator();
            while (it.hasNext()) {
                sb.append("Warning: " + it.next() + "\n");
            }
        }
        return sb.toString();
    }

    public String getPrintableErrors() {
        StringBuffer sb = new StringBuffer();
        if (errors != null) {
            Iterator<String> it = getErrors().iterator();
            while (it.hasNext()) {
                sb.append("Error: " + it.next() + "\n");
            }
        }
        return sb.toString();

    }

    public String getPrintableFatalErrors() {
        StringBuffer sb = new StringBuffer();
        if (fatalErrors != null) {
            Iterator<String> it = getFatalErrors().iterator();
            while (it.hasNext()) {
                sb.append("Fatal Error: " + it.next() + "\n");
            }
        }
        return sb.toString();
    }

    public boolean addWarning(String warning, String lineNumber) {
        if (getWarnings() == null) {
            setWarnings(new Vector<String>());
            setLinesWarnings(new Vector<String>());
        }
        return (getWarnings().add(warning) && getLinesWarnings().add(lineNumber));
    }

    public boolean addError(String error, String lineNumber) {
        if (getErrors() == null) {
            setErrors(new Vector<String>());
            setLinesErrors(new Vector<String>());
        }
        return (getErrors().add(error) && getLinesErrors().add(lineNumber));
    }

    public boolean addFatalError(String fatalError, String lineNumber) {
        if (getFatalErrors() == null) {
            setFatalErrors(new Vector<String>());
            setLinesErrors(new Vector<String>());
        }
        return (getFatalErrors().add(fatalError) && getLinesErrors().add(
                lineNumber));
    }

    public Vector<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(Vector<String> warnings) {
        this.warnings = warnings;
    }

    public Vector<String> getLinesWarnings() {
        return linesWarnings;
    }

    public void setLinesWarnings(Vector<String> linesWarnings) {
        this.linesWarnings = linesWarnings;
    }

    public Vector<String> getErrors() {
        return errors;
    }

    public void setErrors(Vector<String> errors) {
        this.errors = errors;
    }

    public Vector<String> getLinesErrors() {
        return linesErrors;
    }

    public void setLinesErrors(Vector<String> linesErrors) {
        this.linesErrors = linesErrors;
    }

    public Vector<String> getFatalErrors() {
        return fatalErrors;
    }

    public void setFatalErrors(Vector<String> fatalErrors) {
        this.fatalErrors = fatalErrors;
    }

    public Vector<String> getLinesFatalErrors() {
        return linesFatalErrors;
    }

    public void setLinesFatalErrors(Vector<String> linesFatalErrors) {
        this.linesFatalErrors = linesFatalErrors;
    }

    public int getNumberErrors() {
        try {
            return linesErrors.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public int getNumberWarnings() {
        try {
            return linesWarnings.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public int getNumberFatalErrors() {
        try {
            return linesFatalErrors.size();
        } catch (Exception e) {
            return 0;
        }
    }
}
