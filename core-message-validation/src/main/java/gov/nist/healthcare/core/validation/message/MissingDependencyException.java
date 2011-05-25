/*
 * NIST Healthcare Core
 * MissingDependencyException.java Mar 03, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message;

/**
 * This exception is thrown when a dependency is missing for the validation to
 * complete.
 * 
 * @author Sydney Henrard (NIST)
 */
public class MissingDependencyException extends Exception {

    /**
     * Constructor
     * 
     * @param reason
     *        The reason for the exception.
     */
    public MissingDependencyException(String reason) {
        super(reason);
    }
}
