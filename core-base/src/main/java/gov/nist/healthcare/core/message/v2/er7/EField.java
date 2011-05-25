/*
 * NIST Healthcare Core
 * EField.java Nov 18, 2009
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

/**
 * This class represents an ER7 field.
 * 
 * @author Sydney Henrard (NIST)
 */
public class EField extends Er7Element {

    private Map<Integer, EComponent> components;
    private String componentSeparator;
    private String subComponentSeparator;

    /**
     * Constructor.
     * 
     * @param field
     *        a String representation of the field
     * @param lineNumber
     *        the line number
     * @param columnNumber
     *        the column number
     * @param componentSeparator
     *        the component separator character
     * @param subComponentSeparator
     *        the subcomponent separator character
     * @param fieldPosition
     * @param fieldInstanceNumber
     */
    public EField(String field, int lineNumber, int columnNumber,
            String componentSeparator, String subComponentSeparator,
            int fieldPosition, int fieldInstanceNumber) {
        setValue(field, lineNumber, columnNumber, componentSeparator,
                subComponentSeparator, fieldPosition, fieldInstanceNumber);
    }

    /**
     * Specific constructor for the separators field.
     * 
     * @param separators
     *        a String representation of the separators
     * @param lineNumber
     *        the line number
     * @param columnNumber
     *        the column number
     * @param fieldPosition
     * @param fieldInstanceNumber
     */
    public EField(String separators, int lineNumber, int columnNumber,
            int fieldPosition, int fieldInstanceNumber) {
        // System.out.println("Field: " + separators);
        this.componentSeparator = "";
        this.subComponentSeparator = "";
        components = new HashMap<Integer, EComponent>();
        value = separators;
        this.line = lineNumber;
        this.column = columnNumber;
        this.position = fieldPosition;
        this.instanceNumber = fieldInstanceNumber;
    }

    /**
     * Set the value at this level and at the sublevel
     * 
     * @param field
     *        a String representation of the field
     * @param lineNumber
     *        the line number
     * @param columnNumber
     *        the column number
     * @param componentSeparator
     *        the component separator character
     * @param subComponentSeparator
     *        the subcomponent separator character
     * @param fieldPosition
     * @param fieldInstanceNumber
     */
    public void setValue(String field, int lineNumber, int columnNumber,
            String componentSeparator, String subComponentSeparator,
            int fieldPosition, int fieldInstanceNumber) {
        this.componentSeparator = componentSeparator;
        this.subComponentSeparator = subComponentSeparator;
        components = new HashMap<Integer, EComponent>();
        value = field;
        this.line = lineNumber;
        this.column = columnNumber;
        this.position = fieldPosition;
        this.instanceNumber = fieldInstanceNumber;
        // System.out.println("Field: " + value + " " + column);
        int componentPosition = 1;
        int componentColumn = columnNumber;
        // if ("".equals(field)) {
        // EComponent ecomponent = new EComponent(field, lineNumber,
        // componentColumn, subComponentSeparator);
        // components.put(componentPosition, ecomponent);
        // } else {

        // Scanner sComponent = new Scanner(field);
        // sComponent.useDelimiter(componentSeparator);
        // while (sComponent.hasNext()) {
        String[] componentValues = field.split(componentSeparator, -1);
        for (String componentValue : componentValues) {
            // String componentValue = sComponent.next();
            EComponent ecomponent = new EComponent(componentValue, lineNumber,
                    componentColumn, subComponentSeparator, componentPosition);
            components.put(componentPosition, ecomponent);
            if (componentPosition == componentValues.length) {
                componentColumn += componentValue.length();
            } else {
                componentColumn += (componentValue.length() + 1);
            }
            componentPosition++;
        }
        // componentColumn++;
        // }
    }

    /**
     * Get the component at the specified location.
     * 
     * @param componentPosition
     * @return an EComponent
     */
    public EComponent get(int componentPosition) {
        return components.get(componentPosition);
    }

    /**
     * Get the valued locations (children).
     * 
     * @param profile
     * @param segmentName
     * @param segmentInstanceNumber
     * @return a list a valued message location
     */
    public List<ValuedMessageLocation> getLocations(Profile profile,
            String segmentName, int segmentInstanceNumber) {
        List<ValuedMessageLocation> valuedLocations = new ArrayList<ValuedMessageLocation>();
        for (int componentPosition : components.keySet()) {
            EComponent eComponent = components.get(componentPosition);
            if (eComponent.isPrimitive()) {
                if (!"".equals(eComponent.getValue())) {
                    ValuedMessageLocation vml = new ValuedMessageLocation(null,
                            segmentName, segmentInstanceNumber, this.position,
                            this.instanceNumber, eComponent.getPosition(),
                            eComponent.getValue());
                    if (profile == null
                            || (profile != null && vml.isPrimitive(profile))) {
                        valuedLocations.add(vml);
                    }
                }
            }
            valuedLocations.addAll(eComponent.getLocations(profile,
                    segmentName, segmentInstanceNumber, this.position,
                    this.instanceNumber));
        }
        return valuedLocations;
    }

    @Override
    public boolean isPrimitive() {
        return value.split(componentSeparator, -1).length == 1
                && value.split(subComponentSeparator, -1).length == 1;
    }

    /**
     * Get the last component position
     * 
     * @return the last component position
     */
    public int getComponentMax() {
        return Collections.max(components.keySet());
    }

}
