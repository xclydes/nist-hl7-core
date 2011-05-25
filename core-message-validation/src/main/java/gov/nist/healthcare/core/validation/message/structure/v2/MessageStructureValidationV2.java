/*
 * NIST Healthcare Core
 * MessageStructureValidationV2.java Aug 18, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.structure.v2;

import gov.nist.healthcare.core.MalformedMessageException;
import gov.nist.healthcare.core.message.v2.HL7V2Message;
import gov.nist.healthcare.core.message.v2.er7.Er7Message;
import gov.nist.healthcare.core.profile.Profile;
import gov.nist.healthcare.core.util.MessageDetectionUtils;
import gov.nist.healthcare.core.validation.message.MessageValidationException;
import gov.nist.healthcare.core.validation.message.v2.MessageFailureV2;
import gov.nist.healthcare.core.validation.message.v2.MessageValidationContextV2;
import gov.nist.healthcare.data.TableLibraryDocument;
import gov.nist.healthcare.data.TableLibraryDocument.TableLibrary.TableDefinition;
import gov.nist.healthcare.data.TableLibraryDocument.TableLibrary.TableDefinition.TableElement;
import gov.nist.healthcare.data.TableType;
import gov.nist.healthcare.validation.AssertionTypeV2Constants;
import gov.nist.healthcare.validation.ErrorSeverityConstants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

/**
 * This class validates the message structure for a V2 message.
 * 
 * @author Sydney Henrard (NIST)
 */
public abstract class MessageStructureValidationV2 {

    protected HL7V2Message message;
    protected HL7V2Message inputMessage;
    protected Profile profile;
    protected MessageValidationContextV2 context;
    protected List<TableLibraryDocument> tables;
    protected List<MessageFailureV2> messageFailures;

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
     * @return a list of message failures
     * @throws MessageValidationException
     */
    public List<MessageFailureV2> validate(HL7V2Message message,
            Profile profile, MessageValidationContextV2 context,
            List<TableLibraryDocument> tableLibraryDocuments)
            throws MessageValidationException {
        messageFailures = new ArrayList<MessageFailureV2>();
        // Set the variables needed by the subclass validation
        this.message = message;
        this.inputMessage = message;
        this.profile = profile;
        this.context = context;
        tables = tableLibraryDocuments;
        // Clean Up the Er7 for extra separators
        if (MessageDetectionUtils.isER7(message.getMessageAsString())) {
            try {
                message = ((Er7Message) message).cleanEr7();
            } catch (MalformedMessageException e) {
                throw new MessageValidationException(e.getMessage());
            }
        }
        // Pre-Process the profile for the tables
        preprocessProfileTable();
        // 1- Basic Check
        checkBasic();
        boolean keepgoing = keepGoingMessageStructureIdError(messageFailures);
        if (keepgoing) {
            // 2- Check the Message Structure only SegmentGroup and Segment
            checkMessageStructure();
            keepgoing = keepgoing
                    && keepGoingMessageStructureError(messageFailures);
            // if (keepgoing) {
            // 3- Map the SegmentGroup and Segment in the Profile and in the
            // Message by using a finite state machine
            // 4- Validate the Segment, Field, Component and SubComponent
            // elements
            mapAndCheckElements(profile);
            // }
        }
        return messageFailures;
    }

    /**
     * Pre-Process the profile to fix the table express at a field level whereas
     * the field is not primitives.
     */
    private void preprocessProfileTable() {
        // Look for Field with a CE datatype that has components with no table
        String ceXPath = "//.[@Datatype = 'CE' and @Table and count(child::*[position() = 1 and @Table]) = 0]";
        XmlObject[] rs = profile.getDocument().selectPath(ceXPath);
        for (XmlObject ceField : rs) {
            XmlCursor cursor = ceField.newCursor();
            String table = cursor.getAttributeText(QName.valueOf("Table"));
            cursor.toChild("Component");
            cursor.setAttributeText(QName.valueOf("Table"), table);
            cursor.getChars();
        }
    }

    /**
     * Check the length of a value.
     * 
     * @param value
     *        the value to be checked
     * @param maxLength
     *        the maximum length of the value
     * @return a message failure (can be null if the check goes well)
     */
    protected MessageFailureV2 checkLength(String value, int maxLength) {
        StringBuffer sb = new StringBuffer();
        MessageFailureV2 mf = null;
        if (maxLength != 99999 && value.length() > maxLength) {
            mf = new MessageFailureV2(message.getEncoding());
            sb.append("The value '").append(value);
            sb.append(
                    "' specified in the message exceeds the maximum element length '").append(
                    maxLength).append("' specified in profile.");
            mf.setDescription(sb.toString());
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            mf.setFailureType(AssertionTypeV2Constants.LENGTH);
            mf.setElementContent(value);
        }
        sb = null;
        return mf;
    }

    /**
     * Check if the provided value is in a table.
     * 
     * @param value
     *        the value to be checked
     * @param table
     *        the table id
     * @return a message failure (can be null if the check goes well)
     */
    protected MessageFailureV2 checkTable(String value, String table) {
        StringBuffer sb = new StringBuffer();
        MessageFailureV2 mf = null;
        TableDefinition td = null;
        Iterator<TableElement> it = null;
        boolean tablefound = false;
        for (int i = 0; i < tables.size() && !tablefound; i++) {
            td = getTableDefinition(tables.get(i), table);
            if (td != null) {
                it = td.getTableElementList().iterator();
            }
            tablefound = it != null;
        }
        if (!tablefound) {
            mf = new MessageFailureV2(message.getEncoding());
            sb.append("The value '").append(value);
            sb.append("' specified in the message can't be checked because the table '");
            sb.append(table).append(
                    "' specified in the profile was not found in the tables file.");
            mf.setDescription(sb.toString());
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            mf.setFailureType(AssertionTypeV2Constants.TABLE_NOT_FOUND);
            mf.setElementContent(value);
        }
        if (it != null) {
            // Type of the table
            boolean userTable = td.getType() == TableType.USER;
            boolean hl7Table = td.getType() == TableType.HL_7;
            boolean hasItem = it.hasNext();
            boolean checkThatTable = true;
            if (hl7Table) {
                // HL7 Table
                List<String> hl7Tables = context.getHl7Tables();
                if (hl7Tables.size() == 0) {
                    // None
                    checkThatTable = false;
                } else if (hl7Tables.size() == 1 && hl7Tables.get(0) == null) {
                    // All
                    checkThatTable = true;
                } else if (hl7Tables.size() > 0 && hl7Tables.contains(table)) {
                    // List
                    checkThatTable = true;
                    if (!hasItem) {
                        checkThatTable = false;
                        mf = new MessageFailureV2(message.getEncoding());
                        sb.append("The HL7 Table '").append(table);
                        sb.append("' referenced in the Validation Context file is empty. Validation checks using this table will not be performed.");
                        mf.setDescription(sb.toString());
                        mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                        mf.setFailureType(AssertionTypeV2Constants.VALIDATION_CONFIGURATION);
                    }
                } else {
                    checkThatTable = false;
                }
            } else if (userTable) {
                // User Table
                List<String> userTables = context.getUserTables();
                if (userTables.size() == 0) {
                    // None
                    checkThatTable = false;
                } else if (userTables.size() == 1 && userTables.get(0) == null) {
                    // All
                    checkThatTable = true;
                } else if (userTables.size() > 0 && userTables.contains(table)) {
                    // List
                    checkThatTable = true;
                    if (!hasItem) {
                        checkThatTable = false;
                        mf = new MessageFailureV2(message.getEncoding());
                        sb.append("The User Table '").append(table);
                        sb.append("' referenced in the Validation Context file is empty. Validation checks using this table will not be performed.");
                        mf.setDescription(sb.toString());
                        mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                        mf.setFailureType(AssertionTypeV2Constants.VALIDATION_CONFIGURATION);
                    }
                } else {
                    checkThatTable = false;
                }
            }
            if (checkThatTable) {
                boolean found = false;
                if (hasItem) {
                    while (!found && it.hasNext()) {
                        if (it.next().getCode().equalsIgnoreCase(value)) {
                            found = true;
                        }
                    }
                } else {
                    found = true;
                }
                if (!found) {
                    mf = new MessageFailureV2(message.getEncoding());
                    sb.append("The value '").append(value);
                    sb.append("' specified in the message does not match any of the values in the table '");
                    sb.append(table).append("' specified in the profile.");
                    mf.setDescription(sb.toString());
                    mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
                    mf.setFailureType(AssertionTypeV2Constants.DATA);
                    mf.setElementContent(value);
                }
            }
        }
        sb = null;
        return mf;
    }

    /**
     * Get the table definition.
     * 
     * @param tableLibraryDocument
     *        the table library document containing the table values
     * @param tableId
     *        the table id
     * @return the table definition; null otherwise
     */
    private TableDefinition getTableDefinition(
            TableLibraryDocument tableLibraryDocument, String tableId) {
        Iterator<TableDefinition> it = tableLibraryDocument.getTableLibrary().getTableDefinitionList().iterator();
        while (it.hasNext()) {
            TableDefinition td = it.next();
            if (td.getId().equals(tableId)) {
                return td;
            }
        }
        return null;
    }

    /**
     * Check if the provided value is the constant provided in the profile
     * 
     * @param value
     *        the value to be checked
     * @param constant
     *        the constant value
     * @return a message failure (can be null if the check goes well)
     */
    protected MessageFailureV2 checkConstant(String value, String constant) {
        StringBuffer sb = new StringBuffer();
        MessageFailureV2 mf = null;
        if (!constant.equals(value)) {
            mf = new MessageFailureV2(message.getEncoding());
            sb.append("The value '").append(value);
            sb.append("' does not match the fixed value '");
            sb.append(constant);
            sb.append("' specified in the message instance.");
            mf.setDescription(sb.toString());
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            mf.setFailureType(AssertionTypeV2Constants.DATA);
            mf.setElementContent(value);
        }
        sb = null;
        return mf;
    }

    /**
     * Check if the provided value is valid against the format imposed by its
     * datatype.
     * 
     * @param value
     *        the value to be checked
     * @param datatype
     *        the datatype
     * @return a message failure (can be null if the check goes well)
     */
    protected MessageFailureV2 checkDatatype(String value, String datatype) {
        StringBuffer sb = new StringBuffer();
        MessageFailureV2 mf = null;
        String regex = null;
        if (datatype.equals("DTM")) {
            regex = "([12]\\d\\d\\d((0[1-9]|1[0-2])(([0-2]\\d|3[01])(([01]\\d|2[0-4])([0-5]\\d([0-5]\\d(.\\d\\d?\\d?\\d?)?)?)?)?)?)?((\\+|\\-)([01]\\d|2[0-4])[0-5]\\d)?)?";
        } else if (datatype.equals("TM")) {
            regex = "((((([01]\\d|2[0-4])([0-5]\\d([0-5]\\d(.\\d\\d?\\d?\\d?)?)?)?)?)?)?((\\+|\\-)([01]\\d|2[0-4])[0-5]\\d)?)?";
        } else if (datatype.equals("DT")) {
            regex = "([12]\\d\\d\\d((0[1-9]|1[0-2])([0-2]\\d|3[01])?)?)";
        } else if (datatype.equals("SI")) {
            regex = "[0-9][0-9]{0,3}";
        } else if (datatype.equals("NM")) {
            regex = "(\\+|\\-)?\\d*(\\.?\\d+)?";
        } else if (datatype.equals("TN")) {
            regex = "(\\d{1,2} )?(\\(\\d{3}\\))?\\d{3}-\\d{4}(X\\d{1,5})?(B\\d{1,5})?(C.*)?";
        }
        if (regex != null && !Pattern.matches(regex, value)) {
            mf = new MessageFailureV2(message.getEncoding());
            sb.append("The value '").append(value).append(
                    "' is not valid with respect to the format specified for datatype '");
            sb.append(datatype).append("'");
            mf.setDescription(sb.toString());
            mf.setFailureSeverity(ErrorSeverityConstants.NORMAL);
            mf.setFailureType(AssertionTypeV2Constants.DATATYPE);
            mf.setElementContent(value);
        }
        sb = null;
        return mf;
    }

    /**
     * Keep going even if there is MSH.9.3 error.
     * 
     * @param messageFailures
     *        the current message failures, we check that none has been fatal
     * @return a boolean
     */
    private boolean keepGoingMessageStructureIdError(
            List<MessageFailureV2> messageFailures) {
        boolean keepgoing = true;
        for (MessageFailureV2 mf : messageFailures) {
            if (mf.getFailureSeverity() == ErrorSeverityConstants.FATAL) {
                keepgoing = false;
                break;
            }
        }
        return keepgoing;
    }

    /**
     * Keep going only if there is no MESSAGE_STRUCTURE error.
     * 
     * @param messageFailures
     *        the current message failures, we check that none has been fatal
     * @return a boolean
     */
    private boolean keepGoingMessageStructureError(
            List<MessageFailureV2> messageFailures) {
        boolean keepgoing = true;
        for (MessageFailureV2 mf : messageFailures) {
            if (mf.getFailureType() == AssertionTypeV2Constants.MESSAGE_STRUCTURE) {
                keepgoing = false;
                break;
            }
        }
        return keepgoing;
    }

    protected abstract void checkBasic() throws MessageValidationException;

    protected abstract void checkMessageStructure()
            throws MessageValidationException;

    protected abstract void mapAndCheckElements(Profile profile)
            throws MessageValidationException;

}
