/*
 * NIST Healthcare Core
 * MessageElement.java Mar 3, 2008
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.datatypes.v2;

/**
 * This class is for representing a value of type HD (Hierarchic Designator --
 * Chapter 2, Data Types).
 * 
 * @author Leonard Gebase (NIST)
 */
public class HD {
    private String namespaceId;
    private String universalId;
    private String universalIdType;

    /**
     * For serialization
     */
    protected HD() {
    }

    /**
     * Constructor.
     * 
     * @param nid
     *        namespace id -- may be null
     * @param uid
     *        universal id -- may be null
     * @param uidType
     *        universal id type -- may be null
     */
    public HD(String nid, String uid, String uidType) {
        this.namespaceId = nid;
        this.universalId = uid;
        this.universalIdType = uidType;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getUniversalId() {
        return universalId;
    }

    public void setUniversalId(String universalId) {
        this.universalId = universalId;
    }

    public String getUniversalIdType() {
        return universalIdType;
    }

    public void setUniversalIdType(String universalIdType) {
        this.universalIdType = universalIdType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((namespaceId == null) ? 0 : namespaceId.hashCode());
        result = prime * result
                + ((universalId == null) ? 0 : universalId.hashCode());
        result = prime * result
                + ((universalIdType == null) ? 0 : universalIdType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HD other = (HD) obj;
        if (namespaceId == null || "".equals(namespaceId)) {
            if (other.namespaceId != null && !"".equals(other.namespaceId)) {
                return false;
            }
        } else if (!namespaceId.equalsIgnoreCase(other.namespaceId)) {
            return false;
        }
        if (universalId == null || "".equals(universalId)) {
            if (other.universalId != null && !"".equals(other.universalId)) {
                return false;
            }
        } else if (!universalId.equalsIgnoreCase(other.universalId)) {
            return false;
        }
        if (universalIdType == null || "".equals(universalIdType)) {
            if (other.universalIdType != null
                    && !"".equals(other.universalIdType)) {
                return false;
            }
        } else if (!universalIdType.equalsIgnoreCase(other.universalIdType)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String nsid = namespaceId == null || "".equals(namespaceId) ? ""
                : namespaceId;
        String uid = universalId == null || "".equals(universalId) ? "" : " "
                + universalId;
        String uidType = universalIdType == null || "".equals(universalIdType) ? ""
                : " " + universalIdType;
        return nsid + uid + uidType;
        // return namespaceId + " " + universalId + " " + universalIdType;
    }

}
