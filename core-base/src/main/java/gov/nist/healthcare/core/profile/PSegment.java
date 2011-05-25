/*
 * NIST Healthcare Core
 * PSegment.java Jul 13, 2010
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
 * This class represents a segment in a profile.
 * 
 * @author Sydney Henrard (NIST)
 */
public class PSegment extends PElement {

    private final List<PField> fields;

    /**
     * Constructor
     * 
     * @param cursor
     */
    public PSegment(XmlCursor cursor) {
        cursor.push();
        fields = new ArrayList<PField>();
        setName(cursor.getAttributeText(QName.valueOf("Name")));
        cursor.toChild(QName.valueOf("Field"));
        do {
            PField field = new PField(cursor);
            fields.add(field);
        } while (cursor.toNextSibling(QName.valueOf("Field")));
        cursor.pop();
    }

    /**
     * Get the ith field
     * 
     * @param idx
     * @return a PField; null otherwise
     */
    public PField getField(int idx) {
        PField pField = null;
        if (idx <= fields.size()) {
            pField = fields.get(idx - 1);
        }
        return pField;
    }

}
