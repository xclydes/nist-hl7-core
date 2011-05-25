package gov.nist.healthcare.core.validation.message.plugin.value;

import gov.nist.healthcare.core.validation.message.MessageFailure;

public class ValueCheckResult {

    private boolean passed;
    MessageFailure messageFailure;

    // private List<MessageFailure> messageFailures;

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public MessageFailure getMessageFailure() {
        return messageFailure;
    }

    public void setMessageFailure(MessageFailure messageFailure) {
        this.messageFailure = messageFailure;
    }

    // public List<MessageFailure> getMessageFailures() {
    // return messageFailures;
    // }
    //
    // public void setMessageFailures(List<MessageFailure> messageFailures) {
    // this.messageFailures = messageFailures;
    // }

}
