/*
 * NIST Healthcare Core
 * MatchFoundResultV3.java Aug 19, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.content.v3;

import gov.nist.healthcare.core.validation.message.MessageFailure;
import gov.nist.healthcare.core.validation.message.content.MatchFoundResult;
import gov.nist.healthcare.core.validation.message.v3.MessageFailureV3;
import java.util.List;

/**
 * This class represents the result of the message content matching algorithm
 * for V3.
 * 
 * @author Sydney Henrard (NIST)
 */
public class MatchFoundResultV3 extends MatchFoundResult {

    private MessageFailureV3 messageError;
    private MessageFailureV3 contextError;
    private List<MessageFailure> passedAssertions;

    @Override
    public MessageFailureV3 getMessageError() {
        return messageError;
    }

    @Override
    public void setMessageError(MessageFailure messageError) {
        this.messageError = (MessageFailureV3) messageError;
    }

    @Override
    public MessageFailureV3 getContextError() {
        return contextError;
    }

    @Override
    public void setContextError(MessageFailure contextError) {
        this.contextError = (MessageFailureV3) contextError;
    }

    @Override
    public List<MessageFailure> getPassedAssertions() {
        return passedAssertions;
    }

    @Override
    public void setPassedAssertions(List<MessageFailure> passedAssertions) {
        this.passedAssertions = passedAssertions;
    }

}
