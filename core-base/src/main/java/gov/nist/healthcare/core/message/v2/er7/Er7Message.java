/*
 * NIST Healthcare Core
 * Er7Message.java Jun 9, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.message.v2.er7;

import gov.nist.healthcare.core.Constants.ElementType;
import gov.nist.healthcare.core.Constants.MessageEncoding;
import gov.nist.healthcare.core.MalformedMessageException;
import gov.nist.healthcare.core.message.MessageId;
import gov.nist.healthcare.core.message.MessageLocation;
import gov.nist.healthcare.core.message.Name;
import gov.nist.healthcare.core.message.ValuedMessageLocation;
import gov.nist.healthcare.core.message.v2.HL7V2MessageId;
import gov.nist.healthcare.core.message.v2.HL7V2MessageImpl;
import gov.nist.healthcare.core.message.v2.HL7V2Name;
import gov.nist.healthcare.core.profile.Profile;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents an ER7 V2 message.
 * 
 * @author Sydney Henrard (NIST)
 */
public class Er7Message extends HL7V2MessageImpl {

    private File messageFile;
    protected HashMap<Integer, String> er7Mapping;
    protected Map<String, List<ESegment>> segments;

    /* separators and escape -- we need 2 encodings of the separators */
    private String fieldSeparator = "";
    private String componentSeparator = "";
    private String repetitionSeparator = "";
    private String subComponentSeparator = "";
    private String fieldSeparatorChar = "";
    private String componentSeparatorChar = "";
    private String repetitionSeparatorChar = "";
    private String subComponentSeparatorChar = "";
    private String escapeChar = "";

    /**
     * Default Constructor.
     */
    protected Er7Message() {
    }

    /**
     * Create a Message using a File object using the platform's default
     * charset.
     * 
     * @param messageFile
     * @throws MalformedMessageException
     */
    public Er7Message(File messageFile) throws MalformedMessageException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(messageFile));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\r");
            }
            this.messageFile = messageFile;
            br.close();
            setEr7Mapping(sb.toString());
            loadMessage();
            parseMessage();
        } catch (Exception e) {
            throw new MalformedMessageException(e.getMessage());
        }
    }

    /**
     * Create a Message using a File object using a specific encoding.
     * 
     * @param messageFile
     * @param encoding
     * @throws MalformedMessageException
     */
    public Er7Message(File messageFile, String encoding)
            throws MalformedMessageException {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(messageFile), encoding));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\r");
            }
            this.messageFile = messageFile;
            br.close();
            String encodedString = new String(sb.toString().getBytes(encoding),
                    encoding);
            setEr7Mapping(encodedString);
            loadMessage();
            parseMessage();
        } catch (Exception e) {
            throw new MalformedMessageException(e.getMessage());
        }
    }

    /**
     * Create a Message using a String object.
     * 
     * @param messageString
     * @throws MalformedMessageException
     */
    public Er7Message(String messageString) throws MalformedMessageException {
        try {
            setEr7Mapping(messageString);
            loadMessage();
            parseMessage();
        } catch (Exception e) {
            throw new MalformedMessageException(e.getMessage());
        }
    }

    /**
     * Sets the ER7 mapping : line number - ER7 segment.
     * 
     * @param messageString
     *        the message content as a String
     * @throws MalformedMessageException
     */
    protected void setEr7Mapping(String messageString)
            throws MalformedMessageException {
        BufferedReader er7 = null;
        er7Mapping = new HashMap<Integer, String>();
        er7 = new BufferedReader(new StringReader(messageString));
        String tmp;
        int lineNr = 0;
        try {
            while (((tmp = er7.readLine()) != null)) {
                lineNr++;
                er7Mapping.put(lineNr, tmp);
            }
        } catch (IOException e) {
            throw new MalformedMessageException(e.getMessage());
        }

        setDelimiters();
    }

    /**
     * Sets the delimiters.
     * 
     * @throws MalformedMessageException
     */
    private void setDelimiters() throws MalformedMessageException {
        String mshSegment = er7Mapping.get(1);
        if ("MSH".equals(mshSegment.substring(0, 3)) && mshSegment.length() > 8) {
            fieldSeparatorChar = mshSegment.substring(3, 4);
            componentSeparatorChar = mshSegment.substring(4, 5);
            repetitionSeparatorChar = mshSegment.substring(5, 6);
            escapeChar = mshSegment.substring(6, 7);
            subComponentSeparatorChar = mshSegment.substring(7, 8);
            fieldSeparator = Pattern.quote(fieldSeparatorChar);
            componentSeparator = Pattern.quote(componentSeparatorChar);
            repetitionSeparator = Pattern.quote(repetitionSeparatorChar);
            subComponentSeparator = Pattern.quote(subComponentSeparatorChar);
        } else {
            throw new MalformedMessageException(
                    "MSH is segment is missing or does not contain the separators.");
        }
        // MessageLocation mshSeg = new MessageLocation("MSH", 1);
        // String msh = getValue(mshSeg);
        // if (msh == null) {
        // throw new MalformedMessageException(
        // "MSH.1 is missing in the message");
        // }
        // fieldSeparatorChar = filterValue(String.valueOf(msh.charAt(3)));
        // fieldSeparator = Pattern.quote(fieldSeparatorChar);
        //
        // MessageLocation msh2Loc = new MessageLocation("MSH", 1, 2, 1);
        // String msh2 = getValue(msh2Loc);
        // if (msh2 == null) {
        // throw new MalformedMessageException(
        // "MSH.2 is missing in the message");
        // }
        // componentSeparatorChar = filterValue(String.valueOf(msh2.charAt(0)));
        // repetitionSeparatorChar =
        // filterValue(String.valueOf(msh2.charAt(1)));
        // escapeChar = filterValue(String.valueOf(msh2.charAt(2)));
        // subComponentSeparatorChar =
        // filterValue(String.valueOf(msh2.charAt(3)));
        // componentSeparator = Pattern.quote(componentSeparatorChar);
        // repetitionSeparator = Pattern.quote(repetitionSeparatorChar);
        // subComponentSeparator = Pattern.quote(subComponentSeparatorChar);
    }

    /**
     * Load a message into memory.
     * 
     * @param messageString
     *        the message as a String
     */
    private void loadMessage() {
        segments = new HashMap<String, List<ESegment>>();
        for (int i = 0; i < er7Mapping.keySet().size(); i++) {
            // for (int lineNumber : er7Mapping.keySet()) {
            int lineNumber = i + 1;
            String segment = er7Mapping.get(lineNumber);
            if (segment.length() >= 3) {
                String segmentName = segment.substring(0, 3);
                List<ESegment> listSegment = segments.get(segment.substring(0,
                        3));
                int segmentCount = listSegment == null ? 0 : listSegment.size();
                ESegment eSegment = new ESegment(segment, lineNumber,
                        fieldSeparator, repetitionSeparator,
                        componentSeparator, subComponentSeparator,
                        segmentCount + 1);
                if (listSegment == null) {
                    listSegment = new ArrayList<ESegment>();
                }
                listSegment.add(eSegment);
                segments.put(segmentName, listSegment);
            }
        }
    }

    // /**
    // * Returns an empty string if the parameter is null.
    // *
    // * @param value
    // * @return empty string or the value
    // */
    // private String filterValue(String value) {
    // return value == null ? "" : value;
    // }
    public String getFieldSeparator() {
        return fieldSeparator;
    }

    public void setFieldSeparator(String fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    public String getComponentSeparator() {
        return componentSeparator;
    }

    public void setComponentSeparator(String componentSeparator) {
        this.componentSeparator = componentSeparator;
    }

    public String getRepetitionSeparator() {
        return repetitionSeparator;
    }

    public void setRepetitionSeparator(String repetitionSeparator) {
        this.repetitionSeparator = repetitionSeparator;
    }

    public String getSubComponentSeparator() {
        return subComponentSeparator;
    }

    public void setSubComponentSeparator(String subComponentSeparator) {
        this.subComponentSeparator = subComponentSeparator;
    }

    public String getFieldSeparatorChar() {
        return fieldSeparatorChar;
    }

    public void setFieldSeparatorChar(String fieldSeparatorChar) {
        this.fieldSeparatorChar = fieldSeparatorChar;
    }

    public String getComponentSeparatorChar() {
        return componentSeparatorChar;
    }

    public void setComponentSeparatorChar(String componentSeparatorChar) {
        this.componentSeparatorChar = componentSeparatorChar;
    }

    public String getRepetitionSeparatorChar() {
        return repetitionSeparatorChar;
    }

    public void setRepetitionSeparatorChar(String repetitionSeparatorChar) {
        this.repetitionSeparatorChar = repetitionSeparatorChar;
    }

    public String getSubComponentSeparatorChar() {
        return subComponentSeparatorChar;
    }

    public void setSubComponentSeparatorChar(String subComponentSeparatorChar) {
        this.subComponentSeparatorChar = subComponentSeparatorChar;
    }

    public String getEscapeChar() {
        return escapeChar;
    }

    public void setEscapeChar(String escapeChar) {
        this.escapeChar = escapeChar;
    }

    public MessageEncoding getEncoding() {
        return MessageEncoding.V2_ER7;
    }

    public String getFilename() {
        String filename = null;
        if (messageFile != null) {
            filename = messageFile.getAbsolutePath();
        }
        return filename;
    }

    public String getMessageAsString() {
        StringBuffer sb = new StringBuffer();
        int i = 0;
        while (i < er7Mapping.size()) {
            i++;
            sb.append(er7Mapping.get(i));
            sb.append("\r");
        }
        return sb.toString();
    }

    protected String getSegment(String segmentName, int segmentInstanceNumber) {
        String segment = null;
        MessageLocation location = new MessageLocation(segmentName,
                segmentInstanceNumber);
        int line = getLine(location);
        if (line > 0) {
            segment = er7Mapping.get(line);
        }
        return segment;
    }

    private String getField(int fieldPosition, String segment) {
        Scanner s = new Scanner(segment);
        s.useDelimiter(fieldSeparator);
        int i = 0;
        if (segment.startsWith("MSH")) {
            i = 1;
        }
        while (i <= fieldPosition && s.hasNext()) {
            String value = s.next();
            if (i == fieldPosition) {
                return value;
            }
            i++;
        }
        return null;
    }

    private String getFieldRepetition(int repetitionNumber, String field) {
        Scanner s = new Scanner(field);
        s.useDelimiter(repetitionSeparatorChar);
        int i = 1;
        if (field.startsWith(repetitionSeparatorChar)) {
            repetitionNumber--;
        }
        while (i <= repetitionNumber && s.hasNext()) {
            String value = s.next();
            if (i == repetitionNumber) {
                return value;
            }
            i++;
        }
        return null;
    }

    private String getComponent(int componentPosition, String fieldRep) {

        Scanner s = new Scanner(fieldRep);
        s.useDelimiter(componentSeparator);
        int i = 1;
        if (fieldRep.startsWith(componentSeparatorChar)) {
            componentPosition--;
        }
        while (i <= componentPosition && s.hasNext()) {
            String value = s.next();
            if (i == componentPosition) {
                return value;
            }
            i++;
        }
        return null;
    }

    private String getSubComponent(int subcomponentPosition, String component) {
        Scanner s = new Scanner(component);
        s.useDelimiter(subComponentSeparator);
        int i = 1;
        if (component.startsWith(subComponentSeparatorChar)) {
            subcomponentPosition--;
        }
        while (i <= subcomponentPosition && s.hasNext()) {
            String value = s.next();
            if (i == subcomponentPosition) {
                return value;
            }
            i++;
        }
        return null;
    }

    public String getValueOld(MessageLocation location) {
        int line = getLine(location);
        String segment = er7Mapping.get(line);
        if (segment != null) {
            if (segment.startsWith(location.getSegmentName())) {
                /* MSH.1 */
                if (location.getSegmentName().equals("MSH")
                        && location.getFieldPosition() == 1) {
                    return fieldSeparatorChar;
                }

                if (location.getFieldPosition() > 0) {
                    /* Get the field */
                    String field = getField(location.getFieldPosition(),
                            segment);
                    if ("".equals(field) || field == null) {
                        return null;
                    }
                    /* MSH.2 */
                    if (segment.startsWith("MSH")
                            && location.getFieldPosition() == 2) {
                        return field;
                    }
                    /* Get the repetition */
                    String repetition = getFieldRepetition(
                            location.getFieldInstanceNumber(), field);
                    if ("".equals(repetition) || repetition == null) {
                        return null;
                    }
                    if (location.getComponentPosition() > 0) {
                        /* Get the component */
                        String component = getComponent(
                                location.getComponentPosition(), repetition);
                        if ("".equals(component) || component == null) {
                            return null;
                        }
                        if (location.getSubComponentPosition() > 0) {
                            /* Get the subcomponent */
                            String subcomponent = getSubComponent(
                                    location.getSubComponentPosition(),
                                    component);
                            if ("".equals(subcomponent) || subcomponent == null) {
                                return null;
                            }
                            return subcomponent;
                        } else {
                            return component;
                        }
                    } else {
                        return repetition;
                    }
                } else {
                    return segment;
                }
            }
        }
        return null;
    }

    public String getValue(MessageLocation location) {
        // int line = getLine(location);
        // String segment = er7Mapping.get(line);
        // String segmentName = segment.substring(0, 3);
        String value = null;
        String segmentName = location.getSegmentName();
        int segmentInstanceNumber = location.getSegmentInstanceNumber();
        List<ESegment> segments = this.segments.get(segmentName);
        if (segments != null) {
            ESegment esegment = null;
            if (segments.size() >= segmentInstanceNumber) {
                esegment = segments.get(segmentInstanceNumber - 1);
            }
            if (esegment != null) {
                value = esegment.getValue();
                int fieldPosition = location.getFieldPosition();
                if (fieldPosition != 0) {
                    EField efield = esegment.get(fieldPosition,
                            location.getFieldInstanceNumber());
                    if (efield != null) {
                        value = efield.getValue();
                        int componentPosition = location.getComponentPosition();
                        if (componentPosition != 0) {
                            EComponent ecomponent = efield.get(componentPosition);
                            if (ecomponent != null) {
                                value = ecomponent.getValue();
                                int subComponentPosition = location.getSubComponentPosition();
                                if (subComponentPosition != 0) {
                                    ESubComponent esubcomponent = ecomponent.get(subComponentPosition);
                                    if (esubcomponent != null) {
                                        value = esubcomponent.getValue();
                                    } else {
                                        value = null;
                                    }
                                }
                            } else {
                                value = null;
                            }
                        }
                    } else {
                        value = null;
                    }
                }
            } else {
                value = null;
            }
        }
        if ("".equals(value)) {
            value = null;
        }
        return unescape(value);
    }

    /**
     * Unescape special characters like the separators
     * 
     * @param value
     * @return the unescaped value
     */
    private String unescape(String value) {
        // String value = "\\F\\\\S\\\\T\\\\R\\\\E\\";
        if (value != null) {
            // System.out.println(value);
            value = value.replaceAll(
                    Pattern.quote(escapeChar) + "F" + Pattern.quote(escapeChar),
                    fieldSeparatorChar);
            value = value.replaceAll(
                    Pattern.quote(escapeChar) + "S" + Pattern.quote(escapeChar),
                    componentSeparatorChar);
            value = value.replaceAll(
                    Pattern.quote(escapeChar) + "T" + Pattern.quote(escapeChar),
                    subComponentSeparatorChar);
            value = value.replaceAll(
                    Pattern.quote(escapeChar) + "R" + Pattern.quote(escapeChar),
                    repetitionSeparatorChar);
            value = value.replaceAll(
                    Pattern.quote(escapeChar) + "E" + Pattern.quote(escapeChar),
                    escapeChar + escapeChar);
            // System.out.println(value);
        }
        return value;
    }

    public boolean hasGroups() {
        return false;
    }

    public boolean replaceValue(MessageLocation location, String newValue) {
        boolean replaced = false;
        int lineNumber = getLine(location);
        int columnNumber = getColumn(location);
        if (lineNumber > 0 && columnNumber > 0) {
            String segment = er7Mapping.get(lineNumber);
            int nextSeparator = findNextSeparator(segment, columnNumber - 1);
            String left = segment.substring(0, columnNumber - 1);
            String right = segment.substring(nextSeparator);
            StringBuffer newSegment = new StringBuffer();
            newSegment.append(left).append(newValue).append(right);
            er7Mapping.put(lineNumber, newSegment.toString());
            loadMessage();
            replaced = true;
        }
        return replaced;
        // boolean replaced = false;
        // String segmentName = location.getSegmentName();
        // int segmentInstanceNumber = location.getSegmentInstanceNumber();
        // List<ESegment> eSegments = segments.get(segmentName);
        // ESegment segmentToModify = eSegments.get(segmentInstanceNumber - 1);
        // int fieldPosition = location.getFieldPosition();
        // int fieldInstanceNumber = location.getFieldInstanceNumber();
        // if (fieldPosition > 0 && fieldInstanceNumber > 0) {
        // EField fieldToModify = segmentToModify.get(fieldPosition,
        // fieldInstanceNumber);
        // int componentPosition = location.getComponentPosition();
        // if (componentPosition > 0) {
        // EComponent componentToModify = fieldToModify.get(componentPosition);
        // int subComponentPosition = location.getSubComponentPosition();
        // if (subComponentPosition > 0) {
        // ESubComponent subComponentToModify =
        // componentToModify.get(subComponentPosition);
        // if (subComponentToModify != null) {
        // subComponentToModify.setValue(newValue,
        // subComponentToModify.getLine(),
        // subComponentToModify.getColumn());
        // replaced = true;
        // }
        // } else {
        // // Modification of the component
        // if (componentToModify != null) {
        // componentToModify.setValue(newValue,
        // componentToModify.getLine(),
        // componentToModify.getColumn(),
        // subComponentSeparator);
        // replaced = true;
        // }
        // }
        // } else {
        // // Modification of the field
        // if (fieldToModify != null) {
        // fieldToModify.setValue(newValue, fieldToModify.getLine(),
        // fieldToModify.getColumn(), componentSeparator,
        // subComponentSeparator);
        // replaced = true;
        // }
        // }
        // } else {
        // // Modification of the segment
        // if (segmentToModify != null) {
        // segmentToModify.setValue(newValue, segmentToModify.getLine(),
        // fieldSeparator, repetitionSeparator,
        // componentSeparator, subComponentSeparator);
        // replaced = true;
        // }
        // }
        // return replaced;
    }

    /**
     * Find the next separator
     * 
     * @param segment
     * @param startIndex
     * @return the next separator; the end of the segment otherwise
     */
    private int findNextSeparator(String segment, int startIndex) {
        int[] separators = new int[4];
        separators[0] = segment.indexOf(fieldSeparatorChar, startIndex);
        separators[1] = segment.indexOf(repetitionSeparatorChar, startIndex);
        separators[2] = segment.indexOf(componentSeparatorChar, startIndex);
        separators[3] = segment.indexOf(subComponentSeparatorChar, startIndex);
        int minIndex = 10000;
        for (int separator : separators) {
            if (separator != -1 && separator < minIndex) {
                minIndex = separator;
            }
        }
        if (minIndex == 10000) {
            minIndex = segment.length();
        }
        return minIndex;
    }

    public boolean replaceValue(MessageLocation location, String oldValue,
            String newValue) {
        // Test old value value
        String messageValue = getValue(location);
        if (!oldValue.equals(messageValue)) {
            return false;
        }
        return replaceValue(location, newValue);
    }

    public void save(File file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(this.getMessageAsString());
        bw.close();
    }

    public int getLine(MessageLocation location) {
        int line = -1;
        String segmentName = location.getSegmentName();
        int segmentInstanceNumber = location.getSegmentInstanceNumber();
        List<ESegment> segments = this.segments.get(segmentName);
        if (segments != null) {
            ESegment esegment = null;
            if (segments.size() >= segmentInstanceNumber) {
                esegment = segments.get(segmentInstanceNumber - 1);
            }
            if (esegment != null) {
                line = esegment.getLine();
                int fieldPosition = location.getFieldPosition();
                if (fieldPosition != 0) {
                    EField efield = esegment.get(fieldPosition,
                            location.getFieldInstanceNumber());
                    if (efield != null) {
                        line = efield.getLine();
                        int componentPosition = location.getComponentPosition();
                        if (componentPosition != 0) {
                            EComponent ecomponent = efield.get(componentPosition);
                            if (ecomponent != null) {
                                line = ecomponent.getLine();
                                int subComponentPosition = location.getSubComponentPosition();
                                if (subComponentPosition != 0) {
                                    ESubComponent esubcomponent = ecomponent.get(subComponentPosition);
                                    if (esubcomponent != null) {
                                        line = esubcomponent.getLine();
                                    } else {
                                        line = -1;
                                    }
                                }
                            } else {
                                line = -1;
                            }
                        }
                    } else {
                        line = -1;
                    }
                }
            } else {
                line = -1;
            }
        }
        return line;
        // /* Get the segment */
        // BufferedReader er7 = null;
        // er7 = new BufferedReader(new StringReader(getMessageAsString()));
        // String segment;
        // int line = 0;
        // int count = 0;
        // String segName = location.getSegmentName();
        // int segRepetition = location.getSegmentInstanceNumber();
        // try {
        // while (((segment = er7.readLine()) != null)) {
        // line++;
        // if (segment.startsWith(segName)) {
        // count++;
        // if (count == segRepetition) {
        // return line;
        // }
        // }
        // }
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // return 0;
    }

    public int getColumn(MessageLocation location) {
        int column = -1;
        String segmentName = location.getSegmentName();
        int segmentInstanceNumber = location.getSegmentInstanceNumber();
        List<ESegment> segments = this.segments.get(segmentName);
        if (segments != null) {
            ESegment esegment = null;
            if (segments.size() >= segmentInstanceNumber) {
                esegment = segments.get(segmentInstanceNumber - 1);
            }
            if (esegment != null) {
                column = esegment.getColumn();
                int fieldPosition = location.getFieldPosition();
                if (fieldPosition != 0) {
                    EField efield = esegment.get(fieldPosition,
                            location.getFieldInstanceNumber());
                    if (efield != null) {
                        column = efield.getColumn();
                        int componentPosition = location.getComponentPosition();
                        if (componentPosition != 0) {
                            EComponent ecomponent = efield.get(componentPosition);
                            if (ecomponent != null) {
                                column = ecomponent.getColumn();
                                int subComponentPosition = location.getSubComponentPosition();
                                if (subComponentPosition != 0) {
                                    ESubComponent esubcomponent = ecomponent.get(subComponentPosition);
                                    if (esubcomponent != null) {
                                        column = esubcomponent.getColumn();
                                    } else {
                                        column = -1;
                                    }
                                }
                            } else {
                                column = -1;
                            }
                        }
                    } else {
                        column = -1;
                    }
                }
            } else {
                column = -1;
            }
        }
        return column;
        // String segment = er7Mapping.get(getLine(location));
        // String field;
        // String component;
        // int idx = 1;
        // try {
        // if (location.getFieldPosition() > 0) {
        // /* Get the field */
        // int fieldNumber = location.getFieldPosition();
        // String[] fields = segment.split(getFieldSeparator());
        // if (location.getSegmentName().equals("MSH")) {
        // fieldNumber--;
        // }
        // if (fieldNumber >= fields.length) {
        // idx = segment.length() + 1;
        // return idx;
        // }
        // for (int i = 0; i < fieldNumber && i < fields.length; i++) {
        // idx = idx + fields[i].length();
        // if (i < fields.length - 1) {
        // idx++;
        // }
        // }
        // /* Get the instance number. 1 is 1st repetition etc */
        // if (location.getFieldInstanceNumber() >= 1) {
        // field = fields[fieldNumber];
        // int fieldRepetition = location.getFieldInstanceNumber();
        // String[] fieldRepetitions = field.split(getRepetitionSeparator());
        // if (fieldRepetition > fieldRepetitions.length) {
        // idx = idx + field.length() + 1;
        // return idx;
        // }
        // for (int i = 0; i < fieldRepetition - 1
        // && i < fieldRepetitions.length; i++) {
        // idx = idx + fieldRepetitions[i].length();
        // if (i < fieldRepetitions.length - 1) {
        // idx++;
        // }
        // }
        // field = fieldRepetitions[fieldRepetition - 1];
        // } else {
        // field = fields[fieldNumber];
        // }
        // if (location.getComponentPosition() > 0) {
        //
        // /* Get the component */
        // int compNumber = location.getComponentPosition();
        // String[] components = field.split(getComponentSeparator());
        // if (compNumber > components.length) {
        // idx = idx + field.length() + 1;
        // return idx;
        // }
        // for (int i = 0; i < compNumber - 1 && i < components.length; i++) {
        // idx = idx + components[i].length();
        // if (i < components.length - 1) {
        // idx++;
        // }
        // }
        // if (location.getSubComponentPosition() > 0) {
        // component = components[compNumber - 1];
        // /* Get the subcomponent */
        // int subcompNumber = location.getSubComponentPosition();
        // String[] subcomponents = component.split(getSubComponentSeparator());
        // if (subcompNumber > subcomponents.length) {
        // idx = idx + component.length() + 1;
        // return idx;
        // }
        // for (int i = 0; i < subcompNumber - 1
        // && i < subcomponents.length; i++) {
        // idx = idx + subcomponents[i].length();
        // if (i < subcomponents.length - 1) {
        // idx++;
        // }
        // }
        // }
        // }
        // }
        // return idx;
        // } catch (NumberFormatException e) {
        // e.printStackTrace();
        // }
        // return idx;
    }

    // @Deprecated
    // public ArrayList<MessageLocation> getLocations(MessageLocation location)
    // {
    // ArrayList<MessageLocation> result = new ArrayList<MessageLocation>();
    // String element = getValue(location);
    // if (element == null) {
    // return result;
    // }
    //
    // if (location.getSubComponentPosition() > 0) {
    // ;
    // } else if (location.getComponentPosition() > 0) {
    // /* Return the subcomponents */
    // String[] subComponents = element.split(getSubComponentSeparator());
    // if (subComponents.length > 1) {
    // int i = 0;
    // while (i < subComponents.length) {
    // int subCompPosition = i + 1;
    // MessageLocation ml = new MessageLocation(
    // location.getSegmentName(),
    // location.getSegmentInstanceNumber(),
    // location.getFieldPosition(),
    // location.getFieldInstanceNumber(),
    // location.getComponentPosition(), subCompPosition);
    // result.add(ml);
    // i++;
    // }
    // }
    // } else if (location.getFieldPosition() > 0) {
    // /* Return components and subcomponents */
    // if (location.getSegmentName().equals("MSH")
    // && location.getFieldPosition() == 2) {
    // /* Add delimiters */
    // result.add(new MessageLocation(location.getSegmentName(),
    // location.getSegmentInstanceNumber(), 2, 1));
    // return result;
    // }
    //
    // String[] components = element.split(getComponentSeparator());
    // if (components.length > 1) {
    // int i = 0;
    // while (i < components.length) {
    // int componentPosition = i + 1;
    // ArrayList<MessageLocation> subcomponents = getLocations(new
    // MessageLocation(
    // location.getSegmentName(),
    // location.getSegmentInstanceNumber(),
    // location.getFieldPosition(),
    // location.getFieldInstanceNumber(),
    // componentPosition));
    // if (subcomponents.size() > 0) {
    // result.addAll(subcomponents);
    // } else {
    // MessageLocation ml = new MessageLocation(
    // location.getSegmentName(),
    // location.getSegmentInstanceNumber(),
    // location.getFieldPosition(),
    // location.getFieldInstanceNumber(),
    // componentPosition);
    // result.add(ml);
    // }
    // i++;
    // }
    // }
    // } else {
    // /* Return fields, components and subcomponents */
    // String[] fields = element.split(getFieldSeparator());
    // if (fields.length > 1) {
    // int i = 0;
    // int limit = fields.length - 1;
    // if (location.getSegmentName().equals("MSH")) {
    // /* Add first delimiter */
    // result.add(new MessageLocation(location.getSegmentName(),
    // location.getSegmentInstanceNumber(), 1, 1));
    // i++;
    // limit++;
    // }
    // while (i < limit) {
    // int fieldPosition = i + 1;
    // /* Split repetitions */
    // String[] repetitions;
    // if (location.getSegmentName().equals("MSH")) {
    // if (fieldPosition == 2) {
    // repetitions = fields[i].split("\r");
    // } else {
    // repetitions = fields[i].split(getRepetitionSeparator());
    // }
    // } else {
    // repetitions = fields[i + 1].split(getRepetitionSeparator());
    // }
    //
    // int j = 0;
    // while (j < repetitions.length) {
    // int fieldInstanceNumber = j + 1;
    // ArrayList<MessageLocation> components = getLocations(new MessageLocation(
    // location.getSegmentName(),
    // location.getSegmentInstanceNumber(),
    // fieldPosition, fieldInstanceNumber));
    // if (components.size() > 0) {
    // result.addAll(components);
    // } else {
    // MessageLocation ml = new MessageLocation(
    // location.getSegmentName(),
    // location.getSegmentInstanceNumber(),
    // fieldPosition, fieldInstanceNumber);
    // result.add(ml);
    // }
    // j++;
    // }
    // i++;
    // }
    // }
    // }
    // return result;
    // }

    /**
     * Removes unnecessary delimiters
     * 
     * @return a cleaned copy of this er7 message
     * @throws MalformedMessageException
     */
    public Er7Message cleanEr7() throws MalformedMessageException {
        StringBuffer sb = new StringBuffer();
        int i = 0;
        while (i < er7Mapping.size()) {
            i++;
            String line = er7Mapping.get(i);
            String tmp = "";

            if (line.startsWith("MSH")) {
                int idx = line.indexOf(getFieldSeparatorChar(),
                        line.indexOf(getFieldSeparatorChar()) + 1);
                tmp = line.substring(0, idx);
                line = line.substring(idx);
            }

            /* Remove useless subcomponent char */
            Pattern p = Pattern.compile("(" + getRepetitionSeparator() + "|"
                    + getComponentSeparator() + ")"
                    + getSubComponentSeparator() + "+" + "("
                    + getRepetitionSeparator() + "|" + getComponentSeparator()
                    + "|" + getFieldSeparator() + "|$" + ")");
            Matcher m = p.matcher(line);
            boolean b = m.find();
            if (b) {
                line = m.replaceAll(m.group(1) + m.group(2));
            }

            /* Remove useless component char */
            p = Pattern.compile("(" + getRepetitionSeparator() + "|"
                    + getFieldSeparator() + ")" + getComponentSeparator() + "+"
                    + "(" + getRepetitionSeparator() + "|"
                    + getFieldSeparator() + "|$" + ")");
            m = p.matcher(line);
            b = m.find();
            if (b) {
                line = m.replaceAll(m.group(1) + m.group(2));
                // System.out.println(line);
            }

            /* Remove useless repetition char */
            p = Pattern.compile("(" + getFieldSeparator() + ")"
                    + getRepetitionSeparator() + "+" + "("
                    + getFieldSeparator() + "|$" + ")");
            m = p.matcher(line);
            b = m.find();
            if (b) {
                line = m.replaceAll(m.group(1) + m.group(2));
                // System.out.println(line);
            }

            /* Remove useless field separator */
            p = Pattern.compile(getFieldSeparator() + "+$");
            m = p.matcher(line);
            b = m.find();
            if (b) {
                line = m.replaceAll(getFieldSeparatorChar());
                // System.out.println(line);
            }

            sb.append(tmp);
            sb.append(line);
            sb.append("\r");
        }
        Er7Message er7 = new Er7Message(sb.toString());
        return er7;
    }

    /**
     * Get the message structure id present in the message if present. If not,
     * looks in the HL7 database to determine the message structure id, using
     * MSH.9.1 and MSH.9.2
     * 
     * @return the message structure id
     */
    // TODO: rewrite
    public String getFixedMessageStructureID() {
        // String result = getMessageStructureID();
        // if (result == null) {
        // String messageType = getMessageCode();
        // String eventCode = getMessageEvent();
        // result = ValidationUtils.checkMsgStructId(messageType, eventCode,
        // "", VersionConstants.getConstant(getVersionAsString()));
        // }
        // return result;
        return null;
    }

    // public MessageClass getImplementation() throws MalformedMessageException,
    // UnsupportedMessageClassException {
    // MessageClass messageClass = null;
    // GeneralMessageType type = null;
    // type = this.getMessageType();
    // switch (type) {
    // case UNSUPPORTED:
    // throw new UnsupportedMessageClassException(
    // "The message type is UNSUPPORTED.");
    // case HL7_ACKNOWLEDGMENT:
    // messageClass = new AcknowledgmentV2HL7Er7(this);
    // break;
    // case PATIENT_ADD:
    // messageClass = new PatientAddV2HL7Er7(this);
    // break;
    // case PATIENT_UPDATE:
    // messageClass = new PatientUpdateV2HL7Er7(this);
    // break;
    // case PATIENT_MERGE_DUPLICATE:
    // messageClass = new PatientMergeDuplicateV2HL7Er7(this);
    // break;
    // case GET_IDENTIFIERS_QUERY:
    // messageClass = new GetIdentifiersQueryV2HL7Er7(this);
    // break;
    // case GET_IDENTIFIERS_QUERY_RESPONSE:
    // messageClass = new GetIdentifiersQueryResponseV2HL7Er7(this);
    // break;
    // case PDQ_QUERY:
    // messageClass = new PatientDemographicsQueryV2HL7Er7(this);
    // break;
    // case PDQ_QUERY_RESPONSE:
    // messageClass = new PatientDemographicsQueryResponseV2HL7Er7(this);
    // break;
    // case PDQ_QUERY_CANCEL:
    // messageClass = new PatientDemographicsQueryCancelV2HL7Er7(this);
    // break;
    // }
    // return messageClass;
    // }

    public int getSegmentCount(String segmentName) {
        int count = 0;
        Iterator<Integer> lines = er7Mapping.keySet().iterator();
        while (lines.hasNext()) {
            int line = lines.next();
            String segment = er7Mapping.get(line);
            if (segment.startsWith(segmentName)) {
                count++;
            }
        }
        return count;
    }

    public int getFieldCount(String segmentName, int segmentInstanceNumber,
            int fieldPosition) {
        String segment = getSegment(segmentName, segmentInstanceNumber);
        if (segment == null) {
            return 0;
        }
        String field = getField(fieldPosition, segment);
        if (field == null) {
            return 0;
        }
        return getFieldRepetitionCount(field);
    }

    /**
     * Get the number of repetition of a field
     * 
     * @param field
     * @return the number of repetition
     */
    public int getFieldRepetitionCount(String field) {
        int count = 1;
        for (int i = 0; i < field.length(); i++) {
            if (repetitionSeparatorChar.equals(String.valueOf(field.charAt(i)))) {
                count++;
            }
        }
        return count;
    }

    public List<String> getValues(String location) {
        ArrayList<String> values = new ArrayList<String>();
        Pattern p = Pattern.compile("([A-Z0-9]{3})\\[(?:(\\d+|\\*))\\]"
                + "(?:\\.(\\d+)\\[(?:(\\d+|\\*))\\]"
                + "(?:\\.(\\d+)(?:\\.(\\d+))?)?)?");
        Matcher m = p.matcher(location);
        if (m.matches()) {
            String segmentName = m.group(1);
            String segmentInstanceNumber = m.group(2);
            String fieldPosition = m.group(3);
            String fieldInstanceNumber = m.group(4);
            String componentPosition = m.group(5);
            String subComponentPosition = m.group(6);
            MessageLocation ml = null;
            int segmentCount = 1;
            if ("*".equals(segmentInstanceNumber)) {
                segmentCount = getSegmentCount(segmentName);
            }
            for (int i = 0; i < segmentCount; i++) {
                int iSegmentInstanceNumber = -1;
                if ("*".equals(segmentInstanceNumber)) {
                    iSegmentInstanceNumber = i + 1;
                } else {
                    iSegmentInstanceNumber = Integer.parseInt(segmentInstanceNumber);
                }
                if (fieldPosition == null) {
                    ml = new MessageLocation(segmentName,
                            iSegmentInstanceNumber);
                    String value = getValue(ml);
                    if (value != null && !"".equals(value)) {
                        values.add(value);
                    }
                } else {
                    int fieldRepetitionCount = 1;
                    if ("*".equals(fieldInstanceNumber)) {
                        String segment = getSegment(segmentName,
                                iSegmentInstanceNumber);
                        // TODO : do something if segment is null !!!!
                        if (segment != null) {
                            String field = getField(
                                    Integer.parseInt(fieldPosition), segment);
                            if (field != null) {
                                fieldRepetitionCount = getFieldRepetitionCount(field);
                            }
                        }
                    }
                    for (int j = 0; j < fieldRepetitionCount; j++) {
                        int iFieldInstanceNumber = -1;
                        if ("*".equals(fieldInstanceNumber)) {
                            iFieldInstanceNumber = j + 1;
                        } else {
                            iFieldInstanceNumber = Integer.parseInt(fieldInstanceNumber);
                        }
                        if (componentPosition == null) {
                            ml = new MessageLocation(segmentName,
                                    iSegmentInstanceNumber,
                                    Integer.parseInt(fieldPosition),
                                    iFieldInstanceNumber);

                        } else {
                            if (subComponentPosition == null) {
                                ml = new MessageLocation(segmentName,
                                        iSegmentInstanceNumber,
                                        Integer.parseInt(fieldPosition),
                                        iFieldInstanceNumber,
                                        Integer.parseInt(componentPosition));
                            } else {
                                ml = new MessageLocation(segmentName,
                                        iSegmentInstanceNumber,
                                        Integer.parseInt(fieldPosition),
                                        iFieldInstanceNumber,
                                        Integer.parseInt(componentPosition),
                                        Integer.parseInt(subComponentPosition));
                            }
                        }
                        String value = getValue(ml);
                        if (value != null && !"".equals(value)) {
                            values.add(value);
                        }
                    }
                }
            }
        }

        return values;
    }

    public boolean replaceSending(Name name) {
        HL7V2Name sending = (HL7V2Name) name;
        boolean replaced = false;
        if (sending != null) {
            StringBuffer newmsh = new StringBuffer();
            // Get the MSH segment
            String msh = er7Mapping.get(1);
            if (msh != null) {
                // Get the index of the second and fouth field separator
                int idx2nd = -1;
                for (int i = 0; i < 2; i++) {
                    idx2nd = msh.indexOf(fieldSeparatorChar, idx2nd + 1);
                }
                int idx4th = idx2nd;
                for (int i = 2; i < 4; i++) {
                    idx4th = msh.indexOf(fieldSeparatorChar, idx4th + 1);
                }
                if (idx2nd != -1 && idx4th != -1) {
                    StringBuffer sb = new StringBuffer();
                    String namespaceId = sending.getApplicationName().getNamespaceId();
                    if (namespaceId != null) {
                        sb.append(namespaceId);
                    } else {
                        sb.append("");
                    }
                    sb.append(componentSeparatorChar);
                    String universalId = sending.getApplicationName().getUniversalId();
                    if (universalId != null) {
                        sb.append(universalId);
                    } else {
                        sb.append("");
                    }
                    sb.append(componentSeparatorChar);
                    String universalIdType = sending.getApplicationName().getUniversalIdType();
                    if (universalIdType != null) {
                        sb.append(universalIdType);
                    } else {
                        sb.append("");
                    }
                    sb.append(fieldSeparatorChar);
                    namespaceId = sending.getFacilityName().getNamespaceId();
                    if (namespaceId != null) {
                        sb.append(namespaceId);
                    } else {
                        sb.append("");
                    }
                    sb.append(componentSeparatorChar);
                    universalId = sending.getFacilityName().getUniversalId();
                    if (universalId != null) {
                        sb.append(universalId);
                    } else {
                        sb.append("");
                    }
                    sb.append(componentSeparatorChar);
                    universalIdType = sending.getFacilityName().getUniversalIdType();
                    if (universalIdType != null) {
                        sb.append(universalIdType);
                    } else {
                        sb.append("");
                    }
                    // Replace
                    newmsh.append(msh.substring(0, idx2nd + 1)).append(
                            sb.toString()).append(msh.substring(idx4th));
                    er7Mapping.put(1, newmsh.toString());
                    replaced = true;
                }
            }
            if (replaced) {
                ESegment eSegment = new ESegment(newmsh.toString(), 1,
                        fieldSeparator, repetitionSeparator,
                        componentSeparator, subComponentSeparator, 1);
                List<ESegment> listSegment = segments.get("MSH");
                if (listSegment == null) {
                    listSegment = new ArrayList<ESegment>();
                }
                listSegment.clear();
                listSegment.add(eSegment);
                segments.put("MSH", listSegment);
                this.sending = sending;
            }
        }
        return replaced;
    }

    public boolean replaceReceiving(Name name) {
        HL7V2Name receiving = (HL7V2Name) name;
        boolean replaced = false;
        if (receiving != null) {
            StringBuffer newmsh = new StringBuffer();
            // Get the MSH segment
            String msh = er7Mapping.get(1);
            if (msh != null) {
                // Get the index of the fourth and sixth field separator
                int idx4th = -1;
                for (int i = 0; i < 4; i++) {
                    idx4th = msh.indexOf(fieldSeparatorChar, idx4th + 1);
                }
                int idx6th = idx4th;
                for (int i = 4; i < 6; i++) {
                    idx6th = msh.indexOf(fieldSeparatorChar, idx6th + 1);
                }
                if (idx4th != -1 && idx6th != -1) {
                    StringBuffer sb = new StringBuffer();
                    String namespaceId = receiving.getApplicationName().getNamespaceId();
                    if (namespaceId != null) {
                        sb.append(namespaceId);
                    } else {
                        sb.append("");
                    }
                    sb.append(componentSeparatorChar);
                    String universalId = receiving.getApplicationName().getUniversalId();
                    if (universalId != null) {
                        sb.append(universalId);
                    } else {
                        sb.append("");
                    }
                    sb.append(componentSeparatorChar);
                    String universalIdType = receiving.getApplicationName().getUniversalIdType();
                    if (universalIdType != null) {
                        sb.append(universalIdType);
                    } else {
                        sb.append("");
                    }
                    sb.append(fieldSeparatorChar);
                    namespaceId = receiving.getFacilityName().getNamespaceId();
                    if (namespaceId != null) {
                        sb.append(namespaceId);
                    } else {
                        sb.append("");
                    }
                    sb.append(componentSeparatorChar);
                    universalId = receiving.getFacilityName().getUniversalId();
                    if (universalId != null) {
                        sb.append(universalId);
                    } else {
                        sb.append("");
                    }
                    sb.append(componentSeparatorChar);
                    universalIdType = receiving.getFacilityName().getUniversalIdType();
                    if (universalIdType != null) {
                        sb.append(universalIdType);
                    } else {
                        sb.append("");
                    }
                    // Replace
                    newmsh.append(msh.substring(0, idx4th + 1)).append(
                            sb.toString()).append(msh.substring(idx6th));
                    er7Mapping.put(1, newmsh.toString());
                    replaced = true;
                }
            }
            if (replaced) {
                ESegment eSegment = new ESegment(newmsh.toString(), 1,
                        fieldSeparator, repetitionSeparator,
                        componentSeparator, subComponentSeparator, 1);
                List<ESegment> listSegment = segments.get("MSH");
                if (listSegment == null) {
                    listSegment = new ArrayList<ESegment>();
                }
                listSegment.clear();
                listSegment.add(eSegment);
                segments.put("MSH", listSegment);
                this.receiving = receiving;
            }
        }
        return replaced;
    }

    public boolean replaceDateTimeOfMessage(String dateTimeOfMessage) {
        boolean replaced = false;
        if (dateTimeOfMessage != null) {
            StringBuffer newmsh = new StringBuffer();
            // Get the MSH segment
            String msh = er7Mapping.get(1);
            if (msh != null) {
                // Get the index of the sixth and seventh field separator
                int idx6th = -1;
                for (int i = 0; i < 6; i++) {
                    idx6th = msh.indexOf(fieldSeparatorChar, idx6th + 1);
                }
                int idx7th = msh.indexOf(fieldSeparatorChar, idx6th + 1);
                if (idx6th != -1 && idx7th != -1) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(dateTimeOfMessage);
                    // Replace
                    newmsh.append(msh.substring(0, idx6th + 1)).append(
                            sb.toString()).append(msh.substring(idx7th));
                    er7Mapping.put(1, newmsh.toString());
                    replaced = true;
                }
            }
            if (replaced) {
                ESegment eSegment = new ESegment(newmsh.toString(), 1,
                        fieldSeparator, repetitionSeparator,
                        componentSeparator, subComponentSeparator, 1);
                List<ESegment> listSegment = segments.get("MSH");
                if (listSegment == null) {
                    listSegment = new ArrayList<ESegment>();
                }
                listSegment.clear();
                listSegment.add(eSegment);
                segments.put("MSH", listSegment);
                this.creationTime = dateTimeOfMessage;
            }
        }
        return replaced;
    }

    public boolean replaceMessageId(MessageId messageId) {
        HL7V2MessageId newMessageId = (HL7V2MessageId) messageId;
        boolean replaced = false;
        if (newMessageId != null) {
            StringBuffer newmsh = new StringBuffer();
            // Get the MSH segment
            String msh = er7Mapping.get(1);
            if (msh != null) {
                // Get the index of the ninth and tenth field separator
                int idx9th = -1;
                for (int i = 0; i < 9; i++) {
                    idx9th = msh.indexOf(fieldSeparatorChar, idx9th + 1);
                }
                int idx10th = msh.indexOf(fieldSeparatorChar, idx9th + 1);
                if (idx9th != -1 && idx10th != -1) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(newMessageId.getMessageId());
                    // Replace
                    newmsh.append(msh.substring(0, idx9th + 1)).append(
                            sb.toString()).append(msh.substring(idx10th));
                    er7Mapping.put(1, newmsh.toString());
                    replaced = true;
                }
            }
            if (replaced) {
                ESegment eSegment = new ESegment(newmsh.toString(), 1,
                        fieldSeparator, repetitionSeparator,
                        componentSeparator, subComponentSeparator, 1);
                List<ESegment> listSegment = segments.get("MSH");
                if (listSegment == null) {
                    listSegment = new ArrayList<ESegment>();
                }
                listSegment.clear();
                listSegment.add(eSegment);
                segments.put("MSH", listSegment);
                this.messageId = newMessageId;
            }
        }
        return replaced;
    }

    public Map<String, List<ESegment>> getSegments() {
        return segments;
    }

    public List<ValuedMessageLocation> getLocations(Profile p,
            MessageLocation start) {
        List<ValuedMessageLocation> locations = new ArrayList<ValuedMessageLocation>();
        String segmentName = start.getSegmentName();
        int segmentInstanceNumber = start.getSegmentInstanceNumber();
        List<ESegment> segments = this.segments.get(segmentName);
        if (segments != null) {
            ESegment eSegment = null;
            if (segments.size() >= segmentInstanceNumber) {
                eSegment = segments.get(segmentInstanceNumber - 1);
            }
            if (eSegment != null) {
                if (start.getElementType() == ElementType.SEGMENT) {
                    locations.addAll(eSegment.getLocations(p));
                } else {
                    int fieldPosition = start.getFieldPosition();
                    if (fieldPosition != 0) {
                        EField eField = eSegment.get(fieldPosition,
                                start.getFieldInstanceNumber());
                        if (eField != null) {
                            if (start.getElementType() == ElementType.FIELD) {
                                locations.addAll(eField.getLocations(p,
                                        segmentName, segmentInstanceNumber));
                            } else {
                                int componentPosition = start.getComponentPosition();
                                if (componentPosition != 0) {
                                    EComponent eComponent = eField.get(componentPosition);
                                    if (eComponent != null) {
                                        if (start.getElementType() == ElementType.COMPONENT) {
                                            locations.addAll(eComponent.getLocations(
                                                    p,
                                                    segmentName,
                                                    segmentInstanceNumber,
                                                    start.getFieldPosition(),
                                                    start.getFieldInstanceNumber()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return locations;
    }
}
