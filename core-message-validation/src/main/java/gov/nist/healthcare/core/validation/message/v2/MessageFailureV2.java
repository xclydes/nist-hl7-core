/*
 * NIST Healthcare Core
 * MessageFailureV2.java Aug 19, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.v2;

import gov.nist.healthcare.core.Constants.MessageEncoding;
import gov.nist.healthcare.core.profile.Profile;
import gov.nist.healthcare.core.validation.message.MessageFailure;
import gov.nist.healthcare.profile.Component;
import gov.nist.healthcare.profile.Field;
import gov.nist.healthcare.profile.Segment;
import gov.nist.healthcare.profile.SegmentGroup;
import gov.nist.healthcare.profile.SubComponent;
import gov.nist.healthcare.validation.message.hl7.v2.report.HL7V2MessageReport.AssertionList.Assertion.Location;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a failure when validating a V2 message.
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessageFailureV2 extends MessageFailure {

    // private AssertionTypeV2Constants.Enum failureType;
    private final MessageEncoding encoding;

    public MessageFailureV2(MessageEncoding encoding) {
        this.encoding = encoding;
    }

    // public AssertionTypeV2Constants.Enum getFailureType() {
    // return (Enum) failureType;
    // }

    // public void setFailureType(AssertionTypeV2Constants.Enum failureType) {
    // this.failureType = failureType;
    // }

    public MessageEncoding getEncoding() {
        return encoding;
    }

    /**
     * Get the Location for the Report
     * 
     * @param profile
     * @return a Location object
     */
    public Location getLocationForReport(Profile profile) {
        Location location = Location.Factory.newInstance();
        SegmentGroup sg = null;
        Segment s = null;
        Field f = null;
        Component c = null;
        SubComponent sc = null;
        int fieldSeqNum = -1, componentSeqNum = -1, subcomponentSeqNum = -1;
        int fieldNumber = -1;
        gov.nist.healthcare.profile.ProfileElement peLoc = null;
        if (encoding == MessageEncoding.V2_XML && path != null) {
            StringBuffer sb1 = new StringBuffer();
            Pattern p = Pattern.compile("([A-Z_\\d\\.]+\\.?\\d*)\\[(\\d+)\\]");
            Matcher m = null;
            String[] tokens = path.split("/");
            int j = 0;
            for (int i = 0; i < tokens.length; i++) {
                m = p.matcher(tokens[i]);
                if (m.matches()) {
                    String name = m.group(1);
                    int number = Integer.parseInt(m.group(2));
                    if (j == 0) {
                        j = 1;
                    } else if (j == 1) {
                        // Segment
                        if (name.length() == 3) {
                            sb1.append("/").append(name).append("[").append(
                                    number).append("]");
                            j = 2;
                        } else {
                            int idxDot = name.indexOf(".");
                            if (idxDot != -1) {
                                name = name.substring(idxDot + 1);
                                sb1.append("/.").append(name);
                            }
                        }
                    } else if (j == 2) {
                        int idxDot = name.indexOf(".");
                        if (idxDot != -1) {
                            fieldSeqNum = Integer.parseInt(name.substring(idxDot + 1));
                            fieldNumber = number;
                            sb1.append(".").append(fieldSeqNum).append("[").append(
                                    fieldNumber).append("]");
                            j = 3;
                        }
                    } else if (j == 3) {
                        int idxDot = name.indexOf(".");
                        if (idxDot != -1) {
                            componentSeqNum = Integer.parseInt(name.substring(idxDot + 1));
                            sb1.append(".").append(componentSeqNum);
                            j = 4;
                        }
                    } else if (j == 4) {
                        int idxDot = name.indexOf(".");
                        if (idxDot != -1) {
                            subcomponentSeqNum = Integer.parseInt(name.substring(idxDot + 1));
                            sb1.append(".").append(subcomponentSeqNum);
                            j = 5;
                        }
                    }
                }
            }
            List<String> names = profile.getNames(sb1.toString());
            if (names != null) {
                if (names.size() > 4) {
                    peLoc = location.addNewProfileElement();
                    for (int i = 4; i < names.size(); i++) {
                        if (sg == null) {
                            sg = peLoc.addNewSegmentGroup();
                        } else {
                            sg = sg.addNewSegmentGroup();
                        }
                        sg.setName(names.get(i));
                    }
                }
                String segmentName = names.get(0);
                if (segmentName != null) {
                    if (peLoc == null) {
                        peLoc = location.addNewProfileElement();
                    }
                    s = peLoc.addNewSegment();
                    s.setName(segmentName);
                    String fieldName = names.get(1);
                    if (fieldName != null) {
                        f = s.addNewField();
                        f.setName(fieldName);
                        String componentName = names.get(2);
                        if (componentName != null) {
                            c = f.addNewComponent();
                            c.setName(componentName);
                            String subComponentName = names.get(3);
                            if (subComponentName != null) {
                                sc = c.addNewSubComponent();
                                sc.setName(subComponentName);
                            }
                        }
                    }
                }
            }
        } else if (encoding == MessageEncoding.V2_ER7 && path != null) {
            List<String> names = profile.getNames(path);
            if (names != null) {
                String segmentName = names.get(0);
                if (segmentName != null) {
                    peLoc = location.addNewProfileElement();
                    s = peLoc.addNewSegment();
                    s.setName(segmentName);
                    String fieldName = names.get(1);
                    if (fieldName != null) {
                        f = s.addNewField();
                        f.setName(fieldName);
                        String componentName = names.get(2);
                        if (componentName != null) {
                            c = f.addNewComponent();
                            c.setName(componentName);
                            String subComponentName = names.get(3);
                            if (subComponentName != null) {
                                sc = c.addNewSubComponent();
                                sc.setName(subComponentName);
                            }
                        }
                    }
                }
            }
        }
        if (line != -1) {
            location.setLine(line);
        }
        if (column != -1) {
            location.setColumn(column);
        }
        if (path != null) {
            location.setPath(path);
        }
        return location;
    }

    /**
     * Get the Location for the Report
     * 
     * @return a Location object
     */
    public Location getLocationForReport() {
        Location location = Location.Factory.newInstance();
        if (line != -1) {
            location.setLine(line);
        }
        if (column != -1) {
            location.setColumn(column);
        }
        if (path != null) {
            location.setPath(path);
        }

        return location;
    }

}
