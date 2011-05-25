package gov.nist.healthcare.core.validation.message.plugin.datatype;

import gov.nist.healthcare.core.Constants.ElementType;
import gov.nist.healthcare.core.Constants.MessageEncoding;
import gov.nist.healthcare.core.message.HL7Message;
import gov.nist.healthcare.core.message.MessageLocation;
import gov.nist.healthcare.core.message.v2.HL7V2Message;
import gov.nist.healthcare.core.message.v2.er7.Er7Message;
import gov.nist.healthcare.core.validation.message.MessageFailure;
import gov.nist.healthcare.core.validation.message.plugin.PluginUtil;
import gov.nist.healthcare.core.validation.message.plugin.ValidationPlugin;
import gov.nist.healthcare.core.validation.message.util.MessageElementUtil;
import gov.nist.healthcare.core.validation.message.v2.MessageFailureV2;
import gov.nist.healthcare.data.TableLibraryDocument.TableLibrary.TableDefinition;
import gov.nist.healthcare.message.MessageElement;
import gov.nist.healthcare.validation.AssertionResultConstants;
import gov.nist.healthcare.validation.AssertionTypeV2Constants;
import gov.nist.healthcare.validation.ErrorSeverityConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

public class DatatypeCheckPlugin extends ValidationPlugin {

    @Override
    public List<MessageFailure> validate(HL7Message message, String params,
            AssertionResultConstants.Enum assertionResult, String userComment)
            throws JsonParseException, JsonMappingException, IOException {
        List<MessageFailure> messageFailures = null;
        if (message instanceof HL7V2Message) {
            messageFailures = validateMessage((HL7V2Message) message, params,
                    assertionResult, userComment);
        }
        return messageFailures;
    }

    private List<MessageFailure> validateMessage(HL7V2Message message,
            String params, AssertionResultConstants.Enum assertionResult,
            String userComment) throws JsonParseException,
            JsonMappingException, IOException {
        DatatypeCheckParam vcParams = mapper.readValue(params,
                DatatypeCheckParam.class);
        return validate(message, vcParams.getLocation(),
                vcParams.getDatatype(), assertionResult, userComment);
    }

    private List<MessageFailure> validate(HL7V2Message message,
            String location, String datatype,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        List<MessageFailure> messageFailures = null;
        if ("HD".equals(datatype)) {
            messageFailures = checkHD(message, location, assertionResult,
                    userComment);
        } else if ("CE".equals(datatype)) {
            messageFailures = checkCE(message, location, assertionResult,
                    userComment);
        } else if ("CWE".equals(datatype)) {
            messageFailures = checkCWE(message, location, assertionResult,
                    userComment);
        } else if ("CNE".equals(datatype)) {
            messageFailures = checkCNE(message, location, assertionResult,
                    userComment);
        }
        return messageFailures;
    }

    /**
     * Check a HD datatype. The valid combinations are HD.1, HD.2 + HD.3 and
     * HD.1 + HD.2 + HD.3
     * 
     * @param message
     * @param location
     * @param assertionResult
     * @param userComment
     * @return a list of MessageFailure; null otherwise
     */
    private List<MessageFailure> checkHD(HL7V2Message message, String location,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        List<MessageFailure> messageFailures = new ArrayList<MessageFailure>();
        List<String> failedLocations = new ArrayList<String>();
        List<String> invalidLocations = new ArrayList<String>();
        MessageElement messageLocation = MessageElementUtil.getMessageElement(location);
        if (messageLocation == null) {
            // Invalid Location
            invalidLocations.add(location);
        }
        List<MessageLocation> mls = MessageElementUtil.getMessageLocations(
                messageLocation, message);
        for (MessageLocation ml : mls) {
            String encodedLocation = message instanceof Er7Message ? ml.getEPath()
                    : ml.getXPath();
            if (ml.getElementType() == ElementType.FIELD
                    || ml.getElementType() == ElementType.COMPONENT) {
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
                        failedLocations.add(encodedLocation);
                    }
                }
            } else {
                invalidLocations.add(encodedLocation);
            }
        }
        if (failedLocations.size() > 0) {
            String description = String.format(
                    "The following location(s) %s are not valued correctly for a HD datatype. "
                            + "The possible combination are HD.1 only, or HD.2 and HD.3, or all HD.1, HD.2 and HD.3",
                    PluginUtil.valuesToString(failedLocations));
            MessageFailure messageFailure = getNotValidDatatypeMessageFailure(
                    message, description, location, assertionResult,
                    userComment);
            // messageEncoding, description, location, assertionResult,
            // userComment);
            // MessageFailure messageFailure = PluginUtil.getMessageFailure(
            // messageEncoding, description, location, assertionResult,
            // userComment);
            // messageFailure.setFailureType(AssertionTypeV2Constants.DATATYPE);
            messageFailures.add(messageFailure);
        }
        if (invalidLocations.size() > 0) {
            String description = String.format(
                    "The following location(s) %s are not a Field or a Component.",
                    PluginUtil.valuesToString(invalidLocations));
            MessageFailure messageFailure = getWrongTypeElementMessageFailure(
                    message, description, location, assertionResult,
                    userComment);
            // MessageFailure messageFailure = PluginUtil.getMessageFailure(
            // messageEncoding, description, location, assertionResult,
            // userComment);
            // messageFailure.setFailureType(AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
            messageFailures.add(messageFailure);
        }
        return messageFailures;
    }

    /**
     * Check a CE datatype. The value in CE.1/CE.4 must come from the table
     * provided in CE.3/CE.6
     * 
     * @param message
     * @param location
     * @param assertionResult
     * @param userComment
     * @return a list of MessageFailure; null otherwise
     */
    private List<MessageFailure> checkCE(HL7V2Message message, String location,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        List<MessageFailure> messageFailures = new ArrayList<MessageFailure>();
        List<String> failedLocations123 = new ArrayList<String>();
        List<String> failedLocations456 = new ArrayList<String>();
        List<String> invalidLocations = new ArrayList<String>();
        MessageElement messageLocation = MessageElementUtil.getMessageElement(location);
        if (messageLocation == null) {
            // Invalid Location
            invalidLocations.add(location);
        }
        List<MessageLocation> mls = MessageElementUtil.getMessageLocations(
                messageLocation, message);
        for (MessageLocation ml : mls) {
            String encodedLocation = message instanceof Er7Message ? ml.getEPath()
                    : ml.getXPath();
            if (ml.getElementType() == ElementType.FIELD
                    || ml.getElementType() == ElementType.COMPONENT) {
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
                        String failedLocation = message instanceof Er7Message ? ml.getEPath()
                                : ml.getXPath();
                        failedLocations123.add(failedLocation);
                        // MessageFailureV2 mf = checkCombination(ce123Valid,
                        // ml,
                        // assertionResult, userComment);
                        // if (mf != null) {
                        // mf.setDescription("The provided location is not valued correctly for a CE datatype. CE.1 and CE.3 should be populated.");
                        // }
                    } else {
                        if (ceValues.get(2) != null) {
                            MessageFailureV2 mf = checkTable(message,
                                    ceValues.get(0), stripHL7(ceValues.get(2)),
                                    null, userComment);
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
                        String failedLocation = message instanceof Er7Message ? ml.getEPath()
                                : ml.getXPath();
                        failedLocations456.add(failedLocation);
                        // MessageFailureV2 mf = checkCombination(ce456Valid,
                        // ml,
                        // assertionResult, userComment);
                        // if (mf != null) {
                        // mf.setDescription("The provided location is not valued correctly for a CE datatype. CE.4 and CE.6 should be populated.");
                        // }
                    } else {
                        if (ceValues.get(5) != null) {
                            MessageFailureV2 mf = checkTable(message,
                                    ceValues.get(3), stripHL7(ceValues.get(5)),
                                    null, userComment);
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
            } else {
                invalidLocations.add(encodedLocation);
            }
        }
        if (failedLocations123.size() > 0) {
            String description = String.format(
                    "The following location(s) %s are not valued correctly for a CE datatype. "
                            + "CE.1 and CE.3 should be populated.",
                    PluginUtil.valuesToString(failedLocations123));
            MessageFailure messageFailure = getNotValidDatatypeMessageFailure(
                    message, description, location, assertionResult,
                    userComment);
            // MessageFailure messageFailure = PluginUtil.getMessageFailure(
            // messageEncoding, description, location, assertionResult,
            // userComment);
            // messageFailure.setFailureType(AssertionTypeV2Constants.DATATYPE);
            messageFailures.add(messageFailure);
        }
        if (failedLocations456.size() > 0) {
            String description = String.format(
                    "The following location(s) %s are not valued correctly for a CE datatype. "
                            + "CE.4 and CE.6 should be populated.",
                    PluginUtil.valuesToString(failedLocations456));
            MessageFailure messageFailure = getNotValidDatatypeMessageFailure(
                    message, description, location, assertionResult,
                    userComment);
            // MessageFailure messageFailure = PluginUtil.getMessageFailure(
            // messageEncoding, description, location, assertionResult,
            // userComment);
            // messageFailure.setFailureType(AssertionTypeV2Constants.DATATYPE);
            messageFailures.add(messageFailure);
        }
        if (invalidLocations.size() > 0) {
            String description = String.format(
                    "The following location(s) %s are not a Field or a Component.",
                    PluginUtil.valuesToString(invalidLocations));
            MessageFailure messageFailure = getWrongTypeElementMessageFailure(
                    message, description, location, assertionResult,
                    userComment);
            // MessageFailure messageFailure = PluginUtil.getMessageFailure(
            // messageEncoding, description, location, assertionResult,
            // userComment);
            // messageFailure.setFailureType(AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
            messageFailures.add(messageFailure);
        }
        return messageFailures;
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
     * @return a list of MessageFailure; null otherwise
     */
    private List<MessageFailure> checkCWE(HL7V2Message message,
            String location, AssertionResultConstants.Enum assertionResult,
            String userComment) {
        List<MessageFailure> messageFailures = new ArrayList<MessageFailure>();
        List<String> failedLocations123 = new ArrayList<String>();
        List<String> failedLocations456 = new ArrayList<String>();
        List<String> invalidLocations = new ArrayList<String>();
        MessageElement messageLocation = MessageElementUtil.getMessageElement(location);
        if (messageLocation == null) {
            // Invalid Location
            invalidLocations.add(location);
        }
        List<MessageLocation> mls = MessageElementUtil.getMessageLocations(
                messageLocation, message);
        for (MessageLocation ml : mls) {
            String encodedLocation = message instanceof Er7Message ? ml.getEPath()
                    : ml.getXPath();
            if (ml.getElementType() == ElementType.FIELD
                    || ml.getElementType() == ElementType.COMPONENT) {
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
                        String failedLocation = message instanceof Er7Message ? ml.getEPath()
                                : ml.getXPath();
                        failedLocations123.add(failedLocation);
                        // MessageFailureV2 mf = checkCombination(message,
                        // cwe123Valid, ml,
                        // assertionResult, userComment);
                        // if (mf != null) {
                        // mf.setDescription("The provided location is not valued correctly for a CWE datatype. CWE.1 and CWE.3, or only CWE.2 should be populated.");
                        // }
                    } else {
                        if (cweValues.get(2) != null) {
                            MessageFailureV2 mf = checkTable(message,
                                    cweValues.get(0),
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
                        String failedLocation = message instanceof Er7Message ? ml.getEPath()
                                : ml.getXPath();
                        failedLocations456.add(failedLocation);
                        // MessageFailureV2 mf = checkCombination(cwe456Valid,
                        // ml,
                        // assertionResult, userComment);
                        // if (mf != null) {
                        // mf.setDescription("The provided location is not valued correctly for a CWE datatype. CWE.4 and CWE.6, or only CWE.5 should be populated.");
                        // }
                    } else {
                        if (cweValues.get(5) != null) {
                            MessageFailureV2 mf = checkTable(message,
                                    cweValues.get(3),
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
            } else {
                invalidLocations.add(encodedLocation);
            }
        }
        if (failedLocations123.size() > 0) {
            String description = String.format(
                    "The following location(s) %s are not valued correctly for a CWE datatype. "
                            + "CWE.1 and CWE.3, or only CWE.2 should be populated.",
                    PluginUtil.valuesToString(failedLocations123));
            MessageFailure messageFailure = getNotValidDatatypeMessageFailure(
                    message, description, location, assertionResult,
                    userComment);
            // MessageFailure messageFailure = PluginUtil.getMessageFailure(
            // messageEncoding, description, location, assertionResult,
            // userComment);
            // messageFailure.setFailureType(AssertionTypeV2Constants.DATATYPE);
            messageFailures.add(messageFailure);
        }
        if (failedLocations456.size() > 0) {
            String description = String.format(
                    "The following location(s) %s are not valued correctly for a CWE datatype. "
                            + "CWE.4 and CWE.6, or only CWE.5 should be populated.",
                    PluginUtil.valuesToString(failedLocations456));
            MessageFailure messageFailure = getNotValidDatatypeMessageFailure(
                    message, description, location, assertionResult,
                    userComment);
            // MessageFailure messageFailure = PluginUtil.getMessageFailure(
            // messageEncoding, description, location, assertionResult,
            // userComment);
            // messageFailure.setFailureType(AssertionTypeV2Constants.DATATYPE);
            messageFailures.add(messageFailure);
        }
        if (invalidLocations.size() > 0) {
            String description = String.format(
                    "The following location(s) %s are not a Field or a Component.",
                    PluginUtil.valuesToString(invalidLocations));
            // MessageFailure messageFailure =
            // getNotValidDatatypeMessageFailure(
            // message, description, location, assertionResult,
            // userComment);
            MessageFailure messageFailure = getWrongTypeElementMessageFailure(
                    message, description, location, assertionResult,
                    userComment);
            // MessageFailure messageFailure = PluginUtil.getMessageFailure(
            // messageEncoding, description, location, assertionResult,
            // userComment);
            // messageFailure.setFailureType(AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
            messageFailures.add(messageFailure);
        }
        return messageFailures;
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
     * @return a list of MessageFailure; null otherwise
     */
    private List<MessageFailure> checkCNE(HL7V2Message message,
            String location, AssertionResultConstants.Enum assertionResult,
            String userComment) {
        List<MessageFailure> messageFailures = new ArrayList<MessageFailure>();
        List<String> failedLocations123 = new ArrayList<String>();
        List<String> failedLocations456 = new ArrayList<String>();
        List<String> invalidLocations = new ArrayList<String>();
        MessageElement messageLocation = MessageElementUtil.getMessageElement(location);
        if (messageLocation == null) {
            // Invalid Location
            invalidLocations.add(location);
        }
        List<MessageLocation> mls = MessageElementUtil.getMessageLocations(
                messageLocation, message);
        for (MessageLocation ml : mls) {
            String encodedLocation = message instanceof Er7Message ? ml.getEPath()
                    : ml.getXPath();
            if (ml.getElementType() == ElementType.FIELD
                    || ml.getElementType() == ElementType.COMPONENT) {
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
                        String failedLocation = message instanceof Er7Message ? ml.getEPath()
                                : ml.getXPath();
                        failedLocations123.add(failedLocation);
                        // MessageFailureV2 mf = checkCombination(cne123Valid,
                        // ml,
                        // assertionResult, userComment);
                        // if (mf != null) {
                        // mf.setDescription("The provided location is not valued correctly for a CNE datatype. CNE.1 and CNE.3 should be populated.");
                        // }
                    } else {
                        if (cneValues.get(2) != null) {
                            MessageFailureV2 mf = checkTable(message,
                                    cneValues.get(0),
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
                            String failedLocation = message instanceof Er7Message ? ml.getEPath()
                                    : ml.getXPath();
                            failedLocations456.add(failedLocation);
                            // MessageFailureV2 mf =
                            // checkCombination(cne456Valid,
                            // ml, assertionResult, userComment);
                            // if (mf != null) {
                            // mf.setDescription("The provided location is not valued correctly for a CNE datatype. CNE.1, CNE.3, CNE.4 and CNE.6 should be populated.");
                            // }
                        } else {
                            if (cneValues.get(5) != null) {
                                MessageFailureV2 mf = checkTable(message,
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
            } else {
                invalidLocations.add(encodedLocation);
            }
        }
        if (failedLocations123.size() > 0) {
            String description = String.format(
                    "The following location(s) %s are not valued correctly for a CNE datatype. "
                            + "CNE.1 and CNE.3 should be populated.",
                    PluginUtil.valuesToString(failedLocations123));
            MessageFailure messageFailure = getNotValidDatatypeMessageFailure(
                    message, description, location, assertionResult,
                    userComment);
            // MessageFailure messageFailure = PluginUtil.getMessageFailure(
            // messageEncoding, description, location, assertionResult,
            // userComment);
            // messageFailure.setFailureType(AssertionTypeV2Constants.DATATYPE);
            messageFailures.add(messageFailure);
        }
        if (failedLocations456.size() > 0) {
            String description = String.format(
                    "The following location(s) %s are not valued correctly for a CNE datatype. "
                            + "CNE.1 and CNE.3 should be populated.",
                    PluginUtil.valuesToString(failedLocations456));
            MessageFailure messageFailure = getNotValidDatatypeMessageFailure(
                    message, description, location, assertionResult,
                    userComment);
            // MessageFailure messageFailure = PluginUtil.getMessageFailure(
            // messageEncoding, description, location, assertionResult,
            // userComment);
            // messageFailure.setFailureType(AssertionTypeV2Constants.DATATYPE);
            messageFailures.add(messageFailure);
        }
        if (invalidLocations.size() > 0) {
            String description = String.format(
                    "The following location(s) %s are not a Field or a Component.",
                    PluginUtil.valuesToString(invalidLocations));
            // MessageFailure messageFailure =
            // getNotValidDatatypeMessageFailure(
            // message, description, location, assertionResult,
            // userComment);
            MessageFailure messageFailure = getWrongTypeElementMessageFailure(
                    message, description, location, assertionResult,
                    userComment);
            // MessageFailure messageFailure = PluginUtil.getMessageFailure(
            // messageEncoding, description, location, assertionResult,
            // userComment);
            // messageFailure.setFailureType(AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
            messageFailures.add(messageFailure);
        }
        return messageFailures;
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
    // private MessageFailureV2 checkCombination(boolean validCombination,
    // MessageLocation location,
    // AssertionResultConstants.Enum assertionResult, String userComment) {
    // MessageFailureV2 mf = null;
    // if (!validCombination) {
    // if (message instanceof Er7Message) {
    // mf = new MessageFailureV2(MessageEncoding.V2_ER7);
    // // mf.setPath(getEPath(location));
    // mf.setPath(location.getEPath());
    // mf.setLine(((Er7Message) message).getLine(location));
    // mf.setColumn(((Er7Message) message).getColumn(location));
    // } else if (message instanceof XmlMessage) {
    // mf = new MessageFailureV2(MessageEncoding.V2_XML);
    // // mf.setPath(getXPath(location));
    // mf.setPath(location.getXPath());
    // }
    // mf.setFailureType(AssertionTypeV2Constants.DATATYPE);
    // mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
    // mf.setAssertionResult(assertionResult);
    // mf.setUserComment(userComment);
    // messageFailures.add(mf);
    // }
    // return mf;
    // }

    // TODO: This is redundant code with structure validation
    /**
     * Check if the provided value is in a table.
     * 
     * @param message
     * @param value
     *        the value to be checked
     * @param tableId
     *        the table id
     * @param tableVersion
     * @param userComment
     * @return a message failure (can be null if the check goes well)
     */
    protected MessageFailureV2 checkTable(HL7Message message, String value,
            String tableId, String tableVersion, String userComment) {
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
     * Create a MessageFailure when the element is not valid against the
     * datatype constraints.
     * 
     * @param message
     * @param description
     * @param location
     * @param assertionResult
     * @param userComment
     * @return a MessageFailure object
     */
    private MessageFailure getNotValidDatatypeMessageFailure(
            HL7Message message, String description, String location,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        MessageEncoding messageEncoding = message.getEncoding();
        MessageFailure messageFailure = PluginUtil.getMessageFailure(messageEncoding);
        messageFailure.setDescription(description);
        messageFailure.setPath(location);
        messageFailure.setFailureType(AssertionTypeV2Constants.DATATYPE);
        messageFailure.setAssertionResult(assertionResult);
        messageFailure.setUserComment(userComment);
        return messageFailure;
    }

    /**
     * Create a MessageFailure when the element is not a valid type the checked
     * datatype.
     * 
     * @param message
     * @param description
     * @param location
     * @param assertionResult
     * @param userComment
     * @return a MessageFailure object
     */
    private MessageFailure getWrongTypeElementMessageFailure(
            HL7Message message, String description, String location,
            AssertionResultConstants.Enum assertionResult, String userComment) {
        MessageEncoding messageEncoding = message.getEncoding();
        MessageFailure messageFailure = PluginUtil.getMessageFailure(messageEncoding);
        messageFailure.setDescription(description);
        messageFailure.setPath(location);
        messageFailure.setFailureType(AssertionTypeV2Constants.MESSAGE_VALIDATION_CONTEXT);
        messageFailure.setAssertionResult(assertionResult);
        messageFailure.setUserComment(userComment);
        return messageFailure;
    }

}
