/*
 * NIST Healthcare Core
 * HL7V3MessageId.java Jan 20, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message.v3;

import gov.nist.healthcare.core.message.MessageId;

/**
 * This class represents the message id for an HL7V3 message.
 * 
 * @author Sydney Henrard (NIST)
 */
public class HL7V3MessageId implements MessageId {

    private String root;
    private String extension;

    /**
     * Constructor.
     * 
     * @param root
     * @param extension
     */
    public HL7V3MessageId(String root, String extension) {
        this.root = root;
        this.extension = extension;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((extension == null) ? 0 : extension.hashCode());
        result = prime * result + ((root == null) ? 0 : root.hashCode());
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
        HL7V3MessageId other = (HL7V3MessageId) obj;
        if (extension == null) {
            if (other.extension != null) {
                return false;
            }
        } else if (!extension.equals(other.extension)) {
            return false;
        }
        if (root == null) {
            if (other.root != null) {
                return false;
            }
        } else if (!root.equals(other.root)) {
            return false;
        }
        return true;
    }

}
