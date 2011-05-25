/*
 * NIST Healthcare Core
 * HL7TablesCreator.java Jan 11, 2011
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.tools;

import gov.nist.healthcare.core.util.XmlBeansUtils;
import gov.nist.healthcare.data.SourceType;
import gov.nist.healthcare.data.StatusType;
import gov.nist.healthcare.data.TableLibraryDocument;
import gov.nist.healthcare.data.TableLibraryDocument.TableLibrary;
import gov.nist.healthcare.data.TableLibraryDocument.TableLibrary.TableDefinition;
import gov.nist.healthcare.data.TableLibraryDocument.TableLibrary.TableDefinition.TableElement;
import gov.nist.healthcare.data.TableType;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * This class extracts the HL7 tables from the access database and creates XML
 * files of it.
 * 
 * @author Sydney Henrard (NIST)
 */
public class HL7TablesCreator {

    private final NumberFormat nf;
    private final Map<String, TableMetaData> hMetaData;

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            HL7TablesCreator main = new HL7TablesCreator();
            main.createTables("2.3.1");
            main.createTables("2.4");
            main.createTables("2.5");
            main.createTables("2.5.1");
            main.createTables("2.6");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HL7TablesCreator() {
        nf = new DecimalFormat("0000");
        hMetaData = new HashMap<String, TableMetaData>();
        hMetaData.put("2.3.1", new TableMetaData(
                "2.16.840.1.113883.3.72.4.1.00004", 4, "HL7v2.upto5.mdb"));
        hMetaData.put("2.4", new TableMetaData(
                "2.16.840.1.113883.3.72.4.1.00005", 5, "HL7v2.upto5.mdb"));
        hMetaData.put("2.5", new TableMetaData(
                "2.16.840.1.113883.3.72.4.1.00006", 6, "HL7v2.5.mdb"));
        hMetaData.put("2.5.1", new TableMetaData(
                "2.16.840.1.113883.3.72.4.1.00007", 61, "HL7v2.5.mdb"));
        hMetaData.put("2.6", new TableMetaData(
                "2.16.840.1.113883.3.72.4.1.00008", 7, "HL7v2.6.mdb"));
    }

    /**
     * @param version
     *        the HL7 version
     * @throws SQLException
     * @throws IOException
     */
    public void createTables(String version) throws SQLException, IOException {
        TableMetaData meta = hMetaData.get(version);
        TableLibraryDocument doc = TableLibraryDocument.Factory.newInstance();
        TableLibrary lib = doc.addNewTableLibrary();
        lib.setDescription("The set of default HL7 Version " + version
                + " tables as defined by the standard");
        lib.setOrganizationName("HL7 NIST");
        lib.setTableLibraryVersion("1.0");
        lib.setStatus(StatusType.ACTIVE);
        lib.setTableLibraryIdentifier(meta.getIdentifier());
        lib.setName("HL7 Table v" + version);
        String filename = String.format("D:\\home\\hl7db\\%s", meta.getDbFile());
        String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
        database += filename.trim() + ";DriverID=22;READONLY=true}";
        Connection con = DriverManager.getConnection(database, "", "");
        Statement s = con.createStatement();
        PreparedStatement ps = con.prepareStatement("SELECT HL7TableValues.table_value, HL7TableValues.description  FROM HL7TableValues "
                + "WHERE HL7TableValues.table_id = ? AND HL7TableValues.version_id = ? ORDER BY sort_no");
        List<String> tableIds = new ArrayList<String>();
        ResultSet rs = s.executeQuery("SELECT HL7Tables.table_id, HL7Tables.description, HL7Tables.table_type FROM HL7Tables WHERE table_type IN('1','2') AND version_id = "
                + meta.getDbVersion() + " ORDER BY table_id");
        while (rs.next()) {
            int tableId = rs.getInt(1);
            String tableName = rs.getString(2);
            int tableType = rs.getInt(3);
            tableIds.add(nf.format(tableId));
            TableDefinition def = lib.addNewTableDefinition();
            def.setId(nf.format(tableId));
            def.setName(tableName);
            def.setCodesys("HL7");
            def.setVersion(version);
            gov.nist.healthcare.data.TableType.Enum type = null;
            switch (tableType) {
            case 1:
                type = TableType.USER;
                break;
            case 2:
                type = TableType.HL_7;
                break;
            default:
                type = null;
            }
            def.setType(type);
            // Get the values
            ps.setInt(1, tableId);
            ps.setInt(2, meta.getDbVersion());
            ResultSet rs1 = ps.executeQuery();
            while (rs1.next()) {
                String code = rs1.getString(1);
                String display = rs1.getString(2);
                if (!("...".equals(code))) {
                    TableElement value = def.addNewTableElement();
                    value.setCode(code);
                    value.setDisplayName(display);
                    value.setSource(SourceType.HL_7);
                }
            }
        }
        fixTable0301(doc);
        fixTable0396(doc, tableIds);
        List<XmlError> errors = XmlBeansUtils.validate(doc);
        for (XmlError error : errors) {
            System.out.println(error.getMessage());
        }
        if (errors.size() > 0) {
            System.exit(-1);
        } else {
            doc.save(
                    new File(String.format(
                            "src/main/resources/data/HL7tableV%s.xml", version)),
                    new XmlOptions().setSavePrettyPrint().setUseDefaultNamespace());
            System.out.println(doc.toString());
        }
        con.close();
    }

    private void fixTable0301(TableLibraryDocument doc) {
        XmlObject[] rs = doc.selectPath("//*:TableDefinition[@Id='0301']");
        if (rs.length == 1) {
            TableDefinition def = (TableDefinition) rs[0];
            XmlObject[] rs1 = rs[0].selectPath("*:TableElement[@Code='L,M,N']");
            if (rs1.length == 1) {
                TableElement value = def.addNewTableElement();
                value.setCode("L");
                value.setDisplayName("L");
                value.setSource(SourceType.HL_7);
                value = def.addNewTableElement();
                value.setCode("M");
                value.setDisplayName("M");
                value.setSource(SourceType.HL_7);
                value = def.addNewTableElement();
                value.setCode("N");
                value.setDisplayName("N");
                value.setSource(SourceType.HL_7);
                rs1[0].newCursor().removeXml();
            }
        }
    }

    private void fixTable0396(TableLibraryDocument doc, List<String> tableIds) {
        XmlObject[] rs = doc.selectPath("//*:TableDefinition[@Id='0396']");
        if (rs.length == 1) {
            TableDefinition def = (TableDefinition) rs[0];
            XmlObject[] rs1 = rs[0].selectPath("*:TableElement[@Code='99zzz or L']");
            if (rs1.length == 1) {
                TableElement value = def.addNewTableElement();
                value.setCode("L");
                value.setDisplayName("Local");
                value.setSource(SourceType.HL_7);
                rs1[0].newCursor().removeXml();
            }
            rs1 = rs[0].selectPath("*:TableElement[@Code='HL7nnnn']");
            if (rs1.length == 1) {
                for (String tableId : tableIds) {
                    TableElement value = def.addNewTableElement();
                    value.setCode(String.format("HL7%s", tableId));
                    value.setDisplayName(String.format(
                            "HL7 Defined Codes for table %s", tableId));
                    value.setSource(SourceType.HL_7);
                }
                rs1[0].newCursor().removeXml();
            }
        }
    }

    class TableMetaData {
        private final String identifier;
        private final int dbVersion;
        private final String dbFile;

        public TableMetaData(String identifier, int dbVersion, String dbFile) {
            this.identifier = identifier;
            this.dbVersion = dbVersion;
            this.dbFile = dbFile;
        }

        public String getIdentifier() {
            return identifier;
        }

        public int getDbVersion() {
            return dbVersion;
        }

        public String getDbFile() {
            return dbFile;
        }

    }
}
