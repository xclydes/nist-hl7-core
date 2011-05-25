/*
 * NIST Healthcare Core
 * MessageValidationResult.java May 10, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.v3;

import gov.nist.healthcare.core.message.v3.HL7V3Message;
import gov.nist.healthcare.core.validation.message.MessageValidationConstants;
import gov.nist.healthcare.core.validation.message.MessageValidationResult;
import gov.nist.healthcare.validation.AssertionResultConstants.Enum;
import gov.nist.healthcare.validation.message.ReportHeader;
import gov.nist.healthcare.validation.message.ReportHeader.TestObjectReferenceList;
import gov.nist.healthcare.validation.message.ReportHeader.TestObjectReferenceList.TestObjectReference;
import gov.nist.healthcare.validation.message.StandardTypeType;
import gov.nist.healthcare.validation.message.TestObjectReferenceType;
import gov.nist.healthcare.validation.message.ValidationStatusType;
import gov.nist.healthcare.validation.message.ValidationType;
import gov.nist.healthcare.validation.message.hl7.v3.report.HL7V3MessageReport;
import gov.nist.healthcare.validation.message.hl7.v3.report.HL7V3MessageReport.AssertionList;
import gov.nist.healthcare.validation.message.hl7.v3.report.HL7V3MessageReport.AssertionList.Assertion;
import gov.nist.healthcare.validation.message.hl7.v3.report.HL7V3MessageReport.AssertionList.Assertion.Location;
import gov.nist.healthcare.validation.message.hl7.v3.report.HL7V3MessageReport.MetaData;
import gov.nist.healthcare.validation.message.hl7.v3.report.HL7V3MessageReport.MetaData.Message;
import gov.nist.healthcare.validation.message.hl7.v3.report.HL7V3MessageValidationReportDocument;
import gov.nist.healthcare.validation.message.hl7.v3.report.HL7V3MessageValidationReportDocument.HL7V3MessageValidationReport;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.xmlbeans.StringEnumAbstractBase;

/**
 * This class contains the result of a message validation for V3
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageValidationResultV3 extends
        MessageValidationResult<MessageFailureV3> {

    // private final HL7V3Message message;

    // private final List<MessageFailureV3> messageFailures;

    // private final MessageValidationContextV3 context;

    /**
     * Constructor
     * 
     * @param message
     *        the message used by the validation
     * @param context
     *        the message validation context used by the validation
     * @param structureFailures
     *        the result of the structural validation done with the web service
     * @param contentFailures
     *        the content failures detected by the validation
     */
    public MessageValidationResultV3(HL7V3Message message,
            MessageValidationContextV3 context,
            List<MessageFailureV3> structureFailures,
            List<MessageFailureV3> contentFailures) {
        this.message = message;
        this.context = context;
        messageFailures = new ArrayList<MessageFailureV3>();
        if (contentFailures != null) {
            messageFailures.addAll(contentFailures);
        }
        if (structureFailures != null) {
            messageFailures.addAll(structureFailures);
        }
        count();
    }

    /**
     * Constructor
     * 
     * @param message
     *        the message used by the validation
     * @param structureFailures
     *        the result of the structural validation done with the web service
     */
    public MessageValidationResultV3(HL7V3Message message,
            List<MessageFailureV3> structureFailures) {
        this.message = message;
        this.context = new MessageValidationContextV3();
        messageFailures = new ArrayList<MessageFailureV3>();
        messageFailures.addAll(structureFailures);
        count();
    }

    /**
     * Constructor
     * 
     * @param message
     *        the message used by the validation
     * @param context
     *        the message validation context used by the validation
     * @param contentFailures
     *        the content failures detected by the validation
     */
    public MessageValidationResultV3(HL7V3Message message,
            MessageValidationContextV3 context,
            List<MessageFailureV3> contentFailures) {
        this(message, context, null, contentFailures);
    }

    // /**
    // * Calculate the count variables
    // */
    // private void count() {
    // HashMap<AssertionTypeV3Constants.Enum, AssertionResultConstants.Enum> map
    // = context.getFailureResults();
    // affirmativeCount = 0;
    // errorCount = 0;
    // warningCount = 0;
    // ignoreCount = 0;
    // alertCount = 0;
    // Iterator<MessageFailureV3> it = messageFailures.iterator();
    // MessageFailureV3 mf = null;
    // while (it.hasNext()) {
    // mf = it.next();
    // if (map.get(mf.getFailureType()).equals(
    // AssertionResultConstants.ERROR)) {
    // errorCount++;
    // } else if (map.get(mf.getFailureType()).equals(
    // AssertionResultConstants.WARNING)) {
    // warningCount++;
    // } else if (map.get(mf.getFailureType()).equals(
    // AssertionResultConstants.IGNORE)) {
    // ignoreCount++;
    // } else if (map.get(mf.getFailureType()).equals(
    // AssertionResultConstants.ALERT)) {
    // alertCount++;
    // } else if (map.get(mf.getFailureType()).equals(
    // AssertionResultConstants.AFFIRMATIVE)) {
    // affirmativeCount++;
    // }
    // }
    // }

    public MessageValidationContextV3 getContext() {
        return (MessageValidationContextV3) context;
    }

    // /**
    // * Get all message failure depending on the FailureLevel
    // *
    // * @param failureResult
    // * @return a list of MessageFailureV3
    // */
    // public List<MessageFailureV3> getMessageFailure(
    // AssertionResultConstants.Enum failureResult) {
    // ArrayList<MessageFailureV3> al = new ArrayList<MessageFailureV3>();
    // Iterator<MessageFailureV3> it = messageFailures.iterator();
    // MessageFailureV3 mf = null;
    // while (it.hasNext()) {
    // mf = it.next();
    // if (context.getFailureResult(mf.getFailureType()).equals(
    // failureResult)) {
    // al.add(mf);
    // }
    // }
    // return al;
    // }

    // /**
    // * Get all errors depending on the MessageValidationContext settings
    // *
    // * @return an Iterator of MessageFailureV3
    // */
    // public Iterator<MessageFailureV3> getErrors() {
    // return getMessageFailure(AssertionResultConstants.ERROR).iterator();
    // }
    //
    // /**
    // * Get all warnings depending on the MessageValidationContext settings
    // *
    // * @return an Iterator of MessageFailureV3
    // */
    // public Iterator<MessageFailureV3> getWarnings() {
    // return getMessageFailure(AssertionResultConstants.WARNING).iterator();
    // }
    //
    // /**
    // * Get all ignores depending on the MessageValidationContext settings
    // *
    // * @return an Iterator of MessageFailureV3
    // */
    // public Iterator<MessageFailureV3> getIgnores() {
    // return getMessageFailure(AssertionResultConstants.IGNORE).iterator();
    // }
    //
    // /**
    // * Get all alerts depending on the MessageValidationContext settings
    // *
    // * @return an Iterator of MessageFailureV3
    // */
    // public Iterator<MessageFailureV3> getAlerts() {
    // return getMessageFailure(AssertionResultConstants.ALERT).iterator();
    // }
    //
    // /**
    // * Get all affirmatives depending on the MessageValidationContext settings
    // *
    // * @return an Iterator of MessageFailureV3
    // */
    // public Iterator<MessageFailureV3> getAffirmatives() {
    // return
    // getMessageFailure(AssertionResultConstants.AFFIRMATIVE).iterator();
    // }

    /**
     * Build a report represented as an XMLBeans document
     * 
     * @return a HL7V3MessageValidationReportDocument object
     */
    public HL7V3MessageValidationReportDocument getReport() {
        String messageFilename = "";
        if (message != null) {
            messageFilename = message.getFilename();
        }
        return getReport(messageFilename);
    }

    /**
     * Build a report represented as an XMLBeans document
     * 
     * @param messageFilename
     * @return a HL7V3MessageValidationReportDocument object
     */
    public HL7V3MessageValidationReportDocument getReport(String messageFilename) {
        HL7V3MessageValidationReportDocument doc = HL7V3MessageValidationReportDocument.Factory.newInstance();
        HL7V3MessageValidationReport vr = doc.addNewHL7V3MessageValidationReport();
        setReportHeader(vr);
        setReportSpecific(vr, messageFilename);
        return doc;
    }

    /**
     * Create the report header.
     * 
     * @param report
     *        the report
     */
    private void setReportHeader(HL7V3MessageValidationReport report) {
        ReportHeader header = report.addNewHeaderReport();
        header.setValidationStatus(ValidationStatusType.COMPLETE);
        header.setServiceName("NIST HL7V3 Message Validation");
        header.setServiceProvider("NIST");
        header.setServiceVersion(MessageValidationConstants.MESSAGE_VALIDATION_V3_VERSION);
        header.setStandardType(StandardTypeType.HL_7_V_3);
        header.setValidationType(ValidationType.AUTOMATED);
        // Date Test
        Calendar cal = Calendar.getInstance();
        header.setDateOfTest(cal);
        // Time of Test
        header.setTimeOfTest(cal);

        TestObjectReferenceList torl = header.addNewTestObjectReferenceList();
        TestObjectReference messageReference = torl.addNewTestObjectReference();
        if (message.getFilename() != null) {
            messageReference.setType(TestObjectReferenceType.FILENAME);
            messageReference.setStringValue(message.getFilename());
        }

        header.setPositiveAssertionIndicator(true);

        // header.setTotalAssertionsMade();
        header.setAffirmCount(affirmativeCount);
        header.setErrorCount(errorCount);
        header.setWarningCount(warningCount);
        header.setIgnoreCount(ignoreCount);
        header.setAlertCount(alertCount);

        header.setTestObject(message.getMessageAsString());
    }

    /**
     * Create the specific part of the report.
     * 
     * @param report
     *        the report
     * @param messageFilename
     */
    private void setReportSpecific(HL7V3MessageValidationReport report,
            String messageFilename) {
        HL7V3MessageReport specific = report.addNewSpecificReport();
        // MetaData
        MetaData md = specific.addNewMetaData();
        if (message != null) {
            Message metaMessage = md.addNewMessage();
            metaMessage.setFilename(messageFilename);
        }
        // Message Validation Context
        md.setContext(getContext().getContext().getHL7V3MessageValidationContextDefinition());
        // Assertions
        AssertionList assertions = specific.addNewAssertionList();
        Map<StringEnumAbstractBase, Enum> map = context.getFailureResults();
        // Affirmative / Error / Warning / Ignore / Alert
        Iterator<MessageFailureV3> itMF = messageFailures.iterator();
        MessageFailureV3 mf = null;
        while (itMF.hasNext()) {
            mf = itMF.next();
            String elementContent = mf.getElementContent();
            String assertionDeclaration = mf.getAssertionDeclaration();
            if (assertionDeclaration == null) {
                assertionDeclaration = mf.getUserComment();
            }
            Assertion assertion = assertions.addNewAssertion();
            assertion.setType((gov.nist.healthcare.validation.AssertionTypeV3Constants.Enum) mf.getFailureType());
            assertion.setResult(map.get(mf.getFailureType()));
            assertion.setSeverity(mf.getFailureSeverity());
            if (elementContent != null) {
                assertion.setContent(elementContent);
            }
            assertion.setDescription(mf.getDescription());
            if (assertionDeclaration != null) {
                assertion.setAssertionDeclaration(assertionDeclaration);
            }
            if (mf.getPath() != null) {
                Location location = assertion.addNewLocation();
                location.setXPath(mf.getPath());
            }
        }
    }
}
