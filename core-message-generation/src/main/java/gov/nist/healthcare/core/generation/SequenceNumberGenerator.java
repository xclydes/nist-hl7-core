/*
 * NIST Healthcare Core
 * SequenceNumberGenerator.java May 5, 2008
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.generation;

import gov.nist.healthcare.core.Constants;
import gov.nist.healthcare.core.profile.Profile;
import gov.nist.healthcare.core.profile.ProfileElement;
import gov.nist.healthcare.generation.message.SequenceNumbersDocument;
import gov.nist.healthcare.generation.message.SequenceNumbersDocument.SequenceNumbers.Segment;
import gov.nist.healthcare.generation.message.SequenceNumbersDocument.SequenceNumbers.Segment.Field;
import gov.nist.healthcare.generation.message.XsltSequenceNumbersDocument;
import gov.nist.healthcare.generation.message.XsltSequenceNumbersDocument.XsltSequenceNumbers;
import gov.nist.healthcare.generation.message.XsltSequenceNumbersDocument.XsltSequenceNumbers.FieldElement;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * This class generates a sequence number file for a Profile
 * 
 * @author Sydney Henrard (NIST)
 */
public class SequenceNumberGenerator {

    /**
     * Create a sequence number file for a specific profile
     * 
     * @param aProfile
     * @return a File
     * @throws IOException
     * @throws XmlException
     */
    public File getSequenceNumberFile(Profile aProfile) throws IOException,
            XmlException {
        // Load the sequence numbers file for the profile version
        SequenceNumbersDocument snd = null;
        snd = SequenceNumbersDocument.Factory.parse(SequenceNumberGenerator.class.getResourceAsStream(Constants.getSequenceNumbers(aProfile.getHl7VersionAsString())));
        Iterator<Segment> itSegment = null;
        Iterator<Field> itField = null;
        HashMap<String, ArrayList<FieldPosition>> h = new HashMap<String, ArrayList<FieldPosition>>();
        // Parse the profile
        XmlObject[] segments = aProfile.getDocument().selectPath(
                "//Segment[@Usage = 'R' or @Usage = 'RE' or @Usage = 'C' or @Usage = 'CE' or @Usage = 'O' or @Usage = 'X']");
        int k = 0;
        for (int i = 0; i < segments.length; i++) {
            XmlCursor segCur = segments[i].newCursor();
            String segName = segCur.getAttributeText(QName.valueOf("Name"));
            ArrayList<FieldPosition> al = h.get(segName);
            if (al == null) {
                al = new ArrayList<FieldPosition>();
                h.put(segName, al);
            }
            itSegment = snd.getSequenceNumbers().getSegmentList().iterator();
            boolean endSegment = false;
            Segment s = null;
            while (itSegment.hasNext() && !endSegment) {
                s = itSegment.next();
                if (s.getName().equals(segName)) {
                    endSegment = true;
                }
            }
            String fieldName = "";
            StringBuffer sb = new StringBuffer();
            sb.delete(0, sb.length());
            sb.append("*:Field[@Usage = 'R' or @Usage = 'RE' or @Usage = 'C' or @Usage = 'CE' or @Usage = 'O' or @Usage = 'X']");
            XmlObject[] fields = segCur.getObject().selectPath(sb.toString());
            for (int j = 0; j < fields.length; j++) {
                XmlCursor fieldCur = fields[j].newCursor();
                fieldName = fieldCur.getAttributeText(QName.valueOf("Name"));
                // The field was found in the file
                int seqNum = -1;
                if (endSegment) {
                    itField = s.getFieldList().iterator();
                    boolean endField = false;
                    Field f = null;
                    while (itField.hasNext() && !endField) {
                        f = itField.next();
                        if (f.getName().equals(fieldName)) {
                            seqNum = f.getPosition();
                            endField = true;
                        }
                    }
                    if (seqNum == -1) {
                        ProfileElement pe = new ProfileElement(null, segName,
                                fieldName, "", "");
                        pe.setXmlObject(fieldCur.getObject());
                        seqNum = pe.getSequenceNumber(aProfile.getHl7VersionAsString());
                    }
                } else {
                    ProfileElement pe = new ProfileElement(null, segName,
                            fieldName, "", "");
                    pe.setXmlObject(fieldCur.getObject());
                    seqNum = pe.getSequenceNumber(aProfile.getHl7VersionAsString());
                }
                FieldPosition fp = new FieldPosition(fieldName, seqNum);
                if (!al.contains(fp)) {
                    al.add(fp);
                }
                k++;
            }
        }
        // System.out.println(k);
        // Create the file
        File sequenceNumberFile = null;
        XsltSequenceNumbersDocument snDoc = XsltSequenceNumbersDocument.Factory.newInstance();
        XsltSequenceNumbers sn = snDoc.addNewXsltSequenceNumbers();
        Iterator<String> it = h.keySet().iterator();
        while (it.hasNext()) {
            String segName = it.next();
            ArrayList<FieldPosition> al = h.get(segName);
            for (int i = 0; i < al.size(); i++) {
                FieldPosition fp = al.get(i);
                FieldElement fe = sn.addNewFieldElement();
                fe.setSegmentName(segName);
                fe.setFieldName(fp.getFieldName());
                fe.setPosition(fp.getFieldPosition());
            }
        }
        // Validate
        if (snDoc.validate()) {
            sequenceNumberFile = File.createTempFile("SequenceNumber", ".xml");
            sequenceNumberFile.deleteOnExit();
            snDoc.save(sequenceNumberFile,
                    new XmlOptions().setSavePrettyPrint());
        } else {
            throw new IOException(
                    "The generated sequence numbers file is not valid");
        }
        return sequenceNumberFile;
    }

    class FieldPosition {

        private final String fieldName;
        private final int fieldPosition;

        FieldPosition(String fieldName, int fieldPosition) {
            this.fieldName = fieldName;
            this.fieldPosition = fieldPosition;
        }

        public String getFieldName() {
            return fieldName;
        }

        public int getFieldPosition() {
            return fieldPosition;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((fieldName == null) ? 0 : fieldName.hashCode());
            result = prime * result + fieldPosition;
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
            final FieldPosition other = (FieldPosition) obj;
            if (fieldName == null) {
                if (other.fieldName != null) {
                    return false;
                }
            } else if (!fieldName.equals(other.fieldName)) {
                return false;
            }
            if (fieldPosition != other.fieldPosition) {
                return false;
            }
            return true;
        }
    }

}
