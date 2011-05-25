/*
 * NIST Healthcare Core
 * AbstractMessageValidation.java Dec 21, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.v3;

import gov.nist.healthcare.core.message.v3.HL7V3Message;
import gov.nist.healthcare.core.validation.message.MessageValidationException;
import gov.nist.healthcare.core.validation.message.content.v3.MessageContentValidationV3;
import gov.nist.healthcare.core.validation.message.structure.v3.MessageStructureValidationV3;
import java.util.List;

/**
 * This is an abstract class for V2 message validation
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageValidationV3 {

    protected MessageStructureValidationV3 structureValidator;
    protected MessageContentValidationV3 contentValidator;

    /**
     * Constructor
     */
    public MessageValidationV3() {
        contentValidator = new MessageContentValidationV3();
        structureValidator = new MessageStructureValidationV3();
    }

    /**
     * Validate the message content and structure
     * 
     * @param message
     *        the message to validate
     * @param context
     *        the message validation context
     * @param schemaLocation
     *        the location of the schema to validate with
     * @param schematronLocation
     *        the location of the schematron to validate with
     * @return the message validation result
     * @throws MessageValidationException
     */
    public MessageValidationResultV3 validate(HL7V3Message message,
            MessageValidationContextV3 context, String schemaLocation,
            String schematronLocation) throws MessageValidationException {
        MessageValidationResultV3 result = null;
        if (schemaLocation != null && schematronLocation != null) {
            // Structure Validation
            List<MessageFailureV3> structureFailures = structureValidator.validate(
                    message, schemaLocation, schematronLocation);
            // Content Validation
            List<MessageFailureV3> contentFailures = contentValidator.validate(
                    message, context);
            // Create the results
            result = new MessageValidationResultV3(message, context,
                    structureFailures, contentFailures);
        }
        return result;
    }

    public MessageValidationResultV3 validate(HL7V3Message message,
            String schemaLocation, String schematronLocation)
            throws MessageValidationException {
        MessageValidationResultV3 result = null;
        if (schemaLocation != null && schematronLocation != null) {
            // Structure Validation
            List<MessageFailureV3> structureFailures = structureValidator.validate(
                    message, schemaLocation, schematronLocation);
            // Create the results
            result = new MessageValidationResultV3(message, structureFailures);
        }
        return result;
    }

    public MessageValidationResultV3 validate(HL7V3Message message,
            String schemaLocation) throws MessageValidationException {
        MessageValidationResultV3 result = null;
        if (schemaLocation != null) {
            // Structure Validation
            List<MessageFailureV3> structureFailures = structureValidator.validate(
                    message, schemaLocation);
            // Create the results
            result = new MessageValidationResultV3(message, structureFailures);
        }
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
    public MessageValidationResultV3 validate(HL7V3Message message,
            MessageValidationContextV3 context) {
        MessageValidationResultV3 result;
        // Content Validation
        List<MessageFailureV3> contentFailures = contentValidator.validate(
                message, context);
        // Create the results
        result = new MessageValidationResultV3(message, context,
                contentFailures);

        return result;
    }

}
