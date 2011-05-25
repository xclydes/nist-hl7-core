/*
 * NIST Healthcare Core
 * ValidationConstants.java Oct 4, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message;

/**
 * This class contains all the constants used for the validation
 * 
 * @author Sydney Henrard (NIST)
 */
public final class MessageValidationConstants {

    /**
     * Private Constructor
     */
    private MessageValidationConstants() {
    }

    public static final String MESSAGE_VALIDATION_VERSION = "0.2";

    public static final String XSLT_CHECK_STRUCTURE = "xslt/validation/message/CheckMessageStructure.xsl";

    public static final String XSLT_SKELETON = "xslt/validation/message/schematron-Validator-report.xsl";

    public static final String XSLT_SKELETON_1_5 = "xslt/validation/message/skeleton1-5.xsl";

    public static final String MESSAGE_VALIDATION_V2_VERSION = "1.0";

    public static final String MESSAGE_VALIDATION_V3_VERSION = "1.0";

    public enum ValidationState {
        NORMAL, MAX, ERR, XERR, XTRA, UNMATCHING, SKIP
    };

    public enum FailureSeverity {
        FATAL, NORMAL
    }

}
