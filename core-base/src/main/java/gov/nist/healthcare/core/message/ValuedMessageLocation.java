/*
 * NIST Healthcare Core
 * ValuedMessageLocaiton.java Aug 4, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message;

import java.util.List;

/**
 * This class represents a values message location
 * 
 * @author Sydney Henrard (NIST)
 */
public class ValuedMessageLocation extends MessageLocation {

    private String value;

    /**
     * Constructor for a SubComponent with segment groups.
     * 
     * @param segmentGroups
     * @param segmentName
     * @param segmentInstanceNumber
     * @param fieldPosition
     * @param fieldInstanceNumber
     * @param componentPosition
     * @param subComponentPosition
     * @param value
     */
    public ValuedMessageLocation(
            List<SegmentGroupInstanceNumber> segmentGroups, String segmentName,
            int segmentInstanceNumber, int fieldPosition,
            int fieldInstanceNumber, int componentPosition,
            int subComponentPosition, String value) {
        super(segmentGroups, segmentName, segmentInstanceNumber, fieldPosition,
                fieldInstanceNumber, componentPosition, subComponentPosition);
        this.value = value;
    }

    /**
     * Constructor for a Component with segment groups.
     * 
     * @param segmentGroups
     * @param segmentName
     * @param segmentInstanceNumber
     * @param fieldPosition
     * @param fieldInstanceNumber
     * @param componentPosition
     * @param value
     */
    public ValuedMessageLocation(
            List<SegmentGroupInstanceNumber> segmentGroups, String segmentName,
            int segmentInstanceNumber, int fieldPosition,
            int fieldInstanceNumber, int componentPosition, String value) {
        super(segmentGroups, segmentName, segmentInstanceNumber, fieldPosition,
                fieldInstanceNumber, componentPosition);
        this.value = value;
    }

    /**
     * Constructor for a Field with segment groups.
     * 
     * @param segmentGroups
     * @param segmentName
     * @param segmentInstanceNumber
     * @param fieldPosition
     * @param fieldInstanceNumber
     * @param value
     */
    public ValuedMessageLocation(
            List<SegmentGroupInstanceNumber> segmentGroups, String segmentName,
            int segmentInstanceNumber, int fieldPosition,
            int fieldInstanceNumber, String value) {
        super(segmentGroups, segmentName, segmentInstanceNumber, fieldPosition,
                fieldInstanceNumber);
        this.value = value;
    }

    /**
     * Constructor for a Segment with segment groups.
     * 
     * @param segmentGroups
     * @param segmentName
     * @param segmentInstanceNumber
     * @param value
     */
    public ValuedMessageLocation(
            List<SegmentGroupInstanceNumber> segmentGroups, String segmentName,
            int segmentInstanceNumber, String value) {
        super(segmentGroups, segmentName, segmentInstanceNumber);
        this.value = value;
    }

    /**
     * Constructor for a SubComponent.
     * 
     * @param segmentName
     * @param segmentInstanceNumber
     * @param fieldPosition
     * @param fieldInstanceNumber
     * @param componentPosition
     * @param subComponentPosition
     * @param value
     */
    public ValuedMessageLocation(String segmentName, int segmentInstanceNumber,
            int fieldPosition, int fieldInstanceNumber, int componentPosition,
            int subComponentPosition, String value) {
        this(null, segmentName, segmentInstanceNumber, fieldPosition,
                fieldInstanceNumber, componentPosition, subComponentPosition,
                value);
    }

    /**
     * Constructor for a Component.
     * 
     * @param segmentName
     * @param segmentInstanceNumber
     * @param fieldPosition
     * @param fieldInstanceNumber
     * @param componentPosition
     * @param value
     */
    public ValuedMessageLocation(String segmentName, int segmentInstanceNumber,
            int fieldPosition, int fieldInstanceNumber, int componentPosition,
            String value) {
        this(null, segmentName, segmentInstanceNumber, fieldPosition,
                fieldInstanceNumber, componentPosition, value);
    }

    /**
     * Constructor for a Field.
     * 
     * @param segmentName
     * @param segmentInstanceNumber
     * @param fieldPosition
     * @param fieldInstanceNumber
     * @param value
     */
    public ValuedMessageLocation(String segmentName, int segmentInstanceNumber,
            int fieldPosition, int fieldInstanceNumber, String value) {
        this(null, segmentName, segmentInstanceNumber, fieldPosition,
                fieldInstanceNumber, value);
    }

    /**
     * Constructor for a Segment.
     * 
     * @param segmentName
     * @param segmentInstanceNumber
     * @param value
     */
    public ValuedMessageLocation(String segmentName, int segmentInstanceNumber,
            String value) {
        this(null, segmentName, segmentInstanceNumber, value);
    }

    /**
     * Construtor from a path and a value.
     * 
     * @param path
     *        the message location (ex: MSH[1].9[2].3)
     * @param value
     */
    public ValuedMessageLocation(String path, String value) {
        super(path);
        setValue(value);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
