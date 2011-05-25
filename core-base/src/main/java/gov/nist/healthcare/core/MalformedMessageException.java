/*
 * NIST Healthcare Core
 * MalformedProfileException.java Oct 29, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core;

/**
 * This exception is thrown when a Message is malformed
 * 
 * @author Sydney Henrard (NIST)
 */
public class MalformedMessageException extends Exception {

    /**
     * Constructor
     * 
     * @param reason
     *        The reason for the exception.
     */
    public MalformedMessageException(String reason) {
        super(reason);
    }
}
