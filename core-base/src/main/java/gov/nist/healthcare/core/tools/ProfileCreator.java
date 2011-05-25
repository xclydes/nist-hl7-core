package gov.nist.healthcare.core.tools;

import gov.nist.healthcare.core.profile.Profile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

public class ProfileCreator {

    private Connection connection;

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            ProfileCreator main = new ProfileCreator();
            main.generateProfiles("2.6", "HL7v2.6.mdb", 7);
            main.getMessageStructureIds(7);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ProfileCreator() {

    }

    private void generateProfiles(String version, String dbFile, int dbVersion)
            throws SQLException {
        String filename = String.format("D:\\home\\hl7db\\%s", dbFile);
        String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
        database += filename.trim() + ";DriverID=22;READONLY=true}";
        connection = DriverManager.getConnection(database, "", "");
        List<String> messageStructureIds = getMessageStructureIds(dbVersion);
        for (String messageStructureId : messageStructureIds) {
            createProfile(messageStructureId, dbVersion);
        }
    }

    /**
     * List of message events
     * 
     * @param dbVersion
     * @return a list of message events
     * @throws SQLException
     */
    private List<String> getMessageStructureIds(int dbVersion)
            throws SQLException {
        String messageEventsQuery = "SELECT HL7EventMessageTypes.event_code, HL7EventMessageTypes.message_typ_snd, HL7EventMessageTypes.message_structure_snd, HL7EventMessageTypes.message_typ_return, HL7EventMessageTypes.message_structure_return "
                + "FROM HL7EventMessageTypes WHERE HL7EventMessageTypes.version_id = ?";
        PreparedStatement ps = connection.prepareStatement(messageEventsQuery);
        ps.setInt(1, dbVersion);
        ResultSet rs = ps.executeQuery();
        List<String> messageStructureIds = new ArrayList<String>();
        while (rs.next()) {
            String event = rs.getString(1);
            String type = rs.getString(2);
            String messageStructureId = rs.getString(3);
            System.out.println(event + "^" + type + "^" + messageStructureId);
            if (!messageStructureIds.contains(messageStructureId)) {
                messageStructureIds.add(messageStructureId);
            }
        }
        return messageStructureIds;
    }

    private PreparedStatement psAllSegments;

    /**
     * Generate a profile
     * 
     * @throws SQLException
     */
    private Profile createProfile(String messageStructureId, int dbVersion)
            throws SQLException {
        XmlObject xml = XmlObject.Factory.newInstance();
        XmlCursor cursor = xml.newCursor();
        cursor.toNextToken();
        cursor.beginElement(messageStructureId);
        // messageStructureId = "PMU_B07";
        if (psAllSegments == null) {
            psAllSegments = connection.prepareStatement("SELECT HL7MsgStructIDSegments.seg_code, HL7MsgStructIDSegments.groupname, HL7MsgStructIDSegments.usage, HL7MsgStructIDSegments.repetitional, HL7MsgStructIDSegments.optional"
                    + " FROM HL7MsgStructIDSegments WHERE HL7MsgStructIDSegments.version_id = ? AND HL7MsgStructIDSegments.message_structure = ? ORDER BY HL7MsgStructIDSegments.seq_no");
        }
        psAllSegments.setInt(1, dbVersion);
        psAllSegments.setString(2, messageStructureId);
        ResultSet rs = psAllSegments.executeQuery();
        while (rs.next()) {
            String segmentName = rs.getString(1);
            String groupName = rs.getString(2);
            String usage = rs.getString(3);
            boolean repetitional = rs.getBoolean(4);
            boolean optional = rs.getBoolean(5);
            // System.out.println(String.format("%s.%s ", groupName,
            // segmentName));
            if (!"".equals(groupName)) {
                if (segmentName.contains("{") || segmentName.contains("[")) {
                    cursor.beginElement("SegGroup");
                    optional = segmentName.contains("[");
                    repetitional = segmentName.contains("{");
                    setAttributes(cursor, groupName, usage, repetitional,
                            optional);
                } else {
                    cursor.toNextToken();
                }
            } else {
                if (segmentName.length() != 3) {
                    if (segmentName.contains("{") || segmentName.contains("[")) {
                        cursor.toParent();
                        cursor.setAttributeText(QName.valueOf("Min"), "0");
                        cursor.setAttributeText(QName.valueOf("Max"), "*");
                        cursor.toEndToken();
                    } else if ("}".equals(segmentName)) {
                        // Do Nothing
                    } else if ("<".equals(segmentName)) {
                        // discard profile with choice
                        System.out.println("CHOICE in the profile");
                        break;
                    } else {
                        System.out.println(segmentName);
                        System.exit(0);
                    }
                } else {
                    cursor.beginElement("Segment");
                    setAttributes(cursor, segmentName, usage, repetitional,
                            optional);
                    addFields(cursor, segmentName, dbVersion);
                    cursor.toNextToken();
                }
            }
        }
        // System.out.println(xml);
        // System.exit(0);
        return null;
    }

    private void setAttributes(XmlCursor cursor, String name, String usage,
            boolean repetitional, boolean optional) {
        String min = "1";
        String max = "1";
        if (repetitional) {
            max = "*";
        }
        if (optional) {
            min = "0";
        }
        cursor.insertAttributeWithValue("Name", name);
        cursor.insertAttributeWithValue("Usage", usage);
        cursor.insertAttributeWithValue("Min", min);
        cursor.insertAttributeWithValue("Max", max);
    }

    private PreparedStatement psDatatypeElementary;

    private boolean isDatatypeElementary(String datatype, int dbVersion)
            throws SQLException {
        boolean elementary = false;
        if (psDatatypeElementary == null) {
            psDatatypeElementary = connection.prepareStatement("SELECT HL7DataStructures.elementary FROM HL7DataStructures WHERE HL7DataStructures.data_structure = ? AND HL7DataStructures.version_id = ?");
        }
        psDatatypeElementary.setString(1, datatype);
        psDatatypeElementary.setInt(2, dbVersion);
        ResultSet rs = psDatatypeElementary.executeQuery();
        while (rs.next()) {
            elementary = rs.getBoolean(1);
            break;
        }
        return elementary;
    }

    private PreparedStatement psAllFields;

    /**
     * Add all fields for a segment
     * 
     * @throws SQLException
     */
    private void addFields(XmlCursor cursor, String segmentName, int dbVersion)
            throws SQLException {
        if (psAllFields == null) {
            psAllFields = connection.prepareStatement("SELECT HL7SegmentDataElements.req_opt, HL7SegmentDataElements.repetitional, HL7SegmentDataElements.repetitions, HL7DataElements.description, HL7DataElements.data_structure, HL7DataElements.length, HL7DataElements.table_id "
                    + "FROM HL7DataElements INNER JOIN HL7SegmentDataElements ON (HL7DataElements.version_id = HL7SegmentDataElements.version_id) AND (HL7DataElements.data_item = HL7SegmentDataElements.data_item) "
                    + "WHERE HL7SegmentDataElements.seg_code = ? AND HL7SegmentDataElements.version_id = ? ORDER BY HL7SegmentDataElements.seq_no");
        }
        psAllFields.setString(1, segmentName);
        psAllFields.setInt(2, dbVersion);
        ResultSet rs = psAllFields.executeQuery();
        while (rs.next()) {
            String usage = rs.getString(1);
            String repetitional = rs.getString(2);
            int repetitions = rs.getInt(3);
            String description = rs.getString(4);
            String datatype = rs.getString(5);
            int length = rs.getInt(6);
            int tableId = rs.getInt(7);
            cursor.beginElement("Field");
            setFieldAttributes(cursor, usage, repetitional, repetitions,
                    description, datatype, length, tableId);
            if (!isDatatypeElementary(datatype, dbVersion)) {
                addComponents(cursor, datatype, dbVersion);
            }
            cursor.toNextToken();
        }
    }

    private void setFieldAttributes(XmlCursor cursor, String usage,
            String repetitional, int repetitions, String description,
            String datatype, int length, int tableId) {
        String min = "0";
        String max = "1";
        if ("R".equals(usage)) {
            min = "1";
        }
        if ("Y".equals(repetitional)) {
            if (repetitions > 0) {
                max = String.format("%d", repetitions);
            } else {
                max = "*";
            }
        }
        cursor.insertAttributeWithValue("Name", description);
        cursor.insertAttributeWithValue("Usage", usage);
        cursor.insertAttributeWithValue("Min", min);
        cursor.insertAttributeWithValue("Max", max);
        cursor.insertAttributeWithValue("Datatype", datatype);
        cursor.insertAttributeWithValue("Length", String.format("%d", length));
        if (tableId > 0) {
            cursor.insertAttributeWithValue("Table",
                    String.format("%04d", tableId));
        }
    }

    private PreparedStatement psAllComponents;

    /**
     * Add all components for a field
     * 
     * @throws SQLException
     */
    private void addComponents(XmlCursor cursor, String datatype, int dbVersion)
            throws SQLException {
        if (psAllComponents == null) {
            psAllComponents = connection.prepareStatement("SELECT HL7Components.description, HL7DataStructureComponents.table_id, HL7DataStructureComponents.length, HL7DataStructureComponents.req_opt, HL7Components.data_type_code "
                    + "FROM HL7Components INNER JOIN HL7DataStructureComponents ON (HL7Components.version_id = HL7DataStructureComponents.version_id) AND (HL7Components.comp_no = HL7DataStructureComponents.comp_no) "
                    + "WHERE HL7DataStructureComponents.data_structure = ? AND HL7DataStructureComponents.version_id = ? ORDER BY HL7DataStructureComponents.seq_no");
        }
        psAllComponents.setString(1, datatype);
        psAllComponents.setInt(2, dbVersion);
        ResultSet rs = psAllComponents.executeQuery();
        while (rs.next()) {
            String description = rs.getString(1);
            int tableId = rs.getInt(2);
            int length = rs.getInt(3);
            String usage = rs.getString(4);
            String datatypeComponent = rs.getString(5);
            cursor.beginElement("Component");
            setComponentsAttributes(cursor, description, tableId, length,
                    usage, datatypeComponent);
            if (!isDatatypeElementary(datatypeComponent, dbVersion)) {
                addSubComponents(cursor, datatypeComponent, dbVersion);
            }
            cursor.toNextToken();
        }
    }

    private void setComponentsAttributes(XmlCursor cursor, String description,
            int tableId, int length, String usage, String datatype) {
        cursor.insertAttributeWithValue("Name", description);
        cursor.insertAttributeWithValue("Usage", usage);
        cursor.insertAttributeWithValue("Datatype", datatype);
        cursor.insertAttributeWithValue("Length", String.format("%d", length));
        if (tableId > 0) {
            cursor.insertAttributeWithValue("Table",
                    String.format("%04d", tableId));
        }
    }

    private PreparedStatement psAllSubComponents;

    /**
     * Add all subcomponents for a component
     * 
     * @throws SQLException
     */
    private void addSubComponents(XmlCursor cursor, String datatype,
            int dbVersion) throws SQLException {
        if (psAllSubComponents == null) {
            psAllSubComponents = connection.prepareStatement("SELECT HL7Components.description, HL7DataStructureComponents.table_id, HL7DataStructureComponents.length, HL7DataStructureComponents.req_opt, HL7Components.data_type_code "
                    + "FROM HL7Components INNER JOIN HL7DataStructureComponents ON (HL7Components.version_id = HL7DataStructureComponents.version_id) AND (HL7Components.comp_no = HL7DataStructureComponents.comp_no) "
                    + "WHERE HL7DataStructureComponents.data_structure = ? AND HL7DataStructureComponents.version_id = ? ORDER BY HL7DataStructureComponents.seq_no");
        }
        psAllSubComponents.setString(1, datatype);
        psAllSubComponents.setInt(2, dbVersion);
        ResultSet rs = psAllSubComponents.executeQuery();
        while (rs.next()) {
            String description = rs.getString(1);
            int tableId = rs.getInt(2);
            int length = rs.getInt(3);
            String usage = rs.getString(4);
            String datatypeComponent = rs.getString(5);
            cursor.beginElement("SubComponent");
            setComponentsAttributes(cursor, description, tableId, length,
                    usage, datatypeComponent);
            if (!isDatatypeElementary(datatypeComponent, dbVersion)) {
                System.out.println("SubComponent should be elementary");
                // System.exit(0);
            }
            cursor.toNextToken();
        }
    }

}
