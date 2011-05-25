/*
 * NIST Healthcare Core
 * MessageValidationContext.java Jun 4, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.v3;

import gov.nist.healthcare.core.message.v3.HL7V3Message;
import gov.nist.healthcare.core.util.XmlBeansUtils;
import gov.nist.healthcare.core.validation.message.MessageValidationContext;
import gov.nist.healthcare.core.validation.message.MissingDependencyException;
import gov.nist.healthcare.core.validation.message.plugin.value.ValueCheck;
import gov.nist.healthcare.core.validation.message.plugin.value.ValueCheck.ValueCheckType;
import gov.nist.healthcare.core.validation.message.plugin.value.ValueCheckParam;
import gov.nist.healthcare.validation.AssertionResultConstants;
import gov.nist.healthcare.validation.AssertionTypeV3Constants;
import gov.nist.healthcare.validation.PluginCheckType;
import gov.nist.healthcare.validation.message.hl7.context.MessageValidationCorrelationDocument;
import gov.nist.healthcare.validation.message.hl7.context.MessageValidationCorrelationDocument.MessageValidationCorrelation.Correlations;
import gov.nist.healthcare.validation.message.hl7.context.MessageValidationCorrelationDocument.MessageValidationCorrelation.Correlations.Correlation;
import gov.nist.healthcare.validation.message.hl7.context.MessageValidationCorrelationDocument.MessageValidationCorrelation.Correlations.Correlation.Copy;
import gov.nist.healthcare.validation.message.hl7.context.MessageValidationCorrelationDocument.MessageValidationCorrelation.Correlations.Correlation.SingleValue;
import gov.nist.healthcare.validation.message.hl7.v3.context.HL7V3MessageValidationContextDefinitionDocument;
import gov.nist.healthcare.validation.message.hl7.v3.context.MessageFailureInterpretationV3;
import gov.nist.healthcare.validation.message.hl7.v3.context.MessageFailureV3;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.xmlbeans.StringEnumAbstractBase;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

/**
 * This class allows the user to customize the message validation. Wrap an
 * HL7V3MessageValidationContextDefinition class
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageValidationContextV3 extends MessageValidationContext {

    private HL7V3MessageValidationContextDefinitionDocument context;

    // private HashMap<AssertionTypeV3Constants.Enum,
    // AssertionResultConstants.Enum> hFailureResult;

    /**
     * Constructor
     */
    public MessageValidationContextV3() {
        context = HL7V3MessageValidationContextDefinitionDocument.Factory.newInstance();
        MessageFailureInterpretationV3 mfi = context.addNewHL7V3MessageValidationContextDefinition().addNewFailureInterpretation();
        // Add the default message failures
        MessageFailureV3 mf = null;
        mf = mfi.addNewMessageFailure();
        mf.setType(AssertionTypeV3Constants.DATA);
        mf.setResult(AssertionResultConstants.ERROR);
        mf = mfi.addNewMessageFailure();
        mf.setType(AssertionTypeV3Constants.STRUCTURE);
        mf.setResult(AssertionResultConstants.ERROR);
        mf = mfi.addNewMessageFailure();
        mf.setType(AssertionTypeV3Constants.DATA_PASSED);
        mf.setResult(AssertionResultConstants.IGNORE);
        init();
    }

    /**
     * Constructor
     * 
     * @param context
     *        a HL7V3MessageValidationContextDefinitionDocument
     */
    public MessageValidationContextV3(
            HL7V3MessageValidationContextDefinitionDocument context) {
        this.context = context;
        init();
    }

    /**
     * Init the context to the default settings
     */
    public void init() {
        // Special Case
        MessageFailureInterpretationV3 mfi = context.getHL7V3MessageValidationContextDefinition().getFailureInterpretation();
        List<MessageFailureV3> alFailures = new ArrayList<MessageFailureV3>();
        alFailures = mfi.getMessageFailureList();
        // Check if each message failure has been set. For special case, set the
        // result even if it has been done by the user in the context file.
        boolean messageValidationContext = false;
        boolean data = false;
        boolean structure = false;
        boolean data_passed = false;
        Iterator<MessageFailureV3> it = alFailures.iterator();
        while (it.hasNext()) {
            MessageFailureV3 mf = it.next();
            if (mf.getType() == AssertionTypeV3Constants.MESSAGE_VALIDATION_CONTEXT) {
                messageValidationContext = true;
                mf.setResult(AssertionResultConstants.ALERT);
            } else if (mf.getType() == AssertionTypeV3Constants.DATA) {
                data = true;
            } else if (mf.getType() == AssertionTypeV3Constants.STRUCTURE) {
                structure = true;
            } else if (mf.getType() == AssertionTypeV3Constants.DATA_PASSED) {
                data_passed = true;
            }
        }
        // Add the missing message failures
        if (!messageValidationContext) {
            MessageFailureV3 mf = MessageFailureV3.Factory.newInstance();
            mf.setType(AssertionTypeV3Constants.MESSAGE_VALIDATION_CONTEXT);
            mf.setResult(AssertionResultConstants.ALERT);
            alFailures.add(mf);
        }
        if (!data) {
            MessageFailureV3 mf = MessageFailureV3.Factory.newInstance();
            mf.setType(AssertionTypeV3Constants.DATA);
            mf.setResult(AssertionResultConstants.ERROR);
            alFailures.add(mf);
        }
        if (!structure) {
            MessageFailureV3 mf = MessageFailureV3.Factory.newInstance();
            mf.setType(AssertionTypeV3Constants.STRUCTURE);
            mf.setResult(AssertionResultConstants.ERROR);
            alFailures.add(mf);
        }
        if (!data_passed) {
            MessageFailureV3 mf = MessageFailureV3.Factory.newInstance();
            mf.setType(AssertionTypeV3Constants.DATA_PASSED);
            mf.setResult(AssertionResultConstants.IGNORE);
            alFailures.add(mf);
        }
        mfi.setMessageFailureArray(alFailures.toArray(new MessageFailureV3[alFailures.size()]));
        // Create the HashMap
        // hFailureResult = new HashMap<AssertionTypeV3Constants.Enum,
        // AssertionResultConstants.Enum>();
        hFailureResult = new HashMap<StringEnumAbstractBase, AssertionResultConstants.Enum>();
        it = alFailures.iterator();
        while (it.hasNext()) {
            MessageFailureV3 mf = it.next();
            hFailureResult.put(mf.getType(), mf.getResult());
        }
    }

    // /**
    // * Get a FailureResult for a MessageFailureType
    // *
    // * @param failure
    // * @return a AssertionResultConstants
    // */
    // public AssertionResultConstants.Enum getFailureResult(
    // AssertionTypeV3Constants.Enum failure) {
    // return hFailureResult.get(failure);
    // }

    /**
     * Set a result of a failure. The method can return an
     * IllegalArgumentException if you try to set a result for a fixed failure
     * 
     * @param failure
     * @param result
     */
    public void setFailureResult(AssertionTypeV3Constants.Enum failure,
            AssertionResultConstants.Enum result) {
        if (failure == AssertionTypeV3Constants.MESSAGE_VALIDATION_CONTEXT) {
            throw new IllegalArgumentException(
                    "This type of failure can't be ignored or set as a warning");
        }
        MessageFailureInterpretationV3 mfi = context.getHL7V3MessageValidationContextDefinition().getFailureInterpretation();
        List<MessageFailureV3> alFailures = new ArrayList<MessageFailureV3>();
        alFailures = mfi.getMessageFailureList();
        Iterator<MessageFailureV3> it = alFailures.iterator();
        while (it.hasNext()) {
            MessageFailureV3 mf = it.next();
            if (mf.getType() == failure) {
                mf.setResult(result);
                hFailureResult.put(failure, result);
            }
        }
    }

    // /**
    // * Return the mapping between the assertion types and assertion results.
    // *
    // * @return a HashMap
    // */
    // public HashMap<AssertionTypeV3Constants.Enum,
    // AssertionResultConstants.Enum> getFailureResults() {
    // return hFailureResult;
    // }

    public HL7V3MessageValidationContextDefinitionDocument getContext() {
        return context;
    }

    public void setContext(
            HL7V3MessageValidationContextDefinitionDocument context) {
        this.context = context;
    }

    @Override
    public void load(String xmlMessageValidationContext) throws XmlException {
        context = HL7V3MessageValidationContextDefinitionDocument.Factory.parse(xmlMessageValidationContext);
        ArrayList<XmlError> validationErrors = new ArrayList<XmlError>();
        XmlOptions validationOptions = new XmlOptions();
        validationOptions.setErrorListener(validationErrors);
        if (!context.validate(validationOptions)) {
            StringBuffer sb = new StringBuffer();
            sb.append("The message validation context file is not valid.\n");
            sb.append(XmlBeansUtils.getValidationMessages(validationErrors));
            throw new IllegalArgumentException(sb.toString());
        }
    }

    /**
     * Save a MessageValidationContext as a File
     * 
     * @param xmlMessageValidationContext
     * @throws IOException
     */
    public void save(File xmlMessageValidationContext) throws IOException {
        context.save(xmlMessageValidationContext,
                new XmlOptions().setSavePrettyPrint());
    }

    /**
     * Add validation checks using a MessageValidationCorrelation document. This
     * allows to create multi-message validation checks.
     * 
     * @param correlationDoc
     * @param messages
     *        a HashMap of HL7Message, the key is used by the correlation
     *        document
     * @throws MissingDependencyException
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     */
    public void addValidationChecks(
            MessageValidationCorrelationDocument correlationDoc,
            Map<String, HL7V3Message> messages)
            throws MissingDependencyException, JsonGenerationException,
            JsonMappingException, IOException {
        Correlations correlations = correlationDoc.getMessageValidationCorrelation().getCorrelations();
        for (Correlation correlation : correlations.getCorrelationList()) {
            // Single Value
            for (SingleValue single : correlation.getSingleValueList()) {
                // Get the value from the other message
                String key = single.getAnd().getMessageKey();
                String sourceElement = single.getAnd().getStringValue();
                HL7V3Message message = messages.get(key);
                if (message == null) {
                    throw new MissingDependencyException(String.format(
                            "Message with id '%s' is missing.", key));
                }
                String sourceValue = message.getValue(sourceElement);
                // Set the value for the destination location
                String destinationElement = single.getBetween();
                addValue(destinationElement, sourceValue, single.getOptional());
            }
            // Copy
            for (Copy copy : correlation.getCopyList()) {
                String key = copy.getSource().getMessageKey();
                String sourceElement = copy.getSource().getStringValue();
                HL7V3Message message = messages.get(key);
                if (message == null) {
                    throw new MissingDependencyException(String.format(
                            "Message with id '%s' is missing.", key));
                }
                List<String> locations = message.getLocations(sourceElement);
                for (String location : locations) {
                    String value = message.getValue(location);
                    addValue(location, value, false);
                }
            }
        }
    }

    private void addValue(String location, String value, boolean optional)
            throws JsonGenerationException, JsonMappingException, IOException {
        if (value != null && !"".equals(value)) {
            PluginCheckType pluginCheck = context.getHL7V3MessageValidationContextDefinition().addNewPluginCheck();
            pluginCheck.setName("gov.nist.healthcare.core.validation.message.plugin.value.ValueCheckPlugin");
            ValueCheckParam param = new ValueCheckParam();
            param.setLocation(location);
            List<ValueCheck> values = new ArrayList<ValueCheck>();
            param.setValues(values);
            ValueCheck valueCheck = new ValueCheck();
            valueCheck.setType(ValueCheckType.PLAIN);
            valueCheck.setText(value);
            values.add(valueCheck);
            if (optional) {
                ValueCheck optionalValue = new ValueCheck();
                optionalValue.setType(ValueCheckType.EMPTY);
                values.add(optionalValue);
            } else {
                Map<String, Boolean> options = new HashMap<String, Boolean>();
                valueCheck.setOptions(options);
                options.put("required", true);
            }
            String jsonParam = mapper.writeValueAsString(param);
            pluginCheck.setParams(jsonParam);
            // MessageInstanceSpecificValuesV3 misv =
            // context.getHL7V3MessageValidationContextDefinition().getMessageInstanceSpecificValues();
            // DataValueLocationItemV3 dvli =
            // misv.addNewDataValueLocationItem();
            // dvli.addNewXPath().setStringValue(location);
            // dvli.addNewValue().addNewPlainText().setStringValue(value);
            // if (optional) {
            // dvli.addNewValue().addNewEmpty();
            // }
        }
    }

}
