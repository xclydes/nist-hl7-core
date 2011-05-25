/*
 * NIST Healthcare Core
 * TableManager.java Mar 03, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.util;

import gov.nist.healthcare.data.TableLibraryDocument;
import gov.nist.healthcare.data.TableLibraryDocument.TableLibrary.TableDefinition;
import gov.nist.healthcare.data.TableLibraryDocument.TableLibrary.TableDefinition.TableElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.xmlbeans.XmlObject;

/**
 * This class manages all the tables for the validation
 * 
 * @author Sydney Henrard (NIST)
 */
public class TableManager {

    // TODO: Restrict the list of table to check
    // TODO: Check for valid TableLibrary
    private final Map<String, TableLibraryDocument> mapLibrary;
    private final List<String> orderedKeys;

    public TableManager() {
        mapLibrary = new HashMap<String, TableLibraryDocument>();
        orderedKeys = new ArrayList<String>();
    }

    /**
     * Set the libraries. All previously added libraries will be removed.
     * 
     * @param libraries
     *        a list of TableLibraryDocument
     */
    public void setLibraries(List<TableLibraryDocument> libraries) {
        mapLibrary.clear();
        orderedKeys.clear();
        if (libraries != null) {
            for (TableLibraryDocument library : libraries) {
                addLibrary(library);
            }
        }
    }

    /**
     * Add a library. The Name attribute will be used as a key to identify the
     * library.
     * 
     * @param library
     */
    public void addLibrary(TableLibraryDocument library) {
        if (library != null) {
            String name = library.getTableLibrary().getName();
            if (name == null || "".equals(name)) {
                throw new IllegalArgumentException(
                        "The library can't be added because the Name attribute is not set.");
            }
            mapLibrary.put(name, library);
            orderedKeys.add(name);
        }
    }

    /**
     * Get a table. The table will be looked for in each library until we found
     * one. If in the same library we found several versions of the table. The
     * method will return null and you will need to specify the version.
     * 
     * @param tableId
     * @param tableVersion
     * @return a TableDefinition object; null if the table is not found
     */
    public TableDefinition getTable(String tableId, String tableVersion) {
        TableDefinition table = null;
        String xpath = String.format("//*:TableDefinition[@Id='%s']", tableId);
        if (tableVersion != null && !"".equals(tableVersion)) {
            xpath = String.format(
                    "//*:TableDefinition[@Id='%s' and @Version='%s']", tableId,
                    tableVersion);
        }
        for (String key : orderedKeys) {
            TableLibraryDocument library = mapLibrary.get(key);
            XmlObject[] rs = library.selectPath(xpath);
            if (rs.length > 0) {
                if (rs.length == 1) {
                    table = (TableDefinition) rs[0];
                } else {
                    break;
                }
            }
        }
        return table;
    }

    /**
     * Check if a value is in a table
     * 
     * @param value
     * @param table
     * @return true if the value is in the table; false otherwise
     */
    public boolean isValueInTable(String value, TableDefinition table) {
        boolean valueInTable = false;
        for (TableElement item : table.getTableElementList()) {
            if (item.getCode().equals(value)) {
                valueInTable = true;
                break;
            }
        }
        return valueInTable;
    }
}
