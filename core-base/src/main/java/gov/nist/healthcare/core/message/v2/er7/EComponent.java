/*
 * NIST Healthcare Core
 * EComponent.java Nov 18, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message.v2.er7;

import gov.nist.healthcare.core.message.ValuedMessageLocation;
import gov.nist.healthcare.core.profile.Profile;
import java.util.ArrayList;
import java.util.List;
import org.apache.xmlbeans.XmlCursor;

/**
 * This class represents an ER7 component.
 * 
 * @author Sydney Henrard (NIST)
 */
public class EComponent extends Er7Element {

    private List<ESubComponent> subComponents;
    private String subComponentSeparator;

    /**
     * Constructor.
     * 
     * @param component
     *        a String representation of the component
     * @param lineNumber
     *        the line number
     * @param columnNumber
     *        the column number
     * @param subComponentSeparator
     *        the subcomponent separator character
     * @param componentPosition
     */
    public EComponent(String component, int lineNumber, int columnNumber,
            String subComponentSeparator, int componentPosition) {
        setValue(component, lineNumber, columnNumber, subComponentSeparator,
                componentPosition);
    }

    /**
     * Set the value at this level and at the sublevel
     * 
     * @param component
     *        a String representation of the component
     * @param lineNumber
     *        the line number
     * @param columnNumber
     *        the column number
     * @param subComponentSeparator
     *        the subcomponent separator character
     * @param componentPosition
     */
    public void setValue(String component, int lineNumber, int columnNumber,
            String subComponentSeparator, int componentPosition) {
        this.subComponentSeparator = subComponentSeparator;
        subComponents = new ArrayList<ESubComponent>();
        value = component;
        this.line = lineNumber;
        this.column = columnNumber;
        this.position = componentPosition;
        this.instanceNumber = 1;
        int subComponentPosition = 1;
        int subComponentColumn = columnNumber;
        // if ("".equals(component)) {
        // ESubComponent esubcomponent = new ESubComponent(component,
        // lineNumber, subComponentColumn);
        // subComponents.add(esubcomponent);
        // subComponentColumn++;
        // } else {
        // System.out.println("Component: " + value + " " + column);
        // Scanner sSubComponent = new Scanner(component);
        // sSubComponent.useDelimiter(subComponentSeparator);
        // while (sSubComponent.hasNext()) {
        String[] subComponentValues = component.split(subComponentSeparator, -1);
        for (String subComponentValue : subComponentValues) {
            // String subComponentValue = sSubComponent.next();
            ESubComponent esubcomponent = new ESubComponent(subComponentValue,
                    lineNumber, subComponentColumn, subComponentPosition);
            subComponents.add(esubcomponent);
            if (subComponentPosition == subComponentValues.length) {
                subComponentColumn += subComponentValue.length();
            } else {
                subComponentColumn += (subComponentValue.length() + 1);
            }
            subComponentPosition++;
        }
        // subComponentColumn++;
        // }
    }

    /**
     * Get the subcomponent at the specified location.
     * 
     * @param subComponentPosition
     * @return an ESubComponent
     */
    public ESubComponent get(int subComponentPosition) {
        ESubComponent esubcomponent = null;
        if (subComponents.size() >= subComponentPosition) {
            esubcomponent = subComponents.get(subComponentPosition - 1);
        }
        return esubcomponent;
    }

    public void transform2Xml(XmlCursor cursor) {
        for (int i = 0; i < subComponents.size(); i++) {
            ESubComponent subComponent = subComponents.get(i);
            cursor.beginElement("SC" + (i + 1));
            cursor.insertAttributeWithValue("value", subComponent.getValue());
            cursor.toParent();
            cursor.toParent();
            cursor.toEndToken();
        }
    }

    /**
     * Get the valued locations (children).
     * 
     * @param profile
     * @param segmentName
     * @param segmentInstanceNumber
     * @param fieldPosition
     * @param fieldInstanceNumber
     * @return a list a valued message location
     */
    public List<ValuedMessageLocation> getLocations(Profile profile,
            String segmentName, int segmentInstanceNumber, int fieldPosition,
            int fieldInstanceNumber) {
        List<ValuedMessageLocation> valuedLocations = new ArrayList<ValuedMessageLocation>();
        for (ESubComponent eSubComponent : subComponents) {
            if (!"".equals(eSubComponent.getValue())) {
                ValuedMessageLocation vml = new ValuedMessageLocation(null,
                        segmentName, segmentInstanceNumber, fieldPosition,
                        fieldInstanceNumber, this.position,
                        eSubComponent.getPosition(), eSubComponent.getValue());
                if (profile == null
                        || (profile != null && vml.isPrimitive(profile))) {
                    valuedLocations.add(vml);
                }
            }
        }
        return valuedLocations;
    }

    @Override
    public boolean isPrimitive() {
        return value.split(subComponentSeparator, -1).length == 1;
    }

    /**
     * Get the last component position
     * 
     * @return the last component position
     */
    public int getSubComponentMax() {
        return subComponents.size();
    }
}
