/*
 * NIST Healthcare Core
 * MessageValidationResult.java May 10, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.v2;

import gov.nist.healthcare.core.Constants.MessageEncoding;
import gov.nist.healthcare.core.message.v2.HL7V2Message;
import gov.nist.healthcare.core.message.v2.xml.XmlMessage;
import gov.nist.healthcare.core.profile.Profile;
import gov.nist.healthcare.core.validation.message.MessageValidationConstants;
import gov.nist.healthcare.core.validation.message.MessageValidationResult;
import gov.nist.healthcare.message.EncodingConstants;
import gov.nist.healthcare.validation.AssertionResultConstants;
import gov.nist.healthcare.validation.AssertionResultConstants.Enum;
import gov.nist.healthcare.validation.AssertionTypeV2Constants;
import gov.nist.healthcare.validation.ErrorSeverityConstants;
import gov.nist.healthcare.validation.message.ReportHeader;
import gov.nist.healthcare.validation.message.ReportHeader.TestObjectReferenceList;
import gov.nist.healthcare.validation.message.ReportHeader.TestObjectReferenceList.TestObjectReference;
import gov.nist.healthcare.validation.message.ReportHeader.ValidationObjectReferenceList;
import gov.nist.healthcare.validation.message.ReportHeader.ValidationObjectReferenceList.ValidationObjectReference;
import gov.nist.healthcare.validation.message.ResultOfTestType;
import gov.nist.healthcare.validation.message.StandardTypeType;
import gov.nist.healthcare.validation.message.TestObjectReferenceType;
import gov.nist.healthcare.validation.message.ValidationObjectReferenceType;
import gov.nist.healthcare.validation.message.ValidationStatusType;
import gov.nist.healthcare.validation.message.ValidationType;
import gov.nist.healthcare.validation.message.hl7.v2.report.HL7V2MessageReport;
import gov.nist.healthcare.validation.message.hl7.v2.report.HL7V2MessageReport.AssertionList;
import gov.nist.healthcare.validation.message.hl7.v2.report.HL7V2MessageReport.AssertionList.Assertion;
import gov.nist.healthcare.validation.message.hl7.v2.report.HL7V2MessageReport.MetaData;
import gov.nist.healthcare.validation.message.hl7.v2.report.HL7V2MessageReport.MetaData.Message;
import gov.nist.healthcare.validation.message.hl7.v2.report.HL7V2MessageValidationReportDocument;
import gov.nist.healthcare.validation.message.hl7.v2.report.HL7V2MessageValidationReportDocument.HL7V2MessageValidationReport;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.xmlbeans.StringEnumAbstractBase;

/**
 * This class contains the result of a message validation for V2
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageValidationResultV2 extends
        MessageValidationResult<MessageFailureV2> {

    // private final HL7V2Message message;
    private final Profile profile;

    // private final List<MessageFailureV2> messageFailures;
    // private final MessageValidationContextV2 context;

    /**
     * Constructor
     * 
     * @param message
     *        the message used by the validation
     * @param profile
     *        the profile used by the validation
     * @param context
     *        the message validation context used by the validation
     * @param messageFailures
     *        the message failures detected by the validation
     */
    public MessageValidationResultV2(HL7V2Message message, Profile profile,
            MessageValidationContextV2 context,
            List<MessageFailureV2> messageFailures) {
        this.message = message;
        this.profile = profile;
        this.context = context;
        this.messageFailures = messageFailures;
        generateAffirmatives();
        count();
    }

    /**
     * Constructor
     * 
     * @param message
     *        the message used by the validation
     * @param context
     *        the message validation context used by the validation
     * @param messageFailures
     *        the message failures detected by the validation
     */
    public MessageValidationResultV2(HL7V2Message message,
            MessageValidationContextV2 context,
            List<MessageFailureV2> messageFailures) {
        this(message, null, context, messageFailures);
    }

    /**
     * Generate affirmatives for all the assertion types.
     */
    private void generateAffirmatives() {
        // Create the descriptions for all potential ERROR assertion type
        Map<AssertionTypeV2Constants.Enum, String> descriptions = new HashMap<AssertionTypeV2Constants.Enum, String>();
        descriptions.put(AssertionTypeV2Constants.USAGE,
                "The message contains no usage error.");
        descriptions.put(AssertionTypeV2Constants.CARDINALITY,
                "The message contains no cardinality error.");
        descriptions.put(AssertionTypeV2Constants.LENGTH,
                "The message contains no length error.");
        descriptions.put(AssertionTypeV2Constants.DATATYPE,
                "The message contains no datatype error.");
        descriptions.put(AssertionTypeV2Constants.DATA,
                "The message contains no data (value of an element) error.");

        // Remove the description is an error is found for the assertion type
        Map<StringEnumAbstractBase, Enum> map = context.getFailureResults();
        Iterator<MessageFailureV2> it = messageFailures.iterator();
        MessageFailureV2 mf = null;
        while (it.hasNext()) {
            mf = it.next();
            // If there is one FATAL failure, we generate nothing
            if (mf.getFailureSeverity().equals(ErrorSeverityConstants.FATAL)) {
                descriptions.clear();
                break;
            }
            if (map.get(mf.getFailureType()).equals(
                    AssertionResultConstants.ERROR)) {
                descriptions.remove(mf.getFailureType());
            }
        }

        // Add the affirmatives
        for (AssertionTypeV2Constants.Enum assertionType : descriptions.keySet()) {
            if (map.get(assertionType).equals(AssertionResultConstants.ERROR)) {
                mf = new MessageFailureV2(message.getEncoding());
                mf.setDescription(descriptions.get(assertionType));
                mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                mf.setFailureType(AssertionTypeV2Constants.CHECKED);
                messageFailures.add(mf);
            }
        }
    }

    // /**
    // * Calculate the count variables
    // */
    // private void count() {
    // HashMap<AssertionTypeV2Constants.Enum, AssertionResultConstants.Enum> map
    // = context.getFailureResults();
    // affirmativeCount = 0;
    // errorCount = 0;
    // warningCount = 0;
    // ignoreCount = 0;
    // alertCount = 0;
    // Iterator<MessageFailureV2> it = messageFailures.iterator();
    // MessageFailureV2 mf = null;
    // while (it.hasNext()) {
    // mf = it.next();
    // Enum assertionResult = mf.getAssertionResult();
    // if (assertionResult != null) {
    // if (assertionResult == AssertionResultConstants.ERROR) {
    // errorCount++;
    // } else if (assertionResult == AssertionResultConstants.WARNING) {
    // warningCount++;
    // } else if (assertionResult == AssertionResultConstants.IGNORE) {
    // ignoreCount++;
    // } else if (assertionResult == AssertionResultConstants.ALERT) {
    // alertCount++;
    // } else if (assertionResult == AssertionResultConstants.AFFIRMATIVE) {
    // affirmativeCount++;
    // }
    // } else {
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
    // }

    public MessageValidationContextV2 getContext() {
        return (MessageValidationContextV2) context;
    }

    // public int getErrorCount() {
    // return errorCount;
    // }
    //
    // public int getWarningCount() {
    // return warningCount;
    // }
    //
    // public int getIgnoreCount() {
    // return ignoreCount;
    // }
    //
    // public int getAlertCount() {
    // return alertCount;
    // }
    //
    // public int getAffirmationCount() {
    // return affirmativeCount;
    // }

    // /**
    // * Get all message failure depending on the FailureLevel
    // *
    // * @param failureResult
    // * @return a list of MessageFailureV2
    // */
    // public List<MessageFailureV2> getMessageFailure(
    // AssertionResultConstants.Enum failureResult) {
    // ArrayList<MessageFailureV2> al = new ArrayList<MessageFailureV2>();
    // Iterator<MessageFailureV2> it = messageFailures.iterator();
    // MessageFailureV2 mf = null;
    // while (it.hasNext()) {
    // mf = it.next();
    // Enum assertionResult = mf.getAssertionResult();
    // if (assertionResult != null && assertionResult == failureResult) {
    // al.add(mf);
    // } else if (assertionResult == null) {
    // if (context.getFailureResult(
    // (gov.nist.healthcare.validation.AssertionTypeV2Constants.Enum)
    // mf.getFailureType()).equals(
    // failureResult)) {
    // al.add(mf);
    // }
    // }
    // }
    // return al;
    // }
    //
    // /**
    // * Get all errors depending on the MessageValidationContext settings
    // *
    // * @return an Iterator of MessageFailureV2
    // */
    // @Override
    // public Iterator<MessageFailureV2> getErrors() {
    // return getMessageFailure(AssertionResultConstants.ERROR).iterator();
    // }
    //
    // /**
    // * Get all warnings depending on the MessageValidationContext settings
    // *
    // * @return an Iterator of MessageFailureV2
    // */
    // @Override
    // public Iterator<MessageFailureV2> getWarnings() {
    // return getMessageFailure(AssertionResultConstants.WARNING).iterator();
    // }
    //
    // /**
    // * Get all ignores depending on the MessageValidationContext settings
    // *
    // * @return an Iterator of MessageFailureV2
    // */
    // @Override
    // public Iterator<MessageFailureV2> getIgnores() {
    // return getMessageFailure(AssertionResultConstants.IGNORE).iterator();
    // }
    //
    // /**
    // * Get all alerts depending on the MessageValidationContext settings
    // *
    // * @return an Iterator of MessageFailureV2
    // */
    // @Override
    // public Iterator<MessageFailureV2> getAlerts() {
    // return getMessageFailure(AssertionResultConstants.ALERT).iterator();
    // }
    //
    // /**
    // * Get all affirmatives depending on the MessageValidationContext settings
    // *
    // * @return an Iterator of MessageFailureV2
    // */
    // @Override
    // public Iterator<MessageFailureV2> getAffirmatives() {
    // return
    // getMessageFailure(AssertionResultConstants.AFFIRMATIVE).iterator();
    // }

    /**
     * Build a report represented as an XMLBeans document
     * 
     * @return a HL7V2MessageValidationReportDocument object
     */
    public HL7V2MessageValidationReportDocument getReport() {
        String profileFilename = "", messageFilename = "";
        if (profile != null) {
            profileFilename = profile.getFilename();
        }
        if (message != null) {
            messageFilename = message.getFilename();
        }
        return getReport(profileFilename, messageFilename, true);
    }

    /**
     * Build a report represented as an XMLBeans document
     * 
     * @param includeValidationContext
     * @return a HL7V2MessageValidationReportDocument object
     */
    public HL7V2MessageValidationReportDocument getReport(
            boolean includeValidationContext) {
        String profileFilename = "", messageFilename = "";
        if (profile != null) {
            profileFilename = profile.getFilename();
        }
        if (message != null) {
            messageFilename = message.getFilename();
        }
        return getReport(profileFilename, messageFilename,
                includeValidationContext);
    }

    /**
     * Build a report represented as an XMLBeans document
     * 
     * @param profileFilename
     * @param messageFilename
     * @param includeValidationContext
     * @return a HL7V2MessageValidationReportDocument object
     */
    public HL7V2MessageValidationReportDocument getReport(
            String profileFilename, String messageFilename,
            boolean includeValidationContext) {
        HL7V2MessageValidationReportDocument doc = HL7V2MessageValidationReportDocument.Factory.newInstance();
        HL7V2MessageValidationReport vr = doc.addNewHL7V2MessageValidationReport();
        setReportHeader(vr);
        setReportSpecific(vr, profileFilename, messageFilename,
                includeValidationContext);
        return doc;
    }

    /**
     * Create the report header.
     * 
     * @param report
     *        the report
     */
    private void setReportHeader(HL7V2MessageValidationReport report) {
        ReportHeader header = report.addNewHeaderReport();
        header.setValidationStatus(ValidationStatusType.COMPLETE);
        header.setServiceName("NIST HL7V2 Message Validation");
        header.setServiceProvider("NIST");
        header.setServiceVersion(MessageValidationConstants.MESSAGE_VALIDATION_V2_VERSION);
        header.setStandardType(StandardTypeType.HL_7_V_2);
        if (profile != null) {
            header.setStandardVersion(profile.getHl7VersionAsString());
        }
        header.setValidationType(ValidationType.AUTOMATED);
        header.setTestIdentifier("");
        // Date Test
        Calendar cal = Calendar.getInstance();
        header.setDateOfTest(cal);
        // Time of Test
        header.setTimeOfTest(cal);

        ValidationObjectReferenceList vorl = header.addNewValidationObjectReferenceList();
        if (profile != null || context != null) {
            if (profile != null && profile.getFilename() != null) {
                ValidationObjectReference profileReference = vorl.addNewValidationObjectReference();
                profileReference.setType(ValidationObjectReferenceType.PROFILE);
                profileReference.setStringValue(profile.getFilename());
            }
            if (context != null && context.getFilename() != null) {
                ValidationObjectReference contextReference = vorl.addNewValidationObjectReference();
                contextReference.setType(ValidationObjectReferenceType.VALIDATION_CONTEXT);
                contextReference.setStringValue(context.getFilename());
            }
        }
        TestObjectReferenceList torl = header.addNewTestObjectReferenceList();
        if (message != null && message.getFilename() != null) {
            TestObjectReference messageReference = torl.addNewTestObjectReference();
            messageReference.setType(TestObjectReferenceType.FILENAME);
            messageReference.setStringValue(message.getFilename());
        }

        header.setPositiveAssertionIndicator(true);
        if (errorCount > 0) {
            header.setResultOfTest(ResultOfTestType.FAILED);
        } else if (errorCount == 0 && alertCount == 0) {
            header.setResultOfTest(ResultOfTestType.PASSED);
        } else if (errorCount == 0 && alertCount > 0) {
            header.setResultOfTest(ResultOfTestType.INCONCLUSIVE);
        }

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
     * @param profileFilename
     * @param messageFilename
     * @param includeValidationContext
     */
    private void setReportSpecific(HL7V2MessageValidationReport report,
            String profileFilename, String messageFilename,
            boolean includeValidationContext) {
        HL7V2MessageReport specific = report.addNewSpecificReport();
        // MetaData
        MetaData md = specific.addNewMetaData();
        if (profile != null) {
            gov.nist.healthcare.validation.message.hl7.v2.report.HL7V2MessageReport.MetaData.Profile metaProfile = md.addNewProfile();
            metaProfile.setOrganization(profile.getOrganization());
            metaProfile.setName(profile.getName());
            StringBuffer sb = new StringBuffer();
            sb.append(profile.getMessageType()).append("^").append(
                    profile.getMessageEvent()).append("^").append(
                    profile.getMessageStructureID());
            metaProfile.setType(sb.toString());
            sb = null;
            // TODO:
            metaProfile.setVersion(profile.getVersion());
            metaProfile.setHL7Version(profile.getHl7VersionAsString());
        }
        if (message != null) {
            Message metaMessage = md.addNewMessage();
            metaMessage.setFilename(messageFilename);
            if (message.getEncoding() == MessageEncoding.V2_ER7) {
                metaMessage.setEncoding(EncodingConstants.ER_7);
                metaMessage.setEr7Message(message.getMessageAsString());
            } else if (message.getEncoding() == MessageEncoding.V2_XML) {
                metaMessage.setEncoding(EncodingConstants.XML);
                metaMessage.setXmlMessage(((XmlMessage) message).getDocument());
            }
            metaMessage.setTransformed(false);
        }
        // Message Validation Context
        if (includeValidationContext) {
            md.setContext(getContext().getContext().getHL7V2MessageValidationContextDefinition());
        }
        // Assertions
        AssertionList assertions = specific.addNewAssertionList();
        Map<StringEnumAbstractBase, Enum> map = context.getFailureResults();
        // Affirmative / Error / Warning / Ignore / Alert
        Iterator<MessageFailureV2> itMF = messageFailures.iterator();
        MessageFailureV2 mf = null;
        while (itMF.hasNext()) {
            mf = itMF.next();
            String elementContent = mf.getElementContent();
            String assertionDeclaration = mf.getAssertionDeclaration();
            if (assertionDeclaration == null) {
                assertionDeclaration = mf.getUserComment();
            }
            Assertion assertion = assertions.addNewAssertion();
            assertion.setType((gov.nist.healthcare.validation.AssertionTypeV2Constants.Enum) mf.getFailureType());
            if (mf.getAssertionResult() != null) {
                assertion.setResult(mf.getAssertionResult());
            } else {
                assertion.setResult(map.get(mf.getFailureType()));
            }
            assertion.setSeverity(mf.getFailureSeverity());
            if (elementContent != null) {
                assertion.setContent(elementContent);
            }
            assertion.setDescription(mf.getDescription());
            if (assertionDeclaration != null) {
                assertion.setAssertionDeclaration(assertionDeclaration);
            }
            if (profile != null) {
                assertion.setLocation(mf.getLocationForReport(profile));
            } else {
                assertion.setLocation(mf.getLocationForReport());
            }

        }
    }
}
