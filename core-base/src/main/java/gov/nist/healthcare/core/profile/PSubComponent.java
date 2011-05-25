/*
 * NIST Healthcare Core
 * PSubComponent.java Jul 13, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.profile;

import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;

/**
 * This class represents a subcomponent in a profile.
 * 
 * @author Sydney Henrard (NIST)
 */
public class PSubComponent extends PElement {

    /**
     * Constructor
     * 
     * @param cursor
     */
    public PSubComponent(XmlCursor cursor) {
        setName(cursor.getAttributeText(QName.valueOf("Name")));
    }

}
