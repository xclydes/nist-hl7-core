/*
 * NIST Healthcare Core
 * MatchFoundResultV2.java Aug 19, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.content.v2;

import gov.nist.healthcare.core.validation.message.MessageFailure;
import gov.nist.healthcare.core.validation.message.content.MatchFoundResult;
import gov.nist.healthcare.core.validation.message.v2.MessageFailureV2;
import java.util.List;

/**
 * This class represents the result of the message content matching algorithm
 * for V2.
 * 
 * @author Sydney Henrard (NIST)
 */
public class MatchFoundResultV2 extends MatchFoundResult {

    private MessageFailureV2 messageError;
    private MessageFailureV2 contextError;
    private List<MessageFailure> passedAssertions;

    @Override
    public MessageFailureV2 getMessageError() {
        return messageError;
    }

    @Override
    public void setMessageError(MessageFailure messageError) {
        this.messageError = (MessageFailureV2) messageError;
    }

    @Override
    public MessageFailureV2 getContextError() {
        return contextError;
    }

    @Override
    public void setContextError(MessageFailure contextError) {
        this.contextError = (MessageFailureV2) contextError;
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
