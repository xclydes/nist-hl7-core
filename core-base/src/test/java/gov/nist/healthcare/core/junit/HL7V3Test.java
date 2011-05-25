/*
 * NIST Healthcare Core
 * HL7V3Test.java Oct 7, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.junit;

import gov.nist.healthcare.core.message.v3.HL7V3Message;
import java.io.File;
import java.util.List;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Caroline Rosin (NIST)
 */
public class HL7V3Test extends TestCase {

    private HL7V3Message message;

    @Override
    @BeforeClass
    public void setUp() {
        try {
            message = new HL7V3Message(new File(getClass().getResource(
                    "/ValidV3Message.xml").getFile()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @AfterClass
    public void tearDown() {
        message = null;
    }

    @Test
    public void testGetLocations() {
        List<String> locations = message.getLocations("//*:patientPerson/*:name");
        assertEquals(locations.size(), 2);

        locations = message.getLocations("//*:patientPerson/*:telecom");
        assertEquals(locations.size(), 2);

        locations = message.getLocations("//*:patientPerson/*:addr");
        assertEquals(locations.size(), 3);

        locations = message.getLocations("//*:patientPerson/*:asOtherIDs[@classCode='PAT']");
        assertEquals(locations.size(), 6);

        locations = message.getLocations("//*:patientPerson");
        assertEquals(locations.size(), 21);
    }

}
