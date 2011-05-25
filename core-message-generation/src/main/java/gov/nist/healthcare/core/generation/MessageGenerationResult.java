/*
 * NIST Healthcare Core
 * MessageGenerationResult.java Jun 3, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.generation;

import gov.nist.healthcare.core.message.v2.HL7V2Message;
import java.util.List;

/**
 * This class contains the result of a message generation
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageGenerationResult {

    private final List<HL7V2Message> messages;
    private List<String> errors;

    /**
     * Constructor
     * 
     * @param messages
     *        the list of generated messages
     * @param errors
     *        the list of errors during generation
     */
    public MessageGenerationResult(List<HL7V2Message> messages,
            List<String> errors) {
        this.messages = messages;
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<HL7V2Message> getMessages() {
        return messages;
    }

    /**
     * Return true if the generation process runs into error.
     * 
     * @return true if there were errors during the generation process; false
     *         otherwise
     */
    public boolean hasError() {
        return errors.size() != 0;
    }

}
