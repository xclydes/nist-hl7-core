/*
 * NIST Healthcare Core
 * MessagePopulation.java Aug 8, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.generation;

import gov.nist.healthcare.core.generation.MessageGenerationConstants.GenerationError;
import gov.nist.healthcare.core.message.v2.xml.XmlMessage;
import gov.nist.healthcare.core.profile.Profile;
import gov.nist.healthcare.core.profile.ProfileElement;
import gov.nist.healthcare.data.TableLibraryDocument;
import gov.nist.healthcare.generation.DataValueLocationItemGeneration;
import gov.nist.healthcare.generation.MessageInstanceSpecificValuesGeneration;
import gov.nist.healthcare.generation.MessagePopulationModule.Resource;
import gov.nist.healthcare.generation.MessagePopulationResourceTypeConstants;
import gov.nist.healthcare.generation.MessagePopulationResourceTypeConstants.Enum;
import gov.nist.healthcare.generation.message.data.DefaultValueLibraryDocument;
import gov.nist.healthcare.generation.message.data.PrimitiveValueLibraryDocument;
import gov.nist.healthcare.message.Component;
import gov.nist.healthcare.message.Field;
import gov.nist.healthcare.message.MessageElement;
import gov.nist.healthcare.message.Segment;
import gov.nist.healthcare.message.SegmentGroup;
import gov.nist.healthcare.message.SubComponent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * This class handles the population of Message
 * 
 * @author Sydney Henrard (NIST)
 */
public class MessagePopulation {

    private final List<PrimitiveValueLibraryDocument> primitives;
    private final List<TableLibraryDocument> tables;
    // private TableLibraryDocument tablesHL7;
    private final List<DefaultValueLibraryDocument> defaults;

    private HashMap<ProfileElement, ArrayList<String>> hPrimitive;
    private HashMap<String, ArrayList<String>> hTable;
    // private HashMap<String, ArrayList<String>> hTableHL7;
    private HashMap<String, ArrayList<String>> hSimpleDefault;
    private HashMap<String, ArrayList<String>> hTableDefault;

    private final Pattern pResource;
    private boolean timeOfMessage;

    /**
     * Constructor
     */
    public MessagePopulation() {
        primitives = new ArrayList<PrimitiveValueLibraryDocument>();
        tables = new ArrayList<TableLibraryDocument>();
        defaults = new ArrayList<DefaultValueLibraryDocument>();
        hPrimitive = new HashMap<ProfileElement, ArrayList<String>>();
        hTable = new HashMap<String, ArrayList<String>>();
        // hTableHL7 = new HashMap<String, ArrayList<String>>();
        hSimpleDefault = new HashMap<String, ArrayList<String>>();
        hTableDefault = new HashMap<String, ArrayList<String>>();
        pResource = Pattern.compile("(classpath|file)\\:(.*)");
        timeOfMessage = true;
    }

    /**
     * Constructor
     * 
     * @param primitiveValues
     * @param tableValues
     * @param defaultTableValues
     * @param defaultValues
     */
    // public MessagePopulation(PrimitiveValueLibraryDocument primitiveValues,
    // TableLibraryDocument tableValues,
    // TableLibraryDocument defaultTableValues,
    // DefaultValueLibraryDocument defaultValues) {
    // values = primitiveValues;
    // tables = tableValues;
    // tablesHL7 = defaultTableValues;
    // defaults = defaultValues;
    // initPrimitiveValues();
    // initTableValues();
    // initTableHL7Values();
    // initDefaultValues();
    // }

    /**
     * Add a resource
     * 
     * @param resource
     * @return a GenerationError if the resource is not valid; null otherwise
     */
    public GenerationError addResource(Resource resource) {
        boolean errorFlag = true;
        GenerationError error = null;
        Matcher m = null;
        Enum type = resource.getType();
        if (type == MessagePopulationResourceTypeConstants.PRIMITIVE) {
            m = pResource.matcher(resource.getSource());
            if (m.matches()) {
                String resourceType = m.group(1);
                String resourceSrc = m.group(2);
                PrimitiveValueLibraryDocument primitive = null;
                if ("classpath".equals(resourceType)) {
                    URL src = getClass().getResource(resourceSrc);
                    if (src != null) {
                        try {
                            primitive = PrimitiveValueLibraryDocument.Factory.parse(src);
                        } catch (XmlException e) {
                            errorFlag = false;
                        } catch (IOException e) {
                            errorFlag = false;
                        }
                    }
                } else if ("file".equals(resourceType)) {
                    File src = new File(resourceSrc);
                    try {
                        primitive = PrimitiveValueLibraryDocument.Factory.parse(src);
                    } catch (XmlException e) {
                        errorFlag = false;
                    } catch (IOException e) {
                        errorFlag = false;
                    }
                }
                if (primitive != null) {
                    if (primitive.validate()) {
                        primitives.add(primitive);
                        hashPrimitiveValues();
                        errorFlag = false;
                    }
                }
            }
        } else if (type == MessagePopulationResourceTypeConstants.TABLE) {
            m = pResource.matcher(resource.getSource());
            if (m.matches()) {
                String resourceType = m.group(1);
                String resourceSrc = m.group(2);
                TableLibraryDocument table = null;
                if ("classpath".equals(resourceType)) {
                    URL src = getClass().getResource(resourceSrc);
                    if (src != null) {
                        try {
                            table = TableLibraryDocument.Factory.parse(src);
                        } catch (XmlException e) {
                            errorFlag = false;
                        } catch (IOException e) {
                            errorFlag = false;
                        }
                    }
                } else if ("file".equals(resourceType)) {
                    File src = new File(resourceSrc);
                    try {
                        table = TableLibraryDocument.Factory.parse(src);
                    } catch (XmlException e) {
                        errorFlag = false;
                    } catch (IOException e) {
                        errorFlag = false;
                    }
                }
                if (table != null) {
                    if (table.validate()) {
                        tables.add(table);
                        hashTableValues();
                        errorFlag = false;
                    }
                }
            }
        } else if (type == MessagePopulationResourceTypeConstants.DEFAULT) {
            m = pResource.matcher(resource.getSource());
            if (m.matches()) {
                String resourceType = m.group(1);
                String resourceSrc = m.group(2);
                DefaultValueLibraryDocument _default = null;
                if ("classpath".equals(resourceType)) {
                    URL src = getClass().getResource(resourceSrc);
                    if (src != null) {
                        try {
                            _default = DefaultValueLibraryDocument.Factory.parse(src);
                        } catch (XmlException e) {
                            errorFlag = false;
                        } catch (IOException e) {
                            errorFlag = false;
                        }
                    }
                } else if ("file".equals(resourceType)) {
                    File src = new File(resourceSrc);
                    try {
                        _default = DefaultValueLibraryDocument.Factory.parse(src);
                    } catch (XmlException e) {
                        errorFlag = false;
                    } catch (IOException e) {
                        errorFlag = false;
                    }
                }
                if (_default != null) {
                    if (_default.validate()) {
                        defaults.add(_default);
                        hashDefaultValues();
                        errorFlag = false;
                    }
                }
            }
        }
        if (errorFlag) {
            error = GenerationError.INVALID_RESOURCE;
        }
        return error;
    }

    /**
     * Set to true if the generation should generate a date and time
     * automatically.
     * 
     * @param timeOfMessage
     */
    public void setTimeOfMessage(boolean timeOfMessage) {
        this.timeOfMessage = timeOfMessage;
    }

    /**
     * Populate a Template
     * 
     * @param aTemplate
     * @param aProfile
     */
    public void populateTemplate(XmlMessage aTemplate, Profile aProfile) {
        Pattern pattern = Pattern.compile("^(.*):(.*):(.*):(.*):(.*):(.*)$");
        XmlCursor cursor = null;
        String xpath = "//*[@Populate]";
        XmlObject[] text = aTemplate.getDocument().selectPath(xpath);
        for (int i = 0; i < text.length; i++) {
            cursor = text[i].newCursor();
            Matcher match = pattern.matcher(cursor.getTextValue());
            if (match.matches()) {
                // Process segment groups
                List<String> segmentGroups = new ArrayList<String>();
                if (match.group(1) != null) {
                }
                ProfileElement pe = new ProfileElement(segmentGroups,
                        match.group(2), match.group(3), match.group(4),
                        match.group(5));
                int length = 3;
                try {
                    length = Integer.parseInt(cursor.getAttributeText(QName.valueOf("Length")));
                } catch (NumberFormatException nfe) {
                    length = 3;
                }
                String value = getValue(pe,
                        cursor.getAttributeText(QName.valueOf("Datatype")),
                        cursor.getAttributeText(QName.valueOf("Table")), length);
                cursor.setTextValue(value);
                cursor.removeAttribute(QName.valueOf("Populate"));
                cursor.removeAttribute(QName.valueOf("Datatype"));
                cursor.removeAttribute(QName.valueOf("Table"));
                cursor.removeAttribute(QName.valueOf("Length"));
            }
        }
        if (cursor != null) {
            cursor.dispose();
        }
    }

    /**
     * Get a value
     * 
     * @param aProfileElement
     * @param datatype
     * @param table
     * @param length
     * @return a value
     */
    private String getValue(ProfileElement aProfileElement, String datatype,
            String table, int length) {
        String value = "";
        if (table == null || table.equals("")) {
            ArrayList<String> alSimpleValue = getSimpleValues(aProfileElement);
            if (alSimpleValue != null && alSimpleValue.size() > 0) {
                // Simple Value
                for (int i = 0; i < MessageGenerationConstants.POPULATION_MAX_TRIES; i++) {
                    value = alSimpleValue.get(pickRandomValue(alSimpleValue.size()));
                    if (value.length() <= length) {
                        i = MessageGenerationConstants.POPULATION_MAX_TRIES + 1;
                    }
                }
            } else {
                // Default Value
                ArrayList<String> alDefaultValue = getDefaultValues(datatype,
                        false);
                if (alDefaultValue != null) {
                    for (int i = 0; i < MessageGenerationConstants.POPULATION_MAX_TRIES; i++) {
                        value = alDefaultValue.get(pickRandomValue(alDefaultValue.size()));
                        if (value.length() <= length) {
                            i = MessageGenerationConstants.POPULATION_MAX_TRIES + 1;
                        }
                    }
                }
            }
        } else {
            ArrayList<String> alTableValue = getTableValues(table);
            if (alTableValue != null && alTableValue.size() > 0) {
                // Table Value
                for (int i = 0; i < MessageGenerationConstants.POPULATION_MAX_TRIES; i++) {
                    value = alTableValue.get(pickRandomValue(alTableValue.size()));
                    if (value.length() <= length) {
                        i = MessageGenerationConstants.POPULATION_MAX_TRIES + 1;
                    }
                }
            } else {
                // Default Value
                ArrayList<String> alDefaultValue = getDefaultValues(datatype,
                        true);
                if (alDefaultValue != null) {
                    for (int i = 0; i < MessageGenerationConstants.POPULATION_MAX_TRIES; i++) {
                        value = alDefaultValue.get(pickRandomValue(alDefaultValue.size()));
                        if (value.length() <= length) {
                            i = MessageGenerationConstants.POPULATION_MAX_TRIES + 1;
                        }
                    }
                }
            }
        }
        return value;
    }

    /**
     * Populate a Message
     * 
     * @param aMessage
     * @param aProfile
     */
    public void populate(XmlMessage aMessage, Profile aProfile) {
        // Pattern pattern = Pattern.compile("^(.*):(.*):(.*):(.*):(.*):(.*)$");
        Pattern pattern = Pattern.compile("^(.*):(.*):(.*):(.*):(.*)$");
        XmlCursor cursor = null;
        String xpath = "//*[@Populate]";
        XmlObject[] text = aMessage.getDocument().selectPath(xpath);
        for (int i = 0; i < text.length; i++) {
            cursor = text[i].newCursor();
            Matcher match = pattern.matcher(cursor.getTextValue().trim());
            // System.out.println(cursor.getTextValue().trim());
            if (match.matches()) {
                // Process segment groups
                List<String> segmentGroups = new ArrayList<String>();
                if (!"".equals(match.group(1))) {
                    // System.out.println(match.group(1));
                    Scanner scanner = new Scanner(match.group(1)).useDelimiter("/");
                    while (scanner.hasNext()) {
                        segmentGroups.add(scanner.next());
                    }
                }
                ProfileElement pe = new ProfileElement(segmentGroups,
                        match.group(2), match.group(3), match.group(4),
                        match.group(5));
                pe.setXmlObject(aProfile);
                String value = populateElement(pe);
                cursor.setTextValue(value);
                cursor.removeAttribute(QName.valueOf("Populate"));
            }
        }
        if (cursor != null) {
            cursor.dispose();
        }
    }

    /**
     * Find a value for a profile element
     * 
     * @param aProfileElement
     */
    private String populateElement(ProfileElement aProfileElement) {
        String value = "";
        int length = 3;
        try {
            length = Integer.parseInt(aProfileElement.getAttributeValue("Length"));
        } catch (NumberFormatException nfe) {
            length = 3;
        }
        String table = aProfileElement.getAttributeValue("Table");
        String datatype = aProfileElement.getAttributeValue("Datatype");
        boolean foundValue = false;
        if (table == null || table.equals("")) {
            ArrayList<String> alSimpleValue = getSimpleValues(aProfileElement);
            if (alSimpleValue != null && alSimpleValue.size() > 0) {
                // Simple Value
                for (int i = 0; i < MessageGenerationConstants.POPULATION_MAX_TRIES; i++) {
                    value = alSimpleValue.get(pickRandomValue(alSimpleValue.size()));
                    if (value.length() <= length) {
                        i = MessageGenerationConstants.POPULATION_MAX_TRIES + 1;
                        foundValue = true;
                    }
                }
            }
            if (!foundValue) {
                // Default Value
                ArrayList<String> alDefaultValue = getDefaultValues(datatype,
                        false);
                if (alDefaultValue != null) {
                    for (int i = 0; i < MessageGenerationConstants.POPULATION_MAX_TRIES; i++) {
                        value = alDefaultValue.get(pickRandomValue(alDefaultValue.size()));
                        if (value.length() <= length) {
                            i = MessageGenerationConstants.POPULATION_MAX_TRIES + 1;
                        }
                    }
                }
            }
        } else {
            ArrayList<String> alTableValue = getTableValues(table);
            // if (alTableValue == null || alTableValue.size() == 0) {
            // alTableValue = getTableHL7Values(table);
            // }
            if (alTableValue != null && alTableValue.size() > 0) {
                // Table Value
                for (int i = 0; i < MessageGenerationConstants.POPULATION_MAX_TRIES; i++) {
                    value = alTableValue.get(pickRandomValue(alTableValue.size()));
                    if (value.length() <= length) {
                        i = MessageGenerationConstants.POPULATION_MAX_TRIES + 1;
                        foundValue = true;
                    }
                }
            }
            if (!foundValue) {
                // Default Value
                ArrayList<String> alDefaultValue = getDefaultValues(datatype,
                        true);
                if (alDefaultValue != null) {
                    for (int i = 0; i < MessageGenerationConstants.POPULATION_MAX_TRIES; i++) {
                        value = alDefaultValue.get(pickRandomValue(alDefaultValue.size()));
                        if (value.length() <= length) {
                            i = MessageGenerationConstants.POPULATION_MAX_TRIES + 1;
                        }
                    }
                }
            }
        }
        return value;
    }

    /**
     * Get all the values for a specific profile element
     * 
     * @param pe
     *        a ProfileElement
     * @return an ArrayList of String
     */
    private ArrayList<String> getSimpleValues(ProfileElement pe) {
        return hPrimitive.get(pe);
    }

    /**
     * Get all the table values for a specific table number
     * 
     * @param table
     * @return an ArrayList of String
     */
    private ArrayList<String> getTableValues(String table) {
        return hTable.get(table);
    }

    /**
     * Get all the HL7 table values for a specific table number
     * 
     * @param table
     * @return an ArrayList of String
     */
    // private ArrayList<String> getTableHL7Values(String table) {
    // return hTableHL7.get(table);
    // }

    /**
     * Get all the default values for a specific datatype, the boolean specify
     * if the default value is for a table element
     * 
     * @param datatype
     * @param table
     * @return an ArrayList of String
     */
    private ArrayList<String> getDefaultValues(String datatype, boolean table) {
        ArrayList<String> al = null;
        if (table) {
            al = hTableDefault.get(datatype);
        } else {
            al = hSimpleDefault.get(datatype);
        }
        return al;
    }

    /**
     * Pick one random value from an ArrayList
     * 
     * @param alSize
     *        the size of the ArrayList
     * @return the index of the picked value
     */
    private int pickRandomValue(int alSize) {
        Random generator = new Random();
        int index = generator.nextInt(alSize);
        generator = null;
        return index;
    }

    /**
     * Hash the HashMap for the primitive values
     */
    private void hashPrimitiveValues() {
        hPrimitive = new HashMap<ProfileElement, ArrayList<String>>();
        for (PrimitiveValueLibraryDocument primitive : primitives) {
            Iterator<PrimitiveValueLibraryDocument.PrimitiveValueLibrary.ProfileElement> profileElements = primitive.getPrimitiveValueLibrary().getProfileElementList().iterator();
            while (profileElements.hasNext()) {
                PrimitiveValueLibraryDocument.PrimitiveValueLibrary.ProfileElement peXSD = profileElements.next();
                ProfileElement pe = new ProfileElement(null,
                        peXSD.getSegment(), peXSD.getField(),
                        peXSD.getComponent(), peXSD.getSubComponent());
                ArrayList<String> al = hPrimitive.get(pe);
                if (al == null) {
                    al = new ArrayList<String>();
                    hPrimitive.put(pe, al);
                }
                Iterator<PrimitiveValueLibraryDocument.PrimitiveValueLibrary.ProfileElement.Item> items = peXSD.getItemList().iterator();
                while (items.hasNext()) {
                    PrimitiveValueLibraryDocument.PrimitiveValueLibrary.ProfileElement.Item valueXSD = items.next();
                    String value = valueXSD.getValue();
                    if (!al.contains(value)) {
                        al.add(value);
                    }
                }
            }
        }
    }

    /**
     * Hash the HashMap of table values
     */
    private void hashTableValues() {
        hTable = new HashMap<String, ArrayList<String>>();
        for (TableLibraryDocument table : tables) {
            Iterator<TableLibraryDocument.TableLibrary.TableDefinition> tableDefinitions = table.getTableLibrary().getTableDefinitionList().iterator();
            while (tableDefinitions.hasNext()) {
                TableLibraryDocument.TableLibrary.TableDefinition td = tableDefinitions.next();
                String tableId = td.getId();
                ArrayList<String> al = hTable.get(tableId);
                if (al == null) {
                    al = new ArrayList<String>();
                    hTable.put(tableId, al);
                }
                Iterator<TableLibraryDocument.TableLibrary.TableDefinition.TableElement> tableElements = td.getTableElementList().iterator();
                while (tableElements.hasNext()) {
                    TableLibraryDocument.TableLibrary.TableDefinition.TableElement te = tableElements.next();
                    String value = te.getCode();
                    if (!al.contains(value)) {
                        al.add(value);
                    }
                }
            }
        }
    }

    /**
     * Init the HashMap of HL7 table values
     */
    // private void initTableHL7Values() {
    // hTableHL7 = new HashMap<String, ArrayList<String>>();
    // Iterator<TableLibraryDocument.TableLibrary.TableDefinition>
    // tableDefinitions =
    // tablesHL7.getTableLibrary().getTableDefinitionList().iterator();
    // while (tableDefinitions.hasNext()) {
    // TableLibraryDocument.TableLibrary.TableDefinition td =
    // tableDefinitions.next();
    // String tableId = td.getId();
    // ArrayList<String> al = hTableHL7.get(tableId);
    // if (al == null) {
    // al = new ArrayList<String>();
    // hTableHL7.put(tableId, al);
    // }
    // Iterator<TableLibraryDocument.TableLibrary.TableDefinition.TableElement>
    // tableElements = td.getTableElementList().iterator();
    // while (tableElements.hasNext()) {
    // TableLibraryDocument.TableLibrary.TableDefinition.TableElement te =
    // tableElements.next();
    // String value = te.getCode();
    // if (!al.contains(value)) {
    // al.add(value);
    // }
    // }
    // }
    // }

    /**
     * Hash the HashMap of default values
     */
    private void hashDefaultValues() {
        for (DefaultValueLibraryDocument _default : defaults) {
            hSimpleDefault = new HashMap<String, ArrayList<String>>();
            Iterator<DefaultValueLibraryDocument.DefaultValueLibrary.SimpleValue> simpleValues = _default.getDefaultValueLibrary().getSimpleValueList().iterator();
            while (simpleValues.hasNext()) {
                DefaultValueLibraryDocument.DefaultValueLibrary.SimpleValue simpleValue = simpleValues.next();
                String datatype = simpleValue.getDatatype();
                ArrayList<String> al = hSimpleDefault.get(datatype);
                if (al == null) {
                    al = new ArrayList<String>();
                    hSimpleDefault.put(datatype, al);
                }
                Iterator<DefaultValueLibraryDocument.DefaultValueLibrary.SimpleValue.Item> items = simpleValue.getItemList().iterator();
                while (items.hasNext()) {
                    DefaultValueLibraryDocument.DefaultValueLibrary.SimpleValue.Item valueXSD = items.next();
                    String value = valueXSD.getValue();
                    if (!al.add(value)) {
                        al.add(value);
                    }
                }
            }
            hTableDefault = new HashMap<String, ArrayList<String>>();
            Iterator<DefaultValueLibraryDocument.DefaultValueLibrary.TableValue> tableValues = _default.getDefaultValueLibrary().getTableValueList().iterator();
            while (tableValues.hasNext()) {
                DefaultValueLibraryDocument.DefaultValueLibrary.TableValue tableValue = tableValues.next();
                String datatype = tableValue.getDatatype();
                ArrayList<String> al = hTableDefault.get(datatype);
                if (al == null) {
                    al = new ArrayList<String>();
                    hTableDefault.put(datatype, al);
                }
                Iterator<DefaultValueLibraryDocument.DefaultValueLibrary.TableValue.Item> items = tableValue.getItemList().iterator();
                while (items.hasNext()) {
                    DefaultValueLibraryDocument.DefaultValueLibrary.TableValue.Item valueXSD = items.next();
                    String value = valueXSD.getValue();
                    if (!al.add(value)) {
                        al.add(value);
                    }
                }
            }
        }
    }

    /**
     * Set a value at the specific xpath location
     * 
     * @param aMessage
     * @param xpath
     * @param value
     * @param mark
     * @return a GenerationError if there is one; null otherwise
     */
    private GenerationError setValueAtXPath(XmlMessage aMessage, String xpath,
            String value, boolean mark) {
        GenerationError error = null;
        XmlCursor cursor = null;
        XmlObject[] rs = aMessage.getDocument().selectPath(xpath);
        if (rs.length == 0) {
            error = GenerationError.NON_EXISTING;
        } else {
            for (int i = 0; i < rs.length; i++) {
                cursor = rs[i].newCursor();
                // Check that the element is a primitive
                // cursor.getAttributeText(QName.valueOf("Varies")) != null
                // ||
                if ("".equals(cursor.getAttributeText(QName.valueOf("Varies")))
                        || !cursor.toFirstChild()) {
                    cursor.setTextValue(value);
                    if (mark) {
                        cursor.toFirstAttribute();
                        cursor.toNextToken();
                        cursor.insertAttribute("Marked");
                        cursor.toParent();
                        // Mark the parents
                        while (cursor.toParent()
                                && cursor.getName() != null
                                && cursor.getAttributeText(QName.valueOf("Marked")) == null) {
                            cursor.toFirstAttribute();
                            cursor.toNextToken();
                            cursor.insertAttribute("Marked");
                            cursor.toParent();
                        }
                    }
                } else {
                    error = GenerationError.NON_PRIMITIVE;
                }
                cursor = null;
            }
        }
        rs = null;
        return error;
    }

    /**
     * Set a value at the message location, and mark the parents
     * 
     * @param aMessage
     * @param aDataValueLocationItem
     * @param mark
     * @return a GenerationError if there is one; null otherwise
     */
    public GenerationError populateFixedData(XmlMessage aMessage,
            DataValueLocationItemGeneration aDataValueLocationItem, boolean mark) {
        String xpath = getXPath(aDataValueLocationItem);
        int idx = pickRandomValue(aDataValueLocationItem.getValueList().size());
        GenerationError error = setValueAtXPath(aMessage, xpath,
                aDataValueLocationItem.getValueArray(idx), mark);
        return error;
    }

    /**
     * Transform a DataValueLocationItem into an XPath expression
     * 
     * @param aDataValueLocationItem
     * @return an XPath expression
     */
    private String getXPath(
            DataValueLocationItemGeneration aDataValueLocationItem) {
        StringBuffer sb = new StringBuffer("/");
        SegmentGroup sg = aDataValueLocationItem.getLocation().getSegmentGroup();
        Segment s = null;
        while (sg != null) {
            int segmentGroupInstanceNumber = sg.getInstanceNumber() == 0 ? 1
                    : sg.getInstanceNumber();
            sb.append("/*[ends-with(name(), '.").append(sg.getName()).append(
                    "')][").append(segmentGroupInstanceNumber).append("]");
            if (sg != null) {
                s = sg.getSegment();
            }
            sg = sg.getSegmentGroup();
        }
        if (s == null) {
            s = aDataValueLocationItem.getLocation().getSegment();
        }
        if (s != null) {
            int segmentInstanceNumber = s.getInstanceNumber() == 0 ? 1
                    : s.getInstanceNumber();
            sb.append("/*:").append(s.getName()).append("[").append(
                    segmentInstanceNumber).append("]");
            Field f = s.getField();
            if (f != null) {
                int fieldSequenceNumber = f.getPosition();
                if (fieldSequenceNumber != 0) {
                    int fieldInstanceNumber = f.getInstanceNumber() == 0 ? 1
                            : f.getInstanceNumber();
                    sb.append("/*[ends-with(name(), '.").append(
                            fieldSequenceNumber).append("')][").append(
                            fieldInstanceNumber).append("]");
                }
                Component c = f.getComponent();
                if (c != null) {
                    int componentSequenceNumber = c.getPosition();
                    if (componentSequenceNumber != 0) {
                        // int componentInstanceNumber = c.getInstanceNumber()
                        // == 0 ? 1
                        // : c.getInstanceNumber();
                        sb.append("/*[ends-with(name(), '.").append(
                                componentSequenceNumber).append("')]");
                    }
                    SubComponent sc = c.getSubComponent();
                    if (sc != null) {
                        int subcomponentSequenceNumber = sc.getPosition();
                        if (subcomponentSequenceNumber != 0) {
                            // int subComponentInstanceNumber =
                            // sc.getInstanceNumber() == 0
                            // ? 1 : sc.getInstanceNumber();
                            sb.append("/*[ends-with(name(), '.").append(
                                    subcomponentSequenceNumber).append("')]");
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Populate special values that should be populated from profile values
     * 
     * @param aMessage
     * @param aProfile
     * @param mark
     */
    public void populateProfileData(XmlMessage aMessage, Profile aProfile,
            boolean mark) {
        // Version
        setValueAtXPath(aMessage, "//*:MSH/*:MSH.12/*:VID.1",
                aProfile.getHl7VersionAsString(), mark);
        // MSH9.1
        setValueAtXPath(aMessage, "//*:MSH/*:MSH.9/*:MSG.1",
                aProfile.getMessageType(), mark);
        // MSH9.2
        setValueAtXPath(aMessage, "//*:MSH/*:MSH.9/*:MSG.2",
                aProfile.getMessageEvent(), mark);
        // MSH9.3
        setValueAtXPath(aMessage, "//*:MSH/*:MSH.9/*:MSG.3",
                aProfile.getMessageStructureID(), mark);
        // EVN.1
        setValueAtXPath(aMessage, "//*:EVN/*:EVN.1",
                aProfile.getMessageEvent(), mark);
        // MSH7.1
        if (timeOfMessage) {
            setTimeOfMessage(aMessage, mark);
        }
    }

    /**
     * Set the message control Id
     * 
     * @param template
     * @param mark
     *        boolean used by the SimpleMessageGeneration
     */
    public void setMessageControlId(XmlMessage template, boolean mark) {
        DataValueLocationItemGeneration dvli = MessageInstanceSpecificValuesGeneration.Factory.newInstance().addNewDataValueLocationItem();
        MessageElement location = dvli.addNewLocation();
        Segment s = location.addNewSegment();
        s.setName("MSH");
        Field f = s.addNewField();
        f.setPosition(10);
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSS");
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        date.setTime(cal.getTimeInMillis());
        StringBuffer sb = new StringBuffer();
        sb.append("NIST-").append(sdf.format(date));
        dvli.addNewValue().setStringValue(sb.toString());
        populateFixedData(template, dvli, mark);
    }

    /**
     * Set the separators
     * 
     * @param template
     * @param mark
     *        boolean used by the SimpleMessageGeneration
     */
    public void setMessageSeparators(XmlMessage template, boolean mark) {
        // MSH.1
        DataValueLocationItemGeneration dvli = MessageInstanceSpecificValuesGeneration.Factory.newInstance().addNewDataValueLocationItem();
        MessageElement location = dvli.addNewLocation();
        Segment s = location.addNewSegment();
        s.setName("MSH");
        Field f = s.addNewField();
        f.setPosition(1);
        dvli.addNewValue().setStringValue("|");
        populateFixedData(template, dvli, mark);
        // MSH.2
        dvli = MessageInstanceSpecificValuesGeneration.Factory.newInstance().addNewDataValueLocationItem();
        location = dvli.addNewLocation();
        s = location.addNewSegment();
        s.setName("MSH");
        f = s.addNewField();
        f.setPosition(2);
        dvli.addNewValue().setStringValue("^~\\&");
        populateFixedData(template, dvli, mark);
    }

    /**
     * Set the time of message
     * 
     * @param template
     * @param mark
     *        boolean used by the SimpleMessageGeneration
     */
    public void setTimeOfMessage(XmlMessage template, boolean mark) {
        DataValueLocationItemGeneration dvli = MessageInstanceSpecificValuesGeneration.Factory.newInstance().addNewDataValueLocationItem();
        MessageElement location = dvli.addNewLocation();
        Segment s = location.addNewSegment();
        s.setName("MSH");
        Field f = s.addNewField();
        f.setPosition(7);
        Component c = f.addNewComponent();
        c.setPosition(1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        date.setTime(cal.getTimeInMillis());
        dvli.addNewValue().setStringValue(sdf.format(date));
        populateFixedData(template, dvli, mark);
    }

}
