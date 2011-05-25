package gov.nist.healthcare.core.validation.message.plugin.value;

import java.util.Map;

public class ValueCheck {

    public enum ValueCheckType {
        PLAIN, REGEX, LOCATION, EMPTY, PRESENT
    }

    private String text;
    private ValueCheckType type;
    private Map<String, Boolean> options;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ValueCheckType getType() {
        return type;
    }

    public void setType(ValueCheckType type) {
        this.type = type;
    }

    public Map<String, Boolean> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Boolean> options) {
        this.options = options;
    }

}
