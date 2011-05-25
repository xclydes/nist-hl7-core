package gov.nist.healthcare.core.validation.message.plugin.value;

import java.util.List;

public class ValueCheckParam {

    private String location;
    private List<ValueCheck> values;
    private boolean matchAll;
    private int minMatch;
    private String maxMatch;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<ValueCheck> getValues() {
        return values;
    }

    public void setValues(List<ValueCheck> values) {
        this.values = values;
    }

    public boolean isMatchAll() {
        return matchAll;
    }

    public void setMatchAll(boolean matchAll) {
        this.matchAll = matchAll;
    }

    public int getMinMatch() {
        return minMatch;
    }

    public void setMinMatch(int minMatch) {
        this.minMatch = minMatch;
    }

    public String getMaxMatch() {
        return maxMatch;
    }

    public void setMaxMatch(String maxMatch) {
        this.maxMatch = maxMatch;
    }

}
