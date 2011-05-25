/*
 * NIST Healthcare Core
 * MessageValidationContext.java Jun 4, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.v2;

import gov.nist.healthcare.core.message.ValuedMessageLocation;
import gov.nist.healthcare.core.message.v2.HL7V2Message;
import gov.nist.healthcare.core.profile.Profile;
import gov.nist.healthcare.core.util.XmlBeansUtils;
import gov.nist.healthcare.core.validation.message.MessageValidationContext;
import gov.nist.healthcare.core.validation.message.MissingDependencyException;
import gov.nist.healthcare.core.validation.message.plugin.value.ValueCheck;
import gov.nist.healthcare.core.validation.message.plugin.value.ValueCheck.ValueCheckType;
import gov.nist.healthcare.core.validation.message.plugin.value.ValueCheckParam;
import gov.nist.healthcare.validation.AssertionResultConstants;
import gov.nist.healthcare.validation.AssertionTypeV2Constants;
import gov.nist.healthcare.validation.PluginCheckType;
import gov.nist.healthcare.validation.message.hl7.context.MessageValidationCorrelationDocument;
import gov.nist.healthcare.validation.message.hl7.context.MessageValidationCorrelationDocument.MessageValidationCorrelation.Correlations;
import gov.nist.healthcare.validation.message.hl7.context.MessageValidationCorrelationDocument.MessageValidationCorrelation.Correlations.Correlation;
import gov.nist.healthcare.validation.message.hl7.context.MessageValidationCorrelationDocument.MessageValidationCorrelation.Correlations.Correlation.Copy;
import gov.nist.healthcare.validation.message.hl7.context.MessageValidationCorrelationDocument.MessageValidationCorrelation.Correlations.Correlation.SingleValue;
import gov.nist.healthcare.validation.message.hl7.v2.context.HL7V2MessageValidationContextDefinition.ValidationConfiguration;
import gov.nist.healthcare.validation.message.hl7.v2.context.HL7V2MessageValidationContextDefinition.ValidationConfiguration.HL7Tables;
import gov.nist.healthcare.validation.message.hl7.v2.context.HL7V2MessageValidationContextDefinition.ValidationConfiguration.UserTables;
import gov.nist.healthcare.validation.message.hl7.v2.context.HL7V2MessageValidationContextDefinitionDocument;
import gov.nist.healthcare.validation.message.hl7.v2.context.MessageFailureInterpretationV2;
import gov.nist.healthcare.validation.message.hl7.v2.context.MessageFailureV2;
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
 * HL7V2MessageValidationContextDefinition class
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageValidationContextV2 extends MessageValidationContext {

    private HL7V2MessageValidationContextDefinitionDocument context;
    // private HashMap<AssertionTypeV2Constants.Enum,
    // AssertionResultConstants.Enum> hFailureResult;
    // private Map< ? extends StringEnumAbstractBase,
    // AssertionResultConstants.Enum> hFailureResult;
    private List<String> hl7Tables;
    private List<String> userTables;

    // private final Pattern regexLocation =
    // Pattern.compile("([A-Z0-9]{3})-(\\d+)(?:\\[(\\d+)\\])?(?:\\.(\\d+)?(?:\\.(\\d+)?)?)?");

    /**
     * Constructor
     */
    public MessageValidationContextV2() {
        context = HL7V2MessageValidationContextDefinitionDocument.Factory.newInstance();
        MessageFailureInterpretationV2 mfi = context.addNewHL7V2MessageValidationContextDefinition().addNewFailureInterpretation();
        // Add the default message failures
        MessageFailureV2 mf = mfi.addNewMessageFailure();
        mf.setType(AssertionTypeV2Constants.MESSAGE_STRUCTURE);
        mf.setResult(AssertionResultConstants.ERROR);
        mf = mfi.addNewMessageFailure();
        mf.setType(AssertionTypeV2Constants.USAGE);
        mf.setResult(AssertionResultConstants.ERROR);
        mf = mfi.addNewMessageFailure();
        mf.setType(AssertionTypeV2Constants.CARDINALITY);
        mf.setResult(AssertionResultConstants.ERROR);
        mf = mfi.addNewMessageFailure();
        mf.setType(AssertionTypeV2Constants.LENGTH);
        mf.setResult(AssertionResultConstants.IGNORE);
        mf = mfi.addNewMessageFailure();
        mf.setType(AssertionTypeV2Constants.DATATYPE);
        mf.setResult(AssertionResultConstants.ERROR);
        mf = mfi.addNewMessageFailure();
        mf.setType(AssertionTypeV2Constants.DATA);
        mf.setResult(AssertionResultConstants.ERROR);
        mf = mfi.addNewMessageFailure();
        mf.setType(AssertionTypeV2Constants.DATA_PASSED);
        mf.setResult(AssertionResultConstants.IGNORE);
        mf = mfi.addNewMessageFailure();
        mf.setType(AssertionTypeV2Constants.CHECKED);
        mf.setResult(AssertionResultConstants.AFFIRMATIVE);
        mf = mfi.addNewMessageFailure();
        mf.setType(AssertionTypeV2Constants.X_USAGE);
        mf.setResult(AssertionResultConstants.ERROR);
        mf = mfi.addNewMessageFailure();
        mf.setType(AssertionTypeV2Constants.XTRA);
        mf.setResult(AssertionResultConstants.ERROR);
        init();
    }

    /**
     * Constructor
     * 
     * @param context
     *        a HL7V2MessageValidationContextDefinitionDocument
     */
    public MessageValidationContextV2(
            HL7V2MessageValidationContextDefinitionDocument context) {
        this.context = context;
        init();
    }

    /**
     * Init the context to the default settings
     */
    public void init() {
        // Special Case
        MessageFailureInterpretationV2 mfi = context.getHL7V2MessageValidationContextDefinition().getFailureInterpretation();
        List<MessageFailureV2> alFailures = new ArrayList<MessageFailureV2>();
        alFailures = mfi.getMessageFailureList();
        // Check if each message failure has been set. For special case, set the
        // result even if it has been done by the user in the context file.
        boolean version = false;
        boolean messageStructureId = false;
        boolean messageValidationContext = false;
        boolean tableNotFound = false;
        boolean ambiguousProfile = false;
        boolean messageStructure = false;
        boolean usage = false;
        boolean cardinality = false;
        boolean length = false;
        boolean datatype = false;
        boolean data = false;
        boolean data_passed = false;
        boolean checked = false;
        boolean x_usage = false;
        boolean xtra = false;
        boolean validationConf = false;
        Iterator<MessageFailureV2> it = alFailures.iterator();
        while (it.hasNext()) {
            MessageFailureV2 mf = it.next();
            if (mf.getType() == AssertionTypeV2Constants.VERSION) {
                version = true;
                mf.setResult(AssertionResultConstants.ERROR);
            } else if (mf.getType() == AssertionTypeV2Constants.MESSAGE_STRUCTURE_ID) {
                messageStructureId = true;
                mf.setResult(AssertionResultConstants.ERROR);
            } else if (mf.getType() == AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT) {
                messageValidationContext = true;
                mf.setResult(AssertionResultConstants.ALERT);
            } else if (mf.getType() == AssertionTypeV2Constants.TABLE_NOT_FOUND) {
                tableNotFound = true;
                mf.setResult(AssertionResultConstants.ALERT);
            } else if (mf.getType() == AssertionTypeV2Constants.AMBIGUOUS_PROFILE) {
                ambiguousProfile = true;
                mf.setResult(AssertionResultConstants.ERROR);
            } else if (mf.getType() == AssertionTypeV2Constants.MESSAGE_STRUCTURE) {
                messageStructure = true;
            } else if (mf.getType() == AssertionTypeV2Constants.USAGE) {
                usage = true;
            } else if (mf.getType() == AssertionTypeV2Constants.CARDINALITY) {
                cardinality = true;
            } else if (mf.getType() == AssertionTypeV2Constants.LENGTH) {
                length = true;
            } else if (mf.getType() == AssertionTypeV2Constants.DATATYPE) {
                datatype = true;
            } else if (mf.getType() == AssertionTypeV2Constants.DATA) {
                data = true;
            } else if (mf.getType() == AssertionTypeV2Constants.DATA_PASSED) {
                data_passed = true;
            } else if (mf.getType() == AssertionTypeV2Constants.CHECKED) {
                checked = true;
            } else if (mf.getType() == AssertionTypeV2Constants.X_USAGE) {
                x_usage = true;
            } else if (mf.getType() == AssertionTypeV2Constants.XTRA) {
                xtra = true;
            } else if (mf.getType() == AssertionTypeV2Constants.VALIDATION_CONFIGURATION) {
                validationConf = true;
                mf.setResult(AssertionResultConstants.ALERT);
            }
        }
        // Add the missing message failures
        if (!version) {
            MessageFailureV2 mf = MessageFailureV2.Factory.newInstance();
            mf.setType(AssertionTypeV2Constants.VERSION);
            mf.setResult(AssertionResultConstants.ERROR);
            alFailures.add(mf);
        }
        if (!messageStructureId) {
            MessageFailureV2 mf = MessageFailureV2.Factory.newInstance();
            mf.setType(AssertionTypeV2Constants.MESSAGE_STRUCTURE_ID);
            mf.setResult(AssertionResultConstants.ERROR);
            alFailures.add(mf);
        }
        if (!messageValidationContext) {
            MessageFailureV2 mf = MessageFailureV2.Factory.newInstance();
            mf.setType(AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
            mf.setResult(AssertionResultConstants.ALERT);
            alFailures.add(mf);
        }
        if (!tableNotFound) {
            MessageFailureV2 mf = MessageFailureV2.Factory.newInstance();
            mf.setType(AssertionTypeV2Constants.TABLE_NOT_FOUND);
            mf.setResult(AssertionResultConstants.ALERT);
            alFailures.add(mf);
        }
        if (!ambiguousProfile) {
            MessageFailureV2 mf = MessageFailureV2.Factory.newInstance();
            mf.setType(AssertionTypeV2Constants.AMBIGUOUS_PROFILE);
            mf.setResult(AssertionResultConstants.ERROR);
            alFailures.add(mf);
        }
        if (!messageStructure) {
            MessageFailureV2 mf = MessageFailureV2.Factory.newInstance();
            mf.setType(AssertionTypeV2Constants.MESSAGE_STRUCTURE);
            mf.setResult(AssertionResultConstants.ERROR);
            alFailures.add(mf);
        }
        if (!usage) {
            MessageFailureV2 mf = MessageFailureV2.Factory.newInstance();
            mf.setType(AssertionTypeV2Constants.USAGE);
            mf.setResult(AssertionResultConstants.ERROR);
            alFailures.add(mf);
        }
        if (!cardinality) {
            MessageFailureV2 mf = MessageFailureV2.Factory.newInstance();
            mf.setType(AssertionTypeV2Constants.CARDINALITY);
            mf.setResult(AssertionResultConstants.ERROR);
            alFailures.add(mf);
        }
        if (!length) {
            MessageFailureV2 mf = MessageFailureV2.Factory.newInstance();
            mf.setType(AssertionTypeV2Constants.LENGTH);
            mf.setResult(AssertionResultConstants.ERROR);
            alFailures.add(mf);
        }
        if (!datatype) {
            MessageFailureV2 mf = MessageFailureV2.Factory.newInstance();
            mf.setType(AssertionTypeV2Constants.DATATYPE);
            mf.setResult(AssertionResultConstants.ERROR);
            alFailures.add(mf);
        }
        if (!data) {
            MessageFailureV2 mf = MessageFailureV2.Factory.newInstance();
            mf.setType(AssertionTypeV2Constants.DATA);
            mf.setResult(AssertionResultConstants.ERROR);
            alFailures.add(mf);
        }
        if (!data_passed) {
            MessageFailureV2 mf = MessageFailureV2.Factory.newInstance();
            mf.setType(AssertionTypeV2Constants.DATA_PASSED);
            mf.setResult(AssertionResultConstants.IGNORE);
            alFailures.add(mf);
        }
        if (!checked) {
            MessageFailureV2 mf = MessageFailureV2.Factory.newInstance();
            mf.setType(AssertionTypeV2Constants.CHECKED);
            mf.setResult(AssertionResultConstants.AFFIRMATIVE);
            alFailures.add(mf);
        }
        if (!x_usage) {
            MessageFailureV2 mf = MessageFailureV2.Factory.newInstance();
            mf.setType(AssertionTypeV2Constants.X_USAGE);
            mf.setResult(AssertionResultConstants.ERROR);
            alFailures.add(mf);
        }
        if (!xtra) {
            MessageFailureV2 mf = MessageFailureV2.Factory.newInstance();
            mf.setType(AssertionTypeV2Constants.XTRA);
            mf.setResult(AssertionResultConstants.ERROR);
            alFailures.add(mf);
        }
        if (!validationConf) {
            MessageFailureV2 mf = MessageFailureV2.Factory.newInstance();
            mf.setType(AssertionTypeV2Constants.VALIDATION_CONFIGURATION);
            mf.setResult(AssertionResultConstants.ALERT);
            alFailures.add(mf);
        }
        mfi.setMessageFailureArray(alFailures.toArray(new MessageFailureV2[alFailures.size()]));
        // Create the HashMap
        // hFailureResult = new HashMap<AssertionTypeV2Constants.Enum,
        // AssertionResultConstants.Enum>();
        hFailureResult = new HashMap<StringEnumAbstractBase, AssertionResultConstants.Enum>();
        it = alFailures.iterator();
        while (it.hasNext()) {
            MessageFailureV2 mf = it.next();
            hFailureResult.put(mf.getType(), mf.getResult());
        }
        // Validation Configuration
        ValidationConfiguration validationConfiguration = context.getHL7V2MessageValidationContextDefinition().getValidationConfiguration();
        // HL7 Tables
        hl7Tables = new ArrayList<String>();
        if (validationConfiguration != null) {
            HL7Tables hl7TablesContext = validationConfiguration.getHL7Tables();
            if (hl7TablesContext.isSetAll()) {
                hl7Tables.add(null);
            } else if (!hl7TablesContext.isSetNone()) {
                for (String hl7Table : hl7TablesContext.getHL7TableList()) {
                    hl7Tables.add(hl7Table);
                }
            }
        } else {
            hl7Tables.add(null);
        }
        // User Tables
        userTables = new ArrayList<String>();
        if (validationConfiguration != null) {
            UserTables userTablesContext = validationConfiguration.getUserTables();
            if (userTablesContext.isSetAll()) {
                userTables.add(null);
            } else if (!userTablesContext.isSetNone()) {
                for (String userTable : userTablesContext.getUserTableList()) {
                    userTables.add(userTable);
                }
            }
        } else {
            userTables.add(null);
        }
    }

    // /**
    // * Get a FailureResult for a MessageFailureType
    // *
    // * @param failure
    // * @return a AssertionResultConstants
    // */
    // public AssertionResultConstants.Enum getFailureResult(
    // AssertionTypeV2Constants.Enum failure) {
    // return hFailureResult.get(failure);
    // }

    /**
     * Set a result of a failure. The method can return an
     * IllegalArgumentException if you try to set a result for a fixed failure
     * 
     * @param failure
     * @param result
     */
    public void setFailureResult(AssertionTypeV2Constants.Enum failure,
            AssertionResultConstants.Enum result) {
        if (failure == AssertionTypeV2Constants.VERSION
                || failure == AssertionTypeV2Constants.MESSAGE_STRUCTURE_ID
                || failure == AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT
                || failure == AssertionTypeV2Constants.TABLE_NOT_FOUND
                || failure == AssertionTypeV2Constants.AMBIGUOUS_PROFILE
                || failure == AssertionTypeV2Constants.CHECKED
                || failure == AssertionTypeV2Constants.VALIDATION_CONFIGURATION) {
            throw new IllegalArgumentException(
                    "This type of failure can't be ignored or set as a warning");
        }
        MessageFailureInterpretationV2 mfi = context.getHL7V2MessageValidationContextDefinition().getFailureInterpretation();
        List<MessageFailureV2> alFailures = new ArrayList<MessageFailureV2>();
        alFailures = mfi.getMessageFailureList();
        Iterator<MessageFailureV2> it = alFailures.iterator();
        while (it.hasNext()) {
            MessageFailureV2 mf = it.next();
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
    // public HashMap<AssertionTypeV2Constants.Enum,
    // AssertionResultConstants.Enum> getFailureResults() {
    // return hFailureResult;
    // }

    public HL7V2MessageValidationContextDefinitionDocument getContext() {
        return context;
    }

    public void setContext(
            HL7V2MessageValidationContextDefinitionDocument context) {
        this.context = context;
    }

    public List<String> getHl7Tables() {
        return hl7Tables;
    }

    public List<String> getUserTables() {
        return userTables;
    }

    @Override
    public void load(String xmlMessageValidationContext) throws XmlException {
        context = HL7V2MessageValidationContextDefinitionDocument.Factory.parse(xmlMessageValidationContext);
        ArrayList<XmlError> validationErrors = new ArrayList<XmlError>();
        XmlOptions validationOptions = new XmlOptions();
        validationOptions.setErrorListener(validationErrors);
        if (!context.validate(validationOptions)) {
            StringBuffer sb = new StringBuffer();
            sb.append("The message validation context file is not valid.\n");
            sb.append(XmlBeansUtils.getValidationMessages(validationErrors));
            throw new IllegalArgumentException(sb.toString());
        }
        init();
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
     *        a HashMap of HL7V2Message, the key is used by the correlation
     *        document
     * @param profile
     *        the profile is used in case of a copy
     * @exception MissingDependencyException
     *            thrown when a message is missing
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     */
    public void addValidationChecks(
            MessageValidationCorrelationDocument correlationDoc,
            Map<String, HL7V2Message> messages, Profile profile)
            throws MissingDependencyException, JsonGenerationException,
            JsonMappingException, IOException {
        Correlations correlations = correlationDoc.getMessageValidationCorrelation().getCorrelations();
        for (Correlation correlation : correlations.getCorrelationList()) {
            // Single Value
            for (SingleValue single : correlation.getSingleValueList()) {
                // Get the value from the other message
                String key = single.getAnd().getMessageKey();
                String sourceElement = single.getAnd().getStringValue();
                HL7V2Message message = messages.get(key);
                if (message == null) {
                    throw new MissingDependencyException(String.format(
                            "Message with id '%s' is missing.", key));
                }
                ValuedMessageLocation sourceLocation = new ValuedMessageLocation(
                        sourceElement, "");
                String sourceValue = message.getValue(sourceLocation);
                // Set the value for the destination location
                String destinationElement = single.getBetween();
                ValuedMessageLocation destinationLocation = new ValuedMessageLocation(
                        destinationElement, sourceValue);
                addValue(destinationLocation, single.getOptional());
            }
            // Copy
            for (Copy copy : correlation.getCopyList()) {
                String key = copy.getSource().getMessageKey();
                String sourceElement = copy.getSource().getStringValue();
                HL7V2Message message = messages.get(key);
                if (message == null) {
                    throw new MissingDependencyException(String.format(
                            "Message with id '%s' is missing.", key));
                }
                ValuedMessageLocation sourceLocation = new ValuedMessageLocation(
                        sourceElement, "");
                List<ValuedMessageLocation> locations = message.getLocations(
                        profile, sourceLocation);
                for (ValuedMessageLocation location : locations) {
                    addValue(location, false);
                }
            }
        }
    }

    private void addValue(ValuedMessageLocation location, boolean optional)
            throws JsonGenerationException, JsonMappingException, IOException {
        PluginCheckType pluginCheck = context.getHL7V2MessageValidationContextDefinition().addNewPluginCheck();
        pluginCheck.setName("gov.nist.healthcare.core.validation.message.plugin.value.ValueCheckPlugin");
        ValueCheckParam param = new ValueCheckParam();
        param.setLocation(location.getEPath());
        List<ValueCheck> values = new ArrayList<ValueCheck>();
        param.setValues(values);
        ValueCheck valueCheck = new ValueCheck();
        valueCheck.setType(ValueCheckType.PLAIN);
        valueCheck.setText(location.getValue());
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
        // MessageInstanceSpecificValuesV2 misv =
        // context.getHL7V2MessageValidationContextDefinition().getMessageInstanceSpecificValues();
        // if (location.getValue() != null && !"".equals(location.getValue())) {
        // DataValueLocationItemV2 dvli = misv.addNewDataValueLocationItem();
        // Location loc = dvli.addNewLocation();
        // Segment s = loc.addNewSegment();
        // s.setName(location.getSegmentName());
        // s.setInstanceNumber(location.getSegmentInstanceNumber());
        // if (location.getFieldPosition() > 0) {
        // Field f = s.addNewField();
        // f.setPosition(location.getFieldPosition());
        // if (location.getFieldInstanceNumber() > 0) {
        // f.setInstanceNumber(location.getFieldInstanceNumber());
        // }
        // if (location.getComponentPosition() > 0) {
        // Component c = f.addNewComponent();
        // c.setPosition(location.getComponentPosition());
        // if (location.getSubComponentPosition() > 0) {
        // c.addNewSubComponent().setPosition(
        // location.getSubComponentPosition());
        // }
        // }
        // }
        // dvli.addNewValue().addNewPlainText().setStringValue(
        // location.getValue());
        // if (optional) {
        // dvli.addNewValue().addNewEmpty();
        // } else {
        // DataValueLocationItemV2 dvli2 = misv.addNewDataValueLocationItem();
        // Location loc2 = dvli2.addNewLocation();
        // Segment s2 = loc2.addNewSegment();
        // s2.setName(location.getSegmentName());
        // s2.setInstanceNumber(location.getSegmentInstanceNumber());
        // if (location.getFieldPosition() > 0) {
        // Field f2 = s2.addNewField();
        // f2.setPosition(location.getFieldPosition());
        // if (location.getFieldInstanceNumber() > 0) {
        // f2.setInstanceNumber(location.getFieldInstanceNumber());
        // }
        // if (location.getComponentPosition() > 0) {
        // Component c2 = f2.addNewComponent();
        // c2.setPosition(location.getComponentPosition());
        // if (location.getSubComponentPosition() > 0) {
        // c2.addNewSubComponent().setPosition(
        // location.getSubComponentPosition());
        // }
        // }
        // }
        // dvli2.addNewValue().addNewPresent();
        // }
        // }
    }
}
