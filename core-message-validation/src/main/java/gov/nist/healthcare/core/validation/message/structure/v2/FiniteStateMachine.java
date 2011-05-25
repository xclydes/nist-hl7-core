/*
 * NIST Healthcare Core
 * FiniteStateMachine.java Dec 21, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.structure.v2;

import gov.nist.healthcare.core.message.v2.er7.Er7Message;
import gov.nist.healthcare.core.message.v2.xml.XmlMessage;
import gov.nist.healthcare.core.profile.Profile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

/**
 * This class represents a finite state machine to map segments in the profile
 * and the message.
 * 
 * @author Sydney Henrard (NIST)
 */
public class FiniteStateMachine {

    private final ArrayList<State> alState;
    private HashMap<State, ArrayList<Transition>> h;

    /**
     * Constructor
     * 
     * @param aProfile
     * @param hasGroups
     */
    public FiniteStateMachine(Profile aProfile, boolean hasGroups) {
        // Create all states
        XmlCursor pCursor = aProfile.getDocument().newCursor();
        State state = null;
        alState = new ArrayList<State>();
        boolean end = false;
        pCursor.push();
        do {
            if (pCursor.toFirstChild()) {
                // Do Nothing
            } else {
                while (!end && !pCursor.toNextSibling()) {
                    end = !pCursor.toParent();
                }
            }
            if (isAllowedElements(pCursor, true)) {
                // if (isAllowedElements(pCursor, hasGroups)) {
                state = new State(
                        pCursor.getAttributeText(QName.valueOf("Name")),
                        pCursor.getObject());
                alState.add(state);
            }
        } while (!end);
        pCursor.pop();
        // List all states
        // for(int i=0;i<alState.size();i++) {
        // System.out.println("State " + i + ": " + alState.get(i).getQName());
        // }

        Transition trans = null;
        ArrayList<Transition> alTransition = new ArrayList<Transition>();
        end = false;
        do {
            if (isAllowedElements(pCursor, true)) {
                // System.out.println(pCursor.getName() + " " +
                // pCursor.getAttributeText(QName.valueOf("Name")));
                int idx = findIndex(pCursor);
                State startState = alState.get(idx);
                // Own Transition
                trans = new Transition(startState, startState.getName(),
                        startState);
                alTransition.add(trans);
                // Analyze the children
                boolean found = false;
                pCursor.push();
                if (pCursor.toFirstChild()) {
                    do {
                        if (isAllowedElements(pCursor, true)) {
                            String usage = pCursor.getAttributeText(QName.valueOf("Usage"));
                            if (usage != null
                                    && (usage.equals("RE") || usage.equals("R")
                                            || usage.equals("O")
                                            || usage.equals("C") || usage.equals("CE"))) {
                                int min = Integer.parseInt(pCursor.getAttributeText(QName.valueOf("Min")));
                                if (min > 0 && "R".equals(usage)) {
                                    found = true;
                                }
                                idx = findIndex(pCursor);
                                State endState = alState.get(idx);
                                // System.out.println("End State: " +
                                // endState.getName());
                                trans = new Transition(startState,
                                        endState.getName(), endState);
                                alTransition.add(trans);
                            }
                        }
                    } while (pCursor.toNextSibling() && !found);
                }
                pCursor.pop();
                // Analyze the siblings
                if (!found) {
                    found = false;
                    pCursor.push();
                    if (pCursor.toNextSibling()) {
                        do {
                            if (isAllowedElements(pCursor, true)) {
                                String usage = pCursor.getAttributeText(QName.valueOf("Usage"));
                                if (usage != null
                                        && (usage.equals("RE")
                                                || usage.equals("R")
                                                || usage.equals("O")
                                                || usage.equals("C") || usage.equals("CE"))) {
                                    int min = Integer.parseInt(pCursor.getAttributeText(QName.valueOf("Min")));
                                    if (min > 0 && "R".equals(usage)) {
                                        found = true;
                                    }
                                    idx = findIndex(pCursor);
                                    State endState = alState.get(idx);
                                    // System.out.println("End State: " +
                                    // endState.getName());
                                    trans = new Transition(startState,
                                            endState.getName(), endState);
                                    alTransition.add(trans);
                                }
                            }
                        } while (pCursor.toNextSibling() && !found);
                    }
                }
                pCursor.pop();
                // Analyze the parent
                if (!found) {
                    pCursor.push();
                    // boolean foundSibling = false;
                    while (pCursor.toParent() && !found) {
                        if (isAllowedElements(pCursor, true)) {
                            String usage = pCursor.getAttributeText(QName.valueOf("Usage"));
                            if (usage != null
                                    && (usage.equals("RE") || usage.equals("R")
                                            || usage.equals("O")
                                            || usage.equals("C") || usage.equals("CE"))) {
                                idx = findIndex(pCursor);
                                State endState = alState.get(idx);
                                // System.out.println("End State: " +
                                // endState.getName());
                                trans = new Transition(startState,
                                        endState.getName(), endState);
                                alTransition.add(trans);
                            }
                        }
                        while (pCursor.toNextSibling() && !found) {
                            if (isAllowedElements(pCursor, true)) {
                                String usage = pCursor.getAttributeText(QName.valueOf("Usage"));
                                if (usage != null
                                        && (usage.equals("RE")
                                                || usage.equals("R")
                                                || usage.equals("O")
                                                || usage.equals("C") || usage.equals("CE"))) {
                                    int min = Integer.parseInt(pCursor.getAttributeText(QName.valueOf("Min")));
                                    if (min > 0 && "R".equals(usage)) {
                                        found = true;
                                    }
                                    idx = findIndex(pCursor);
                                    State endState = alState.get(idx);
                                    // System.out.println("End State: " +
                                    // endState.getName());
                                    trans = new Transition(startState,
                                            endState.getName(), endState);
                                    alTransition.add(trans);

                                }
                            }
                        }
                    }
                }
            }
            pCursor.pop();

            if (pCursor.toFirstChild()) {
                // Do Nothing
            } else {
                while (!end && !pCursor.toNextSibling()) {
                    end = !pCursor.toParent();
                }
            }
        } while (!end);

        // Create a HashMap that links a State to all its Transition
        h = new HashMap<State, ArrayList<Transition>>();
        for (int i = 0; i < alTransition.size(); i++) {
            trans = alTransition.get(i);
            state = trans.getStart();
            ArrayList<Transition> al = h.get(state);
            if (al == null) {
                al = new ArrayList<Transition>();
                h.put(state, al);
            }
            al.add(trans);
        }

        if (!hasGroups) {
            HashMap<State, ArrayList<Transition>> hNG = new HashMap<State, ArrayList<Transition>>();
            ArrayList<State> stack = new ArrayList<State>();
            java.util.Iterator<State> it = h.keySet().iterator();
            while (it.hasNext()) {
                State aState = it.next();
                ArrayList<Transition> al = hNG.get(aState);
                if (al == null) {
                    al = new ArrayList<Transition>();
                    hNG.put(aState, al);
                }
                stack.add(aState);
                ArrayList<State> segments = addSegments(stack);
                for (State s : segments) {
                    al.add(new Transition(aState, s.getName(), s));
                }
                stack.clear();
            }

            // it = hNG.keySet().iterator();
            // while(it.hasNext()) {
            // State st = it.next();
            // System.out.println(st.getQName());
            // ArrayList<Transition> al = hNG.get(st);
            // for(Transition t : al) {
            // System.out.println(t.getStart().getQName() + " ==> " +
            // t.getEnd().getQName());
            // }
            // System.out.println();
            // }

            h = hNG;
        }

        // java.util.Iterator<State> it = h.keySet().iterator();
        // while (it.hasNext()) {
        // State st = it.next();
        // System.out.println(st.getQName());
        // ArrayList<Transition> al = h.get(st);
        // for (Transition t : al) {
        // System.out.println(t.getStart().getQName() + " ==> "
        // + t.getEnd().getQName());
        // }
        // System.out.println();
        // }
    }

    /**
     * Find the index of a State object based on the XmlCursor
     * 
     * @param cursor
     * @return the index -1 if not found
     */
    private int findIndex(XmlCursor cursor) {
        int idx = 0;
        for (State s : alState) {
            if (s.getObject().equals(cursor.getObject())) {
                return idx;
            }
            idx++;
        }
        return -1;
    }

    /**
     * Find all the possible following segments
     * 
     * @param stack
     * @return an ArrayList of State
     */
    private ArrayList<State> addSegments(ArrayList<State> stack) {
        ArrayList<State> segments = new ArrayList<State>();
        while (stack.size() != 0) {
            State s = stack.get(0);
            ArrayList<Transition> al = h.get(s);
            for (Transition t : al) {
                XmlCursor sCursor = t.getEnd().getObject().newCursor();
                if (sCursor.getName().equals(QName.valueOf("SegGroup"))) {
                    if (!stack.contains(t.getEnd())) {
                        stack.add(t.getEnd());
                    }
                } else {
                    if (!segments.contains(t.getEnd())) {
                        segments.add(t.getEnd());
                    }
                }
            }
            stack.remove(0);
        }
        return segments;
    }

    /**
     * Allowed elements are SegmentGroup and Segment if the hasGroups is true
     * otherwise only Segment are allowed.
     * 
     * @param cur
     *        an element as an XmlCursor
     * @param hasGroups
     * @return true of false
     */
    private boolean isAllowedElements(XmlCursor cur, boolean hasGroups) {
        boolean allowed = false;
        if (cur != null && cur.getName() != null) {
            String name = cur.getName().getLocalPart();
            if (name != null) {
                allowed = name.equals("Segment")
                        || ((name.equals("SegGroup") && hasGroups));
            }
        }
        return allowed;
    }

    /**
     * Map the segment groups and segments in the profile and the message using
     * a finite state machine
     * 
     * @param profile
     * @param message
     * @return a HashMap with an XmlObject key (profile element) associated with
     *         an ArrayList of XmlObject (message instance)
     */
    public HashMap<XmlObject, ArrayList<XmlObject>> mapSegmentElements(
            Profile profile, XmlMessage message) {
        // Browse the message and feed the finite state machine
        HashMap<XmlObject, ArrayList<XmlObject>> hMap = new HashMap<XmlObject, ArrayList<XmlObject>>();
        State currentState = alState.get(0);
        XmlCursor mCursor = message.getDocument().newCursor();
        String msgType = message.getMessageCode();
        msgType = message.getMessageStructureID();
        boolean messageMapped = true;
        boolean end = false;
        do {
            if (mCursor.toFirstChild()) {
                // Do Nothing
            } else {
                while (!end && !mCursor.toNextSibling()) {
                    end = !mCursor.toParent();
                }
            }
            String condition = mCursor.getName() == null ? null
                    : mCursor.getName().getLocalPart();
            // Skip the root element
            if (condition != null && !msgType.equals(condition)) {
                // Remove the msgType for groups
                if (condition.startsWith(msgType)) {
                    int idx = condition.indexOf(".");
                    condition = condition.substring(idx + 1);
                }
                // Skip Z-Segment, Field, Component and SubComponent
                if (!condition.startsWith("Z") && condition.indexOf(".") == -1) {
                    ArrayList<Transition> alTrans = h.get(currentState);
                    boolean found = false;
                    for (int i = 0; i < alTrans.size(); i++) {
                        Transition trans = alTrans.get(i);
                        if (trans.getCondition().equals(condition)) {
                            currentState = trans.getEnd();
                            // Make a link
                            ArrayList<XmlObject> alXmlObj = hMap.get(currentState.getObject());
                            if (alXmlObj == null) {
                                alXmlObj = new ArrayList<XmlObject>();
                                hMap.put(currentState.getObject(), alXmlObj);
                            }
                            alXmlObj.add(mCursor.getObject());
                            // End
                            i = alTrans.size();
                            found = true;
                        }
                    }
                    if (!found) {
                        // The finite state machine can't map the provided
                        // message.
                        messageMapped = false;
                    }
                }
            }
        } while (!end);
        mCursor.dispose();
        if (!messageMapped) {
            hMap = mapMessageWithProfile(message, profile.getUniqueSegments());
        }

        return hMap;
    }

    /**
     * Map the segment groups and segments in the profile and the message using
     * a finite state machine
     * 
     * @param profile
     * @param message
     * @return a HashMap with an XmlObject key (profile element) associated with
     *         an ArrayList of Integer representing the line number in the
     *         message instance
     * @throws IOException
     */
    public HashMap<XmlObject, ArrayList<Integer>> mapSegmentElements(
            Profile profile, Er7Message message) throws IOException {
        // Browse the message and feed the finite state machine
        HashMap<XmlObject, ArrayList<Integer>> hMap = new HashMap<XmlObject, ArrayList<Integer>>();
        BufferedReader br = new BufferedReader(new StringReader(
                message.getMessageAsString()));
        State currentState = alState.get(0);
        String lastCondition = "";
        String line = null;
        int lineNumber = 1;
        boolean messageMapped = true;
        while ((line = br.readLine()) != null) {
            // Get the segment name
            if (line.length() >= 3) {
                String condition = line.substring(0, 3);
                // Skip Z-Segment
                if (!condition.startsWith("Z")) {
                    ArrayList<Transition> alTrans = h.get(currentState);
                    boolean found = false;
                    for (int i = 0; i < alTrans.size(); i++) {
                        Transition trans = alTrans.get(i);
                        if (trans.getCondition().equals(condition)
                                && !lastCondition.equals(condition)) {
                            currentState = trans.getEnd();
                            // Make a link
                            ArrayList<Integer> alLines = hMap.get(currentState.getObject());
                            if (alLines == null) {
                                alLines = new ArrayList<Integer>();
                                hMap.put(currentState.getObject(), alLines);
                            }
                            alLines.add(lineNumber);
                            // End
                            i = alTrans.size();
                            lastCondition = condition;
                            found = true;
                        }
                    }
                    if (!found && lastCondition.equals(condition)) {
                        // Make a link
                        ArrayList<Integer> alLines = hMap.get(currentState.getObject());
                        alLines.add(lineNumber);
                    } else if (!found) {
                        // The finite state machine can't map the provided
                        // message.
                        messageMapped = false;
                    }
                }
            }
            lineNumber++;
        }
        if (!messageMapped) {
            hMap = mapMessageWithProfile(message, profile.getUniqueSegments());
        }

        return hMap;
    }

    /**
     * Map the message with the profile by mapping segments that have only 1
     * definition in the profile. The order in the message is wrong but it does
     * not matter.
     * 
     * @param message
     * @param uniqueSegments
     * @return a HashMap with an XmlObject key (profile element) associated with
     *         an ArrayList of Integer representing the line number in the
     *         message instance
     * @throws IOException
     */
    private HashMap<XmlObject, ArrayList<Integer>> mapMessageWithProfile(
            Er7Message message, Map<String, XmlObject> uniqueSegments)
            throws IOException {
        HashMap<XmlObject, ArrayList<Integer>> hMap = new HashMap<XmlObject, ArrayList<Integer>>();
        BufferedReader br = new BufferedReader(new StringReader(
                message.getMessageAsString()));
        String line = null;
        int lineNumber = 1;
        while ((line = br.readLine()) != null) {
            // Get the segment name
            if (line.length() >= 3) {
                String segmentName = line.substring(0, 3);
                XmlObject profileElement = uniqueSegments.get(segmentName);
                // Make a link
                ArrayList<Integer> alLines = hMap.get(profileElement);
                if (alLines == null) {
                    alLines = new ArrayList<Integer>();
                    hMap.put(profileElement, alLines);
                }
                alLines.add(lineNumber);
                lineNumber++;
            }
        }
        return hMap;
    }

    /**
     * Map the message with the profile by mapping segments that have only 1
     * definition in the profile. The order in the message is wrong but it does
     * not matter.
     * 
     * @param message
     * @param uniqueSegments
     * @return a HashMap with an XmlObject key (profile element) associated with
     *         an ArrayList of XmlObject (message instance)
     */
    private HashMap<XmlObject, ArrayList<XmlObject>> mapMessageWithProfile(
            XmlMessage message, Map<String, XmlObject> uniqueSegments) {
        HashMap<XmlObject, ArrayList<XmlObject>> hMap = new HashMap<XmlObject, ArrayList<XmlObject>>();
        XmlCursor mCursor = message.getDocument().newCursor();
        boolean end = false;
        do {
            if (mCursor.toFirstChild()) {
                // Do Nothing
            } else {
                while (!end && !mCursor.toNextSibling()) {
                    end = !mCursor.toParent();
                }
            }
            String segmentName = mCursor.getName() != null ? mCursor.getName().getLocalPart()
                    : null;
            if (segmentName != null && segmentName.length() == 3) {
                XmlObject profileElement = uniqueSegments.get(segmentName);
                if (profileElement != null) {
                    // Make a link
                    ArrayList<XmlObject> alXmlObj = hMap.get(profileElement);
                    if (alXmlObj == null) {
                        alXmlObj = new ArrayList<XmlObject>();
                        hMap.put(profileElement, alXmlObj);
                    }
                    alXmlObj.add(mCursor.getObject());
                }
            }
        } while (!end);
        mCursor.dispose();
        return hMap;
    }

}
