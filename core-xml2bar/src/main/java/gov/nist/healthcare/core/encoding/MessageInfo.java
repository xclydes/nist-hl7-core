/*
 * NIST Healthcare Core
 * MessageInfo.java Nov 13, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.encoding;

/**
 * Object used to store the information related to a message : - message
 * structure id - version - separators - data (profile or HL7 standard).
 * 
 * @author Caroline Rosin (NIST)
 */
public class MessageInfo {

    private String msgStructId;
    private int vId = -1;

    // private Data data;

    private String fieldSepChar = "";
    private String compSepChar = "";
    private String repSepChar = "";
    private String escapeChar = "";
    private String subCompChar = "";

    private String fieldSeparator = "";
    private String compSeparator = "";
    private String repSeparator = "";
    private String subCompSeparator = "";

    public MessageInfo() {
    }

    /**
     * Returns the unicode of the separator.
     * 
     * @param code
     *        the separator (field, component, escape character, repetition or
     *        subcomponent)
     * @return the separator as a String
     */
    // public String getUnicode(Separator code) {
    //
    // if (code.equals(Separator.FIELD_SEP)) {
    // return EncodingUtils.getUnicode((int) fieldSepChar.charAt(0));
    // } else if (code.equals(Separator.COMP_SEP)) {
    // return EncodingUtils.getUnicode((int) compSepChar.charAt(0));
    // } else if (code.equals(Separator.REP_SEP)) {
    // return EncodingUtils.getUnicode((int) repSepChar.charAt(0));
    // } else if (code.equals(Separator.ESC_CHAR)) {
    // return EncodingUtils.getUnicode((int) escapeChar.charAt(0));
    // } else if (code.equals(Separator.SUBCOMP_SEP)) {
    // return EncodingUtils.getUnicode((int) subCompChar.charAt(0));
    // }
    // return null;
    // }
    /**
     * Get the component separator.
     * 
     * @return the component separator
     */
    public String getCompSepChar() {
        return compSepChar;
    }

    /**
     * Sets the component separator.
     * 
     * @param componentSep
     */
    public void setCompSepChar(String componentSep) {
        this.compSepChar = componentSep;
    }

    /**
     * Get the escape character.
     * 
     * @return the escape character
     */
    public String getEscapeChar() {
        return escapeChar;
    }

    /**
     * Sets the escape character.
     * 
     * @param escChar
     */
    public void setEscapeChar(String escChar) {
        this.escapeChar = escChar;
    }

    /**
     * Get the field separator.
     * 
     * @return the field separator
     */
    public String getFieldSepChar() {
        return fieldSepChar;
    }

    /**
     * Sets the field separator.
     * 
     * @param fieldSep
     */
    public void setFieldSepChar(String fieldSep) {
        this.fieldSepChar = fieldSep;
    }

    /**
     * Get the message structure ID.
     * 
     * @return the message structure ID
     */
    public String getMsgStructId() {
        return msgStructId;
    }

    /**
     * Sets the message structure ID.
     * 
     * @param msgStructId
     */
    public void setMsgStructId(String msgStructId) {
        this.msgStructId = msgStructId;
    }

    /**
     * Get the repetition separator.
     * 
     * @return the repetition separator
     */
    public String getRepSepChar() {
        return repSepChar;
    }

    /**
     * Sets the repetition separator.
     * 
     * @param repetitionSep
     */
    public void setRepSepChar(String repetitionSep) {
        this.repSepChar = repetitionSep;
    }

    /**
     * Get the subcomponent separator.
     * 
     * @return the subcomponent separator
     */
    public String getSubCompChar() {
        return subCompChar;
    }

    /**
     * Sets the subcomponent separator.
     * 
     * @param subcomponentSep
     */
    public void setSubCompChar(String subcomponentSep) {
        this.subCompChar = subcomponentSep;
    }

    /**
     * Get the version ID.
     * 
     * @return the version id
     */
    public int getVId() {
        return vId;
    }

    /**
     * Sets the version id.
     * 
     * @param id
     */
    public void setVId(int id) {
        vId = id;
    }

    /**
     * Gets the data.
     * 
     * @return the data
     */
    // public Data getData() {
    // return data;
    // }
    // /**
    // * Sets the data.
    // * @param data
    // */
    // public void setData(Data data) {
    // this.data = data;
    // }
    public String getCompSeparator() {
        return compSeparator;
    }

    public void setCompSeparator(String compSeparator) {
        this.compSeparator = compSeparator;
    }

    public String getFieldSeparator() {
        return fieldSeparator;
    }

    public void setFieldSeparator(String fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    public String getRepSeparator() {
        return repSeparator;
    }

    public void setRepSeparator(String repSeparator) {
        this.repSeparator = repSeparator;
    }

    public String getSubCompSeparator() {
        return subCompSeparator;
    }

    public void setSubCompSeparator(String subCompSeparator) {
        this.subCompSeparator = subCompSeparator;
    }

}
