/*
 * NIST Healthcare Core
 * Profile.java Jun 19, 2006
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.profile;

import gov.nist.healthcare.core.Constants;
import gov.nist.healthcare.core.Constants.ProfileType;
import gov.nist.healthcare.core.MalformedProfileException;
import gov.nist.healthcare.core.message.v2.xml.XmlMessage;
import gov.nist.healthcare.core.util.XmlBeansUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.values.XmlObjectBase;

/**
 * This class represents an HL7 Profile
 * 
 * @author Sydney Henrard (NIST)
 */
public class Profile implements Cloneable {

    private String id;
    private XmlObject profileDoc;
    private File profileFile;
    private int cardinalityUpperbound = 3;
    private XmlMessage template;
    private Map<String, PSegment> segments;

    /**
     * Create a profile from a File
     * 
     * @param xmlProfile
     * @throws MalformedProfileException
     */
    public Profile(File xmlProfile) throws MalformedProfileException {
        try {
            this.profileFile = xmlProfile;
            profileDoc = XmlObject.Factory.parse(xmlProfile,
                    (new XmlOptions()).setLoadLineNumbers());
            handleProfileType();
            load();
        } catch (Exception e) {
            throw new MalformedProfileException(e.getClass() + " "
                    + e.getMessage());
        }
    }

    /**
     * Create a profile from a File and assigned to it an id
     * 
     * @param id
     * @param xmlProfile
     * @throws MalformedProfileException
     */
    @Deprecated
    public Profile(String id, File xmlProfile) throws MalformedProfileException {
        try {
            this.id = id;
            this.profileFile = xmlProfile;
            profileDoc = XmlObject.Factory.parse(xmlProfile,
                    (new XmlOptions()).setLoadLineNumbers());
            handleProfileType();
            load();
        } catch (Exception e) {
            throw new MalformedProfileException(e.getClass() + " "
                    + e.getMessage());
        }
    }

    /**
     * Create a profile from an XmlObject
     * 
     * @param xmloProfile
     * @throws MalformedProfileException
     */
    public Profile(XmlObject xmloProfile) throws MalformedProfileException {
        try {
            profileDoc = xmloProfile;
            handleProfileType();
            load();
        } catch (Exception e) {
            throw new MalformedProfileException(e.getClass() + " "
                    + e.getMessage());
        }
    }

    /**
     * Create a profile from an XmlObject and assigne to it an id
     * 
     * @param id
     * @param xmloProfile
     * @throws MalformedProfileException
     */
    @Deprecated
    public Profile(String id, XmlObject xmloProfile)
            throws MalformedProfileException {
        try {
            this.id = id;
            profileDoc = xmloProfile;
            handleProfileType();
            load();
        } catch (Exception e) {
            throw new MalformedProfileException(e.getClass() + " "
                    + e.getMessage());
        }
    }

    /**
     * Create a profile from a String
     * 
     * @param strProfile
     * @throws MalformedProfileException
     */
    public Profile(String strProfile) throws MalformedProfileException {
        try {
            profileDoc = XmlObject.Factory.parse(strProfile,
                    (new XmlOptions()).setLoadLineNumbers());
            handleProfileType();
            load();
        } catch (Exception e) {
            throw new MalformedProfileException(e.getClass() + " "
                    + e.getMessage());
        }
    }

    /**
     * Create a profile from a String and assigne to it an id
     * 
     * @param id
     * @param strProfile
     * @throws MalformedProfileException
     */
    @Deprecated
    public Profile(String id, String strProfile)
            throws MalformedProfileException {
        try {
            this.id = id;
            profileDoc = XmlObject.Factory.parse(strProfile,
                    (new XmlOptions()).setLoadLineNumbers());
            handleProfileType();
            load();
        } catch (Exception e) {
            throw new MalformedProfileException(e.getClass() + " "
                    + e.getMessage());
        }
    }

    /**
     * Create a profile from an InputStream
     * 
     * @param isProfile
     * @throws MalformedProfileException
     */
    public Profile(InputStream isProfile) throws MalformedProfileException {
        try {
            profileDoc = XmlObject.Factory.parse(isProfile,
                    (new XmlOptions()).setLoadLineNumbers());
            handleProfileType();
            load();
        } catch (Exception e) {
            throw new MalformedProfileException(e.getClass() + " "
                    + e.getMessage());
        }
    }

    /**
     * Handle MWB profile
     * 
     * @throws MalformedProfileException
     */
    private void handleProfileType() throws MalformedProfileException {
        if (getProfileType() == ProfileType.MWB_PROFILE) {
            transformMWB2Impl();
        }
    }

    /**
     * Return the profile type
     * 
     * @return the profile type
     * @throws MalformedProfileException
     */
    private ProfileType getProfileType() throws MalformedProfileException {
        XmlCursor cursor = profileDoc.newCursor();
        cursor.toFirstChild();
        if (cursor.getName().getLocalPart().equals("Specification")) {
            return ProfileType.MWB_PROFILE;
        }
        if (cursor.getName().getLocalPart().equals("HL7v2xConformanceProfile")) {
            return ProfileType.IMPLEMENTATION_PROFILE;
        }
        throw new MalformedProfileException(
                "The profile type can't be figured out");
    }

    /**
     * Transform a MWB profile to an implementation profile
     * 
     * @throws MalformedProfileException
     */
    private void transformMWB2Impl() throws MalformedProfileException {
        try {
            StreamSource xsltStream = new StreamSource(
                    getClass().getClassLoader().getResourceAsStream(
                            Constants.XSLT_MWB2IMPL_RESOURCE));
            Transformer t = TransformerFactory.newInstance().newTransformer(
                    xsltStream);
            StreamSource src = new StreamSource(profileDoc.newInputStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            t.transform(src, new StreamResult(out));
            profileDoc = XmlObject.Factory.parse(
                    new ByteArrayInputStream(out.toByteArray()),
                    (new XmlOptions()).setLoadLineNumbers());
        } catch (Exception e) {
            throw new MalformedProfileException(e.getClass() + " "
                    + e.getMessage());
        }
    }

    /**
     * Get the profile id
     * 
     * @return the profile id
     */
    public String getId() {
        return id;
    }

    /**
     * Set the profile id
     * 
     * @param id
     *        the profile id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the document root of the XML representation of the profile.
     * 
     * @return the document root of the XML representatin of the profile
     */
    public XmlObject getDocument() {
        return profileDoc;
    }

    /**
     * Define the upperbound for a cardinality with an unlimited value
     * (designated with a *). There is a default defined for this setting.
     * 
     * @param upperbound
     *        an integer that defines the upperbound for a cardinality that is
     *        defined in the profile to be unlimited.
     * @throws IllegalArgumentException
     *         is thrown if the cardinality is negative
     */
    public void setCardinalityUpperbound(int upperbound)
            throws IllegalArgumentException {
        if (upperbound <= 0) {
            throw new IllegalArgumentException(
                    "The cardinality upperbound has to be a positive number");
        }
        cardinalityUpperbound = upperbound;
    }

    /**
     * Return the value for the cardinality upperbound.
     * 
     * @return the cardinality upperbound
     */
    public int getCardinalityUpperbound() {
        return cardinalityUpperbound;
    }

    /**
     * Returns a clone of this profile.
     * 
     * @return a clone of this profile.
     */
    @Override
    public synchronized Object clone() {
        Profile p = null;
        try {
            p = new Profile(id, profileDoc.copy());
        } catch (MalformedProfileException e) {
            e.printStackTrace();
        }
        return p;
    }

    /**
     * Get the value of the element in the profile at the specified location.
     * 
     * @param location
     *        the location of the element in the profile
     * @return a <code>String</code> of the value if successful; null otherwise.
     */
    public String getValue(String location) {
        String value = null;
        XmlObject[] rs = null;
        rs = profileDoc.selectPath(location);
        if (rs.length == 1) {
            value = XmlBeansUtils.getValueFromXmlObject(rs[0]);
        } else if (rs.length == 0) {
            throw new IllegalArgumentException("The XPath " + location
                    + " expression returns no result");
        } else {
            throw new IllegalArgumentException("The XPath " + location
                    + " expression returns several nodes");
        }
        return value;
    }

    // TODO: modify with XmlBeansUtils
    public List<String> getValues(String location) {
        ArrayList<String> results = new ArrayList<String>();
        XmlObject[] rs = null;
        rs = profileDoc.selectPath(location);
        for (int i = 0; i < rs.length; i++) {
            XmlCursor cursor = rs[i].newCursor();
            if (cursor.isAttr() || !cursor.toFirstChild()) {
                XmlObjectBase n = (XmlObjectBase) rs[i];
                results.add(n.getStringValue());
            } else {
                XmlOptions xmlOptions = new XmlOptions();
                results.add(rs[0].xmlText(xmlOptions));
            }
        }
        return results;
    }

    /**
     * Get the HL7 version as a String
     * 
     * @return the version
     */
    public String getHl7VersionAsString() {
        XmlCursor xmlCursor = profileDoc.newCursor();
        if (xmlCursor.toChild("HL7v2xConformanceProfile")) {
            return xmlCursor.getAttributeText(QName.valueOf("HL7Version"));
        }
        throw new IllegalArgumentException("Can't find the HL7 version.");
    }

    /**
     * Get the profile version as a String
     * 
     * @return the version
     */
    public String getVersion() {
        XmlCursor xmlCursor = profileDoc.newCursor();
        if (xmlCursor.toChild("HL7v2xConformanceProfile")) {
            if (xmlCursor.toChild("MetaData")) {
                return xmlCursor.getAttributeText(QName.valueOf("Version"));
            }
        }
        throw new IllegalArgumentException(
                "Can't find the version in the profile. "
                        + "MetaData element is missing.");
    }

    /**
     * Get the profile type
     * 
     * @return the profile type
     */
    public String getType() {
        XmlCursor xmlCursor = profileDoc.newCursor();
        if (xmlCursor.toChild("HL7v2xConformanceProfile")) {
            return xmlCursor.getAttributeText(QName.valueOf("ProfileType"));
        }
        throw new IllegalArgumentException("Can't find the profile type.");
    }

    /**
     * Get the message type
     * 
     * @return the message type
     */
    public String getMessageType() {
        XmlCursor xmlCursor = profileDoc.newCursor();
        if (xmlCursor.toChild("HL7v2xConformanceProfile")) {
            if (xmlCursor.toChild("HL7v2xStaticDef")) {
                return xmlCursor.getAttributeText(QName.valueOf("MsgType"));
            }
        }
        return null;
    }

    /**
     * Get the message event
     * 
     * @return the messsage event
     */
    public String getMessageEvent() {
        XmlCursor xmlCursor = profileDoc.newCursor();
        if (xmlCursor.toChild("HL7v2xConformanceProfile")) {
            if (xmlCursor.toChild("HL7v2xStaticDef")) {
                return xmlCursor.getAttributeText(QName.valueOf("EventType"));
            }
        }
        return null;
    }

    /**
     * Get the message structure id
     * 
     * @return the message structure id
     */
    public String getMessageStructureID() {
        XmlCursor xmlCursor = profileDoc.newCursor();
        if (xmlCursor.toChild("HL7v2xConformanceProfile")) {
            if (xmlCursor.toChild("HL7v2xStaticDef")) {
                return xmlCursor.getAttributeText(QName.valueOf("MsgStructID"));
            }
        }
        return null;
    }

    /**
     * Get the name of the profile HL7v2xConformanceProfile/@Name
     * 
     * @return The name of the profile
     */
    public String getName() {
        String name = "";
        XmlCursor xmlCursor = profileDoc.newCursor();
        if (xmlCursor.toChild("HL7v2xConformanceProfile")) {
            if (xmlCursor.toChild("MetaData")) {
                name = xmlCursor.getAttributeText(QName.valueOf("Name"));
            }
        }
        return name;
    }

    /**
     * Get the organization of the profile HL7v2xConformanceProfile/@OrgName
     * 
     * @return The organization of the profile
     */
    public String getOrganization() {
        String orgName = "";
        XmlCursor xmlCursor = profileDoc.newCursor();
        if (xmlCursor.toChild("HL7v2xConformanceProfile")) {
            if (xmlCursor.toChild("MetaData")) {
                orgName = xmlCursor.getAttributeText(QName.valueOf("OrgName"));
            }
        }
        return orgName;
    }

    /**
     * Get the filename if the profile has been created from a file
     * 
     * @return The filename of the profile; null otherwise
     */
    public String getFilename() {
        String filename = null;
        if (profileFile != null) {
            filename = profileFile.getAbsolutePath();
        }
        return filename;
    }

    /**
     * Set a template for SimpleMessageGeneration
     * 
     * @param aTemplate
     *        an XmlMessage
     */
    // TODO: View if it can be removed
    public void setTemplate(XmlMessage aTemplate) {
        template = aTemplate;
    }

    /**
     * Get a template for SimpleMessageGeneration
     * 
     * @return a template
     */
    public XmlMessage getTemplate() {
        return template;
    }

    /**
     * List the segment that have a unique definition.
     * 
     * @return a Map with the segment as a key and the corresponding XmlObject
     */
    public Map<String, XmlObject> getUniqueSegments() {
        Map<String, XmlObject> uniqueSegments = new HashMap<String, XmlObject>();
        String xpath = "//Segment[@Usage != 'X']";
        Map<String, List<XmlObject>> hUniqueSegmentDefinition = new HashMap<String, List<XmlObject>>();
        XmlObject[] rs = profileDoc.selectPath(xpath);
        for (XmlObject segment : rs) {
            String segmentName = segment.newCursor().getAttributeText(
                    QName.valueOf("Name"));
            List<XmlObject> uniqueSegment = hUniqueSegmentDefinition.get(segmentName);
            if (uniqueSegment == null) {
                uniqueSegment = new ArrayList<XmlObject>();
                hUniqueSegmentDefinition.put(segmentName, uniqueSegment);
            }
            uniqueSegment.add(segment);
        }
        Iterator<String> it = hUniqueSegmentDefinition.keySet().iterator();
        while (it.hasNext()) {
            String segmentName = it.next();
            List<XmlObject> segmentInstances = hUniqueSegmentDefinition.get(segmentName);
            if (segmentInstances.size() == 1) {
                uniqueSegments.put(segmentName, segmentInstances.get(0));
            }
        }
        return uniqueSegments;
    }

    /**
     * Load the profile into a special internal representation to handles names
     */
    private void load() {
        segments = new HashMap<String, PSegment>();
        StringBuffer sb = new StringBuffer();
        XmlCursor cursor = profileDoc.newCursor();
        boolean end = false;
        do {
            if (cursor.toFirstChild()) {
                // Do Nothing
            } else {
                while (!end && !cursor.toNextSibling()) {
                    end = !cursor.toParent();
                }
            }
            String elementName = cursor.getName() != null ? cursor.getName().getLocalPart()
                    : "";
            String name = cursor.getAttributeText(QName.valueOf("Name"));
            if ("SegGroup".equals(elementName)) {
                sb.append("/").append(name);
            } else if ("Segment".equals(elementName)) {
                sb.append("/").append(name);
                cursor.push();
                PSegment pSegment = new PSegment(cursor);
                segments.put(sb.toString(), pSegment);
                sb.delete(0, sb.length());
                cursor.pop();
            }
        } while (!end);
    }

    /**
     * Get the names for a message location
     * 
     * @param path
     *        a path with form PID[1].2[3].4.5 or /PID[1].2[3].4.5 or
     *        /SEGMENT_GROUP/PID[1].2[3].4.5
     * @return a list of names (String) 1st: Segment, 2nd: Field, 3rd:
     *         Component, 4th: SubComponent, the following items contains the
     *         segment group
     */
    public List<String> getNames(String path) {
        List<String> names = null;
        Pattern p = Pattern.compile("([A-Z0-9]{3})\\[(?:(\\d+|\\*))\\]"
                + "(?:\\.(\\d+)\\[(?:(\\d+|\\*))\\]"
                + "(?:\\.(\\d+)(?:\\.(\\d+))?)?)?");
        String[] tokens = path.split("/");
        String location = "";
        List<String> segmentGroupNames = new ArrayList<String>();
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();
            if (p.matcher(token).matches()) {
                location = token;
            } else if (!"".equals(token) && token.indexOf(".") == 0) {
                segmentGroupNames.add(token.substring(1));
            }
        }
        Matcher m = p.matcher(location);
        if (m.matches()) {
            String segment = m.group(1);
            List<PSegment> pSegments = new ArrayList<PSegment>();
            int countSegment = 0;
            for (String key : segments.keySet()) {
                if (key.endsWith(segment)) {
                    countSegment++;
                    pSegments.add(segments.get(key));
                }
            }
            if (countSegment == 1 && pSegments.size() == 1) {
                names = getNames(pSegments.get(0), m);
            } else if (countSegment == 2 && pSegments.size() == 2) {
                // If there are two segments with the same name (e.g. OBX)
                // we only keep the names if they all matching
                List<String> names_1 = getNames(pSegments.get(0), m);
                List<String> names_2 = getNames(pSegments.get(1), m);
                if (names_1.equals(names_2)) {
                    names = names_1;
                }
            }
        }
        if (segmentGroupNames.size() > 0) {
            if (names == null) {
                names = new ArrayList<String>();
                names.add(null);
                names.add(null);
                names.add(null);
                names.add(null);
            }
            names.addAll(segmentGroupNames);
        }
        return names;
    }

    /**
     * Get the names from the PSegment
     * 
     * @param pSegment
     * @param m
     *        a Matcher
     * @return a list of names (String) 1st: Segment, 2nd: Field, 3rd:
     *         Component, 4th: SubComponent
     */
    private List<String> getNames(PSegment pSegment, Matcher m) {
        List<String> names = new ArrayList<String>();
        names.add(pSegment.getName());
        String field = m.group(3);
        if (field != null) {
            int iField = Integer.parseInt(field);
            PField pField = pSegment.getField(iField);
            if (pField != null) {
                names.add(pField.getName());
                String component = m.group(5);
                if (component != null) {
                    int iComponent = Integer.parseInt(component);
                    PComponent pComponent = pField.getComponent(iComponent);
                    if (pComponent != null) {
                        names.add(pComponent.getName());
                        String subComponent = m.group(6);
                        if (subComponent != null) {
                            int iSubComponent = Integer.parseInt(subComponent);
                            PSubComponent pSubComponent = pComponent.getSubComponent(iSubComponent);
                            if (pSubComponent != null) {
                                names.add(pSubComponent.getName());
                            }
                        }
                    }
                }
            }
        }
        for (int i = names.size(); i < 4; i++) {
            names.add(null);
        }
        return names;
    }

}
