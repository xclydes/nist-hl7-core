/*
 * NIST Healthcare Core
 * EntityName.java Jun 9, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message.v3;

import gov.nist.healthcare.core.message.Name;

/**
 * This class represents the name of a V3 test agent or application.
 * 
 * @author mccaffrey
 */

public class HL7V3Name implements Name {

    private String applicationName = null;
    private String facilityName = null;

    /**
     * Constructor.
     * 
     * @param appNameRoot
     *        application name
     * @param facNameRoot
     *        facility name
     */
    public HL7V3Name(String appNameRoot, String facNameRoot) {
        applicationName = appNameRoot;
        facilityName = facNameRoot;
        // setName();
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    // @Override
    // protected void setName() {
    // names = new ArrayList<String>();
    // names.add(applicationName);
    // names.add(facilityName);
    // }

    @Override
    public String toString() {
        return applicationName + " / " + facilityName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((applicationName == null) ? 0 : applicationName.hashCode());
        return result;
    }

    /**
     * Compare this object to another to determine equality. We compare the
     * application name of the compared object to the application name of this
     * and the facility name of the compared object to the facility name of
     * this. Please note that for the purposes of this method, an empty String
     * and a null String are considered equal!
     * 
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        // if (!super.equals(obj)) {
        // return false;
        // }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HL7V3Name other = (HL7V3Name) obj;

        String thisApp = null;
        String otherApp = null;

        if (this.applicationName == null || "".equals(this.applicationName)) {
            thisApp = null;
        } else {
            thisApp = new String(this.applicationName);
        }

        if (other.applicationName == null || "".equals(other.applicationName)) {
            otherApp = null;
        } else {
            otherApp = new String(other.applicationName);
        }

        if (thisApp == null) {
            if (otherApp != null) {
                return false;
            }
        } else if (!thisApp.equals(otherApp)) {
            return false;
        }
        return true;
    }

}
