/*
 * NIST Healthcare Core
 * PField.java Jul 13, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.profile;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;

/**
 * This class represents a field in a profile.
 * 
 * @author Sydney Henrard (NIST)
 */
public class PField extends PElement {

    private final List<PComponent> components;

    /**
     * Constructor
     * 
     * @param cursor
     */
    public PField(XmlCursor cursor) {
        cursor.push();
        components = new ArrayList<PComponent>();
        setName(cursor.getAttributeText(QName.valueOf("Name")));
        cursor.toChild(QName.valueOf("Component"));
        do {
            PComponent component = new PComponent(cursor);
            components.add(component);
        } while (cursor.toNextSibling(QName.valueOf("Component")));
        cursor.pop();
    }

    /**
     * Get the ith component
     * 
     * @param idx
     * @return a PComponent; null otherwise
     */
    public PComponent getComponent(int idx) {
        PComponent pComponent = null;
        if (idx <= components.size()) {
            pComponent = components.get(idx - 1);
        }
        return pComponent;
    }

}
