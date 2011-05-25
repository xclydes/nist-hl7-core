/*
 * NIST Healthcare Core
 * V2MessageContextValidation.java Aug 11, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.content.v2;

import gov.nist.healthcare.core.Constants.ElementType;
import gov.nist.healthcare.core.Constants.MessageEncoding;
import gov.nist.healthcare.core.message.Message;
import gov.nist.healthcare.core.message.MessageLocation;
import gov.nist.healthcare.core.message.SegmentGroupInstanceNumber;
import gov.nist.healthcare.core.message.v2.HL7V2Message;
import gov.nist.healthcare.core.message.v2.er7.Er7Message;
import gov.nist.healthcare.core.message.v2.xml.XmlMessage;
import gov.nist.healthcare.core.profile.Profile;
import gov.nist.healthcare.core.validation.message.MessageFailure;
import gov.nist.healthcare.core.validation.message.content.MatchFoundResult;
import gov.nist.healthcare.core.validation.message.content.MessageContentValidation;
import gov.nist.healthcare.core.validation.message.plugin.ValidationPlugin;
import gov.nist.healthcare.core.validation.message.util.MessageElementUtil;
import gov.nist.healthcare.core.validation.message.util.TableManager;
import gov.nist.healthcare.core.validation.message.v2.MessageFailureV2;
import gov.nist.healthcare.core.validation.message.v2.MessageValidationContextV2;
import gov.nist.healthcare.data.TableLibraryDocument;
import gov.nist.healthcare.data.TableLibraryDocument.TableLibrary.TableDefinition;
import gov.nist.healthcare.message.Component;
import gov.nist.healthcare.message.Field;
import gov.nist.healthcare.message.MessageElement;
import gov.nist.healthcare.message.Segment;
import gov.nist.healthcare.message.SegmentGroup;
import gov.nist.healthcare.message.SubComponent;
import gov.nist.healthcare.validation.AssertionResultConstants;
import gov.nist.healthcare.validation.AssertionResultConstants.Enum;
import gov.nist.healthcare.validation.AssertionTypeV2Constants;
import gov.nist.healthcare.validation.ErrorSeverityConstants;
import gov.nist.healthcare.validation.PluginCheckType;
import gov.nist.healthcare.validation.Value;
import gov.nist.healthcare.validation.Value.PlainText;
import gov.nist.healthcare.validation.message.hl7.v2.context.DataValueLocationItemV2;
import gov.nist.healthcare.validation.message.hl7.v2.context.DataValueLocationItemV2.Location;
import gov.nist.healthcare.validation.message.hl7.v2.context.DatatypeCheckConstants;
import gov.nist.healthcare.validation.message.hl7.v2.context.HL7V2MessageValidationContextDefinition.DatatypeCheck;
import gov.nist.healthcare.validation.message.hl7.v2.context.HL7V2MessageValidationContextDefinition.IfThenElse;
import gov.nist.healthcare.validation.message.hl7.v2.context.MessageInstanceSpecificValuesV2;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlLineNumber;
import org.apache.xmlbeans.XmlObject;

/**
 * This class validates the message content for a V2 message.
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageContentValidationV2 extends MessageContentValidation {

    protected TableManager tableManager;
    protected List<MessageFailureV2> messageFailures;
    // protected List<TableLibraryDocument> tables;
    protected HL7V2Message message;
    protected MessageValidationContextV2 context;

    /**
     * Validate the message content.
     * 
     * @param message
     *        the message to validate
     * @param context
     *        the message validation context
     * @param profile
     *        the profile
     * @param tableLibraryDocuments
     *        a list of TableLibraryDocument
     * @return a list of message failures
     */
    public List<MessageFailureV2> validate(HL7V2Message message,
            MessageValidationContextV2 context, Profile profile,
            List<TableLibraryDocument> tableLibraryDocuments) {
        this.message = message;
        // this.tables = tableLibraryDocuments;
        this.context = context;
        tableManager = new TableManager();
        tableManager.setLibraries(tableLibraryDocuments);
        messageFailures = new ArrayList<MessageFailureV2>();
        MessageInstanceSpecificValuesV2 misv = context.getContext().getHL7V2MessageValidationContextDefinition().getMessageInstanceSpecificValues();
        if (misv != null) {
            Iterator<DataValueLocationItemV2> itDVLI = misv.getDataValueLocationItemList().iterator();
            while (itDVLI.hasNext()) {
                DataValueLocationItemV2 dvli = itDVLI.next();
                List<MatchFoundResult> results = checkItem(message, dvli,
                        profile);
                for (MatchFoundResult result : results) {
                    if (result.getContextError() != null) {
                        messageFailures.add((MessageFailureV2) result.getContextError());
                    }
                    if (result.getMessageError() != null) {
                        messageFailures.add((MessageFailureV2) result.getMessageError());
                    }
                    List<MessageFailure> assertions = result.getPassedAssertions();
                    if (assertions != null) {
                        for (MessageFailure mf : assertions) {
                            messageFailures.add((MessageFailureV2) mf);
                        }
                    }
                }
            }
        }
        // Conditional Checking
        List<IfThenElse> conditionals = context.getContext().getHL7V2MessageValidationContextDefinition().getIfThenElseList();
        if (conditionals != null) {
            Iterator<IfThenElse> it = conditionals.iterator();
            while (it.hasNext()) {
                IfThenElse condition = it.next();
                List<IfThenElse> conditionWithMatchingInstanceNumber = new ArrayList<IfThenElse>();
                if (condition.getMatchingFieldInstanceNumber()) {
                    conditionWithMatchingInstanceNumber = translateFieldAnyInstanceNumberAttribute(
                            condition, message,
                            condition.getMatchingSegmentInstanceNumber());
                } else if (condition.getMatchingSegmentInstanceNumber()) {
                    conditionWithMatchingInstanceNumber = translateSegmentAnyInstanceNumberAttribute(
                            condition, message);
                } else {
                    conditionWithMatchingInstanceNumber.add(condition);
                }
                for (IfThenElse currentCondition : conditionWithMatchingInstanceNumber) {
                    List<MatchFoundResult> resultsIf = checkItem(message,
                            currentCondition.getIf(), profile);
                    List<MatchFoundResult> resultsThenElse = null;
                    boolean ifStatementPassed = true;
                    for (MatchFoundResult mfr : resultsIf) {
                        ifStatementPassed &= !mfr.hasError();
                    }
                    if (ifStatementPassed) {
                        // Process the then part
                        resultsThenElse = checkItem(message,
                                currentCondition.getThen(), profile);
                    } else {
                        // Process the else part
                        if (condition.getElse() != null) {
                            resultsThenElse = checkItem(message,
                                    currentCondition.getElse(), profile);
                        }
                    }
                    if (resultsThenElse != null && resultsThenElse.size() > 0) {
                        for (MatchFoundResult result : resultsThenElse) {
                            if (result.getContextError() != null) {
                                messageFailures.add((MessageFailureV2) result.getContextError());
                            }
                            if (result.getMessageError() != null) {
                                messageFailures.add((MessageFailureV2) result.getMessageError());
                            }
                            List<MessageFailure> assertions = result.getPassedAssertions();
                            if (assertions != null) {
                                for (MessageFailure mf : assertions) {
                                    messageFailures.add((MessageFailureV2) mf);
                                }
                            }
                        }
                    }
                }
            }
        }
        // Datatype Check
        List<DatatypeCheck> datatypeChecks = context.getContext().getHL7V2MessageValidationContextDefinition().getDatatypeCheckList();
        if (datatypeChecks != null) {
            Iterator<DatatypeCheck> it = datatypeChecks.iterator();
            while (it.hasNext()) {
                DatatypeCheck datatypeCheck = it.next();
                DatatypeCheckConstants.Enum datatype = datatypeCheck.getDatatype();
                if (datatype == DatatypeCheckConstants.HD) {
                    checkHD(datatypeCheck.getLocation(), profile,
                            datatypeCheck.getAssertionResult(),
                            datatypeCheck.getComment());
                } else if (datatype == DatatypeCheckConstants.CE) {
                    checkCE(datatypeCheck.getLocation(), profile,
                            datatypeCheck.getAssertionResult(),
                            datatypeCheck.getComment());
                } else if (datatype == DatatypeCheckConstants.CWE) {
                    checkCWE(datatypeCheck.getLocation(), profile,
                            datatypeCheck.getAssertionResult(),
                            datatypeCheck.getComment());
                } else if (datatype == DatatypeCheckConstants.CNE) {
                    checkCNE(datatypeCheck.getLocation(), profile,
                            datatypeCheck.getAssertionResult(),
                            datatypeCheck.getComment());
                }
            }
        }
        // Plugin Check
        List<PluginCheckType> pluginChecks = context.getContext().getHL7V2MessageValidationContextDefinition().getPluginCheckList();
        if (pluginChecks != null) {
            Iterator<PluginCheckType> it = pluginChecks.iterator();
            while (it.hasNext()) {
                PluginCheckType pluginCheck = it.next();
                String pluginName = pluginCheck.getName();
                ValidationPlugin plugin = loadPlugin(pluginName);
                if (plugin == null) {
                    MessageFailureV2 mf = null;
                    if (message instanceof Er7Message) {
                        mf = new MessageFailureV2(MessageEncoding.V2_ER7);
                    } else if (message instanceof XmlMessage) {
                        mf = new MessageFailureV2(MessageEncoding.V2_XML);
                    }
                    mf.setDescription(String.format(
                            "The plugin '%s' can't be loaded.", pluginName));
                    mf.setFailureType(AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
                    mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                    messageFailures.add(mf);
                } else {
                    try {
                        plugin.setTableManager(tableManager);
                        String jsonParams = pluginCheck.getParams();
                        List<MessageFailure> pluginMessageFailures = plugin.validate(
                                message, jsonParams,
                                pluginCheck.getAssertionResult(),
                                pluginCheck.getComment());
                        for (MessageFailure pluginMessageFailure : pluginMessageFailures) {
                            messageFailures.add((MessageFailureV2) pluginMessageFailure);
                        }
                    } catch (Exception e) {
                        MessageFailureV2 mf = null;
                        if (message instanceof Er7Message) {
                            mf = new MessageFailureV2(MessageEncoding.V2_ER7);
                        } else if (message instanceof XmlMessage) {
                            mf = new MessageFailureV2(MessageEncoding.V2_XML);
                        }
                        mf.setDescription(String.format(
                                "Exception occured when using plugin '%s': %s",
                                pluginName, e.getMessage()));
                        mf.setFailureType(AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
                        mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                        messageFailures.add(mf);
                    }
                }
            }
        }
        return messageFailures;
    }

    /**
     * Check a HD datatype. The valid combinations are HD.1, HD.2 + HD.3 and
     * HD.1 + HD.2 + HD.3
     * 
     * @param location
     * @param profile
     * @param assertionResult
     * @param userComment
     */
    private void checkHD(MessageElement location, Profile profile,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        XmlObject elementInProfile = getElementInProfile(location, profile);
        boolean validElement = isValidElement("HD", location, elementInProfile);
        if (validElement) {
            List<MessageLocation> mls = getMessageLocations(location, message);
            for (MessageLocation ml : mls) {
                List<String> hdValues = new ArrayList<String>();
                for (int i = 1; i <= 3; i++) {
                    MessageLocation hdi = null;
                    if (ml.getElementType() == ElementType.FIELD) {
                        hdi = new MessageLocation(ml.getSegmentGroups(),
                                ml.getSegmentName(),
                                ml.getSegmentInstanceNumber() == 0 ? 1
                                        : ml.getSegmentInstanceNumber(),
                                ml.getFieldPosition(),
                                ml.getFieldInstanceNumber(), i);
                    } else if (ml.getElementType() == ElementType.COMPONENT) {
                        hdi = new MessageLocation(ml.getSegmentGroups(),
                                ml.getSegmentName(),
                                ml.getSegmentInstanceNumber() == 0 ? 1
                                        : ml.getSegmentInstanceNumber(),
                                ml.getFieldPosition(),
                                ml.getFieldInstanceNumber(),
                                ml.getComponentPosition(), i);
                    }
                    hdValues.add(message.getValue(hdi));
                }
                if (!(hdValues.get(0) == null && hdValues.get(1) == null && hdValues.get(2) == null)) {
                    if (!((hdValues.get(0) != null && hdValues.get(1) == null && hdValues.get(2) == null)
                            || (hdValues.get(0) == null
                                    && hdValues.get(1) != null && hdValues.get(2) != null) || (hdValues.get(0) != null
                            && hdValues.get(1) != null && hdValues.get(2) != null))) {
                        MessageFailureV2 mf = null;
                        if (message instanceof Er7Message) {
                            mf = new MessageFailureV2(MessageEncoding.V2_ER7);
                            mf.setPath(ml.getEPath());
                            mf.setLine(((Er7Message) message).getLine(ml));
                            mf.setColumn(((Er7Message) message).getColumn(ml));
                        } else if (message instanceof XmlMessage) {
                            mf = new MessageFailureV2(MessageEncoding.V2_XML);
                            mf.setPath(ml.getXPath());
                        }
                        mf.setDescription("The provided location is not valued correctly for a HD datatype. The possible combination are HD.1 only, or HD.2 and HD.3, or all HD.1, HD.2 and HD.3");
                        mf.setFailureType(AssertionTypeV2Constants.DATATYPE);
                        mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                        mf.setAssertionResult(assertionResult);
                        mf.setUserComment(userComment);
                        messageFailures.add(mf);
                    }
                }
            }
        }
    }

    /**
     * Check a CE datatype. The value in CE.1/CE.4 must come from the table
     * provided in CE.3/CE.6
     * 
     * @param location
     * @param profile
     * @param assertionResult
     * @param userComment
     */
    private void checkCE(MessageElement location, Profile profile,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        XmlObject elementInProfile = getElementInProfile(location, profile);
        boolean validElement = isValidElement("CE", location, elementInProfile);
        if (validElement) {
            String table = null;
            if (elementInProfile != null) {
                table = elementInProfile.newCursor().getAttributeText(
                        QName.valueOf("Table"));
            }
            List<MessageLocation> mls = getMessageLocations(location, message);
            for (MessageLocation ml : mls) {
                List<String> ceValues = new ArrayList<String>();
                for (int i = 1; i <= 6; i++) {
                    MessageLocation cei = null;
                    if (ml.getElementType() == ElementType.FIELD) {
                        cei = new MessageLocation(ml.getSegmentGroups(),
                                ml.getSegmentName(),
                                ml.getSegmentInstanceNumber(),
                                ml.getFieldPosition(),
                                ml.getFieldInstanceNumber(), i);
                    } else if (ml.getElementType() == ElementType.COMPONENT) {
                        cei = new MessageLocation(ml.getSegmentGroups(),
                                ml.getSegmentName(),
                                ml.getSegmentInstanceNumber(),
                                ml.getFieldPosition(),
                                ml.getFieldInstanceNumber(),
                                ml.getComponentPosition(), i);
                    }
                    ceValues.add(message.getValue(cei));
                }
                boolean ce123Check = ceValues.get(0) != null
                        || ceValues.get(1) != null || ceValues.get(2) != null;
                if (ce123Check) {
                    boolean ce123Valid = ceValues.get(0) != null
                            && ceValues.get(2) != null;
                    if (!ce123Valid) {
                        MessageFailureV2 mf = checkCombination(ce123Valid, ml,
                                assertionResult, userComment);
                        if (mf != null) {
                            mf.setDescription("The provided location is not valued correctly for a CE datatype. CE.1 and CE.3 should be populated.");
                        }
                    } else {
                        if (ceValues.get(2) != null) {
                            MessageFailureV2 mf = checkTable(ceValues.get(0),
                                    stripHL7(ceValues.get(2)), null,
                                    userComment);
                            if (mf != null) {
                                mf.setAssertionResult(assertionResult);
                                if (message.getEncoding() == MessageEncoding.V2_ER7) {
                                    mf.setPath(ml.getEPath());
                                    mf.setLine(((Er7Message) message).getLine(ml));
                                    mf.setColumn(((Er7Message) message).getColumn(ml));
                                } else {
                                    mf.setPath(ml.getXPath());
                                }
                                messageFailures.add(mf);
                            }
                        }
                    }
                }
                boolean ce456Check = ceValues.get(3) != null
                        || ceValues.get(4) != null || ceValues.get(5) != null;
                if (ce456Check) {
                    boolean ce456Valid = ceValues.get(3) != null
                            && ceValues.get(5) != null;
                    if (!ce456Valid) {
                        MessageFailureV2 mf = checkCombination(ce456Valid, ml,
                                assertionResult, userComment);
                        if (mf != null) {
                            mf.setDescription("The provided location is not valued correctly for a CE datatype. CE.4 and CE.6 should be populated.");
                        }
                    } else {
                        if (ceValues.get(5) != null) {
                            MessageFailureV2 mf = checkTable(ceValues.get(3),
                                    stripHL7(ceValues.get(5)), null,
                                    userComment);
                            if (mf != null) {
                                mf.setAssertionResult(assertionResult);
                                if (message.getEncoding() == MessageEncoding.V2_ER7) {
                                    mf.setPath(ml.getEPath());
                                    mf.setLine(((Er7Message) message).getLine(ml));
                                    mf.setColumn(((Er7Message) message).getColumn(ml));
                                } else {
                                    mf.setPath(ml.getXPath());
                                }
                                messageFailures.add(mf);
                            }
                        }
                        //
                        // if (ce1Value != null && ce3Value == null
                        // && table != null) {
                        // // Check that ce1value is from the table
                        // // specified
                        // // in the profile
                        // MessageFailureV2 mf = checkTable(ce1Value, table,
                        // null);
                        // if (mf != null) {
                        // mf.setAssertionResult(assertionResult);
                        // messageFailures.add(mf);
                        // }
                        // } else if (ce1Value != null && ce3Value != null) {
                        // // Check that the ce1value is from the table
                        // // specified in ce3value
                        // if (ce3Value.startsWith("HL7")) {
                        // ce3Value = ce3Value.substring(3);
                        // }
                        // MessageFailureV2 mf = checkTable(ce1Value,
                        // ce3Value, null);
                        // if (mf != null) {
                        // mf.setAssertionResult(assertionResult);
                        // messageFailures.add(mf);
                        // }
                        // }
                    }
                }
            }
        }
    }

    /**
     * Check a CWE datatype. The value in CWE.1/CWE.4 must come from the table
     * provided in CWE.3/CWE.6. The version of the table can be specified in
     * CWE.7/CWE.8. Also a valid combination is CWE.2/CWE.5 only.
     * 
     * @param location
     * @param profile
     * @param assertionResult
     * @param userComment
     */
    private void checkCWE(MessageElement location, Profile profile,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        XmlObject elementInProfile = getElementInProfile(location, profile);
        boolean validElement = isValidElement("CWE", location, elementInProfile);
        if (validElement) {
            String table = null;
            if (elementInProfile != null) {
                table = elementInProfile.newCursor().getAttributeText(
                        QName.valueOf("Table"));
            }
            List<MessageLocation> mls = getMessageLocations(location, message);
            for (MessageLocation ml : mls) {
                List<String> cweValues = new ArrayList<String>();
                for (int i = 1; i <= 8; i++) {
                    MessageLocation cwei = null;
                    if (ml.getElementType() == ElementType.FIELD) {
                        cwei = new MessageLocation(ml.getSegmentGroups(),
                                ml.getSegmentName(),
                                ml.getSegmentInstanceNumber(),
                                ml.getFieldPosition(),
                                ml.getFieldInstanceNumber(), i);
                    } else if (ml.getElementType() == ElementType.COMPONENT) {
                        cwei = new MessageLocation(ml.getSegmentGroups(),
                                ml.getSegmentName(),
                                ml.getSegmentInstanceNumber(),
                                ml.getFieldPosition(),
                                ml.getFieldInstanceNumber(),
                                ml.getComponentPosition(), i);
                    }
                    cweValues.add(message.getValue(cwei));
                }
                boolean cwe123Check = cweValues.get(0) != null
                        || cweValues.get(1) != null || cweValues.get(2) != null;
                if (cwe123Check) {
                    boolean cwe123Valid = (cweValues.get(0) != null && cweValues.get(2) != null)
                            || (cweValues.get(0) == null
                                    && cweValues.get(1) != null && cweValues.get(2) == null);
                    if (!cwe123Valid) {
                        MessageFailureV2 mf = checkCombination(cwe123Valid, ml,
                                assertionResult, userComment);
                        if (mf != null) {
                            mf.setDescription("The provided location is not valued correctly for a CWE datatype. CWE.1 and CWE.3, or only CWE.2 should be populated.");
                        }
                    } else {
                        if (cweValues.get(2) != null) {
                            MessageFailureV2 mf = checkTable(cweValues.get(0),
                                    stripHL7(cweValues.get(2)),
                                    cweValues.get(6), userComment);
                            if (mf != null) {
                                mf.setAssertionResult(assertionResult);
                                if (message.getEncoding() == MessageEncoding.V2_ER7) {
                                    mf.setPath(ml.getEPath());
                                    mf.setLine(((Er7Message) message).getLine(ml));
                                    mf.setColumn(((Er7Message) message).getColumn(ml));
                                } else {
                                    mf.setPath(ml.getXPath());
                                }
                                messageFailures.add(mf);
                            }
                        }
                    }
                }
                boolean cwe456Check = cweValues.get(3) != null
                        || cweValues.get(4) != null || cweValues.get(5) != null;
                if (cwe456Check) {
                    boolean cwe456Valid = (cweValues.get(3) != null && cweValues.get(5) != null)
                            || (cweValues.get(3) == null
                                    && cweValues.get(4) != null && cweValues.get(5) == null);
                    if (!cwe456Valid) {
                        MessageFailureV2 mf = checkCombination(cwe456Valid, ml,
                                assertionResult, userComment);
                        if (mf != null) {
                            mf.setDescription("The provided location is not valued correctly for a CWE datatype. CWE.4 and CWE.6, or only CWE.5 should be populated.");
                        }
                    } else {
                        if (cweValues.get(5) != null) {
                            MessageFailureV2 mf = checkTable(cweValues.get(3),
                                    stripHL7(cweValues.get(5)),
                                    cweValues.get(7), userComment);
                            if (mf != null) {
                                mf.setAssertionResult(assertionResult);
                                if (message.getEncoding() == MessageEncoding.V2_ER7) {
                                    mf.setPath(ml.getEPath());
                                    mf.setLine(((Er7Message) message).getLine(ml));
                                    mf.setColumn(((Er7Message) message).getColumn(ml));
                                } else {
                                    mf.setPath(ml.getXPath());
                                }
                                messageFailures.add(mf);
                            }
                        }
                    }
                }
                // if (ce1Value != null && ce3Value == null
                // && table != null) {
                // // Check that ce1value is from the table
                // // specified
                // // in the profile
                // MessageFailureV2 mf = checkTable(ce1Value, table,
                // null);
                // if (mf != null) {
                // mf.setAssertionResult(assertionResult);
                // messageFailures.add(mf);
                // }
                // } else if (ce1Value != null && ce3Value != null) {
                // // Check that the ce1value is from the table
                // // specified in ce3value
                // if (ce3Value.startsWith("HL7")) {
                // ce3Value = ce3Value.substring(3);
                // }
                // MessageFailureV2 mf = checkTable(ce1Value,
                // ce3Value, null);
                // if (mf != null) {
                // mf.setAssertionResult(assertionResult);
                // messageFailures.add(mf);
                // }
                // }
                // }
                // }
            }
        }
    }

    /**
     * Check a CNE datatype. The value in CNE.1/CNE.4 must come from the table
     * provided in CNE.3/CNE.6. The version of the table can be specified in
     * CNE.7/CNE.8.
     * 
     * @param location
     * @param profile
     * @param assertionResult
     * @param userComment
     */
    private void checkCNE(MessageElement location, Profile profile,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        XmlObject elementInProfile = getElementInProfile(location, profile);
        boolean validElement = isValidElement("CNE", location, elementInProfile);
        if (validElement) {
            String table = null;
            if (elementInProfile != null) {
                table = elementInProfile.newCursor().getAttributeText(
                        QName.valueOf("Table"));
            }
            List<MessageLocation> mls = getMessageLocations(location, message);
            for (MessageLocation ml : mls) {
                List<String> cneValues = new ArrayList<String>();
                for (int i = 1; i <= 8; i++) {
                    MessageLocation cnei = null;
                    if (ml.getElementType() == ElementType.FIELD) {
                        cnei = new MessageLocation(ml.getSegmentGroups(),
                                ml.getSegmentName(),
                                ml.getSegmentInstanceNumber(),
                                ml.getFieldPosition(),
                                ml.getFieldInstanceNumber(), i);
                    } else if (ml.getElementType() == ElementType.COMPONENT) {
                        cnei = new MessageLocation(ml.getSegmentGroups(),
                                ml.getSegmentName(),
                                ml.getSegmentInstanceNumber(),
                                ml.getFieldPosition(),
                                ml.getFieldInstanceNumber(),
                                ml.getComponentPosition(), i);
                    }
                    cneValues.add(message.getValue(cnei));
                }
                // Always check the first triplet since the first component is
                // required
                boolean cne123Check = true;
                if (cne123Check) {
                    boolean cne123Valid = (cneValues.get(0) != null && cneValues.get(2) != null);
                    if (!cne123Valid) {
                        MessageFailureV2 mf = checkCombination(cne123Valid, ml,
                                assertionResult, userComment);
                        if (mf != null) {
                            mf.setDescription("The provided location is not valued correctly for a CNE datatype. CNE.1 and CNE.3 should be populated.");
                        }
                    } else {
                        if (cneValues.get(2) != null) {
                            MessageFailureV2 mf = checkTable(cneValues.get(0),
                                    stripHL7(cneValues.get(2)),
                                    cneValues.get(6), userComment);
                            if (mf != null) {
                                mf.setAssertionResult(assertionResult);
                                if (message.getEncoding() == MessageEncoding.V2_ER7) {
                                    mf.setPath(ml.getEPath());
                                    mf.setLine(((Er7Message) message).getLine(ml));
                                    mf.setColumn(((Er7Message) message).getColumn(ml));
                                } else {
                                    mf.setPath(ml.getXPath());
                                }
                                messageFailures.add(mf);
                            }
                        }
                    }
                    boolean cne456Check = cneValues.get(3) != null
                            || cneValues.get(4) != null
                            || cneValues.get(5) != null;
                    if (cne456Check) {
                        boolean cne456Valid = (cneValues.get(3) != null && cneValues.get(5) != null);
                        if (!cne456Valid) {
                            MessageFailureV2 mf = checkCombination(cne456Valid,
                                    ml, assertionResult, userComment);
                            if (mf != null) {
                                mf.setDescription("The provided location is not valued correctly for a CNE datatype. CNE.1, CNE.3, CNE.4 and CNE.6 should be populated.");
                            }
                        } else {
                            if (cneValues.get(5) != null) {
                                MessageFailureV2 mf = checkTable(
                                        cneValues.get(3),
                                        stripHL7(cneValues.get(5)),
                                        cneValues.get(7), userComment);
                                if (mf != null) {
                                    mf.setAssertionResult(assertionResult);
                                    if (message.getEncoding() == MessageEncoding.V2_ER7) {
                                        mf.setPath(ml.getEPath());
                                        mf.setLine(((Er7Message) message).getLine(ml));
                                        mf.setColumn(((Er7Message) message).getColumn(ml));
                                    } else {
                                        mf.setPath(ml.getXPath());
                                    }
                                    messageFailures.add(mf);
                                }
                            }
                        }
                    }
                }
                // if (!cne123Valid || !cne456Valid) {
                // MessageFailureV2 mf = null;
                // if (message instanceof Er7Message) {
                // mf = new MessageFailureV2(MessageEncoding.V2_ER7);
                // mf.setPath(getEPath(location));
                // } else if (message instanceof XmlMessage) {
                // mf = new MessageFailureV2(MessageEncoding.V2_XML);
                // mf.setPath(getXPath(location));
                // }
                // if (!cne123Valid) {
                // mf.setDescription("The provided location is not valued correctly for a CNE datatype. CNE.1 and CNE.3 should be populated.");
                // } else if (!cne456Valid) {
                // mf.setDescription("The provided location is not valued correctly for a CNE datatype. CNE.4 and CNE.6 should be populated.");
                // }
                // mf.setFailureType(AssertionTypeV2Constants.DATATYPE);
                // mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                // mf.setAssertionResult(assertionResult);
                // messageFailures.add(mf);
                // } else {
                // if (cneValues.get(2) != null) {
                // MessageFailureV2 mf = checkTable(cneValues.get(0),
                // cneValues.get(2), cneValues.get(6));
                // if (mf != null) {
                // mf.setAssertionResult(assertionResult);
                // messageFailures.add(mf);
                // }
                // }
                // if (cneValues.get(5) != null) {
                // MessageFailureV2 mf = checkTable(cneValues.get(3),
                // cneValues.get(5), cneValues.get(7));
                // if (mf != null) {
                // mf.setAssertionResult(assertionResult);
                // messageFailures.add(mf);
                // }
                // }
                // }

                // if (ce1Value != null && ce3Value == null
                // && table != null) {
                // // Check that ce1value is from the table
                // // specified
                // // in the profile
                // MessageFailureV2 mf = checkTable(ce1Value, table,
                // null);
                // if (mf != null) {
                // mf.setAssertionResult(assertionResult);
                // messageFailures.add(mf);
                // }
                // } else if (ce1Value != null && ce3Value != null) {
                // // Check that the ce1value is from the table
                // // specified in ce3value
                // if (ce3Value.startsWith("HL7")) {
                // ce3Value = ce3Value.substring(3);
                // }
                // MessageFailureV2 mf = checkTable(ce1Value,
                // ce3Value, null);
                // if (mf != null) {
                // mf.setAssertionResult(assertionResult);
                // messageFailures.add(mf);
                // }
                // }
                // }
                // }
            }
        }
    }

    /**
     * Strip HL7 prefix for tables
     * 
     * @param tableId
     * @return the stripped tableId
     */
    private String stripHL7(String tableId) {
        String strippedTableId = tableId;
        if (tableId.startsWith("HL7")) {
            strippedTableId = tableId.substring(3);
        }
        return strippedTableId;
    }

    /**
     * Check if the location is compatible with the type of check
     * 
     * @param datatypeCheck
     * @param location
     * @param elementInProfile
     * @return true if the location is compatible; false otherwise
     */
    private boolean isValidElement(String datatypeCheck,
            MessageElement location, XmlObject elementInProfile) {
        boolean validElement = true;
        if (elementInProfile != null) {
            String datatype = elementInProfile.newCursor().getAttributeText(
                    QName.valueOf("Datatype"));
            if (!datatypeCheck.equals(datatype)) {
                MessageFailureV2 mf = null;
                if (message instanceof Er7Message) {
                    mf = new MessageFailureV2(MessageEncoding.V2_ER7);
                    mf.setPath(getEPath(location));
                } else if (message instanceof XmlMessage) {
                    mf = new MessageFailureV2(MessageEncoding.V2_XML);
                    mf.setPath(getXPath(location));
                }
                mf.setDescription(String.format(
                        "The provided location is not a %s datatype.",
                        datatypeCheck));
                mf.setFailureType(AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
                mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                messageFailures.add(mf);
                validElement = false;
            }
        } else {
            MessageLocation ml = new MessageLocation(location);
            if (ml.getElementType() == ElementType.SEGMENT_GROUP
                    || ml.getElementType() == ElementType.SEGMENT
                    || ml.getElementType() == ElementType.SUBCOMPONENT) {
                MessageFailureV2 mf = null;
                if (message instanceof Er7Message) {
                    mf = new MessageFailureV2(MessageEncoding.V2_ER7);
                    mf.setPath(getEPath(location));
                } else if (message instanceof XmlMessage) {
                    mf = new MessageFailureV2(MessageEncoding.V2_XML);
                    mf.setPath(getXPath(location));
                }
                mf.setDescription(String.format(
                        "The provided location can't be a %s datatype. The location must be a Field or a Component.",
                        datatypeCheck));
                mf.setFailureType(AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
                mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                messageFailures.add(mf);
                validElement = false;
            }
        }
        return validElement;
    }

    /**
     * Create a MessageFailure if the combination is incorrect.
     * 
     * @param validCombination
     * @param location
     * @param assertionResult
     * @param userComment
     * @return a MessageFailure if it's not correct; null otherwise
     */
    private MessageFailureV2 checkCombination(boolean validCombination,
            MessageLocation location,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        MessageFailureV2 mf = null;
        if (!validCombination) {
            if (message instanceof Er7Message) {
                mf = new MessageFailureV2(MessageEncoding.V2_ER7);
                // mf.setPath(getEPath(location));
                mf.setPath(location.getEPath());
                mf.setLine(((Er7Message) message).getLine(location));
                mf.setColumn(((Er7Message) message).getColumn(location));
            } else if (message instanceof XmlMessage) {
                mf = new MessageFailureV2(MessageEncoding.V2_XML);
                // mf.setPath(getXPath(location));
                mf.setPath(location.getXPath());
            }
            mf.setFailureType(AssertionTypeV2Constants.DATATYPE);
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            mf.setAssertionResult(assertionResult);
            mf.setUserComment(userComment);
            messageFailures.add(mf);
        }
        return mf;
    }

    /**
     * Create a MessageFailure if the location is not a valid location
     * 
     * @param location
     * @param assertionResult
     * @return a MessageFailure if it's not a valid location
     */
    private MessageFailureV2 checkLocation(MessageLocation location,
            AssertionResultConstants.Enum assertionResult) {
        MessageFailureV2 mf = null;
        if (message instanceof Er7Message) {
            mf = new MessageFailureV2(MessageEncoding.V2_ER7);
            // mf.setPath(getEPath(location));
            mf.setPath(location.getEPath());
            mf.setLine(((Er7Message) message).getLine(location));
            mf.setColumn(((Er7Message) message).getColumn(location));
        } else if (message instanceof XmlMessage) {
            mf = new MessageFailureV2(MessageEncoding.V2_XML);
            // mf.setPath(getXPath(location));
            mf.setPath(location.getXPath());
        }
        mf.setFailureType(AssertionTypeV2Constants.DATATYPE);
        mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
        mf.setAssertionResult(assertionResult);
        messageFailures.add(mf);
        return mf;
    }

    /**
     * Return all the message locations based on the message. It will replace
     * the AnyInstanceNumber by the actual instance numbers.
     * 
     * @param location
     * @param message
     * @return a list of MessageLocation
     */
    private List<MessageLocation> getMessageLocations(MessageElement location,
            HL7V2Message message) {
        List<MessageLocation> messageLocations = new ArrayList<MessageLocation>();
        boolean isSegmentAnyInstanceNumber = MessageElementUtil.isSegmentAnyInstanceNumberSet(location);
        boolean isFieldAnyInstanceNumber = MessageElementUtil.isFieldAnyInstanceNumberSet(location);
        if (!isSegmentAnyInstanceNumber && !isFieldAnyInstanceNumber) {
            // No any instance number
            messageLocations.add(new MessageLocation(location));
        } else if (isSegmentAnyInstanceNumber && !isFieldAnyInstanceNumber) {
            // segment any instance number only
            int segmentCount = message.getSegmentCount(location.getSegment().getName());
            for (int s = 1; s <= segmentCount; s++) {
                location.getSegment().setInstanceNumber(s);
                messageLocations.add(new MessageLocation(location));
            }
        } else if (!isSegmentAnyInstanceNumber && isFieldAnyInstanceNumber) {
            // field any instance number only
            int fieldCount = message.getFieldCount(
                    location.getSegment().getName(),
                    location.getSegment().getInstanceNumber() == 0 ? 1
                            : location.getSegment().getInstanceNumber(),
                    location.getSegment().getField().getPosition());
            for (int f = 1; f <= fieldCount; f++) {
                location.getSegment().getField().setInstanceNumber(f);
                messageLocations.add(new MessageLocation(location));
            }
        } else if (isSegmentAnyInstanceNumber && isFieldAnyInstanceNumber) {
            // both segment and field any instance number
            int segmentCount = message.getSegmentCount(location.getSegment().getName());
            for (int s = 1; s <= segmentCount; s++) {
                location.getSegment().setInstanceNumber(s);
                int fieldCount = message.getFieldCount(
                        location.getSegment().getName(),
                        location.getSegment().getInstanceNumber(),
                        location.getSegment().getField().getPosition());
                for (int f = 1; f <= fieldCount; f++) {
                    location.getSegment().getField().setInstanceNumber(f);
                    messageLocations.add(new MessageLocation(location));
                }
            }
        }
        return messageLocations;
    }

    /**
     * Translate the IfThenElse statement into a list of IfThenElse by replace
     * the AnyInstanceNumber of Segment with actual instance number.
     * 
     * @param condition
     *        the condition to translate
     * @param message
     *        the message to validate
     * @return a list of ifthenelse statement with actual instance number for
     *         segment
     */
    private List<IfThenElse> translateSegmentAnyInstanceNumberAttribute(
            IfThenElse condition, HL7V2Message message) {
        List<IfThenElse> conditions = new ArrayList<IfThenElse>();
        DataValueLocationItemV2 conditionIf = condition.getIf();
        // Count segment in the if condition
        int ifSegmentCount = message.getSegmentCount(conditionIf.getLocation().getSegment().getName());
        for (int i = 1; i <= ifSegmentCount; i++) {
            IfThenElse actualCondition = IfThenElse.Factory.newInstance();
            actualCondition.set(condition.copy());
            actualCondition.getIf().getLocation().getSegment().setAnyInstanceNumber(
                    false);
            actualCondition.getIf().getLocation().getSegment().setInstanceNumber(
                    i);
            actualCondition.getThen().getLocation().getSegment().setAnyInstanceNumber(
                    false);
            actualCondition.getThen().getLocation().getSegment().setInstanceNumber(
                    i);
            if (actualCondition.getElse() != null) {
                actualCondition.getElse().getLocation().getSegment().setAnyInstanceNumber(
                        false);
                actualCondition.getElse().getLocation().getSegment().setInstanceNumber(
                        i);
            }
            conditions.add(actualCondition);
        }
        return conditions;
    }

    /**
     * Translate the IfThenElse statement into a list of IfThenElse by replace
     * the AnyInstanceNumber of Field with actual instance number.
     * 
     * @param condition
     *        the condition to translate
     * @param message
     *        the message to validate
     * @param matchSegment
     *        replace the segment instance number
     * @return a list of ifthenelse statement with actual instance number for
     *         field
     */
    private List<IfThenElse> translateFieldAnyInstanceNumberAttribute(
            IfThenElse condition, HL7V2Message message, boolean matchSegment) {
        List<IfThenElse> conditions = new ArrayList<IfThenElse>();
        DataValueLocationItemV2 conditionIf = condition.getIf();
        // Count segment in the if condition
        int loopStart = 1;
        int ifSegmentCount = message.getSegmentCount(conditionIf.getLocation().getSegment().getName());
        int loopEnd = ifSegmentCount;
        if (!matchSegment) {
            loopStart = conditionIf.getLocation().getSegment().getInstanceNumber() == 0 ? 1
                    : conditionIf.getLocation().getSegment().getInstanceNumber();
            loopEnd = loopStart;
        }
        for (int i = loopStart; i <= loopEnd; i++) {
            // Count field in the sub if condition
            int ifFieldCount = message.getFieldCount(
                    conditionIf.getLocation().getSegment().getName(),
                    i,
                    conditionIf.getLocation().getSegment().getField().getPosition());
            for (int j = 1; j <= ifFieldCount; j++) {
                IfThenElse actualCondition = IfThenElse.Factory.newInstance();
                actualCondition.set(condition.copy());
                actualCondition.getIf().getLocation().getSegment().setAnyInstanceNumber(
                        false);
                actualCondition.getIf().getLocation().getSegment().setInstanceNumber(
                        i);
                actualCondition.getIf().getLocation().getSegment().getField().setAnyInstanceNumber(
                        false);
                actualCondition.getIf().getLocation().getSegment().getField().setInstanceNumber(
                        j);
                actualCondition.getThen().getLocation().getSegment().setAnyInstanceNumber(
                        false);
                actualCondition.getThen().getLocation().getSegment().setInstanceNumber(
                        i);
                actualCondition.getThen().getLocation().getSegment().getField().setAnyInstanceNumber(
                        false);
                actualCondition.getThen().getLocation().getSegment().getField().setInstanceNumber(
                        j);
                if (actualCondition.getElse() != null) {
                    actualCondition.getElse().getLocation().getSegment().setAnyInstanceNumber(
                            false);
                    actualCondition.getElse().getLocation().getSegment().setInstanceNumber(
                            i);
                    actualCondition.getElse().getLocation().getSegment().getField().setAnyInstanceNumber(
                            false);
                    actualCondition.getElse().getLocation().getSegment().getField().setInstanceNumber(
                            j);
                }
                conditions.add(actualCondition);
            }
        }
        return conditions;
    }

    /**
     * Get the number of segment from the DataValueLocationItemV2.
     * 
     * @param message
     *        an HL7V2 message
     * @param location
     *        a MessageElement object
     * @return the number of segment
     */
    private int getSegmentCount(HL7V2Message message, MessageElement location) {
        int segmentCount = 0;
        // SegGroup
        gov.nist.healthcare.message.SegmentGroup sg = location.getSegmentGroup();
        gov.nist.healthcare.message.Segment s = null;
        while (sg != null) {
            if (sg != null) {
                s = sg.getSegment();
            }
            sg = sg.getSegmentGroup();
        }
        if (s == null) {
            s = location.getSegment();
        }
        if (s != null) {
            segmentCount = message.getSegmentCount(s.getName());
        }

        return segmentCount;
    }

    /**
     * Transform a MessageElement into a list of XPath expression
     * 
     * @param location
     *        a MessageElement object
     * @param segmentCount
     *        the number of segment
     * @return a list of XPath expression
     */
    private List<String> getXPaths(MessageElement location, int segmentCount) {
        List<String> xpaths = new ArrayList<String>();
        StringBuffer sb = new StringBuffer("/");
        // SegGroup
        gov.nist.healthcare.message.SegmentGroup sg = location.getSegmentGroup();
        gov.nist.healthcare.message.Segment s = null;
        while (sg != null) {
            int segmentGroupInstanceNumber = sg.getInstanceNumber() == 0 ? 1
                    : sg.getInstanceNumber();
            sb.append("/*[ends-with(name(), '.").append(sg.getName()).append(
                    "')][").append(segmentGroupInstanceNumber).append("]");
            if (sg != null) {
                s = sg.getSegment();
            }
            sg = sg.getSegmentGroup();
        }
        if (s == null) {
            s = location.getSegment();
        }
        if (s != null) {
            StringBuffer sbCopy = new StringBuffer();
            if (s.getAnyInstanceNumber()) {
                for (int i = 1; i <= segmentCount; i++) {
                    sbCopy.delete(0, sbCopy.length());
                    sbCopy.append(sb);
                    sbCopy.append("/*:").append(s.getName()).append("[").append(
                            i).append("]");
                    gov.nist.healthcare.message.Field f = s.getField();
                    if (f != null) {
                        if (!f.getAnyInstanceNumber()) {
                            int fieldInstanceNumber = f.getInstanceNumber() == 0 ? 1
                                    : f.getInstanceNumber();
                            sbCopy.append("/*[ends-with(name(), '.").append(
                                    f.getPosition()).append("')][").append(
                                    fieldInstanceNumber).append("]");
                        } else {
                            sbCopy.append("/*[ends-with(name(), '.").append(
                                    f.getPosition()).append("')]");
                        }
                        gov.nist.healthcare.message.Component c = f.getComponent();
                        if (c != null) {
                            sbCopy.append("/*[ends-with(name(), '.").append(
                                    c.getPosition()).append("')]");
                            gov.nist.healthcare.message.SubComponent sc = c.getSubComponent();
                            if (sc != null) {
                                sbCopy.append("/*[ends-with(name(), '.").append(
                                        sc.getPosition()).append("')]");
                            }
                        }
                    }
                    xpaths.add(sbCopy.toString());
                }
            } else {
                sbCopy.delete(0, sbCopy.length());
                sbCopy.append(sb);
                int segmentInstanceNumber = s.getInstanceNumber() == 0 ? 1
                        : s.getInstanceNumber();
                sbCopy.append("/*:").append(s.getName()).append("[").append(
                        segmentInstanceNumber).append("]");
                gov.nist.healthcare.message.Field f = s.getField();
                if (f != null) {
                    if (!f.getAnyInstanceNumber()) {
                        int fieldInstanceNumber = f.getInstanceNumber() == 0 ? 1
                                : f.getInstanceNumber();
                        sbCopy.append("/*[ends-with(name(), '.").append(
                                f.getPosition()).append("')][").append(
                                fieldInstanceNumber).append("]");
                    } else {
                        sbCopy.append("/*[ends-with(name(), '.").append(
                                f.getPosition()).append("')]");
                    }
                    gov.nist.healthcare.message.Component c = f.getComponent();
                    if (c != null) {
                        sbCopy.append("/*[ends-with(name(), '.").append(
                                c.getPosition()).append("')]");
                        gov.nist.healthcare.message.SubComponent sc = c.getSubComponent();
                        if (sc != null) {
                            sbCopy.append("/*[ends-with(name(), '.").append(
                                    sc.getPosition()).append("')]");
                        }
                    }
                }
                xpaths.add(sbCopy.toString());
            }
        }
        return xpaths;
    }

    /**
     * Transform a MessageElement into a list of EPath expression
     * 
     * @param location
     *        a MessageElement object
     * @param segmentCount
     *        the number of segment
     * @return a list of EPath expression
     */
    private List<String> getEPaths(MessageElement location, int segmentCount) {
        List<String> epaths = new ArrayList<String>();
        StringBuffer sb = new StringBuffer("");
        // SegGroup
        gov.nist.healthcare.message.SegmentGroup sg = location.getSegmentGroup();
        gov.nist.healthcare.message.Segment s = null;
        while (sg != null) {
            if (sg != null) {
                s = sg.getSegment();
            }
            sg = sg.getSegmentGroup();
        }
        if (s == null) {
            s = location.getSegment();
        }
        if (s != null) {
            StringBuffer sbCopy = new StringBuffer();
            if (s.getAnyInstanceNumber()) {
                for (int i = 1; i <= segmentCount; i++) {
                    sbCopy.delete(0, sbCopy.length());
                    sbCopy.append(sb);
                    sbCopy.append(s.getName()).append("[").append(i).append("]");
                    gov.nist.healthcare.message.Field f = s.getField();
                    if (f != null) {
                        if (!f.getAnyInstanceNumber()) {
                            int fieldInstanceNumber = f.getInstanceNumber() == 0 ? 1
                                    : f.getInstanceNumber();
                            sbCopy.append(".").append(f.getPosition()).append(
                                    "[").append(fieldInstanceNumber).append("]");
                        } else {
                            sbCopy.append(".").append(f.getPosition()).append(
                                    "[*]");
                        }
                        gov.nist.healthcare.message.Component c = f.getComponent();
                        if (c != null) {
                            sbCopy.append(".").append(c.getPosition());
                            gov.nist.healthcare.message.SubComponent sc = c.getSubComponent();
                            if (sc != null) {
                                sbCopy.append(".").append(sc.getPosition());
                            }
                        }
                    }
                    epaths.add(sbCopy.toString());
                }
            } else {
                sbCopy.delete(0, sbCopy.length());
                sbCopy.append(sb);
                int segmentInstanceNumber = s.getInstanceNumber() == 0 ? 1
                        : s.getInstanceNumber();
                sbCopy.append(s.getName()).append("[").append(
                        segmentInstanceNumber).append("]");
                gov.nist.healthcare.message.Field f = s.getField();
                if (f != null) {
                    if (!f.getAnyInstanceNumber()) {
                        int fieldInstanceNumber = f.getInstanceNumber() == 0 ? 1
                                : f.getInstanceNumber();
                        sbCopy.append(".").append(f.getPosition()).append("[").append(
                                fieldInstanceNumber).append("]");
                    } else {
                        sbCopy.append(".").append(f.getPosition()).append("[*]");
                    }
                    gov.nist.healthcare.message.Component c = f.getComponent();
                    if (c != null) {
                        sbCopy.append(".").append(c.getPosition());
                        gov.nist.healthcare.message.SubComponent sc = c.getSubComponent();
                        if (sc != null) {
                            sbCopy.append(".").append(sc.getPosition());
                        }
                    }
                }
                epaths.add(sbCopy.toString());
            }
        }
        return epaths;
    }

    /**
     * Check an item. It returns a list of MatchFoundResult object.
     * 
     * @param message
     *        the message
     * @param item
     *        the item to be checked
     * @param profile
     *        the profile
     * @return the result of the check
     */
    private List<MatchFoundResult> checkItem(HL7V2Message message,
            DataValueLocationItemV2 item, Profile profile) {
        List<MatchFoundResult> results = new ArrayList<MatchFoundResult>();
        List<Value> values = item.getValueList();
        List<PlainText> contextValues = getContextValues(values);
        List<MessageElement> contextMessageValues = getContextMessageValues(values);
        contextValues.addAll(processContextMessageValues(contextMessageValues,
                message));
        List<String> regexValues = getRegexValues(values);
        boolean regexCheck = regexValues.size() > 0;
        boolean emptyCheck = contextValues.contains(null);
        boolean presentCheck = isPresentCheck(values);
        boolean checkAll = item.getLocation().getCheckAll();
        Enum assertionResult = item.getAssertionResult();
        String userPath = null;
        if (message instanceof XmlMessage) {
            int segmentCount = getSegmentCount(message, item.getLocation());
            List<String> xpaths = getXPaths(item.getLocation(), segmentCount);
            boolean endProcess = false;
            for (int i = 0; i < xpaths.size() && !endProcess; i++) {
                String xpath = xpaths.get(i);
                List<String> messageValues = message.getValues(xpath);
                // Validate
                List<MatchFoundResult> partialResults = null;
                partialResults = matchFound(checkAll, regexCheck, emptyCheck,
                        presentCheck, messageValues, contextValues,
                        regexValues, xpath, message.getEncoding(),
                        assertionResult, item.getComment());
                for (MatchFoundResult mfr : partialResults) {
                    // if (mfr.getContextError() != null) {
                    // MessageFailureV2 mf = (MessageFailureV2)
                    // mfr.getContextError();
                    // if (mf.getAssertionDeclaration() == null) {
                    // mf.setAssertionDeclaration(item.getComment());
                    // }
                    // }
                    if (mfr.getMessageError() != null) {
                        MessageFailureV2 mf = (MessageFailureV2) mfr.getMessageError();
                        // if (mf.getAssertionDeclaration() == null) {
                        // mf.setAssertionDeclaration(item.getComment());
                        // }
                        if (mf.getPath() != null) {
                            try {
                                setLineColumn(mf, message, mf.getPath());
                            } catch (IllegalArgumentException iae) {
                                setLineColumn(mf, message, item.getLocation());
                            }
                        } else {
                            setLineColumn(mf, message, item.getLocation());
                        }
                    }
                    if (!checkAll && partialResults.size() == 1) {
                        if (!partialResults.get(0).hasError()) {
                            endProcess = true;
                        }
                    }
                }
                results.addAll(partialResults);
            }
            userPath = getXPath(item.getLocation());
        } else if (message instanceof Er7Message) {
            int segmentCount = getSegmentCount(message, item.getLocation());
            List<String> epaths = getEPaths(item.getLocation(), segmentCount);
            boolean endProcess = false;
            for (int i = 0; i < epaths.size() && !endProcess; i++) {
                String epath = epaths.get(i);
                List<String> messageValues = message.getValues(epath);
                // Validate
                List<MatchFoundResult> partialResults = null;
                partialResults = matchFound(checkAll, regexCheck, emptyCheck,
                        presentCheck, messageValues, contextValues,
                        regexValues, epath, message.getEncoding(),
                        assertionResult, item.getComment());
                for (MatchFoundResult mfr : partialResults) {
                    // if (mfr.getContextError() != null) {
                    // MessageFailureV2 mf = (MessageFailureV2)
                    // mfr.getContextError();
                    // if (mf.getAssertionDeclaration() == null) {
                    // mf.setAssertionDeclaration(item.getComment());
                    // }
                    // }
                    if (mfr.getMessageError() != null) {
                        MessageFailureV2 mf = (MessageFailureV2) mfr.getMessageError();
                        // if (mf.getAssertionDeclaration() == null) {
                        // mf.setAssertionDeclaration(item.getComment());
                        // }
                        if (mf.getPath() != null) {
                            try {
                                setLineColumn(mf, message, mf.getPath());
                            } catch (IllegalArgumentException iae) {
                                setLineColumn(mf, message, item.getLocation());
                            }
                        } else {
                            setLineColumn(mf, message, item.getLocation());
                        }
                    }
                }
                if (!checkAll && partialResults.size() == 1) {
                    if (!partialResults.get(0).hasError()) {
                        endProcess = true;
                    }
                }
                results.addAll(partialResults);
            }
            userPath = getEPath(item.getLocation());
        }
        int countPassed = 0;
        int countError = 0;
        for (MatchFoundResult mfr : results) {
            if (mfr.getPassedAssertions() != null) {
                countPassed++;
            } else if (mfr.hasError()) {
                countError++;
            }
        }
        if (userPath != null && userPath.indexOf("[*]") != -1
                && countPassed == 0) {
            if (countError > 0) {
                MatchFoundResult mfr = results.get(0);
                MessageFailureV2 mf = null;
                if (mfr.getContextError() != null) {
                    StringBuffer sb = new StringBuffer(
                            "The specified message element match location does not map to a message element. The data value at the specified match location can't be evaluated. Refine the message element match location. The provided path expression is ");
                    sb.append(userPath);
                    mf = (MessageFailureV2) mfr.getContextError();
                    mf.setDescription(sb.toString());
                    results.clear();
                    results.add(mfr);
                } else if (mfr.getMessageError() != null) {
                    mf = (MessageFailureV2) mfr.getMessageError();
                    mf.setColumn(-1);
                    mf.setLine(-1);
                    StringBuffer sb = new StringBuffer("'");
                    for (PlainText contextValue : contextValues) {
                        sb.append(contextValue.getStringValue()).append("; ");
                    }
                    for (String regexValue : regexValues) {
                        sb.append(regexValue).append("; ");
                    }
                    if (sb.length() > 2) {
                        sb.delete(sb.length() - 2, sb.length());
                    }
                    sb.append("' has not been found in the message at the location '");
                    sb.append(userPath).append("'");
                    mf.setDescription(sb.toString());
                    mf.setElementContent(null);
                    mf.setPath(userPath);
                    results.clear();
                    results.add(mfr);
                }
            }
        } else if ((!checkAll && countPassed > 0)
                || (checkAll && countError == 0)) {
            MatchFoundResultV2 result = new MatchFoundResultV2();
            List<MessageFailure> assertions = new ArrayList<MessageFailure>();
            for (MatchFoundResult mfr : results) {
                if (mfr.getPassedAssertions() != null) {
                    assertions.addAll(mfr.getPassedAssertions());
                }
            }
            result.setPassedAssertions(assertions);
            results.clear();
            results.add(result);
        }
        for (MatchFoundResult result : results) {
            if (result.getContextError() != null) {
                if (getElementInProfile(item.getLocation(), profile) != null) {
                    MessageFailureV2 mf = (MessageFailureV2) result.getContextError();
                    mf.setFailureType(AssertionTypeV2Constants.DATA);
                    result.setMessageError(mf);
                    result.setContextError(null);
                } else {
                    MessageFailureV2 mf = (MessageFailureV2) result.getContextError();
                    mf.setAssertionResult(null);
                }
            }
        }
        return results;
    }

    /**
     * Get the actual values in the message from the context location.
     * 
     * @param locations
     *        a list of MessageElement (location in the message)
     * @param message
     * @return a list of PlainText
     */
    private List<PlainText> processContextMessageValues(
            List<MessageElement> locations, HL7V2Message message) {
        List<PlainText> contextMessageValues = new ArrayList<PlainText>();
        List<String> paths = null;
        for (MessageElement location : locations) {
            int segmentCount = getSegmentCount(message, location);
            if (message instanceof Er7Message) {
                paths = getEPaths(location, segmentCount);
            } else if (message instanceof XmlMessage) {
                paths = getXPaths(location, segmentCount);
            }
            for (String path : paths) {
                List<String> values = message.getValues(path);
                for (String value : values) {
                    PlainText pl = PlainText.Factory.newInstance();
                    pl.setStringValue(value);
                    contextMessageValues.add(pl);
                }
            }
        }
        return contextMessageValues;
    }

    /**
     * Set the line and column number where the message failure occurs.
     * 
     * @param messageFailure
     *        the messsage failure to update
     * @param message
     * @param location
     */
    private void setLineColumn(MessageFailureV2 messageFailure,
            Message message, Location location) {
        List<SegmentGroupInstanceNumber> segmentGroups = null;
        String segmentName = null;
        int segmentGroupInstanceNumber;
        int segmentInstanceNumber = 0;
        int fieldPosition = 0;
        int fieldInstanceNumber = 0;
        int componentPosition = 0;
        int subComponentPosition = 0;
        // parse the location
        SegmentGroup sg = location.getSegmentGroup();
        Segment s = null;
        if (sg != null) {
            segmentGroups = new ArrayList<SegmentGroupInstanceNumber>();
            while (sg != null) {
                SegmentGroupInstanceNumber sgin = new SegmentGroupInstanceNumber();
                sgin.setName(sg.getName());
                segmentGroupInstanceNumber = sg.getInstanceNumber();
                segmentGroupInstanceNumber = segmentGroupInstanceNumber == 0 ? 1
                        : segmentGroupInstanceNumber;
                sgin.setInstanceNumber(segmentGroupInstanceNumber);
                segmentGroups.add(sgin);
                s = sg.getSegment();
                sg = sg.getSegmentGroup();
            }
        }
        if (s == null) {
            s = location.getSegment();
        }
        if (s != null) {
            segmentName = s.getName();
            segmentInstanceNumber = s.getInstanceNumber();
            segmentInstanceNumber = segmentInstanceNumber == 0 ? 1
                    : segmentInstanceNumber;
            Field f = s.getField();
            if (f != null) {
                fieldPosition = f.getPosition();
                fieldInstanceNumber = f.getInstanceNumber();
                fieldInstanceNumber = fieldInstanceNumber == 0 ? 1
                        : fieldInstanceNumber;
                Component c = f.getComponent();
                if (c != null) {
                    componentPosition = c.getPosition();
                    SubComponent sc = c.getSubComponent();
                    if (sc != null) {
                        subComponentPosition = sc.getPosition();
                    }
                }
            }
        }
        MessageLocation messageLocation = MessageLocation.getMessageLocation(
                segmentGroups, segmentName, segmentInstanceNumber,
                fieldPosition, fieldInstanceNumber, componentPosition,
                subComponentPosition);
        switch (message.getEncoding()) {
        case V2_ER7:
            messageFailure.setLine(((Er7Message) message).getLine(messageLocation));
            messageFailure.setColumn(((Er7Message) message).getColumn(messageLocation));
            break;
        case V2_XML:
            String xpath = messageLocation.getXPath();
            XmlObject[] rs = ((XmlMessage) message).getDocument().selectPath(
                    xpath);
            if (rs.length == 1) {
                XmlCursor cursor = rs[0].newCursor();
                XmlLineNumber xln = (XmlLineNumber) cursor.getBookmark(XmlLineNumber.class);
                if (xln != null) {
                    messageFailure.setLine(xln.getLine());
                    messageFailure.setColumn(xln.getColumn());
                }

            }
            break;
        default:
        }
    }

    /**
     * Set the line and column number where the message failure occurs.
     * 
     * @param messageFailure
     *        the messsage failure to update
     * @param message
     * @param path
     */
    private void setLineColumn(MessageFailureV2 messageFailure,
            Message message, String path) {
        MessageLocation messageLocation = new MessageLocation(path);
        switch (message.getEncoding()) {
        case V2_ER7:
            messageFailure.setLine(((Er7Message) message).getLine(messageLocation));
            messageFailure.setColumn(((Er7Message) message).getColumn(messageLocation));
            break;
        case V2_XML:
            String xpath = messageLocation.getXPath();
            XmlObject[] rs = ((XmlMessage) message).getDocument().selectPath(
                    xpath);
            if (rs.length == 1) {
                XmlCursor cursor = rs[0].newCursor();
                XmlLineNumber xln = (XmlLineNumber) cursor.getBookmark(XmlLineNumber.class);
                if (xln != null) {
                    messageFailure.setLine(xln.getLine());
                    messageFailure.setColumn(xln.getColumn());
                }

            }
            break;
        default:
        }
    }

    /**
     * Check if the location is a possible location from the profile.
     * 
     * @param location
     *        the location
     * @param profile
     *        the profile
     * @return the XmlObject in the profile; null otherwise
     */
    private XmlObject getElementInProfile(MessageElement location,
            Profile profile) {
        XmlObject elementInProfile = null;
        if (profile != null) {
            XmlObject xmlProfile = profile.getDocument();
            XmlObject start = null;
            XmlObject[] rs = xmlProfile.selectPath("/HL7v2xConformanceProfile/HL7v2xStaticDef");
            if (rs.length == 1) {
                start = rs[0];
            }
            // Segment Group
            boolean segmentGroupFound = false;
            SegmentGroup sg = location.getSegmentGroup();
            Segment s = null;
            if (sg != null) {
                do {
                    segmentGroupFound = false;
                    rs = start.selectPath("SegGroup[@Name='" + sg.getName()
                            + "']");
                    if (rs.length > 0) {
                        start = checkUsageCardinality(rs,
                                sg.getInstanceNumber());
                        segmentGroupFound = (start != null);
                        s = sg.getSegment();
                        sg = sg.getSegmentGroup();
                    } else {
                        sg = null;
                    }
                } while (sg != null);
            } else {
                segmentGroupFound = true;
                s = location.getSegment();
            }
            if (segmentGroupFound) {
                // Segment
                boolean segmentFound = false;
                rs = start.selectPath("Segment[@Name='" + s.getName() + "']");
                if (rs.length > 0) {
                    start = checkUsageCardinality(rs, s.getInstanceNumber());
                    segmentFound = (start != null);
                }
                if (segmentFound) {
                    Field f = s.getField();
                    if (f != null) {
                        boolean fieldFound = false;
                        rs = start.selectPath("Field[" + f.getPosition() + "]");
                        if (rs.length == 1) {
                            start = checkUsageCardinality(rs,
                                    f.getInstanceNumber());
                            fieldFound = (start != null);
                        }
                        if (fieldFound) {
                            Component c = f.getComponent();
                            if (c == null) {
                                elementInProfile = rs[0];
                            } else {
                                boolean componentFound = false;
                                rs = start.selectPath("Component["
                                        + c.getPosition() + "]");
                                if (rs.length == 1) {
                                    start = rs[0];
                                    componentFound = true;
                                }
                                if (componentFound) {
                                    SubComponent sc = c.getSubComponent();
                                    if (sc == null) {
                                        elementInProfile = rs[0];
                                    } else {
                                        rs = start.selectPath("SubComponent["
                                                + sc.getPosition() + "]");
                                        if (rs.length == 1) {
                                            elementInProfile = rs[0];
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return elementInProfile;
    }

    /**
     * Check the usage and cardinality in the profile to the location in the
     * context.
     * 
     * @param rs
     * @param instanceNumberContext
     * @return the location in the profile if everything went fine; null
     *         otherwise
     */
    private XmlObject checkUsageCardinality(XmlObject[] rs,
            int instanceNumberContext) {
        XmlObject xmlObj = null;
        boolean end = false;
        int i = 0;
        while (!end && i < rs.length) {
            XmlCursor cursor = rs[i].newCursor();
            // Check the usage
            String usage = cursor.getAttributeText(QName.valueOf("Usage"));
            if (!"X".equals(usage)) {
                // Check the cardinality
                String sInstanceNumberProfile = cursor.getAttributeText(QName.valueOf("Max"));
                if (!"*".equals(sInstanceNumberProfile)) {
                    int instanceNumberProfile = Integer.parseInt(sInstanceNumberProfile);
                    if (instanceNumberContext <= instanceNumberProfile) {
                        end = true;
                        xmlObj = rs[i];
                    }
                } else {
                    end = true;
                    xmlObj = rs[i];
                }
            }
            i++;
        }
        return xmlObj;
    }

    /**
     * Transform a MessageElement into an EPath expression
     * 
     * @param messageElement
     * @return an epath expression
     */
    private String getEPath(MessageElement messageElement) {
        String epath = null;
        MessageElement location = messageElement;
        StringBuffer sb = new StringBuffer("");
        // SegGroup
        gov.nist.healthcare.message.SegmentGroup sg = location.getSegmentGroup();
        gov.nist.healthcare.message.Segment s = null;
        while (sg != null) {
            if (sg != null) {
                s = sg.getSegment();
            }
            sg = sg.getSegmentGroup();
        }
        if (s == null) {
            s = location.getSegment();
        }
        if (s != null) {
            sb.append(s.getName()).append("[");
            if (s.getAnyInstanceNumber()) {
                sb.append("*");
            } else {
                int segmentInstanceNumber = s.getInstanceNumber() == 0 ? 1
                        : s.getInstanceNumber();
                sb.append(segmentInstanceNumber);
            }
            sb.append("]");
            gov.nist.healthcare.message.Field f = s.getField();
            if (f != null) {
                if (!f.getAnyInstanceNumber()) {
                    int fieldInstanceNumber = f.getInstanceNumber() == 0 ? 1
                            : f.getInstanceNumber();
                    sb.append(".").append(f.getPosition()).append("[").append(
                            fieldInstanceNumber).append("]");
                } else {
                    sb.append(".").append(f.getPosition()).append("[*]");
                }
                gov.nist.healthcare.message.Component c = f.getComponent();
                if (c != null) {
                    sb.append(".").append(c.getPosition());
                    gov.nist.healthcare.message.SubComponent sc = c.getSubComponent();
                    if (sc != null) {
                        sb.append(".").append(sc.getPosition());
                    }
                }
            }
        }
        epath = sb.toString();
        return epath;
    }

    /**
     * Transform a MessageElement into an XPath expression
     * 
     * @param messageElement
     * @return a XPath expression
     */
    private String getXPath(MessageElement messageElement) {
        String xpath = null;
        MessageElement location = messageElement;
        StringBuffer sb = new StringBuffer("/");
        // SegGroup
        gov.nist.healthcare.message.SegmentGroup sg = location.getSegmentGroup();
        gov.nist.healthcare.message.Segment s = null;
        while (sg != null) {
            int segmentGroupInstanceNumber = sg.getInstanceNumber() == 0 ? 1
                    : sg.getInstanceNumber();
            sb.append("/*[ends-with(name(), '.").append(sg.getName()).append(
                    "')][").append(segmentGroupInstanceNumber).append("]");
            if (sg != null) {
                s = sg.getSegment();
            }
            sg = sg.getSegmentGroup();
        }
        if (s == null) {
            s = location.getSegment();
        }
        if (s != null) {
            sb.append("/*:").append(s.getName()).append("[");
            if (s.getAnyInstanceNumber()) {
                sb.append("*");
            } else {
                int segmentInstanceNumber = s.getInstanceNumber() == 0 ? 1
                        : s.getInstanceNumber();
                sb.append(segmentInstanceNumber);
            }
            sb.append("]");
            gov.nist.healthcare.message.Field f = s.getField();
            if (f != null) {
                if (!f.getAnyInstanceNumber()) {
                    int fieldInstanceNumber = f.getInstanceNumber() == 0 ? 1
                            : f.getInstanceNumber();
                    sb.append("/*[ends-with(name(), '.").append(f.getPosition()).append(
                            "')][").append(fieldInstanceNumber).append("]");
                } else {
                    sb.append("/*[ends-with(name(), '.").append(f.getPosition()).append(
                            "')]");
                }
                gov.nist.healthcare.message.Component c = f.getComponent();
                if (c != null) {
                    sb.append("/*[ends-with(name(), '.").append(c.getPosition()).append(
                            "')]");
                    gov.nist.healthcare.message.SubComponent sc = c.getSubComponent();
                    if (sc != null) {
                        sb.append("/*[ends-with(name(), '.").append(
                                sc.getPosition()).append("')]");
                    }
                }
            }
        }
        xpath = sb.toString();
        return xpath;
    }

    // TODO: This is redundant code with structure validation
    /**
     * Check if the provided value is in a table.
     * 
     * @param value
     *        the value to be checked
     * @param tableId
     *        the table id
     * @param tableVersion
     * @param userComment
     * @return a message failure (can be null if the check goes well)
     */
    protected MessageFailureV2 checkTable(String value, String tableId,
            String tableVersion, String userComment) {
        StringBuffer sb = new StringBuffer();
        MessageFailureV2 mf = null;
        TableDefinition tableDef = tableManager.getTable(tableId, tableVersion);
        if (tableDef == null) {
            mf = new MessageFailureV2(message.getEncoding());
            sb.append("The value '").append(value);
            sb.append("' specified in the message can't be checked because the table '");
            sb.append(tableId).append("' was not found in the tables file.");
            mf.setDescription(sb.toString());
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            mf.setFailureType(AssertionTypeV2Constants.TABLE_NOT_FOUND);
            mf.setElementContent(value);
            mf.setUserComment(userComment);
        } else {
            boolean isValueInTable = tableManager.isValueInTable(value,
                    tableDef);
            if (!isValueInTable) {
                mf = new MessageFailureV2(message.getEncoding());
                sb.append("The value '").append(value);
                sb.append("' specified in the message does not match any of the values in the table '");
                sb.append(tableId).append("'");
                mf.setDescription(sb.toString());
                mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                mf.setFailureType(AssertionTypeV2Constants.DATA);
                mf.setElementContent(value);
                mf.setUserComment(userComment);
            }
        }
        return mf;
    }

}
