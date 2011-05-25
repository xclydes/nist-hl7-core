/*
 * NIST Healthcare Core
 * Constants.java Mar 1, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core;

/**
 * This class contains constants
 * 
 * @author Sydney Henrard (NIST)
 */
public final class Constants {

    /**
     * Private Constructor
     */
    private Constants() {
    }

    public enum ProfileType {
        MWB_PROFILE, IMPLEMENTATION_PROFILE
    }

    public enum MessageEncoding {
        V2_ER7, V2_XML, V3
    }

    // public enum GeneralMessageType {
    // HL7_ACKNOWLEDGMENT,
    // PATIENT_ADD,
    // PATIENT_UPDATE,
    // PATIENT_MERGE_DUPLICATE,
    // GET_IDENTIFIERS_QUERY,
    // GET_IDENTIFIERS_QUERY_RESPONSE,
    // PDQ_QUERY,
    // PDQ_QUERY_RESPONSE,
    // PDQ_QUERY_CANCEL,
    // PDQ_QUERY_CONTINUATION,
    // UNSUPPORTED
    // }

    public static final String V3_ACKNOWLEDGMENT_INTERACTION_ID = "MCCI_IN000002UV01";
    public static final String V3_PATIENT_ADD_INTERACTION_ID = "PRPA_IN201301UV02";
    public static final String V3_PATIENT_UPDATE_INTERACTION_ID = "PRPA_IN201302UV02";
    public static final String V3_PATIENT_MERGE_DUPLICATES_INTERACTION_ID = "PRPA_IN201304UV02";
    public static final String V3_QUERY_BY_IDENTIFIER_INTERACTION_ID = "PRPA_IN201309UV02";
    public static final String V3_QUERY_BY_IDENTIFIER_RESPONSE_INTERACTION_ID = "PRPA_IN201310UV02";
    public static final String V3_PDQ_QUERY_INTERACTION_ID = "PRPA_IN201305UV02";
    public static final String V3_PDQ_QUERY_RESPONSE_INTERACTION_ID = "PRPA_IN201306UV02";
    public static final String V3_PDQ_GENERAL_QUERY_ACTIVATE_QUERY_CONTINUE = "QUQI_IN000003UV01";

    public static final String XSLT_MWB2IMPL_RESOURCE = "xslt/profile/MWB2Impl.xsl";

    //
    public static final String XSLT_PROFILE_TO_MESSAGE_MAX = "xslt/generation/profile2msgMax.xsl";

    public static final String XSLT_PROFILE_TO_MESSAGE_NEW = "xslt/generation/profile2msgNew.xsl";

    public enum ElementType {
        SEGMENT_GROUP, SEGMENT, FIELD, COMPONENT, SUBCOMPONENT
    };

    public static final String HL7V2_3_1TABLES = "/data/HL7tableV2.3.1.xml";
    public static final String HL7V2_4TABLES = "/data/HL7tableV2.4.xml";
    public static final String HL7V2_5TABLES = "/data/HL7tableV2.5.xml";
    public static final String HL7V2_5_1TABLES = "/data/HL7tableV2.5.1.xml";
    public static final String HL7V2_6TABLES = "/data/HL7tableV2.6.xml";

    public static final String PRIMITIVESV2_3_1 = "/data/PrimitivesV2.3.1.xml";
    public static final String PRIMITIVESV2_4 = "/data/PrimitivesV2.4.xml";
    public static final String PRIMITIVESV2_5 = "/data/PrimitivesV2.5.xml";
    public static final String PRIMITIVESV2_5_1 = "/data/PrimitivesV2.5.1.xml";
    public static final String PRIMITIVESV2_6 = "/data/PrimitivesV2.6.xml";

    public static final String DEFAULT_VALUES = "/data/DefaultValue.xml";

    public static final String SEQUENCENUMBERSV2_3_1 = "/data/SequenceNumbersV2.3.1.xml";
    public static final String SEQUENCENUMBERSV2_4 = "/data/SequenceNumbersV2.4.xml";
    public static final String SEQUENCENUMBERSV2_5 = "/data/SequenceNumbersV2.5.xml";
    public static final String SEQUENCENUMBERSV2_5_1 = "/data/SequenceNumbersV2.5.1.xml";
    public static final String SEQUENCENUMBERSV2_6 = "/data/SequenceNumbersV2.6.xml";
    //
    public static final String DATABASE_CONFIG = "/data/hl7stdConfig.properties";
    public static final String DATABASE_SCRIPT = "/data/hl7std.script";
    public static final String DATABASE_PROPERTIES = "/data/hl7std.properties";
    public static final String DATABASE_TMP_SCRIPT = System.getProperty("java.io.tmpdir")
            + "hl7std.script";
    public static final String DATABASE_TMP_PROPERTIES = System.getProperty("java.io.tmpdir")
            + "hl7std.properties";
    public static final String EPATH_REGEX = "(?:((?:[A-Z0-9_]+\\[(?:\\d+|\\*)\\]/)*)"
            + "(?:([A-Z0-9]{3})\\[(?:(\\d+|\\*))\\]"
            + "(?:\\.(\\d+)\\[(?:(\\d+|\\*))\\]"
            + "(?:\\.(\\d+)(?:\\.(\\d+))?)?)?)?)";

    public static String getHl7Tables(String version) {
        String hl7tables = null;
        if ("2.3.1".equals(version)) {
            hl7tables = HL7V2_3_1TABLES;
        } else if ("2.4".equals(version)) {
            hl7tables = HL7V2_4TABLES;
        } else if ("2.5".equals(version)) {
            hl7tables = HL7V2_5TABLES;
        } else if ("2.5.1".equals(version)) {
            hl7tables = HL7V2_5_1TABLES;
        } else if ("2.6".equals(version)) {
            hl7tables = HL7V2_6TABLES;
        } else {
            throw new IllegalArgumentException(
                    "The version "
                            + version
                            + " is not supported. We support version 2.3.1, 2.4, 2.5, 2.5.1, 2.6.");
        }
        return hl7tables;
    }

    public static String getPrimitives(String version) {
        String primitives = null;
        if ("2.3.1".equals(version)) {
            primitives = PRIMITIVESV2_3_1;
        } else if ("2.4".equals(version)) {
            primitives = PRIMITIVESV2_4;
        } else if ("2.5".equals(version)) {
            primitives = PRIMITIVESV2_5;
        } else if ("2.5.1".equals(version)) {
            primitives = PRIMITIVESV2_5_1;
        } else if ("2.6".equals(version)) {
            primitives = PRIMITIVESV2_6;
        } else {
            throw new IllegalArgumentException(
                    "The version "
                            + version
                            + " is not supported. We support version 2.3.1, 2.4, 2.5, 2.5.1, 2.6.");
        }
        return primitives;
    }

    public static String getSequenceNumbers(String version) {
        String sequenceNumbers = null;
        if ("2.3.1".equals(version)) {
            sequenceNumbers = SEQUENCENUMBERSV2_3_1;
        } else if ("2.4".equals(version)) {
            sequenceNumbers = SEQUENCENUMBERSV2_4;
        } else if ("2.5".equals(version)) {
            sequenceNumbers = SEQUENCENUMBERSV2_5;
        } else if ("2.5.1".equals(version)) {
            sequenceNumbers = SEQUENCENUMBERSV2_5_1;
        } else if ("2.6".equals(version)) {
            sequenceNumbers = SEQUENCENUMBERSV2_6;
        } else {
            throw new IllegalArgumentException(
                    "The version "
                            + version
                            + " is not supported. We support version 2.3.1, 2.4, 2.5, 2.5.1, 2.6.");
        }
        return sequenceNumbers;
    }

}
