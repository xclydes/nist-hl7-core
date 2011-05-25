/*
 * NIST Healthcare Core
 * ProfileElementTest.java Jan 22, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.junit;

import gov.nist.healthcare.core.Constants.ElementType;
import gov.nist.healthcare.core.profile.Profile;
import gov.nist.healthcare.core.profile.ProfileElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestCase;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This JUnit tests the ProfileElement class
 * 
 * @author Sydney Henrard (NIST)
 */
public class ProfileElementTest extends TestCase {

    private static Profile profile;
    private static ProfileElement field;
    private static ProfileElement component;
    private static ProfileElement subcomponent;
    private static ProfileElement peWithGroup;

    @Override
    @BeforeClass
    public void setUp() {
        try {
            profile = new Profile(
                    getClass().getResourceAsStream("/Profile.xml"));
            field = new ProfileElement(null, "PID", "Administrative Sex", null,
                    null);
            field.setXmlObject(profile);
            component = new ProfileElement(null, "PID", "Patient Address",
                    "City", null);
            component.setXmlObject(profile);
            subcomponent = new ProfileElement(null, "PID", "Patient Name",
                    "Family Name", "Surname From Partner/Spouse");
            subcomponent.setXmlObject(profile);
            List<String> segmentGroups = new ArrayList<String>();
            segmentGroups.add("PROCEDURE");
            peWithGroup = new ProfileElement(segmentGroups, "ROL",
                    "Role Person", "Family Name", "Surname");
            peWithGroup.setXmlObject(profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @AfterClass
    public void tearDown() {
        profile = null;
    }

    @Test
    public void testConstructors() {
        List<String> segmentGroups = new ArrayList<String>();
        segmentGroups.add("PROCEDURE");
        ProfileElement pe = new ProfileElement(segmentGroups, "ROL",
                "Role Person", "Family Name", "Surname");
        assertEquals(pe.getSegmentGroups().get(0), "PROCEDURE");
        assertEquals(pe.getSegment(), "ROL");
        assertEquals(pe.getField(), "Role Person");
        assertEquals(pe.getComponent(), "Family Name");
        assertEquals(pe.getSubcomponent(), "Surname");
        assertEquals(pe.getType(), ElementType.SUBCOMPONENT);

        XmlObject[] rs = profile.getDocument().selectPath(
                "//SegGroup[@Name='PROCEDURE']/Segment[@Name='ROL']/Field[@Name='Role Person']/Component[@Name='Family Name']/SubComponent[@Name='Surname']");
        if (rs.length == 1) {
            pe = new ProfileElement(rs[0]);
            assertEquals(pe.getSegmentGroups().get(0), "PROCEDURE");
            assertEquals(pe.getSegment(), "ROL");
            assertEquals(pe.getField(), "Role Person");
            assertEquals(pe.getComponent(), "Family Name");
            assertEquals(pe.getSubcomponent(), "Surname");
            assertEquals(pe.getType(), ElementType.SUBCOMPONENT);
        }
    }

    @Test
    public void testGetSequenceNumber() throws XmlException, IOException {
        String version = profile.getHl7VersionAsString();
        assertEquals(field.getSequenceNumber(version), 8);
        assertEquals(component.getSequenceNumber(version), 3);
        assertEquals(subcomponent.getSequenceNumber(version), 5);
    }

    @Test
    public void testGetXPath() {
        ProfileElement pe = null;
        XmlObject[] rs = null;
        String xpath = null;
        ProfileElement peFromXPath = null;

        pe = field;
        xpath = pe.getXPath();
        rs = profile.getDocument().selectPath(xpath);
        assertEquals(rs.length, 1);
        peFromXPath = new ProfileElement(rs[0]);
        assertEquals(pe, peFromXPath);

        pe = component;
        xpath = pe.getXPath();
        rs = profile.getDocument().selectPath(xpath);
        assertEquals(rs.length, 1);
        peFromXPath = new ProfileElement(rs[0]);
        assertEquals(pe, peFromXPath);

        pe = subcomponent;
        xpath = pe.getXPath();
        rs = profile.getDocument().selectPath(xpath);
        assertEquals(rs.length, 1);
        peFromXPath = new ProfileElement(rs[0]);
        assertEquals(pe, peFromXPath);

        pe = peWithGroup;
        xpath = pe.getXPath();
        rs = profile.getDocument().selectPath(xpath);
        assertEquals(rs.length, 1);
        peFromXPath = new ProfileElement(rs[0]);
        assertEquals(pe, peFromXPath);
    }

    @Test
    public void testGetXPathInMessage() throws XmlException, IOException {
        assertEquals(field.getXPathInMessage(profile),
                "/*:ADT_A01/*:PID/*:PID.8");
        assertEquals(component.getXPathInMessage(profile),
                "/*:ADT_A01/*:PID/*:PID.11/*:XAD.3");
        assertEquals(subcomponent.getXPathInMessage(profile),
                "/*:ADT_A01/*:PID/*:PID.5/*:XPN.1/*:FN.5");
        assertEquals(peWithGroup.getXPathInMessage(profile),
                "/*:ADT_A01/*:ADT_A01.PROCEDURE/*:ROL/*:ROL.4/*:XCN.2/*:FN.1");
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(ProfileElementTest.class);
    }

}
