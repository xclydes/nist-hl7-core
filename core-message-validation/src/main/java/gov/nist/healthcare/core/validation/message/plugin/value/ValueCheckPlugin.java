package gov.nist.healthcare.core.validation.message.plugin.value;

import gov.nist.healthcare.core.Constants.MessageEncoding;
import gov.nist.healthcare.core.message.HL7Message;
import gov.nist.healthcare.core.message.MessageLocation;
import gov.nist.healthcare.core.message.v2.HL7V2Message;
import gov.nist.healthcare.core.message.v2.er7.Er7Message;
import gov.nist.healthcare.core.message.v3.HL7V3Message;
import gov.nist.healthcare.core.validation.message.MessageFailure;
import gov.nist.healthcare.core.validation.message.plugin.PluginUtil;
import gov.nist.healthcare.core.validation.message.plugin.ValidationPlugin;
import gov.nist.healthcare.core.validation.message.plugin.value.ValueCheck.ValueCheckType;
import gov.nist.healthcare.core.validation.message.util.MessageElementUtil;
import gov.nist.healthcare.validation.AssertionResultConstants;
import gov.nist.healthcare.validation.AssertionTypeV2Constants;
import gov.nist.healthcare.validation.AssertionTypeV3Constants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

public class ValueCheckPlugin extends ValidationPlugin {

    @Override
    public List<MessageFailure> validate(HL7Message message, String params,
            AssertionResultConstants.Enum assertionResult, String userComment)
            throws JsonParseException, JsonMappingException, IOException {
        List<MessageFailure> messageFailures = null;
        if (message instanceof HL7V2Message) {
            messageFailures = validateMessage((HL7V2Message) message, params,
                    assertionResult, userComment);
        } else if (message instanceof HL7V3Message) {
            messageFailures = validateMessage((HL7V3Message) message, params,
                    assertionResult, userComment);
        }
        return messageFailures;
    }

    private List<MessageFailure> validateMessage(HL7V2Message message,
            String params, AssertionResultConstants.Enum assertionResult,
            String userComment) throws JsonParseException,
            JsonMappingException, IOException {
        ValueCheckParam vcParams = mapper.readValue(params,
                ValueCheckParam.class);
        return validate(message, vcParams.getLocation(), vcParams.getValues(),
                vcParams.isMatchAll(), vcParams.getMinMatch(),
                vcParams.getMaxMatch(), assertionResult, userComment);
    }

    private List<MessageFailure> validate(HL7V2Message message,
            String location, List<ValueCheck> values, boolean matchAll,
            int minMatch, String maxMatch,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        List<MessageFailure> messageFailures = new ArrayList<MessageFailure>();
        List<MessageFailure> partialMessageFailures = new ArrayList<MessageFailure>();
        List<MessageLocation> locations = MessageElementUtil.getMessageLocations(
                MessageElementUtil.getMessageElement(location), message);
        int countLocationMatch = 0;
        for (MessageLocation messageLocation : locations) {
            int countValueMatch = 0;
            partialMessageFailures.clear();
            String valueInMessage = message.getValue(messageLocation);
            for (ValueCheck value : values) {
                ValueCheckResult result = checkValue(message, valueInMessage,
                        messageLocation, value, userComment);
                if (result.isPassed()) {
                    countValueMatch++;
                }
                MessageFailure partialMessageFailure = result.getMessageFailure();
                if (partialMessageFailure != null) {
                    if (partialMessageFailure.getFailureType() != AssertionTypeV2Constants.DATA_PASSED) {
                        partialMessageFailure.setAssertionResult(assertionResult);
                    }
                    partialMessageFailure.setUserComment(userComment);
                    partialMessageFailures.add(partialMessageFailure);
                }
            }
            // Check matchAll option
            if (matchAll) {
                if (countValueMatch == values.size()) {
                    messageFailures.addAll(partialMessageFailures);
                    countLocationMatch++;
                } else {
                    // String path = messageEncoding == MessageEncoding.V2_ER7 ?
                    // messageLocation.getEPath()
                    // : messageLocation.getXPath();
                    MessageFailure mf = getNoMatchMessageFailure(message,
                            getDescription(valueInMessage, values, matchAll),
                            messageLocation, valueInMessage, assertionResult,
                            userComment);
                    // mf.setFailureType(AssertionTypeV2Constants.DATA);
                    messageFailures.add(mf);
                }
            } else if (!matchAll) {
                if (countValueMatch > 0) {
                    // Keep only one DATA_PASSED
                    for (MessageFailure pmf : partialMessageFailures) {
                        if (pmf.getFailureType() == AssertionTypeV2Constants.DATA_PASSED) {
                            messageFailures.add(pmf);
                            break;
                        }
                    }
                    countLocationMatch++;
                } else {
                    if (partialMessageFailures.size() > 0) {
                        // String path = messageEncoding ==
                        // MessageEncoding.V2_ER7 ? messageLocation.getEPath()
                        // : messageLocation.getXPath();
                        // MessageFailure mf = PluginUtil.getMessageFailure(
                        // messageEncoding,
                        // getDescription(
                        // message.getValue(messageLocation),
                        // values, matchAll), path,
                        // assertionResult, userComment);
                        // mf.setFailureType(AssertionTypeV2Constants.DATA);
                        MessageFailure mf = getNoMatchMessageFailure(
                                message,
                                getDescription(valueInMessage, values, matchAll),
                                messageLocation, valueInMessage,
                                assertionResult, userComment);
                        messageFailures.add(mf);
                    }
                }
            }
        }
        if (minMatch != 0 || maxMatch != null) {
            boolean passed = false;
            if (countLocationMatch >= minMatch) {
                if (maxMatch != null && !"*".equals(maxMatch)) {
                    int iMaxMatch = Integer.parseInt(maxMatch);
                    if (countLocationMatch <= iMaxMatch) {
                        passed = true;
                    }
                } else {
                    passed = true;
                }
            }
            if (passed) {
                List<String> passedLocation = new ArrayList<String>();
                for (int i = 0; i < messageFailures.size(); i++) {
                    if (messageFailures.get(i).getFailureType() == AssertionTypeV2Constants.DATA_PASSED) {
                        passedLocation.add(messageFailures.get(i).getPath());
                    }
                }
                messageFailures.clear();
                String description = String.format(
                        "The values at these locations %s match %s And you asked for a minimum of '%d' and a maximum of '%s'.",
                        PluginUtil.valuesToString(passedLocation),
                        getConstraintsAsString(values, matchAll), minMatch,
                        maxMatch);
                MessageFailure messageFailure = getPassedMessageFailure(
                        message, description, location, assertionResult,
                        userComment);
                // mf = PluginUtil.getMessageFailure(
                // messageEncoding, description, location,
                // assertionResult, userComment);
                // mf.setFailureType(AssertionTypeV2Constants.DATA_PASSED);
                messageFailures.add(messageFailure);
            } else {
                List<String> passedLocation = new ArrayList<String>();
                for (int i = 0; i < messageFailures.size(); i++) {
                    if (messageFailures.get(i).getFailureType() == AssertionTypeV2Constants.DATA_PASSED) {
                        passedLocation.add(messageFailures.get(i).getPath());
                    }
                }
                messageFailures.clear();
                String description = String.format(
                        "The values at these locations %s match %s But you asked for a minimum of '%d' and a maximum of '%s'.",
                        PluginUtil.valuesToString(passedLocation),
                        getConstraintsAsString(values, matchAll), minMatch,
                        maxMatch);
                MessageFailure messageFailure = getNoMatchMessageFailure(
                        message, description, location, assertionResult,
                        userComment);
                // MessageFailure mf = PluginUtil.getMessageFailure(
                // messageEncoding, description, location,
                // assertionResult, userComment);
                // mf.setFailureType(AssertionTypeV2Constants.DATA);
                messageFailures.add(messageFailure);
            }
        }
        return messageFailures;
    }

    /**
     * Check the value at a specific location
     * 
     * @param message
     * @param valueInMessage
     * @param messageLocation
     * @param value
     * @param userComment
     * @return the result of the check
     */
    private ValueCheckResult checkValue(HL7V2Message message,
            String valueInMessage, MessageLocation messageLocation,
            ValueCheck value, String userComment) {
        ValueCheckResult result = null;
        if (value.getType() == ValueCheckType.PLAIN) {
            List<String> valuesInContext = new ArrayList<String>();
            valuesInContext.add(value.getText());
            result = checkAgainstValues(message, valueInMessage,
                    valuesInContext, value.getOptions(), messageLocation,
                    userComment);
        } else if (value.getType() == ValueCheckType.REGEX) {
            result = checkAgainstRegex(message, valueInMessage,
                    value.getText(), value.getOptions(), messageLocation,
                    userComment);
        } else if (value.getType() == ValueCheckType.LOCATION) {
            String locationInContext = value.getText();
            List<MessageLocation> locations = MessageElementUtil.getMessageLocations(
                    MessageElementUtil.getMessageElement(locationInContext),
                    message);
            List<String> valuesInContext = new ArrayList<String>();
            for (MessageLocation location : locations) {
                String valueInMessageFromContext = message.getValue(location);
                valuesInContext.add(valueInMessageFromContext);
            }
            result = checkAgainstValues(message, valueInMessage,
                    valuesInContext, value.getOptions(), messageLocation,
                    userComment);
        } else if (value.getType() == ValueCheckType.PRESENT) {
            result = checkPresence(message, valueInMessage, true,
                    messageLocation, userComment);
        } else if (value.getType() == ValueCheckType.EMPTY) {
            result = checkPresence(message, valueInMessage, false,
                    messageLocation, userComment);
        }
        return result;
    }

    /**
     * Check the value in message against a list of values
     * 
     * @param message
     * @param valueInMessage
     * @param valuesInContext
     * @param options
     * @param messageLocation
     * @param userComment
     * @return a ValueCheckResult
     */
    private ValueCheckResult checkAgainstValues(HL7V2Message message,
            String valueInMessage, List<String> valuesInContext,
            Map<String, Boolean> options, MessageLocation messageLocation,
            String userComment) {
        ValueCheckResult result = new ValueCheckResult();
        boolean ignoreCase = false;
        boolean interpretAsNumber = false;
        boolean required = false;
        if (options != null) {
            ignoreCase = options.get("ignoreCase") != null ? options.get("ignoreCase")
                    : false;
            interpretAsNumber = options.get("interpretAsNumber") != null ? options.get("interpretAsNumber")
                    : false;
            required = options.get("required") != null ? options.get("required")
                    : false;
        }
        boolean match = false;
        MessageFailure messageFailure = null;
        if (valueInMessage != null) {
            for (String valueInContext : valuesInContext) {
                if (interpretAsNumber) {
                    float valueInMessageAsNumber = 0;
                    float valueInContextAsNumber = 0;
                    try {
                        valueInMessageAsNumber = Float.parseFloat(valueInMessage);
                        valueInContextAsNumber = Float.parseFloat(valueInContext);
                    } catch (NumberFormatException nfe) {
                        match = false;
                    }
                    match = valueInMessageAsNumber == valueInContextAsNumber;
                } else {
                    if (ignoreCase) {
                        match = valueInContext.equalsIgnoreCase(valueInMessage);
                    } else {
                        match = valueInContext.equals(valueInMessage);
                    }
                }
                if (match) {
                    break;
                }
            }
            if (!match) {
                messageFailure = getTemporaryMessageFailure(message);
                // messageFailure =
                // PluginUtil.getMessageFailure(MessageEncoding.V2_ER7);
                // messageFailure.setFailureType(AssertionTypeV2Constants.DATA);
            } else {
                String assertionDeclaration = String.format("%s = %s",
                        messageLocation.getEPath(),
                        PluginUtil.valuesToString(valuesInContext));
                messageFailure = getPassedMessageFailure(message,
                        messageLocation, valueInMessage, assertionDeclaration,
                        userComment);
                // messageFailure = PluginUtil.getMessageFailure(
                // MessageEncoding.V2_ER7, "", location, null, null);
                // messageFailure.setElementContent(valueInMessage);
                // messageFailure.setFailureType(AssertionTypeV2Constants.DATA_PASSED);
                // messageFailure.setAssertionDeclaration(String.format("%s = %s",
                // location, PluginUtil.valuesToString(valuesInContext)));
            }
        } else {
            if (required) {
                messageFailure = getTemporaryMessageFailure(message);
                // messageFailure = getRequiredMessageFailure(message,
                // messageLocation, userComment, assertionResult);
                // MessageEncoding.V2_ER7, location, null);
            }
        }
        result.setMessageFailure(messageFailure);
        // if (messageFailure != null) {
        // messageFailures.add(messageFailure);
        // }
        result.setPassed(match);
        return result;
    }

    /**
     * Check the value in message against a regular expression
     * 
     * @param message
     * @param valueInMessage
     * @param regexInContext
     * @param options
     * @param userComment
     * @return a ValueCheckResult
     */
    private ValueCheckResult checkAgainstRegex(HL7V2Message message,
            String valueInMessage, String regexInContext,
            Map<String, Boolean> options, MessageLocation messageLocation,
            String userComment) {
        ValueCheckResult result = new ValueCheckResult();
        // List<MessageFailure> messageFailures = new
        // ArrayList<MessageFailure>();
        // result.setMessageFailures(messageFailures);
        boolean required = false;
        if (options != null) {
            required = options.get("required") != null ? options.get("required")
                    : false;
        }
        boolean match = false;
        MessageFailure messageFailure = null;
        if (valueInMessage != null) {
            match = valueInMessage.matches(regexInContext);
            if (!match) {
                messageFailure = getTemporaryMessageFailure(message);
                // messageFailure = getMessageFailure(MessageEncoding.V2_ER7);
                // messageFailure.setFailureType(AssertionTypeV2Constants.DATA);
            } else {
                String assertionDeclaration = String.format("%s = regex(%s)",
                        messageLocation.getEPath(), regexInContext);
                messageFailure = getPassedMessageFailure(message,
                        messageLocation, valueInMessage, assertionDeclaration,
                        userComment);
                // messageFailure = PluginUtil.getMessageFailure(
                // MessageEncoding.V2_ER7, "", location, null, null);
                // messageFailure.setElementContent(valueInMessage);
                // messageFailure.setFailureType(AssertionTypeV2Constants.DATA_PASSED);
                // messageFailure.setAssertionDeclaration(String.format(
                // "%s = regex(%s)", location, regexInContext));
            }
        } else {
            if (required) {
                messageFailure = getTemporaryMessageFailure(message);
                // messageFailure = getRequiredMessageFailure(
                // MessageEncoding.V2_ER7, location, null);
            }
        }
        // if (messageFailure != null) {
        // messageFailures.add(messageFailure);
        // }
        result.setMessageFailure(messageFailure);
        result.setPassed(match);
        return result;
    }

    /**
     * Check the presence of a value in a message.
     * 
     * @param message
     * @param valueInMessage
     * @param presence
     * @param messageLocation
     * @param userComment
     * @return a ValueCheckResult
     */
    private ValueCheckResult checkPresence(HL7V2Message message,
            String valueInMessage, boolean presence,
            MessageLocation messageLocation, String userComment) {
        ValueCheckResult result = new ValueCheckResult();
        boolean match = valueInMessage != null && presence
                || valueInMessage == null && !presence;
        MessageFailure messageFailure = null;
        if (match) {
            // messageFailure = PluginUtil.getMessageFailure(
            // MessageEncoding.V2_ER7, "", location, null, null);
            // messageFailure.setElementContent(valueInMessage);
            // messageFailure.setFailureType(AssertionTypeV2Constants.DATA_PASSED);
            String assertionDeclaration = "";
            if (presence) {
                // messageFailure.setAssertionDeclaration(String.format(
                // "%s is present", location));
                assertionDeclaration = String.format("%s is present",
                        messageLocation.getEPath());
            } else {
                assertionDeclaration = String.format("%s is empty",
                        messageLocation.getEPath());
                // messageFailure.setAssertionDeclaration(String.format(
                // "%s is empty", location));
            }
            messageFailure = getPassedMessageFailure(message, messageLocation,
                    valueInMessage, assertionDeclaration, userComment);

        } else {
            messageFailure = getTemporaryMessageFailure(message);
            // messageFailure = getMessageFailure(MessageEncoding.V2_ER7);
            // messageFailure.setFailureType(AssertionTypeV2Constants.DATA);
        }
        result.setMessageFailure(messageFailure);
        result.setPassed(match);
        return result;
    }

    /**
     * Create a temporary MessageFailure to indicate that there is no match or
     * if the value is missing whereas it is required.
     * 
     * @param message
     * @return a MessageFailure object
     */
    private MessageFailure getTemporaryMessageFailure(HL7Message message) {
        MessageEncoding messageEncoding = message.getEncoding();
        MessageFailure messageFailure = PluginUtil.getMessageFailure(message.getEncoding());
        if (messageEncoding == MessageEncoding.V3) {
            messageFailure.setFailureType(AssertionTypeV3Constants.DATA);
        } else {
            messageFailure.setFailureType(AssertionTypeV2Constants.DATA);
        }
        return messageFailure;
    }

    /**
     * Create a MessageFailure when the validation passed.
     * 
     * @param message
     * @param messageLocation
     * @param valueInMessage
     * @param assertionDeclaration
     * @param userComment
     * @return a MessageFailure object
     */
    private MessageFailure getPassedMessageFailure(HL7V2Message message,
            MessageLocation messageLocation, String valueInMessage,
            String assertionDeclaration, String userComment) {
        MessageEncoding messageEncoding = message.getEncoding();
        MessageFailure messageFailure = PluginUtil.getMessageFailure(messageEncoding);
        messageFailure.setFailureType(AssertionTypeV2Constants.DATA_PASSED);
        messageFailure.setElementContent(valueInMessage);
        messageFailure.setAssertionDeclaration(assertionDeclaration);
        messageFailure.setUserComment(userComment);
        if (messageEncoding == MessageEncoding.V2_ER7) {
            messageFailure.setPath(messageLocation.getEPath());
            messageFailure.setLine(((Er7Message) message).getLine(messageLocation));
            messageFailure.setColumn(((Er7Message) message).getColumn(messageLocation));
        } else if (messageEncoding == MessageEncoding.V2_XML) {
            messageFailure.setPath(messageLocation.getXPath());
        }
        return messageFailure;
    }

    /**
     * Create a MessageFailure when the validation passed.
     * 
     * @param message
     * @param description
     * @param location
     * @param assertionResult
     * @param userComment
     * @return a MessageFailure object
     */
    private MessageFailure getPassedMessageFailure(HL7Message message,
            String description, String location,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        MessageEncoding messageEncoding = message.getEncoding();
        MessageFailure messageFailure = PluginUtil.getMessageFailure(messageEncoding);
        messageFailure.setDescription(description);
        messageFailure.setPath(location);
        if (messageEncoding == MessageEncoding.V3) {
            messageFailure.setFailureType(AssertionTypeV3Constants.DATA_PASSED);
        } else {
            messageFailure.setFailureType(AssertionTypeV2Constants.DATA_PASSED);
        }
        messageFailure.setAssertionResult(assertionResult);
        messageFailure.setUserComment(userComment);
        return messageFailure;
    }

    /**
     * Create a MessageFailure when no match or partial match is found.
     * 
     * @param message
     * @param description
     * @param messageLocation
     * @param valueInMessage
     * @param assertionResult
     * @param userComment
     * @return a MessageFailure object
     */
    private MessageFailure getNoMatchMessageFailure(HL7V2Message message,
            String description, MessageLocation messageLocation,
            String valueInMessage,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        MessageEncoding messageEncoding = message.getEncoding();
        MessageFailure messageFailure = PluginUtil.getMessageFailure(messageEncoding);
        messageFailure.setDescription(description);
        messageFailure.setFailureType(AssertionTypeV2Constants.DATA);
        messageFailure.setElementContent(valueInMessage);
        messageFailure.setAssertionResult(assertionResult);
        messageFailure.setUserComment(userComment);
        if (messageEncoding == MessageEncoding.V2_ER7) {
            messageFailure.setPath(messageLocation.getEPath());
            messageFailure.setLine(((Er7Message) message).getLine(messageLocation));
            messageFailure.setColumn(((Er7Message) message).getColumn(messageLocation));
        } else if (messageEncoding == MessageEncoding.V2_XML) {
            messageFailure.setPath(messageLocation.getXPath());
        }
        return messageFailure;
    }

    /**
     * Create a MessageFailure when no match or partial match is found.
     * 
     * @param message
     * @param description
     * @param location
     * @param assertionResult
     * @param userComment
     * @return a MessageFailure object
     */
    private MessageFailure getNoMatchMessageFailure(HL7Message message,
            String description, String location,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        MessageEncoding messageEncoding = message.getEncoding();
        MessageFailure messageFailure = PluginUtil.getMessageFailure(messageEncoding);
        messageFailure.setDescription(description);
        messageFailure.setPath(location);
        if (messageEncoding == MessageEncoding.V3) {
            messageFailure.setFailureType(AssertionTypeV3Constants.DATA);
        } else {
            messageFailure.setFailureType(AssertionTypeV2Constants.DATA);
        }
        messageFailure.setAssertionResult(assertionResult);
        messageFailure.setUserComment(userComment);
        return messageFailure;
    }

    // /**
    // * Create a MessageFailure object when the location is required but not in
    // * the message.
    // *
    // * @param message
    // */
    // private MessageFailure getRequiredMessageFailure(HL7Message message,
    // MessageEncoding messageEncoding, String path, String userComment) {
    // StringBuffer sb = new StringBuffer();
    // sb.append("The specified message element match location does not map to a message element. The data value ");
    // sb.append("at the specified match location can't be evaluated. Refine the message element match location. ");
    // sb.append("The provided path expression is '").append(path).append("'.");
    // MessageFailure mf = PluginUtil.getMessageFailure(messageEncoding,
    // sb.toString(), path, null, userComment);
    // mf.setFailureType(AssertionTypeV2Constants.DATA);
    // return mf;
    // return null;
    // }

    /**
     * Create the description for the message failure
     * 
     * @param valueInMessage
     * @param values
     * @param matchAll
     * @return the description
     */
    private String getDescription(String valueInMessage,
            List<ValueCheck> values, boolean matchAll) {
        StringBuffer sb = new StringBuffer();
        if (valueInMessage == null) {
            valueInMessage = "";
        }
        sb.append("The value '").append(valueInMessage);
        sb.append("' at the given location in the message does not match ");
        sb.append(getConstraintsAsString(values, matchAll));
        return sb.toString();
    }

    /**
     * Create the constraints as a String
     * 
     * @param values
     * @param matchAll
     * @return the description
     */
    private String getConstraintsAsString(List<ValueCheck> values,
            boolean matchAll) {
        List<String> plainTextValues = new ArrayList<String>();
        List<String> regexValues = new ArrayList<String>();
        List<String> locationValues = new ArrayList<String>();
        List<String> presentValues = new ArrayList<String>();
        List<String> emptyValues = new ArrayList<String>();
        for (ValueCheck value : values) {
            if (value.getType() == ValueCheckType.PLAIN) {
                plainTextValues.add(value.getText());
            } else if (value.getType() == ValueCheckType.REGEX) {
                regexValues.add(value.getText());
            } else if (value.getType() == ValueCheckType.LOCATION) {
                locationValues.add(value.getText());
            } else if (value.getType() == ValueCheckType.PRESENT) {
                presentValues.add("");
            } else if (value.getType() == ValueCheckType.EMPTY) {
                emptyValues.add("");
            }
        }
        StringBuffer sb = new StringBuffer();
        int totalValues = plainTextValues.size() + regexValues.size()
                + locationValues.size() + presentValues.size()
                + emptyValues.size();
        if (plainTextValues.size() > 0) {
            totalValues -= plainTextValues.size();
            sb.append("one of the expected values ");
            sb.append(PluginUtil.valuesToString(plainTextValues));
            if (totalValues == 0) {
                sb.append(".");
            } else {
                if (matchAll) {
                    sb.append(" AND ");
                } else {
                    sb.append(" OR ");
                }
            }
        }
        if (regexValues.size() > 0) {
            totalValues -= regexValues.size();
            sb.append("one of the regular expression ");
            sb.append(PluginUtil.valuesToString(regexValues));
            if (totalValues == 0) {
                sb.append(".");
            } else {
                if (matchAll) {
                    sb.append(" AND ");
                } else {
                    sb.append(" OR ");
                }
            }
        }
        if (locationValues.size() > 0) {
            totalValues -= locationValues.size();
            sb.append("one of the values at location ");
            sb.append(PluginUtil.valuesToString(locationValues));
            if (totalValues == 0) {
                sb.append(".");
            } else {
                if (matchAll) {
                    sb.append(" AND ");
                } else {
                    sb.append(" OR ");
                }
            }
        }
        if (presentValues.size() > 0) {
            totalValues -= presentValues.size();
            sb.append("a non-empty value");
            if (totalValues == 0) {
                sb.append(".");
            } else {
                if (matchAll) {
                    sb.append(" AND ");
                } else {
                    sb.append(" OR ");
                }
            }
        }
        if (emptyValues.size() > 0) {
            totalValues -= emptyValues.size();
            sb.append("an empty value");
            if (totalValues == 0) {
                sb.append(".");
            } else {
                if (matchAll) {
                    sb.append(" AND ");
                } else {
                    sb.append(" OR ");
                }
            }
        }
        return sb.toString();
    }

    private List<MessageFailure> validateMessage(HL7V3Message message,
            String params, AssertionResultConstants.Enum assertionResult,
            String userComment) throws JsonParseException,
            JsonMappingException, IOException {
        ValueCheckParam vcParams = mapper.readValue(params,
                ValueCheckParam.class);
        return validate(message, vcParams.getLocation(), vcParams.getValues(),
                vcParams.isMatchAll(), vcParams.getMinMatch(),
                vcParams.getMaxMatch(), assertionResult, userComment);
    }

    /**
     * Check the value at a specific location
     * 
     * @param message
     * @param valueInMessage
     * @param messageLocation
     * @param value
     * @param userComment
     * @return the result of the check
     */
    private List<MessageFailure> validate(HL7V3Message message,
            String location, List<ValueCheck> values, boolean matchAll,
            int minMatch, String maxMatch,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        List<MessageFailure> messageFailures = new ArrayList<MessageFailure>();
        List<MessageFailure> partialMessageFailures = new ArrayList<MessageFailure>();
        List<String> valuesInMessage = message.getValues(location);
        if (valuesInMessage.size() == 0) {
            valuesInMessage.add(null);
        }
        // List<MessageLocation> locations =
        // MessageElementUtil.getMessageLocations(
        // MessageElementUtil.getMessageElement(location), message);
        int countLocationMatch = 0;
        for (String valueInMessage : valuesInMessage) {
            int countValueMatch = 0;
            partialMessageFailures.clear();
            for (ValueCheck value : values) {
                ValueCheckResult result = checkValue(message, valueInMessage,
                        value, location, userComment);
                if (result.isPassed()) {
                    countValueMatch++;
                }
                MessageFailure partialMessageFailure = result.getMessageFailure();
                if (partialMessageFailure != null) {
                    if (partialMessageFailure.getFailureType() != AssertionTypeV3Constants.DATA_PASSED) {
                        partialMessageFailure.setAssertionResult(assertionResult);
                    }
                    partialMessageFailure.setUserComment(userComment);
                    partialMessageFailures.add(partialMessageFailure);
                }
            }
            // Check matchAll option
            if (matchAll) {
                if (countValueMatch == values.size()) {
                    messageFailures.addAll(partialMessageFailures);
                    countLocationMatch++;
                } else {
                    // String path = messageEncoding == MessageEncoding.V2_ER7 ?
                    // messageLocation.getEPath()
                    // : messageLocation.getXPath();
                    MessageFailure mf = getNoMatchMessageFailure(message,
                            getDescription(valueInMessage, values, matchAll),
                            location, valueInMessage, assertionResult,
                            userComment);
                    // mf.setFailureType(AssertionTypeV2Constants.DATA);
                    messageFailures.add(mf);
                }
            } else if (!matchAll) {
                if (countValueMatch > 0) {
                    // Keep only one DATA_PASSED
                    for (MessageFailure pmf : partialMessageFailures) {
                        if (pmf.getFailureType() == AssertionTypeV3Constants.DATA_PASSED) {
                            messageFailures.add(pmf);
                            break;
                        }
                    }
                    countLocationMatch++;
                } else {
                    if (partialMessageFailures.size() > 0) {
                        // String path = messageEncoding ==
                        // MessageEncoding.V2_ER7 ? messageLocation.getEPath()
                        // : messageLocation.getXPath();
                        // MessageFailure mf = PluginUtil.getMessageFailure(
                        // messageEncoding,
                        // getDescription(
                        // message.getValue(messageLocation),
                        // values, matchAll), path,
                        // assertionResult, userComment);
                        // mf.setFailureType(AssertionTypeV2Constants.DATA);
                        MessageFailure mf = getNoMatchMessageFailure(
                                message,
                                getDescription(valueInMessage, values, matchAll),
                                location, valueInMessage, assertionResult,
                                userComment);
                        messageFailures.add(mf);
                    }
                }
            }
        }
        if (minMatch != 0 || maxMatch != null) {
            boolean passed = false;
            if (countLocationMatch >= minMatch) {
                if (maxMatch != null && !"*".equals(maxMatch)) {
                    int iMaxMatch = Integer.parseInt(maxMatch);
                    if (countLocationMatch <= iMaxMatch) {
                        passed = true;
                    }
                } else {
                    passed = true;
                }
            }
            if (passed) {
                List<String> passedLocation = new ArrayList<String>();
                for (int i = 0; i < messageFailures.size(); i++) {
                    if (messageFailures.get(i).getFailureType() == AssertionTypeV3Constants.DATA_PASSED) {
                        passedLocation.add(messageFailures.get(i).getPath());
                    }
                }
                messageFailures.clear();
                String description = String.format(
                        "The values at these locations %s match %s And you asked for a minimum of '%d' and a maximum of '%s'.",
                        PluginUtil.valuesToString(passedLocation),
                        getConstraintsAsString(values, matchAll), minMatch,
                        maxMatch);
                MessageFailure messageFailure = getPassedMessageFailure(
                        message, description, location, assertionResult,
                        userComment);
                // mf = PluginUtil.getMessageFailure(
                // messageEncoding, description, location,
                // assertionResult, userComment);
                // mf.setFailureType(AssertionTypeV2Constants.DATA_PASSED);
                messageFailures.add(messageFailure);
            } else {
                List<String> passedLocation = new ArrayList<String>();
                for (int i = 0; i < messageFailures.size(); i++) {
                    if (messageFailures.get(i).getFailureType() == AssertionTypeV3Constants.DATA_PASSED) {
                        passedLocation.add(messageFailures.get(i).getPath());
                    }
                }
                messageFailures.clear();
                String description = String.format(
                        "The values at these locations %s match %s But you asked for a minimum of '%d' and a maximum of '%s'.",
                        PluginUtil.valuesToString(passedLocation),
                        getConstraintsAsString(values, matchAll), minMatch,
                        maxMatch);
                MessageFailure messageFailure = getNoMatchMessageFailure(
                        message, description, location, assertionResult,
                        userComment);
                // MessageFailure mf = PluginUtil.getMessageFailure(
                // messageEncoding, description, location,
                // assertionResult, userComment);
                // mf.setFailureType(AssertionTypeV2Constants.DATA);
                messageFailures.add(messageFailure);
            }
        }
        return messageFailures;
    }

    /**
     * Check the value at a specific location
     * 
     * @param message
     * @param valueInMessage
     * @param value
     * @param location
     * @param userComment
     * @return the result of the check
     */
    private ValueCheckResult checkValue(HL7V3Message message,
            String valueInMessage, ValueCheck value, String location,
            String userComment) {
        ValueCheckResult result = null;
        if (value.getType() == ValueCheckType.PLAIN) {
            List<String> valuesInContext = new ArrayList<String>();
            valuesInContext.add(value.getText());
            result = checkAgainstValues(message, valueInMessage,
                    valuesInContext, value.getOptions(), location, userComment);
        } else if (value.getType() == ValueCheckType.REGEX) {
            result = checkAgainstRegex(message, valueInMessage,
                    value.getText(), value.getOptions(), location, userComment);
        } else if (value.getType() == ValueCheckType.LOCATION) {
            String locationInContext = value.getText();

            // List<MessageLocation> locations =
            // MessageElementUtil.getMessageLocations(
            // MessageElementUtil.getMessageElement(locationInContext),
            // message);
            List<String> valuesInContext = message.getValues(locationInContext);
            // for (MessageLocation location : locations) {
            // String valueInMessageFromContext = message.getValue(location);
            // valuesInContext.add(valueInMessageFromContext);
            // }
            result = checkAgainstValues(message, valueInMessage,
                    valuesInContext, value.getOptions(), location, userComment);
        } else if (value.getType() == ValueCheckType.PRESENT) {
            result = checkPresence(message, valueInMessage, true, location,
                    userComment);
        } else if (value.getType() == ValueCheckType.EMPTY) {
            result = checkPresence(message, valueInMessage, false, location,
                    userComment);
        }
        return result;
    }

    /**
     * Check the value in message against a list of values
     * 
     * @param message
     * @param valueInMessage
     * @param valuesInContext
     * @param options
     * @param location
     * @param userComment
     * @return a ValueCheckResult
     */
    private ValueCheckResult checkAgainstValues(HL7V3Message message,
            String valueInMessage, List<String> valuesInContext,
            Map<String, Boolean> options, String location, String userComment) {
        ValueCheckResult result = new ValueCheckResult();
        boolean ignoreCase = false;
        boolean interpretAsNumber = false;
        boolean required = false;
        if (options != null) {
            ignoreCase = options.get("ignoreCase") != null ? options.get("ignoreCase")
                    : false;
            interpretAsNumber = options.get("interpretAsNumber") != null ? options.get("interpretAsNumber")
                    : false;
            required = options.get("required") != null ? options.get("required")
                    : false;
        }
        boolean match = false;
        MessageFailure messageFailure = null;
        if (valueInMessage != null) {
            for (String valueInContext : valuesInContext) {
                if (interpretAsNumber) {
                    float valueInMessageAsNumber = 0;
                    float valueInContextAsNumber = 0;
                    try {
                        valueInMessageAsNumber = Float.parseFloat(valueInMessage);
                        valueInContextAsNumber = Float.parseFloat(valueInContext);
                    } catch (NumberFormatException nfe) {
                        match = false;
                    }
                    match = valueInMessageAsNumber == valueInContextAsNumber;
                } else {
                    if (ignoreCase) {
                        match = valueInContext.equalsIgnoreCase(valueInMessage);
                    } else {
                        match = valueInContext.equals(valueInMessage);
                    }
                }
                if (match) {
                    break;
                }
            }
            if (!match) {
                messageFailure = getTemporaryMessageFailure(message);
                // messageFailure =
                // PluginUtil.getMessageFailure(MessageEncoding.V2_ER7);
                // messageFailure.setFailureType(AssertionTypeV2Constants.DATA);
            } else {
                String assertionDeclaration = String.format("%s = %s",
                        location, PluginUtil.valuesToString(valuesInContext));
                messageFailure = getPassedMessageFailure(message, location,
                        valueInMessage, assertionDeclaration, userComment);
                // messageFailure = PluginUtil.getMessageFailure(
                // MessageEncoding.V2_ER7, "", location, null, null);
                // messageFailure.setElementContent(valueInMessage);
                // messageFailure.setFailureType(AssertionTypeV2Constants.DATA_PASSED);
                // messageFailure.setAssertionDeclaration(String.format("%s = %s",
                // location, PluginUtil.valuesToString(valuesInContext)));
            }
        } else {
            if (required) {
                messageFailure = getTemporaryMessageFailure(message);
                // messageFailure = getRequiredMessageFailure(message,
                // messageLocation, userComment, assertionResult);
                // MessageEncoding.V2_ER7, location, null);
            }
        }
        result.setMessageFailure(messageFailure);
        // if (messageFailure != null) {
        // messageFailures.add(messageFailure);
        // }
        result.setPassed(match);
        return result;
    }

    /**
     * Check the value in message against a regular expression
     * 
     * @param message
     * @param valueInMessage
     * @param regexInContext
     * @param options
     * @param userComment
     * @param location
     * @return a ValueCheckResult
     */
    private ValueCheckResult checkAgainstRegex(HL7V3Message message,
            String valueInMessage, String regexInContext,
            Map<String, Boolean> options, String location, String userComment) {
        ValueCheckResult result = new ValueCheckResult();
        // List<MessageFailure> messageFailures = new
        // ArrayList<MessageFailure>();
        // result.setMessageFailures(messageFailures);
        boolean required = false;
        if (options != null) {
            required = options.get("required") != null ? options.get("required")
                    : false;
        }
        boolean match = false;
        MessageFailure messageFailure = null;
        if (valueInMessage != null) {
            match = valueInMessage.matches(regexInContext);
            if (!match) {
                messageFailure = getTemporaryMessageFailure(message);
                // messageFailure = getMessageFailure(MessageEncoding.V2_ER7);
                // messageFailure.setFailureType(AssertionTypeV2Constants.DATA);
            } else {
                String assertionDeclaration = String.format("%s = regex(%s)",
                        location, regexInContext);
                messageFailure = getPassedMessageFailure(message, location,
                        valueInMessage, assertionDeclaration, userComment);
                // messageFailure = PluginUtil.getMessageFailure(
                // MessageEncoding.V2_ER7, "", location, null, null);
                // messageFailure.setElementContent(valueInMessage);
                // messageFailure.setFailureType(AssertionTypeV2Constants.DATA_PASSED);
                // messageFailure.setAssertionDeclaration(String.format(
                // "%s = regex(%s)", location, regexInContext));
            }
        } else {
            if (required) {
                messageFailure = getTemporaryMessageFailure(message);
                // messageFailure = getRequiredMessageFailure(
                // MessageEncoding.V2_ER7, location, null);
            }
        }
        // if (messageFailure != null) {
        // messageFailures.add(messageFailure);
        // }
        result.setMessageFailure(messageFailure);
        result.setPassed(match);
        return result;
    }

    /**
     * Check the presence of a value in a message.
     * 
     * @param message
     * @param valueInMessage
     * @param presence
     * @param location
     * @param userComment
     * @return a ValueCheckResult
     */
    private ValueCheckResult checkPresence(HL7V3Message message,
            String valueInMessage, boolean presence, String location,
            String userComment) {
        ValueCheckResult result = new ValueCheckResult();
        boolean match = valueInMessage != null && presence
                || valueInMessage == null && !presence;
        MessageFailure messageFailure = null;
        if (match) {
            // messageFailure = PluginUtil.getMessageFailure(
            // MessageEncoding.V2_ER7, "", location, null, null);
            // messageFailure.setElementContent(valueInMessage);
            // messageFailure.setFailureType(AssertionTypeV2Constants.DATA_PASSED);
            String assertionDeclaration = "";
            if (presence) {
                // messageFailure.setAssertionDeclaration(String.format(
                // "%s is present", location));
                assertionDeclaration = String.format("%s is present", location);
            } else {
                assertionDeclaration = String.format("%s is empty", location);
                // messageFailure.setAssertionDeclaration(String.format(
                // "%s is empty", location));
            }
            messageFailure = getPassedMessageFailure(message, location,
                    valueInMessage, assertionDeclaration, userComment);

        } else {
            messageFailure = getTemporaryMessageFailure(message);
            // messageFailure = getMessageFailure(MessageEncoding.V2_ER7);
            // messageFailure.setFailureType(AssertionTypeV2Constants.DATA);
        }
        result.setMessageFailure(messageFailure);
        result.setPassed(match);
        return result;
    }

    /**
     * Create a MessageFailure when the validation passed.
     * 
     * @param message
     * @param location
     * @param valueInMessage
     * @param assertionDeclaration
     * @param userComment
     * @return a MessageFailure object
     */
    private MessageFailure getPassedMessageFailure(HL7V3Message message,
            String location, String valueInMessage,
            String assertionDeclaration, String userComment) {
        MessageEncoding messageEncoding = message.getEncoding();
        MessageFailure messageFailure = PluginUtil.getMessageFailure(messageEncoding);
        messageFailure.setFailureType(AssertionTypeV3Constants.DATA_PASSED);
        messageFailure.setPath(location);
        messageFailure.setElementContent(valueInMessage);
        messageFailure.setAssertionDeclaration(assertionDeclaration);
        messageFailure.setUserComment(userComment);
        return messageFailure;
    }

    /**
     * Create a MessageFailure when no match or partial match is found.
     * 
     * @param message
     * @param description
     * @param location
     * @param valueInMessage
     * @param assertionResult
     * @param userComment
     * @return a MessageFailure object
     */
    private MessageFailure getNoMatchMessageFailure(HL7V3Message message,
            String description, String location, String valueInMessage,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        MessageEncoding messageEncoding = message.getEncoding();
        MessageFailure messageFailure = PluginUtil.getMessageFailure(messageEncoding);
        messageFailure.setDescription(description);
        messageFailure.setFailureType(AssertionTypeV3Constants.DATA);
        messageFailure.setPath(location);
        messageFailure.setElementContent(valueInMessage);
        messageFailure.setAssertionResult(assertionResult);
        messageFailure.setUserComment(userComment);
        return messageFailure;
    }

}
