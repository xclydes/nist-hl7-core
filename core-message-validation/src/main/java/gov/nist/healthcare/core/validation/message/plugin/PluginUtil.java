package gov.nist.healthcare.core.validation.message.plugin;

import gov.nist.healthcare.core.Constants.MessageEncoding;
import gov.nist.healthcare.core.validation.message.MessageFailure;
import gov.nist.healthcare.core.validation.message.v2.MessageFailureV2;
import gov.nist.healthcare.core.validation.message.v3.MessageFailureV3;
import gov.nist.healthcare.validation.ErrorSeverityConstants;
import java.util.List;

public class PluginUtil {

    /**
     * Format the list of values
     * 
     * @param values
     * @return the formatted list
     */
    public static String valuesToString(List<String> values) {
        StringBuffer sb = new StringBuffer("'");
        if (values.size() > 0) {
            for (String value : values) {
                sb.append(value).append("; ");
            }
            // Remove the trailing ;[space]
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append("'");
        return sb.toString();
    }

    /**
     * Create a MessageFailure object
     * 
     * @param messageEncoding
     * @return a MessageFailure object
     */
    public static MessageFailure getMessageFailure(
            MessageEncoding messageEncoding) {
        MessageFailure mf = null;
        switch (messageEncoding) {
        case V2_ER7:
        case V2_XML:
            mf = new MessageFailureV2(messageEncoding);
            break;
        case V3:
            mf = new MessageFailureV3();
            break;
        default:
            mf = null;
        }
        if (mf != null) {
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
        }
        return mf;
    }

    // /**
    // * Create a MessageFailure object
    // *
    // * @param messageEncoding
    // * @param description
    // * @param location
    // * @param assertionResult
    // * @param userComment
    // * @return a MessageFailure
    // */
    // public static MessageFailure getMessageFailure(
    // MessageEncoding messageEncoding, String description,
    // MessageLocation location,
    // AssertionResultConstants.Enum assertionResult, String userComment) {
    // MessageFailure mf = null;
    // switch (messageEncoding) {
    // case V2_ER7:
    // case V2_XML:
    // mf = new MessageFailureV2(messageEncoding);
    // break;
    // case V3:
    // mf = new MessageFailureV3();
    // break;
    // default:
    // mf = null;
    // }
    // if (mf != null) {
    // mf.setDescription(description);
    // mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
    // if (messageEncoding == MessageEncoding.V2_ER7) {
    // mf.setPath(location.getEPath());
    // mf.setLine(((Er7Message) message).getLine(location));
    // mf.setColumn(((Er7Message) message).getColumn(location));
    // } else if (messageEncoding == MessageEncoding.V2_XML) {
    // mf.setPath(ml.getXPath());
    // }
    //
    // mf.setAssertionResult(assertionResult);
    // mf.setUserComment(userComment);
    // }
    // return mf;
    // }

}
