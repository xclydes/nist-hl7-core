/*
 * NIST Healthcare Core
 * MessageFailureV3.java Aug 19, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.v3;

import gov.nist.healthcare.core.validation.message.MessageFailure;
import gov.nist.healthcare.validation.message.hl7.v3.report.HL7V3MessageReport.AssertionList.Assertion.Location;

/**
 * This class represents a failure when validating a V3 message.
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageFailureV3 extends MessageFailure {

    // private AssertionTypeV3Constants.Enum failureType;

    // public AssertionTypeV3Constants.Enum getFailureType() {
    // return failureType;
    // }
    //
    // public void setFailureType(AssertionTypeV3Constants.Enum failureType) {
    // this.failureType = failureType;
    // }

    /**
     * Get the Location for the Report
     * 
     * @return a Location object
     */
    public Location getLocationForReport() {
        Location location = Location.Factory.newInstance();
        if (line != -1) {
            location.setLine(line);
        }
        if (column != -1) {
            location.setColumn(column);
        }
        if (path != null) {
            location.setXPath(path);
        }

        return location;
    }

}
