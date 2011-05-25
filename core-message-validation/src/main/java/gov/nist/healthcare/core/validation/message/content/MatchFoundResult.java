/*
 * NIST Healthcare Core
 * MatchFoundResult.java Aug 10, 2009
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.content;

import gov.nist.healthcare.core.validation.message.MessageFailure;
import java.util.List;

/**
 * This class represents the result of the message content matching algorithm
 * 
 * @author Sydney Henrard (NIST)
 */
public abstract class MatchFoundResult {

    public abstract MessageFailure getMessageError();

    public abstract void setMessageError(MessageFailure messageError);

    public abstract MessageFailure getContextError();

    public abstract void setContextError(MessageFailure contextError);

    public abstract List<MessageFailure> getPassedAssertions();

    public abstract void setPassedAssertions(
            List<MessageFailure> passedAssertions);

    /**
     * Return true if it contains a context or a message error
     * 
     * @return a boolean
     */
    public boolean hasError() {
        return (getMessageError() != null || getContextError() != null);
    }
}
