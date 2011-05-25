/*
 * NIST Healthcare Core
 * MessageContentValidation.java Aug 13, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.content;

import gov.nist.healthcare.core.Constants.MessageEncoding;
import gov.nist.healthcare.core.validation.message.MessageFailure;
import gov.nist.healthcare.core.validation.message.content.v2.MatchFoundResultV2;
import gov.nist.healthcare.core.validation.message.content.v3.MatchFoundResultV3;
import gov.nist.healthcare.core.validation.message.plugin.ValidationPlugin;
import gov.nist.healthcare.core.validation.message.v2.MessageFailureV2;
import gov.nist.healthcare.core.validation.message.v3.MessageFailureV3;
import gov.nist.healthcare.message.MessageElement;
import gov.nist.healthcare.validation.AssertionResultConstants;
import gov.nist.healthcare.validation.AssertionTypeV2Constants;
import gov.nist.healthcare.validation.AssertionTypeV3Constants;
import gov.nist.healthcare.validation.ErrorSeverityConstants;
import gov.nist.healthcare.validation.Value;
import gov.nist.healthcare.validation.Value.PlainText;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * This class contains methods for message content validation
 * 
 * @author Sydney Henrard (NIST)
 */
public abstract class MessageContentValidation {

    protected ObjectMapper mapper = new ObjectMapper();

    /**
     * Get the values from the context
     * 
     * @param values
     *        a List of Value
     * @return a List of PlainText
     */
    protected List<PlainText> getContextValues(List<Value> values) {
        List<PlainText> contextValues = new ArrayList<PlainText>();
        for (Value value : values) {
            if (value.getPlainText() != null) {
                contextValues.add(value.getPlainText());
            } else if (value.isSetEmpty()) {
                contextValues.add(null);
            }
        }
        return contextValues;
    }

    /**
     * Get the values that has to be calculated from the context and message
     * 
     * @param values
     *        a List of Value
     * @return a List of MessageElement
     */
    protected List<MessageElement> getContextMessageValues(List<Value> values) {
        List<MessageElement> contextMessageValues = new ArrayList<MessageElement>();
        for (Value value : values) {
            if (value.getLocation() != null) {
                contextMessageValues.add(value.getLocation());
            }
        }
        return contextMessageValues;
    }

    /**
     * Get the regular expressions from the context
     * 
     * @param values
     *        a List of Value
     * @return a List of String
     */
    protected List<String> getRegexValues(List<Value> values) {
        List<String> regexValues = new ArrayList<String>();
        for (Value value : values) {
            if (value.getRegex() != null) {
                regexValues.add(value.getRegex());
            }
        }
        return regexValues;
    }

    /**
     * Is there a present element check?
     * 
     * @param values
     *        a List of Value
     * @return a boolean set to true; false otherwise
     */
    protected boolean isPresentCheck(List<Value> values) {
        boolean presentCheck = false;
        for (Value value : values) {
            if (value.isSetPresent()) {
                presentCheck = true;
            }
        }
        return presentCheck;
    }

    /**
     * Validate the content based on the parameter
     * 
     * @param checkAll
     *        a boolean to true if ALL the values at the specified location
     *        matches one of the values in the context
     * @param regexCheck
     *        a boolean to true if there is a regular expression check
     * @param emptyCheck
     *        a boolean to true if there is an empty element check
     * @param presentCheck
     *        a boolean to true if there is a present element check
     * @param messageValues
     *        a list of values in the message
     * @param contextValues
     *        a list of values in the context
     * @param regexValues
     *        a list of regular expressions in the context
     * @param path
     *        the location as a string
     * @param messageEncoding
     *        the encoding of the message
     * @param assertionResult
     * @param userComment
     * @return a list of MatchFoundResult object
     */
    protected List<MatchFoundResult> matchFound(boolean checkAll,
            boolean regexCheck, boolean emptyCheck, boolean presentCheck,
            List<String> messageValues, List<PlainText> contextValues,
            List<String> regexValues, String path,
            MessageEncoding messageEncoding,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        List<MatchFoundResult> results = new ArrayList<MatchFoundResult>();
        if (checkAll && messageValues.size() != 0) {
            List<String> value = new ArrayList<String>();
            for (String messageValue : messageValues) {
                value.add(messageValue);
                MatchFoundResult result = matchFound(regexCheck, emptyCheck,
                        presentCheck, value, contextValues, regexValues, path,
                        messageEncoding, assertionResult, userComment);
                // if (result.hasError() || result.getPassedAssertions() !=
                // null) {
                results.add(result);
                // }
                value.clear();
            }
        } else {
            MatchFoundResult result = matchFound(regexCheck, emptyCheck,
                    presentCheck, messageValues, contextValues, regexValues,
                    path, messageEncoding, assertionResult, userComment);
            // if (result.hasError() || result.getPassedAssertions() != null) {
            results.add(result);
            // }
        }
        return results;
    }

    /**
     * Validate the content based on the parameter
     * 
     * @param regexCheck
     *        a boolean to true if there is a regular expression check
     * @param emptyCheck
     *        a boolean to true if there is an empty element check
     * @param presentCheck
     *        a boolean to true if there is a present element check
     * @param messageValues
     *        a list of values in the message
     * @param contextValues
     *        a list of values in the context
     * @param regexValues
     *        a list of regular expressions in the context
     * @param path
     *        the location as a string
     * @param messageEncoding
     *        the encoding of the message
     * @param assertionResult
     * @param userComment
     * @return a MatchFoundResult object
     */
    private MatchFoundResult matchFound(boolean regexCheck, boolean emptyCheck,
            boolean presentCheck, List<String> messageValues,
            List<PlainText> contextValues, List<String> regexValues,
            String path, MessageEncoding messageEncoding,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        MatchFoundResult result = null;
        switch (messageEncoding) {
        case V2_ER7:
        case V2_XML:
            result = new MatchFoundResultV2();
            break;
        case V3:
            result = new MatchFoundResultV3();
            break;
        default:
            result = null;
        }
        boolean matchFound = true;
        if (messageValues.size() == 0) {
            if (presentCheck) {
                matchFound = false;
                StringBuffer sb = new StringBuffer();
                sb.append("The specified message element is not present in the message. ");
                sb.append("The provided path expression is '").append(path).append(
                        "'.");
                MessageFailure mf = getMessageFailure(messageEncoding,
                        sb.toString(), path, userComment);
                switch (messageEncoding) {
                case V2_ER7:
                case V2_XML:
                    ((MessageFailureV2) mf).setFailureType(AssertionTypeV2Constants.DATA);
                    break;
                case V3:
                    ((MessageFailureV3) mf).setFailureType(AssertionTypeV3Constants.DATA);
                    break;
                default:
                }
                mf.setAssertionDeclaration(String.format("%s is present", path));
                mf.setAssertionResult(assertionResult);
                result.setMessageError(mf);
            } else if (!emptyCheck) {
                matchFound = false;
                StringBuffer sb = new StringBuffer();
                sb.append("The specified message element match location does not map to a message element. The data value ");
                sb.append("at the specified match location can't be evaluated. Refine the message element match location. ");
                sb.append("The provided path expression is '").append(path).append(
                        "'.");
                MessageFailure mf = getMessageFailure(messageEncoding,
                        sb.toString(), path, userComment);
                switch (messageEncoding) {
                case V2_ER7:
                case V2_XML:
                    ((MessageFailureV2) mf).setFailureType(AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
                    break;
                case V3:
                    ((MessageFailureV3) mf).setFailureType(AssertionTypeV3Constants.MESSAGE_VALIDATION_CONTEXT);
                    break;
                default:
                }
                StringBuffer expectedValuesSB = new StringBuffer();
                if (contextValues.size() > 0) {
                    for (PlainText expectedValue : contextValues) {
                        if (expectedValue != null) {
                            expectedValuesSB.append(
                                    expectedValue.getStringValue()).append("; ");
                        }
                    }
                    // Remove the trailing ;[space]
                    if (expectedValuesSB.length() > 2) {
                        expectedValuesSB.delete(expectedValuesSB.length() - 2,
                                expectedValuesSB.length());
                    }
                }
                mf.setAssertionDeclaration(String.format("%s = %s", path,
                        expectedValuesSB.toString()));
                mf.setAssertionResult(assertionResult);
                result.setContextError(mf);
            }
        } else {
            matchFound = matchValues(messageValues, contextValues);
            if (!matchFound) {
                if (regexCheck) {
                    matchFound = matchRegex(messageValues, regexValues);
                }
                if (!matchFound && presentCheck) {
                    matchFound = true;
                }
            }
            // List of message values
            StringBuffer messageValuesSB = new StringBuffer();
            for (String value : messageValues) {
                messageValuesSB.append(value).append("; ");
            }
            // Remove the trailing ;[space]
            messageValuesSB.delete(messageValuesSB.length() - 2,
                    messageValuesSB.length());
            // List of expected values
            StringBuffer expectedValuesSB = new StringBuffer();
            if (contextValues.size() > 0) {
                for (PlainText expectedValue : contextValues) {
                    if (expectedValue != null) {
                        expectedValuesSB.append(expectedValue.getStringValue()).append(
                                "; ");
                    }
                }
                contextValues.remove(null);
                // Remove the trailing ;[space]
                if (expectedValuesSB.length() > 2) {
                    expectedValuesSB.delete(expectedValuesSB.length() - 2,
                            expectedValuesSB.length());
                }
            }
            // List of regular expressions
            StringBuffer regexValuesSB = new StringBuffer();
            if (regexValues.size() > 0) {
                for (String regexValue : regexValues) {
                    regexValuesSB.append(regexValue).append("; ");
                }
                // Remove the trailing ;[space]
                regexValuesSB.delete(regexValuesSB.length() - 2,
                        regexValuesSB.length());
            }

            StringBuffer sb = new StringBuffer();
            if (!matchFound) {
                sb.append("The value(s) '").append(messageValuesSB);
                sb.append("' at the given location in the message does not match ");
                if (emptyCheck) {
                    sb.append("an empty value");
                    if (contextValues.size() > 0 || regexValues.size() > 0) {
                        sb.append(" or ");
                    } else {
                        sb.append(".");
                    }
                }
                if (contextValues.size() > 0) {
                    sb.append("one of the expected value '");
                    sb.append(expectedValuesSB);
                    if (regexValues.size() == 0) {
                        sb.append("'.");
                    } else {
                        sb.append("' or ");
                    }
                }
                if (regexValues.size() > 0) {
                    sb.append("one of the regular expression '");
                    sb.append(regexValuesSB);
                    sb.append("'.");
                }
            }

            MessageFailure mf = getMessageFailure(messageEncoding,
                    sb.toString(), path, userComment);
            mf.setElementContent(messageValuesSB.toString());
            switch (messageEncoding) {
            case V2_ER7:
            case V2_XML:
                if (!matchFound) {
                    ((MessageFailureV2) mf).setFailureType(AssertionTypeV2Constants.DATA);
                    mf.setAssertionResult(assertionResult);
                } else {
                    ((MessageFailureV2) mf).setFailureType(AssertionTypeV2Constants.DATA_PASSED);
                }
                break;
            case V3:
                if (!matchFound) {
                    ((MessageFailureV3) mf).setFailureType(AssertionTypeV3Constants.DATA);
                    mf.setAssertionResult(assertionResult);
                } else {
                    ((MessageFailureV3) mf).setFailureType(AssertionTypeV3Constants.DATA_PASSED);
                }
                break;
            default:
            }
            if (!matchFound) {
                result.setMessageError(mf);
            } else {
                List<MessageFailure> assertions = new ArrayList<MessageFailure>();
                assertions.add(mf);
                result.setPassedAssertions(assertions);
            }
        }
        return result;
    }

    /**
     * Return true if one of the values in the message matches one of the
     * expected values
     * 
     * @param msgValues
     * @param expectedValues
     * @return a boolean
     */
    private boolean matchValues(List<String> msgValues,
            List<PlainText> expectedValues) {
        boolean match = false;
        for (int i = 0; i < msgValues.size() && !match; i++) {
            String msgValue = msgValues.get(i);
            for (int j = 0; j < expectedValues.size() && !match; j++) {
                PlainText plainText = expectedValues.get(j);
                if (plainText != null) {
                    if (plainText.getInterpretAsNumber()) {
                        float fMsgValue = 0;
                        float fContextValue = 0;
                        try {
                            fMsgValue = Float.parseFloat(msgValue);
                            fContextValue = Float.parseFloat(plainText.getStringValue());
                        } catch (NumberFormatException nfe) {
                            match = false;
                        }
                        match = fMsgValue == fContextValue;
                    } else {
                        if (plainText.getIgnoreCase()) {
                            match = msgValue.equalsIgnoreCase(plainText.getStringValue());
                        } else {
                            match = msgValue.equals(plainText.getStringValue());
                        }
                    }
                }
            }
        }
        return match;
    }

    /**
     * Return true if one of the values in the message matches one of the
     * regular expression
     * 
     * @param msgValues
     * @param regexValues
     * @return a boolean
     */
    private boolean matchRegex(List<String> msgValues, List<String> regexValues) {
        boolean match = false;
        for (int i = 0; i < msgValues.size() && !match; i++) {
            String msgValue = msgValues.get(i);
            for (int j = 0; j < regexValues.size() && !match; j++) {
                match = msgValue.matches(regexValues.get(j));
            }
        }
        return match;
    }

    /**
     * Create a MessageFailure object
     * 
     * @param messageEncoding
     * @param description
     * @param path
     * @param userComment
     */
    private MessageFailure getMessageFailure(MessageEncoding messageEncoding,
            String description, String path, String userComment) {
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
        mf.setDescription(description);
        mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
        mf.setPath(path);
        mf.setUserComment(userComment);
        return mf;
    }

    /**
     * Load a plugin
     * 
     * @param pluginName
     * @return the plugin; null otherwise
     */
    protected ValidationPlugin loadPlugin(String pluginName) {
        ValidationPlugin plugin = null;
        Object o = null;
        try {
            o = Class.forName(pluginName).newInstance();
        } catch (Exception e) {
        }
        if (o instanceof ValidationPlugin) {
            plugin = (ValidationPlugin) o;
        }
        return plugin;
    }

}
