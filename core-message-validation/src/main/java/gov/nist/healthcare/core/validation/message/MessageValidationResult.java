/*
 * NIST Healthcare Core
 * MessageValidationResult.java Mar 03, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message;

import gov.nist.healthcare.core.message.HL7Message;
import gov.nist.healthcare.validation.AssertionResultConstants;
import gov.nist.healthcare.validation.AssertionResultConstants.Enum;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.xmlbeans.StringEnumAbstractBase;

public abstract class MessageValidationResult<T extends MessageFailure> {

    protected HL7Message message;
    protected MessageValidationContext context;
    protected List<T> messageFailures;
    protected int affirmativeCount;
    protected int errorCount;
    protected int warningCount;
    protected int ignoreCount;
    protected int alertCount;

    public int getErrorCount() {
        return errorCount;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public int getIgnoreCount() {
        return ignoreCount;
    }

    public int getAlertCount() {
        return alertCount;
    }

    public int getAffirmationCount() {
        return affirmativeCount;
    }

    /**
     * Calculate the count variables
     */
    protected void count() {
        // Map< ? extends StringEnumAbstractBase, AssertionResultConstants.Enum>
        // map) {
        // HashMap<AssertionTypeV3Constants.Enum, AssertionResultConstants.Enum>
        Map<StringEnumAbstractBase, AssertionResultConstants.Enum> map = context.getFailureResults();
        affirmativeCount = 0;
        errorCount = 0;
        warningCount = 0;
        ignoreCount = 0;
        alertCount = 0;
        for (MessageFailure mf : messageFailures) {
            AssertionResultConstants.Enum assertionResult = mf.getAssertionResult();
            if (assertionResult == null) {
                assertionResult = map.get(mf.getFailureType());
            }
            if (assertionResult.equals(AssertionResultConstants.ERROR)) {
                errorCount++;
            } else if (assertionResult.equals(AssertionResultConstants.WARNING)) {
                warningCount++;
            } else if (assertionResult.equals(AssertionResultConstants.IGNORE)) {
                ignoreCount++;
            } else if (assertionResult.equals(AssertionResultConstants.ALERT)) {
                alertCount++;
            } else if (assertionResult.equals(AssertionResultConstants.AFFIRMATIVE)) {
                affirmativeCount++;
            }
        }
        // Iterator<MessageFailureV3> it = messageFailures.iterator();
        // MessageFailureV3 mf = null;
        // while (it.hasNext()) {
        // mf = it.next();
        // if (map.get(mf.getFailureType()).equals(
        // AssertionResultConstants.ERROR)) {
        // errorCount++;
        // } else if (map.get(mf.getFailureType()).equals(
        // AssertionResultConstants.WARNING)) {
        // warningCount++;
        // } else if (map.get(mf.getFailureType()).equals(
        // AssertionResultConstants.IGNORE)) {
        // ignoreCount++;
        // } else if (map.get(mf.getFailureType()).equals(
        // AssertionResultConstants.ALERT)) {
        // alertCount++;
        // } else if (map.get(mf.getFailureType()).equals(
        // AssertionResultConstants.AFFIRMATIVE)) {
        // affirmativeCount++;
        // }
        // }
    }

    /**
     * Get all message failure depending on the FailureLevel
     * 
     * @param failureResult
     * @return a list of T
     */
    public List<T> getMessageFailure(AssertionResultConstants.Enum failureResult) {
        ArrayList<T> al = new ArrayList<T>();
        Iterator<T> it = messageFailures.iterator();
        T mf = null;
        while (it.hasNext()) {
            mf = it.next();
            Enum assertionResult = mf.getAssertionResult();
            if (assertionResult != null && assertionResult == failureResult) {
                al.add(mf);
            } else if (assertionResult == null) {
                if (context.getFailureResult(mf.getFailureType()).equals(
                        failureResult)) {
                    al.add(mf);
                }
            }
        }
        return al;
    }

    /**
     * Is the message valid? Depending on the MessageValidationContext settings
     * 
     * @return a boolean
     */
    public boolean isValid() {
        return !getErrors().hasNext();
    }

    /**
     * Get all errors depending on the MessageValidationContext settings
     * 
     * @return an Iterator of T
     */
    public Iterator<T> getErrors() {
        return getMessageFailure(AssertionResultConstants.ERROR).iterator();
    }

    /**
     * Get all warnings depending on the MessageValidationContext settings
     * 
     * @return an Iterator of T
     */
    public Iterator<T> getWarnings() {
        return getMessageFailure(AssertionResultConstants.WARNING).iterator();
    }

    /**
     * Get all ignores depending on the MessageValidationContext settings
     * 
     * @return an Iterator of T
     */
    public Iterator<T> getIgnores() {
        return getMessageFailure(AssertionResultConstants.IGNORE).iterator();
    }

    /**
     * Get all alerts depending on the MessageValidationContext settings
     * 
     * @return an Iterator of T
     */
    public Iterator<T> getAlerts() {
        return getMessageFailure(AssertionResultConstants.ALERT).iterator();
    }

    /**
     * Get all affirmatives depending on the MessageValidationContext settings
     * 
     * @return an Iterator of T
     */
    public Iterator<T> getAffirmatives() {
        return getMessageFailure(AssertionResultConstants.AFFIRMATIVE).iterator();
    }

}
