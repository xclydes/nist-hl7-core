/*
 * NIST Healthcare Core
 * AbstractMessageValidation.java Dec 21, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.v2;

import gov.nist.healthcare.core.Constants;
import gov.nist.healthcare.core.message.v2.HL7V2Message;
import gov.nist.healthcare.core.message.v2.er7.Er7Message;
import gov.nist.healthcare.core.message.v2.xml.XmlMessage;
import gov.nist.healthcare.core.profile.Profile;
import gov.nist.healthcare.core.validation.message.MessageValidationException;
import gov.nist.healthcare.core.validation.message.content.v2.MessageContentValidationV2;
import gov.nist.healthcare.core.validation.message.structure.v2.MessageStructureValidationV2;
import gov.nist.healthcare.core.validation.message.structure.v2.er7.MessageStructureValidationV2Er7;
import gov.nist.healthcare.core.validation.message.structure.v2.xml.MessageStructureValidationV2Xml;
import gov.nist.healthcare.data.TableLibraryDocument;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an abstract class for V2 message validation
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageValidationV2 {

    // protected Profile profile;
    // protected HL7V2Message message;
    // protected HL7V2Message inputMessage;
    // protected List<TableLibraryDocument> tables;
    // protected MessageValidationContextV2 context;
    protected List<MessageFailureV2> messageFailures;
    protected MessageStructureValidationV2Er7 structureValidatorEr7;
    protected MessageStructureValidationV2Xml structureValidatorXml;
    protected MessageContentValidationV2 contentValidator;

    /**
     * Constructor
     */
    public MessageValidationV2() {
        messageFailures = new ArrayList<MessageFailureV2>();
        structureValidatorEr7 = new MessageStructureValidationV2Er7();
        structureValidatorXml = new MessageStructureValidationV2Xml();
        contentValidator = new MessageContentValidationV2();
    }

    /**
     * Validate the message structure and content. The validation of table
     * values is done with the default table file corresponding to the version
     * of the profile.
     * 
     * @param message
     *        the message to validate
     * @param profile
     *        the profile
     * @param context
     *        the message validation context
     * @return the message validation result
     * @throws MessageValidationException
     */
    public MessageValidationResultV2 validate(HL7V2Message message,
            Profile profile, MessageValidationContextV2 context)
            throws MessageValidationException {
        List<TableLibraryDocument> tableLibraryDocuments = new ArrayList<TableLibraryDocument>();
        try {
            // Use default TableLibraryDocument
            TableLibraryDocument tableLibraryDocument = TableLibraryDocument.Factory.parse(MessageValidationV2.class.getResourceAsStream(Constants.getHl7Tables(profile.getHl7VersionAsString())));
            tableLibraryDocuments.add(tableLibraryDocument);
        } catch (Exception e) {
            throw new MessageValidationException(e.getMessage());
        }
        return validate(message, profile, context, tableLibraryDocuments);
    }

    /**
     * Validate the message structure and content. The validation of table
     * values is done with the list of TableLibraryDocument provided as a
     * parameter. The default table file is not included if not provided in the
     * list.
     * 
     * @param message
     *        the message to validate
     * @param profile
     *        the profile
     * @param context
     *        the message validation context
     * @param tableLibraryDocuments
     *        a list of TableLibraryDocument
     * @return the message validation result
     * @throws MessageValidationException
     */
    public MessageValidationResultV2 validate(HL7V2Message message,
            Profile profile, MessageValidationContextV2 context,
            List<TableLibraryDocument> tableLibraryDocuments)
            throws MessageValidationException {
        messageFailures = new ArrayList<MessageFailureV2>();
        // Structure Validation
        MessageStructureValidationV2 structureValidator = null;
        if (message instanceof Er7Message) {
            structureValidator = structureValidatorEr7;
        } else if (message instanceof XmlMessage) {
            structureValidator = structureValidatorXml;
        }
        List<MessageFailureV2> structureFailures = structureValidator.validate(
                message, profile, context, tableLibraryDocuments);
        messageFailures.addAll(structureFailures);
        // Content Validation
        List<MessageFailureV2> contentFailures = contentValidator.validate(
                message, context, profile, tableLibraryDocuments);
        messageFailures.addAll(contentFailures);
        // Create the results
        MessageValidationResultV2 result = new MessageValidationResultV2(
                message, profile, context, messageFailures);
        return result;
    }

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
     * @return the message validation result
     */
    private MessageValidationResultV2 validate(HL7V2Message message,
            MessageValidationContextV2 context, Profile profile,
            List<TableLibraryDocument> tableLibraryDocuments) {
        messageFailures = new ArrayList<MessageFailureV2>();
        // Content Validation
        List<MessageFailureV2> contentFailures = contentValidator.validate(
                message, context, profile, tableLibraryDocuments);
        messageFailures.addAll(contentFailures);
        // Create the results
        MessageValidationResultV2 result = new MessageValidationResultV2(
                message, context, messageFailures);
        return result;
    }

    /**
     * Validate the message content.
     * 
     * @param message
     *        the message to validate
     * @param context
     *        the message validation context
     * @return the message validation result
     */
    public MessageValidationResultV2 validate(HL7V2Message message,
            MessageValidationContextV2 context) {
        return this.validate(message, context, null, null);
    }

    /**
     * Validate the message content.
     * 
     * @param message
     *        the message to validate
     * @param context
     *        the message validation context
     * @param tableLibraryDocuments
     *        a list of TableLibraryDocument
     * @return the message validation result
     */
    public MessageValidationResultV2 validate(HL7V2Message message,
            MessageValidationContextV2 context,
            List<TableLibraryDocument> tableLibraryDocuments) {
        return this.validate(message, context, null, tableLibraryDocuments);
    }

    /**
     * Validate a message The TableLibraryDocument for the profile version is
     * used for checking the table values.
     * 
     * @param aMessage
     * @param aProfile
     * @param aMessageValidationContext
     * @return a MessageValidationResult
     * @throws ValidationException
     */
    // public synchronized MessageValidationResult validate(Profile aProfile,
    // HL7V2Message aMessage,
    // MessageValidationContextV2 aMessageValidationContext)
    // throws ValidationException {
    // try {
    // // Use default TableLibraryDocument
    // TableLibraryDocument aTableLibraryDocument =
    // TableLibraryDocument.Factory.parse(AbstractMessageValidation.class.getResourceAsStream(Constants.getHl7Tables(aProfile.getHl7VersionAsString())));
    // ArrayList<TableLibraryDocument> alTDP = new
    // ArrayList<TableLibraryDocument>();
    // alTDP.add(aTableLibraryDocument);
    // return validate(aProfile, aMessage, alTDP,
    // aMessageValidationContext);
    // } catch (Exception e) {
    // e.printStackTrace();
    // throw new ValidationException(e.getMessage());
    // }
    // }
    /**
     * Validate a message. The ArrayList of TableLibraryDocument must contain
     * all TableLibraryDocument one wants to use for validation.
     * 
     * @param aMessage
     * @param aProfile
     * @param alTLD
     *        an array of aTableLibraryDocument
     * @param aMessageValidationContext
     * @return a MessageValidationResult
     * @throws ValidationException
     * @throws MalformedMessageException
     */
    // public synchronized MessageValidationResult validate(Profile aProfile,
    // HL7V2Message aMessage, ArrayList<TableLibraryDocument> alTLD,
    // MessageValidationContextV2 aMessageValidationContext)
    // throws ValidationException, MalformedMessageException {
    // profile = aProfile;
    // message = aMessage;
    // inputMessage = aMessage;
    // context = aMessageValidationContext;
    // alMF = new ArrayList<MessageFailure>();
    // tables = alTLD;
    //
    // if (HL7V2Message.isER7(message.getMessageAsString())) {
    // message = ((Er7Message) message).cleanEr7();
    // }
    //
    // // 1- Basic Check
    // checkBasic();
    // boolean keepgoing = keepGoingMessageStructureIdError();
    // int mfCount = alMF.size();
    // if (alMF.size() == 0 || keepgoing) {
    // // 2- Check the Message Structure only SegmentGroup and Segment
    // checkMessageStructure();
    // if (alMF.size() - mfCount == 0 && keepgoing) {
    // // 3- Map the SegmentGroup and Segment in the Profile and in the
    // // Message by using a finite state machine
    // // 4- Validate the Segment, Field, Component and SubComponent
    // // elements
    // mapAndCheckElements();
    // // 5- Validate data
    // // checkFixedData();
    // // checkIfThenElse();
    // }
    // }
    // MessageValidationResult mvr = new MessageValidationResult(aMessage,
    // aProfile, alMF);
    // mvr.setMessageValidationContext(aMessageValidationContext);
    // return mvr;
    // }
    //
    // /**
    // * Validate the message content using only a MessageValidationContext
    // *
    // * @param aMessage
    // * @param aMessageValidationContext
    // * @return a MessageValidationResult
    // * @throws ValidationException
    // */
    // public synchronized MessageValidationResult validate(HL7V2Message
    // aMessage,
    // MessageValidationContextV2 aMessageValidationContext)
    // throws ValidationException {
    // message = aMessage;
    // inputMessage = aMessage;
    // context = aMessageValidationContext;
    // alMF = new ArrayList<MessageFailure>();
    // checkFixedData();
    // MessageValidationResult mvr = new MessageValidationResult(aMessage,
    // null, alMF);
    // mvr.setMessageValidationContext(aMessageValidationContext);
    // return mvr;
    // }
    //
    // /**
    // * Check the length of a value
    // *
    // * @param value
    // * @param maxLength
    // * @return aMessageFailure (can be null if the check goes well)
    // */
    // protected MessageFailure checkLength(String value, int maxLength) {
    // StringBuffer sb = new StringBuffer();
    // MessageFailure mf = null;
    // if (value.length() > maxLength) {
    // mf = new MessageFailure();
    // sb.append("The value '").append(value);
    // sb.append(
    // "' specified in the message exceeds the maximum element length '").append(
    // maxLength).append("' specified in profile.");
    // mf.setDescription(sb.toString());
    // mf.setFailureSeverity(FailureSeverity.NORMAL);
    // mf.setFailureType(MessageFailureConstants.LENGTH);
    // mf.setElementContent(value);
    // }
    // sb = null;
    // return mf;
    // }
    //
    // /**
    // * Check if the provided value is in a table
    // *
    // * @param value
    // * @param table
    // * @return aMessageFailure (can be null if the check goes well)
    // */
    // protected MessageFailure checkTable(String value, String table) {
    // StringBuffer sb = new StringBuffer();
    // MessageFailure mf = null;
    // Iterator<TableElement> it = null;
    // boolean tablefound = false;
    // for (int i = 0; i < tables.size() && !tablefound; i++) {
    // try {
    // it = getTableElements(tables.get(i), table);
    // tablefound = true;
    // } catch (NoTableFoundException ntfe1) {
    // ;
    // }
    // }
    // if (!tablefound) {
    // mf = new MessageFailure();
    // sb.append("The value '").append(value);
    // sb.append("' specified in the message can't be checked because the table '");
    // sb.append(table).append(
    // "' specified in the profile was not found in the tables file.");
    // mf.setDescription(sb.toString());
    // mf.setFailureSeverity(FailureSeverity.NORMAL);
    // // TODO: TABLE?
    // mf.setFailureType(MessageFailureConstants.TABLE_NOT_FOUND);
    // mf.setElementContent(value);
    // }
    // if (it != null) {
    // boolean found = false;
    // while (!found && it.hasNext()) {
    // if (it.next().getCode().equals(value)) {
    // found = true;
    // }
    // }
    // if (!found) {
    // mf = new MessageFailure();
    // sb.append("The value '").append(value);
    // sb.append("' specified in the message does not match any of the values in the table '");
    // sb.append(table).append("' specified in the profile.");
    // mf.setDescription(sb.toString());
    // mf.setFailureSeverity(FailureSeverity.NORMAL);
    // // TODO: TABLE?
    // mf.setFailureType(MessageFailureConstants.DATA);
    // mf.setElementContent(value);
    // }
    // }
    // sb = null;
    // return mf;
    // }
    //
    // /**
    // * Get all table elements for a table
    // *
    // * @param aTableLibraryDocument
    // * @param tableId
    // * @return an Iterator of Element
    // * @throws NoTableFoundException
    // */
    // private Iterator<TableElement> getTableElements(
    // TableLibraryDocument aTableLibraryDocument, String tableId)
    // throws NoTableFoundException {
    // Iterator<TableDefinition> it =
    // aTableLibraryDocument.getTableLibrary().getTableDefinitionList().iterator();
    // while (it.hasNext()) {
    // TableDefinition td = it.next();
    // if (td.getId().equals(tableId)) {
    // return td.getTableElementList().iterator();
    // }
    // }
    // throw new NoTableFoundException("Table not found");
    // }
    //
    // /**
    // * Check if the provided value is the constant provided in the profile
    // *
    // * @param value
    // * @param constant
    // * @return aMessageFailure (can be null if the check goes well)
    // */
    // protected MessageFailure checkConstant(String value, String constant) {
    // StringBuffer sb = new StringBuffer();
    // MessageFailure mf = null;
    // if (!constant.equals(value)) {
    // mf = new MessageFailure();
    // sb.append("The value '").append(value);
    // sb.append("' does not match the fixed value '");
    // sb.append(constant);
    // sb.append("' specified in the message instance.");
    // mf.setDescription(sb.toString());
    // mf.setFailureSeverity(FailureSeverity.NORMAL);
    // mf.setFailureType(MessageFailureConstants.DATA);
    // mf.setElementContent(value);
    // }
    // sb = null;
    // return mf;
    // }
    //
    // /**
    // * Check if the provided value is valid against the format imposed by its
    // * datatype
    // *
    // * @param value
    // * @param datatype
    // * @return aMessageFailure (can be null if the check goes well)
    // */
    // protected MessageFailure checkDatatype(String value, String datatype) {
    // StringBuffer sb = new StringBuffer();
    // MessageFailure mf = null;
    // String regex = null;
    // if (datatype.equals("DTM")) {
    // regex =
    // "([12]\\d\\d\\d((0[1-9]|1[0-2])(([0-2]\\d|3[01])(([01]\\d|2[0-4])([0-5]\\d([0-5]\\d(.\\d\\d?\\d?\\d?)?)?)?)?)?)?((\\+|\\-)([01]\\d|2[0-4])[0-5]\\d)?)?";
    // } else if (datatype.equals("TM")) {
    // regex =
    // "((((([01]\\d|2[0-4])([0-5]\\d([0-5]\\d(.\\d\\d?\\d?\\d?)?)?)?)?)?)?((\\+|\\-)([01]\\d|2[0-4])[0-5]\\d)?)?";
    // } else if (datatype.equals("DT")) {
    // regex = "([12]\\d\\d\\d((0[1-9]|1[0-2])([0-2]\\d|3[01])?)?)";
    // } else if (datatype.equals("SI")) {
    // regex = "[0-9][0-9]{0,3}";
    // } else if (datatype.equals("NM")) {
    // regex = "(\\+|\\-)?\\d+(\\.?\\d+)?";
    // } else if (datatype.equals("TN")) {
    // regex =
    // "(\\d{1,2} )?(\\(\\d{3}\\))?\\d{3}-\\d{4}(X\\d{1,5})?(B\\d{1,5})?(C.*)?";
    // }
    // if (regex != null && !Pattern.matches(regex, value)) {
    // mf = new MessageFailure();
    // sb.append("The value '").append(value).append(
    // "' is not valid with respect to the format specified for datatype '");
    // sb.append(datatype).append("'");
    // mf.setDescription(sb.toString());
    // mf.setFailureSeverity(FailureSeverity.NORMAL);
    // mf.setFailureType(MessageFailureConstants.DATATYPE);
    // mf.setElementContent(value);
    // }
    // sb = null;
    // return mf;
    // }
    //
    // /**
    // * Keep going even if there is MSH.9.3 error
    // *
    // * @return a boolean
    // */
    // private boolean keepGoingMessageStructureIdError() {
    // boolean keepgoing = true;
    // for (MessageFailure mf : alMF) {
    // if (mf.getFailureSeverity() == FailureSeverity.FATAL) {
    // keepgoing = false;
    // }
    // }
    // return keepgoing;
    // }
    //
    // protected abstract void checkBasic() throws ValidationException;
    //
    // protected abstract void checkMessageStructure() throws
    // ValidationException;
    //
    // protected abstract void mapAndCheckElements() throws ValidationException;
    //
    // protected abstract void checkFixedData() throws ValidationException;
    //
    // protected abstract void checkIfThenElse();
}
