/*
 * NIST Healthcare Core
 * ESegment.java Nov 18, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message.v2.er7;

import gov.nist.healthcare.core.message.ValuedMessageLocation;
import gov.nist.healthcare.core.profile.Profile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * This class represents an ER7 segment.
 * 
 * @author Sydney Henrard (NIST)
 */
public class ESegment extends Er7Element {

    private Map<Integer, List<EField>> fields;

    /**
     * Constructor.
     * 
     * @param segment
     *        a String representation of the segment
     * @param lineNumber
     *        the line number
     * @param fieldSeparator
     *        the field separator character
     * @param fieldRepetitionSeparator
     *        the field repetition seperator character
     * @param componentSeparator
     *        the component separator character
     * @param subComponentSeparator
     *        the subcomponent separator character
     * @param segmentInstanceNumber
     */
    public ESegment(String segment, int lineNumber, String fieldSeparator,
            String fieldRepetitionSeparator, String componentSeparator,
            String subComponentSeparator, int segmentInstanceNumber) {
        setValue(segment, lineNumber, fieldSeparator, fieldRepetitionSeparator,
                componentSeparator, subComponentSeparator,
                segmentInstanceNumber);
    }

    /**
     * Set the value at this level and at the sublevel
     * 
     * @param segment
     *        a String representation of the segment
     * @param lineNumber
     *        the line number
     * @param fieldSeparator
     *        the field separator character
     * @param fieldRepetitionSeparator
     *        the field repetition seperator character
     * @param componentSeparator
     *        the component separator character
     * @param subComponentSeparator
     *        the subcomponent separator character
     * @param segmentInstanceNumber
     */
    public void setValue(String segment, int lineNumber, String fieldSeparator,
            String fieldRepetitionSeparator, String componentSeparator,
            String subComponentSeparator, int segmentInstanceNumber) {
        // System.out.println("Segment: " + segment);
        fields = new HashMap<Integer, List<EField>>();
        List<EField> listField = null;
        value = segment;
        this.line = lineNumber;
        this.column = 1;
        this.position = 1;
        this.instanceNumber = segmentInstanceNumber;
        Scanner sField = new Scanner(segment);
        sField.useDelimiter(fieldSeparator);
        int fieldInstanceNumber = 1;
        int fieldPosition = 1;
        int fieldColumn = 1;
        // Skip the segment name
        String segmentName = sField.next();
        fieldColumn += (segmentName.length() + 1);
        if ("MSH".equals(segmentName)) {
            // Add the field separator
            fieldColumn--;
            EField efield = null;
            efield = new EField(String.valueOf(segment.charAt(3)), lineNumber,
                    fieldColumn, fieldPosition, fieldInstanceNumber);
            listField = fields.get(fieldPosition);
            if (listField == null) {
                listField = new ArrayList<EField>();
                fields.put(fieldPosition, listField);
            }
            listField.add(efield);
            fieldColumn++;
            fieldPosition++;
            // Special case the seperators
            String separators = sField.next();
            efield = new EField(separators, lineNumber, fieldColumn,
                    fieldPosition, fieldInstanceNumber);
            listField = fields.get(fieldPosition);
            if (listField == null) {
                listField = new ArrayList<EField>();
                fields.put(fieldPosition, listField);
            }
            listField.add(efield);
            fieldColumn += (separators.length() + 1);
            fieldPosition++;
        }
        while (sField.hasNext()) {
            listField = fields.get(fieldPosition);
            if (listField == null) {
                listField = new ArrayList<EField>();
                fields.put(fieldPosition, listField);
            }
            String fieldValue = sField.next();
            // if ("awgfwweg".equals(fieldValue)) {
            // EField efield = new EField(fieldValue, line, fieldColumn,
            // componentSeparator, subComponentSeparator);
            // listField.add(efield);
            // fieldColumn++;
            // } else {
            // Scanner sRepetition = new Scanner(fieldValue);
            // sRepetition.useDelimiter(fieldRepetitionSeparator);
            String[] repetitionValues = fieldValue.split(
                    fieldRepetitionSeparator, -1);
            int fieldRepetitionNumber = 0;
            // while (sRepetition.hasNext()) {
            for (String repetitionValue : repetitionValues) {
                fieldRepetitionNumber++;
                // String repetitionValue = sRepetition.next();
                EField efield = new EField(repetitionValue, line, fieldColumn,
                        componentSeparator, subComponentSeparator,
                        fieldPosition, fieldRepetitionNumber);
                listField.add(efield);
                if (fieldRepetitionNumber == repetitionValues.length) {
                    fieldColumn += repetitionValue.length();
                } else {
                    fieldColumn += (repetitionValue.length() + 1);
                }
            }
            // if (fieldRepetitionNumber != 1) {
            fieldColumn++;
            // }
            // }
            fieldPosition++;
        }
    }

    /**
     * Get the field at the specified location.
     * 
     * @param fieldPosition
     * @param fieldInstanceNumber
     * @return an EField
     */
    public EField get(int fieldPosition, int fieldInstanceNumber) {
        EField efield = null;
        List<EField> efields = fields.get(fieldPosition);
        if (efields != null) {
            if (efields.size() >= fieldInstanceNumber) {
                efield = efields.get(fieldInstanceNumber - 1);
            }
        }
        return efield;
    }

    /**
     * Get all fields
     * 
     * @param fieldPosition
     * @return a list of EField
     */
    public List<EField> get(int fieldPosition) {
        return fields.get(fieldPosition);
    }

    /**
     * Get the last field position
     * 
     * @return the last field position
     */
    public int getFieldMax() {
        return Collections.max(fields.keySet());
    }

    /**
     * Get the valued locations (children).
     * 
     * @param profile
     * @return a list a valued message location
     */
    public List<ValuedMessageLocation> getLocations(Profile profile) {
        List<ValuedMessageLocation> valuedLocations = new ArrayList<ValuedMessageLocation>();
        for (int fieldPosition : fields.keySet()) {
            List<EField> eFields = fields.get(fieldPosition);
            for (EField eField : eFields) {
                if (eField.isPrimitive()) {
                    if (!"".equals(eField.getValue())) {
                        ValuedMessageLocation vml = new ValuedMessageLocation(
                                null, this.getSegmentName(),
                                this.instanceNumber, eField.getPosition(),
                                eField.getInstanceNumber(), eField.getValue());
                        // if (p == null || (p != null &&
                        // vml.isExistInProfile(p))) {
                        if (profile == null
                                || (profile != null && vml.isPrimitive(profile))) {
                            valuedLocations.add(vml);
                        }
                    }
                }
                valuedLocations.addAll(eField.getLocations(profile,
                        this.getSegmentName(), this.instanceNumber));
            }
        }
        return valuedLocations;
    }

    /**
     * Get the segment name
     * 
     * @return the segment name
     */
    private String getSegmentName() {
        String segmentName = null;
        if (value != null) {
            segmentName = value.substring(0, 3);
        }
        return segmentName;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }
}
