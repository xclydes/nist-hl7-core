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
 * This exception is thrown when a Profile is malformed
 * 
 * @author Sydney Henrard (NIST)
 */
public class MalformedProfileException extends Exception {

    /**
     * Constructor
     * 
     * @param reason
     *        The reason for the exception.
     */
    public MalformedProfileException(String reason) {
        super(reason);
    }
}
