/*
 * NIST Healthcare Core
 * EntityName.java Jun 9, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message.v2;

import gov.nist.healthcare.core.datatypes.v2.HD;
import gov.nist.healthcare.core.message.Name;

/**
 * This class represents the name of a test agent or application consistently
 * with the application and facility names used in the MSH segment.
 * 
 * @author Leonard Gebase (NIST)
 */
public class HL7V2Name implements Name {

    private HD applicationName = null;
    private HD facilityName = null;

    /**
     * Constructor.
     * 
     * @param appName
     *        applicaton name used in MSH segment.
     * @param facName
     *        facility name used in MSH segment.
     */
    public HL7V2Name(HD appName, HD facName) {
        applicationName = appName;
        facilityName = facName;
        // setName();
    }

    public HD getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(HD applicationName) {
        this.applicationName = applicationName;
    }

    public HD getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(HD facilityName) {
        this.facilityName = facilityName;
    }

    // @Override
    // protected void setName() {
    // names = new ArrayList<String>();
    // names.add(applicationName.getNamespaceId());
    // names.add(applicationName.getUniversalId());
    // names.add(applicationName.getUniversalIdType());
    // names.add(facilityName.getNamespaceId());
    // names.add(facilityName.getUniversalId());
    // names.add(facilityName.getUniversalIdType());
    // }
    @Override
    public String toString() {
        return applicationName.toString() + " / " + facilityName.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((applicationName == null) ? 0 : applicationName.hashCode());
        result = prime * result
                + ((facilityName == null) ? 0 : facilityName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        // if (!super.equals(obj)) {
        // return false;
        // }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HL7V2Name other = (HL7V2Name) obj;
        if (applicationName == null) {
            if (other.applicationName != null) {
                return false;
            }
        } else if (!applicationName.equals(other.applicationName)) {
            return false;
        }
        if (facilityName == null) {
            if (other.facilityName != null) {
                return false;
            }
        } else if (!facilityName.equals(other.facilityName)) {
            return false;
        }
        return true;
    }

}
