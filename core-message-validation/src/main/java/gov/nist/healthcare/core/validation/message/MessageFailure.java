/*
 * NIST Healthcare Core
 * MessageFailure.java May 10, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message;

import gov.nist.healthcare.validation.AssertionResultConstants;
import gov.nist.healthcare.validation.ErrorSeverityConstants;
import org.apache.xmlbeans.StringEnumAbstractBase;

/**
 * This class represents a failure when validating a message.
 * 
 * @author Sydney Henrard (NIST)
 */
public abstract class MessageFailure {

    protected String description;
    protected int column;
    protected int line;
    protected String path;
    protected ErrorSeverityConstants.Enum failureSeverity;
    protected String elementContent;
    protected String assertionDeclaration;
    protected String userComment;
    protected AssertionResultConstants.Enum assertionResult;
    protected StringEnumAbstractBase failureType;

    /**
     * Constructor
     */
    public MessageFailure() {
        column = -1;
        line = -1;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ErrorSeverityConstants.Enum getFailureSeverity() {
        return failureSeverity;
    }

    public void setFailureSeverity(ErrorSeverityConstants.Enum failureSeverity) {
        this.failureSeverity = failureSeverity;
    }

    public String getElementContent() {
        return elementContent;
    }

    public void setElementContent(String elementContent) {
        this.elementContent = elementContent;
    }

    public String getAssertionDeclaration() {
        return assertionDeclaration;
    }

    public void setAssertionDeclaration(String comment) {
        this.assertionDeclaration = comment;
    }

    public AssertionResultConstants.Enum getAssertionResult() {
        return assertionResult;
    }

    public void setAssertionResult(AssertionResultConstants.Enum assertionResult) {
        this.assertionResult = assertionResult;
    }

    public String getUserComment() {
        return userComment;
    }

    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }

    public StringEnumAbstractBase getFailureType() {
        return failureType;
    }

    public void setFailureType(StringEnumAbstractBase failureType) {
        this.failureType = failureType;
    }

}
