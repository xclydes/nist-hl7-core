/*
 * NIST Healthcare Core
 * MessageStructureValidationV3.java September 10, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.structure.v3;

import gov.nist.healthcare.core.message.v3.HL7V3Message;
import gov.nist.healthcare.core.validation.message.MessageValidationException;
import gov.nist.healthcare.core.validation.message.v3.MessageFailureV3;
import gov.nist.healthcare.validation.AssertionTypeV3Constants;
import gov.nist.healthcare.validation.ErrorSeverityConstants;
import java.util.ArrayList;
import java.util.List;
import org.apache.xmlbeans.XmlObject;

/**
 * This class validates the message structure for a V3 message.
 * 
 * @author Harold AFFO (NIST)
 */
public class MessageStructureValidationV3 {

    private XmlObject xmlResult;

    public List<MessageFailureV3> validate(HL7V3Message message,
            String schemaLocation, String schematronLocation)
            throws MessageValidationException {
        MessageStructureV3Validator validator = new MessageStructureV3Validator();
        xmlResult = validator.validate(message.getMessageAsString(),
                schemaLocation, schematronLocation);
        List<MessageFailureV3> messageFailures = getMessageFailures();
        return messageFailures;
    }

    public List<MessageFailureV3> validate(HL7V3Message message,
            String schemaLocation) throws MessageValidationException {
        MessageStructureV3Validator validator = new MessageStructureV3Validator();
        xmlResult = validator.validate(message.getMessageAsString(),
                schemaLocation);
        List<MessageFailureV3> messageFailures = getMessageFailures();
        return messageFailures;
    }

    private String getValue(XmlObject obj, String path) {
        String value = null;
        XmlObject[] res = obj.selectPath(path);
        if (res != null && res.length != 0) {
            value = res[0].newCursor().getTextValue();
        }
        return value;
    }

    private List<MessageFailureV3> getMessageFailures() {
        List<MessageFailureV3> failures = new ArrayList<MessageFailureV3>();
        XmlObject[] results = xmlResult.selectPath("/Report/Results");
        for (XmlObject result : results) {
            XmlObject[] xmlIssues = result.selectPath("issue");
            if (xmlIssues != null) {
                for (XmlObject xmlIssue : xmlIssues) {
                    MessageFailureV3 mf = new MessageFailureV3();
                    mf.setFailureType(AssertionTypeV3Constants.STRUCTURE);
                    mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                    mf.setDescription(getIssueMessage(xmlIssue));
                    mf.setPath(getIssueContext(xmlIssue));
                    mf.setAssertionDeclaration(getIssueTest(xmlIssue));
                    failures.add(mf);
                }
            }
        }
        return failures;
    }

    private String getIssueMessage(XmlObject issue) {
        String message = getValue(issue, "message");
        return message;
    }

    private String getIssueContext(XmlObject issue) {
        String message = getValue(issue, "context");
        return message;
    }

    private String getIssueTest(XmlObject issue) {
        String message = getValue(issue, "test");
        return message;
    }

}
