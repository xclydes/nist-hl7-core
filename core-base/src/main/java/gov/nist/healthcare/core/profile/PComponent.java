/*
 * NIST Healthcare Core
 * PComponent.java Jul 13, 2010
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
 * This class represents a component in a profile.
 * 
 * @author Sydney Henrard (NIST)
 */
public class PComponent extends PElement {

    private final List<PSubComponent> subComponents;

    /**
     * Constructor
     * 
     * @param cursor
     */
    public PComponent(XmlCursor cursor) {
        cursor.push();
        subComponents = new ArrayList<PSubComponent>();
        setName(cursor.getAttributeText(QName.valueOf("Name")));
        cursor.toChild(QName.valueOf("SubComponent"));
        do {
            PSubComponent field = new PSubComponent(cursor);
            subComponents.add(field);
        } while (cursor.toNextSibling(QName.valueOf("SubComponent")));
        cursor.pop();
    }

    /**
     * Get the ith subcomponent
     * 
     * @param idx
     * @return a PSubComponent; null otherwise
     */
    public PSubComponent getSubComponent(int idx) {
        PSubComponent pSubComponent = null;
        if (idx <= subComponents.size()) {
            pSubComponent = subComponents.get(idx - 1);
        }
        return pSubComponent;
    }

}
