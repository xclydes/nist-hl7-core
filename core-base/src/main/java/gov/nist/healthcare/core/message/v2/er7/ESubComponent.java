/*
 * NIST Healthcare Core
 * ESubComponent.java Nov 18, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message.v2.er7;

/**
 * This class represents an ER7 subcomponent.
 * 
 * @author Sydney Henrard (NIST)
 */
public class ESubComponent extends Er7Element {

    /**
     * Constructor.
     * 
     * @param subComponent
     *        a String representation of the subcomponent
     * @param lineNumber
     *        the line number
     * @param columnNumber
     *        the column number
     * @param subComponentPosition
     */
    public ESubComponent(String subComponent, int lineNumber, int columnNumber,
            int subComponentPosition) {
        setValue(subComponent, lineNumber, columnNumber, subComponentPosition);
    }

    /**
     * Set the value at this level and at the sublevel
     * 
     * @param subComponent
     *        a String representation of the subcomponent
     * @param lineNumber
     *        the line number
     * @param columnNumber
     *        the column number
     * @param subComponentPosition
     */
    public void setValue(String subComponent, int lineNumber, int columnNumber,
            int subComponentPosition) {
        value = subComponent;
        this.line = lineNumber;
        this.column = columnNumber;
        this.position = subComponentPosition;
        this.instanceNumber = 1;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }
}
