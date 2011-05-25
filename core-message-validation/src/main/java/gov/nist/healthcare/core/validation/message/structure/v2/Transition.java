/*
 * NIST Healthcare Core
 * Transition.java Sep 12, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.structure.v2;

/**
 * This class represents a Transition in a finite state machine.
 * 
 * @author Sydney Henrard (NIST)
 */
public class Transition {

    private State start;
    private String condition;
    private State end;

    /**
     * Constructor
     * 
     * @param start
     * @param condition
     * @param end
     */
    public Transition(State start, String condition, State end) {
        this.start = start;
        this.condition = condition;
        this.end = end;
    }

    public State getStart() {
        return start;
    }

    public void setStart(State start) {
        this.start = start;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public State getEnd() {
        return end;
    }

    public void setEnd(State end) {
        this.end = end;
    }

}
