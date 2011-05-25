/*
 * NIST Healthcare Core
 * State.java Sep 12, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.structure.v2;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlLineNumber;
import org.apache.xmlbeans.XmlObject;

/**
 * This class represents a State in a finite state machine
 * 
 * @author Sydney Henrard (NIST)
 */
public class State {

    private String name;
    private XmlObject object;

    /**
     * Constructor
     * 
     * @param name
     * @param object
     */
    public State(String name, XmlObject object) {
        this.name = name;
        this.object = object;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public XmlObject getObject() {
        return object;
    }

    public void setObject(XmlObject object) {
        this.object = object;
    }

    public String getQName() {
        XmlCursor cursor = object.newCursor();
        XmlLineNumber bm = (XmlLineNumber) cursor.getBookmark(XmlLineNumber.class);
        cursor = null;
        StringBuffer sb = new StringBuffer();
        sb.append("[").append(bm.getLine()).append(",").append(bm.getColumn()).append(
                "] ").append(name);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((object == null) ? 0 : object.hashCode());
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
        State other = (State) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (object == null) {
            if (other.object != null) {
                return false;
            }
        } else if (!object.equals(other.object)) {
            return false;
        }
        return true;
    }

}
