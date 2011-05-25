/*
 * NIST Healthcare Core
 * MessageValidationException.java Sep 22, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message;

/**
 * This exception is thrown when a message validation exception occurs
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageValidationException extends Exception {

    /**
     * Constructor
     * 
     * @param reason
     *        The reason for the exception.
     */
    public MessageValidationException(String reason) {
        super(reason);
    }

}
