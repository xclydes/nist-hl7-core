/*
 * NIST Healthcare Core
 * MessageGenerationConstants.java Feb 6, 2008
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.generation;

/**
 * This class contains constants used by message generation
 * 
 * @author Sydney Henrard (NIST)
 */
public final class MessageGenerationConstants {

    /**
     * Private Constructor
     */
    private MessageGenerationConstants() {
    }

    public static final String XSLT_PROFILE_TO_ANNOTATED_MESSAGE = "/xslt/generation/Profile2AnnotatedMessage.xsl";

    public enum PrimitiveDatatype {
        DT, DTM, FT, GTS, ID, IS, NM, SI, ST, TM, TX
    };

    public static final int POPULATION_MAX_TRIES = 100;

    public enum GenerationError {
        NON_PRIMITIVE, NON_EXISTING, INVALID_RESOURCE
    };

}
