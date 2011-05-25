/*
 * NIST Healthcare Core
 * V3ContentValidation.java Aug 5, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.content.v3;

import gov.nist.healthcare.core.message.v3.HL7V3Message;
import gov.nist.healthcare.core.validation.message.MessageFailure;
import gov.nist.healthcare.core.validation.message.content.MatchFoundResult;
import gov.nist.healthcare.core.validation.message.content.MessageContentValidation;
import gov.nist.healthcare.core.validation.message.plugin.ValidationPlugin;
import gov.nist.healthcare.core.validation.message.v3.MessageFailureV3;
import gov.nist.healthcare.core.validation.message.v3.MessageValidationContextV3;
import gov.nist.healthcare.validation.AssertionTypeV3Constants;
import gov.nist.healthcare.validation.ErrorSeverityConstants;
import gov.nist.healthcare.validation.PluginCheckType;
import gov.nist.healthcare.validation.Value;
import gov.nist.healthcare.validation.Value.PlainText;
import gov.nist.healthcare.validation.message.hl7.v3.context.DataValueLocationItemV3;
import gov.nist.healthcare.validation.message.hl7.v3.context.HL7V3MessageValidationContextDefinition.IfThenElse;
import gov.nist.healthcare.validation.message.hl7.v3.context.MessageInstanceSpecificValuesV3;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.xmlbeans.XmlLineNumber;
import org.apache.xmlbeans.XmlObject;

/**
 * This class validates the message content for a V3 message.
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageContentValidationV3 extends MessageContentValidation {

    /**
     * Validate the message content.
     * 
     * @param message
     *        the message to validate
     * @param context
     *        the message validation context
     * @return a list of message failures
     */
    public List<MessageFailureV3> validate(HL7V3Message message,
            MessageValidationContextV3 context) {
        List<MessageFailureV3> messageFailures = new ArrayList<MessageFailureV3>();
        MessageInstanceSpecificValuesV3 misv = context.getContext().getHL7V3MessageValidationContextDefinition().getMessageInstanceSpecificValues();
        if (misv != null) {
            Iterator<DataValueLocationItemV3> itDVLI = misv.getDataValueLocationItemList().iterator();
            while (itDVLI.hasNext()) {
                DataValueLocationItemV3 dvli = itDVLI.next();
                List<MatchFoundResult> results = checkItem(message, dvli);
                for (MatchFoundResult result : results) {
                    if (result.getContextError() != null) {
                        messageFailures.add((MessageFailureV3) result.getContextError());
                    }
                    if (result.getMessageError() != null) {
                        messageFailures.add((MessageFailureV3) result.getMessageError());
                    }
                }
            }
        }

        // Conditional Checking
        List<IfThenElse> conditionals = context.getContext().getHL7V3MessageValidationContextDefinition().getIfThenElseList();
        if (conditionals != null) {
            Iterator<IfThenElse> it = conditionals.iterator();
            while (it.hasNext()) {
                IfThenElse condition = it.next();
                List<MatchFoundResult> resultsIf = checkItem(message,
                        condition.getIf());
                List<MatchFoundResult> resultsThenElse = null;
                boolean ifStatementPassed = true;
                for (MatchFoundResult mfr : resultsIf) {
                    ifStatementPassed &= !mfr.hasError();
                }
                if (ifStatementPassed) {
                    // Process the then part
                    resultsThenElse = checkItem(message, condition.getThen());
                } else {
                    // Process the else part
                    if (condition.getElse() != null) {
                        resultsThenElse = checkItem(message,
                                condition.getElse());
                    }
                }
                if (resultsThenElse != null && resultsThenElse.size() > 0) {
                    for (MatchFoundResult result : resultsThenElse) {
                        if (result.getContextError() != null) {
                            messageFailures.add((MessageFailureV3) result.getContextError());
                        }
                        if (result.getMessageError() != null) {
                            messageFailures.add((MessageFailureV3) result.getMessageError());
                        }
                    }
                }
            }
        }
        // Plugin Check
        List<PluginCheckType> pluginChecks = context.getContext().getHL7V3MessageValidationContextDefinition().getPluginCheckList();
        if (pluginChecks != null) {
            Iterator<PluginCheckType> it = pluginChecks.iterator();
            while (it.hasNext()) {
                PluginCheckType pluginCheck = it.next();
                String pluginName = pluginCheck.getName();
                ValidationPlugin plugin = loadPlugin(pluginName);
                if (plugin == null) {
                    MessageFailureV3 mf = new MessageFailureV3();
                    mf.setDescription(String.format(
                            "The plugin '%s' can't be loaded.", pluginName));
                    mf.setFailureType(AssertionTypeV3Constants.MESSAGE_VALIDATION_CONTEXT);
                    mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                    messageFailures.add(mf);
                } else {
                    try {
                        String jsonParams = pluginCheck.getParams();
                        List<MessageFailure> pluginMessageFailures = plugin.validate(
                                message, jsonParams,
                                pluginCheck.getAssertionResult(),
                                pluginCheck.getComment());
                        for (MessageFailure pluginMessageFailure : pluginMessageFailures) {
                            messageFailures.add((MessageFailureV3) pluginMessageFailure);
                        }
                    } catch (Exception e) {
                        MessageFailureV3 mf = new MessageFailureV3();
                        mf.setDescription(String.format(
                                "Exception occured when using plugin '%s': %s",
                                pluginName, e.getMessage()));
                        mf.setFailureType(AssertionTypeV3Constants.MESSAGE_VALIDATION_CONTEXT);
                        mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                        messageFailures.add(mf);
                    }
                }
            }
        }
        return messageFailures;
    }

    /**
     * Check an item. It returns a list of MatchFoundResult object.
     * 
     * @param message
     *        the message
     * @param item
     *        the item to be checked
     * @return the result of the check
     */
    private List<MatchFoundResult> checkItem(HL7V3Message message,
            DataValueLocationItemV3 item) {
        String xpath = item.getXPath().getStringValue();
        List<Value> values = item.getValueList();
        List<String> messageValues = message.getValues(xpath);
        List<PlainText> contextValues = getContextValues(values);
        List<String> regexValues = getRegexValues(values);
        boolean regexCheck = regexValues.size() > 0;
        boolean emptyCheck = contextValues.contains(null);
        boolean presentCheck = isPresentCheck(values);
        boolean checkAll = item.getXPath().getCheckAll();
        // Validate
        List<MatchFoundResult> results = matchFound(checkAll, regexCheck,
                emptyCheck, presentCheck, messageValues, contextValues,
                regexValues, xpath, message.getEncoding(), null,
                item.getComment());
        // Change the message validaiton context error into data error
        for (MatchFoundResult result : results) {
            if (result.getContextError() != null) {
                MessageFailureV3 mf = (MessageFailureV3) result.getContextError();
                mf.setFailureType(AssertionTypeV3Constants.DATA);
                result.setMessageError(mf);
                result.setContextError(null);
            }
            // Set the line and column number
            if (result.getMessageError() != null) {
                MessageFailureV3 mf = (MessageFailureV3) result.getMessageError();
                XmlObject[] rs = message.getDocument().selectPath(mf.getPath());
                if (rs.length == 1) {
                    XmlLineNumber bm = (XmlLineNumber) rs[0].newCursor().getBookmark(
                            XmlLineNumber.class);
                    if (bm != null) {
                        mf.setColumn(bm.getColumn());
                        mf.setLine(bm.getLine());
                    }
                }
            }
        }
        return results;
    }

}
