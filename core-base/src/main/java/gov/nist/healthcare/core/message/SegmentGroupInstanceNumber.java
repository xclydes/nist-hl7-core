/*
 * NIST Healthcare Core
 * SegmentGroupInstanceNumber.java Sep 11, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message;

/**
 * This class represents a name/instance number pair for segment groups.
 * 
 * @author Sydney Henrard (NIST)
 */
public class SegmentGroupInstanceNumber {

    private String name;
    private int instanceNumber = 1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }

    public void setInstanceNumber(int instanceNumber) {
        this.instanceNumber = instanceNumber;
    }

}
