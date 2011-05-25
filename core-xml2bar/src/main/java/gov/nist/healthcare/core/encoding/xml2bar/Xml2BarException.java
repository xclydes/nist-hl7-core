/*
 * NIST Healthcare Core
 * Xml2barException.java Jun 23, 2006
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.encoding.xml2bar;

/**
 * This exception is thrown when an error occur while parsing the ER7 file.
 * 
 * @author Caroline Rosin (NIST)
 */
public class Xml2BarException extends Exception {

    /**
     * Constructor.
     * 
     * @param reason
     */
    public Xml2BarException(String reason) {
        super(reason);
    }
}
