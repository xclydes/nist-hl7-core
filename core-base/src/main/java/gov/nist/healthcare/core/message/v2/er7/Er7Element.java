/*
 * NIST Healthcare Core
 * Er7Element.java Nov 18, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message.v2.er7;

/**
 * This abstract class represents an ER7 element.
 * 
 * @author Sydney Henrard (NIST)
 */
public abstract class Er7Element {

    protected int line;
    protected int column;
    protected String value;
    protected int position;
    protected int instanceNumber;

    public int getLine() {
        return line;
    }

    // public void setLine(int line) {
    // this.line = line;
    // }

    public int getColumn() {
        return column;
    }

    // public void setColumn(int column) {
    // this.column = column;
    // }

    public String getValue() {
        return value;
    }

    public int getPosition() {
        return position;
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }

    public abstract boolean isPrimitive();
}
