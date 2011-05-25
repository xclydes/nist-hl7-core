/*
 * NIST Healthcare Core
 * MessageElementUtil.java Dec 27, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.util;

import gov.nist.healthcare.core.Constants;
import gov.nist.healthcare.core.message.MessageLocation;
import gov.nist.healthcare.core.message.v2.HL7V2Message;
import gov.nist.healthcare.message.Component;
import gov.nist.healthcare.message.Field;
import gov.nist.healthcare.message.MessageElement;
import gov.nist.healthcare.message.Segment;
import gov.nist.healthcare.message.SegmentGroup;
import gov.nist.healthcare.message.SubComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides methods to get information about a MessageElement object
 * 
 * @author Sydney Henrard (NIST)
 */
public final class MessageElementUtil {

    private MessageElementUtil() {
    }

    /**
     * Return the segment from a MessageElement
     * 
     * @param messageElement
     * @return the Segment
     */
    public static Segment getSegment(MessageElement messageElement) {
        Segment segment = null;
        if (messageElement != null) {
            SegmentGroup segGroup = messageElement.getSegmentGroup();
            if (segGroup == null) {
                segment = messageElement.getSegment();
            } else {
                while (true) {
                    if (segGroup.getSegmentGroup() != null) {
                        segGroup = segGroup.getSegmentGroup();
                    } else {
                        break;
                    }
                }
                segment = segGroup.getSegment();
            }
        }
        return segment;
    }

    /**
     * Return if the MessageElement has AnyInstanceNumber set at the segment
     * level.
     * 
     * @param messageElement
     * @return a boolean
     */
    public static boolean isSegmentAnyInstanceNumberSet(
            MessageElement messageElement) {
        boolean segmentAnyInstanceNumber = false;
        Segment segment = getSegment(messageElement);
        if (segment != null) {
            segmentAnyInstanceNumber = segment.getAnyInstanceNumber();
        }
        return segmentAnyInstanceNumber;
    }

    /**
     * Return if the MessageElement has AnyInstanceNumber set at the field
     * level.
     * 
     * @param messageElement
     * @return a boolean
     */
    public static boolean isFieldAnyInstanceNumberSet(
            MessageElement messageElement) {
        boolean fieldAnyInstanceNumber = false;
        Segment segment = getSegment(messageElement);
        if (segment != null) {
            Field field = segment.getField();
            if (field != null) {
                fieldAnyInstanceNumber = field.getAnyInstanceNumber();
            }
        }
        return fieldAnyInstanceNumber;
    }

    /**
     * Transform a epath location into a MessageElement.
     * 
     * @param location
     * @return a MessageElement
     */
    public static MessageElement getMessageElement(String location) {
        MessageElement messageElement = null;
        Pattern segmentGroupsRegex = Pattern.compile("(.+)\\[(\\d)\\]");
        Pattern pEPath = Pattern.compile(Constants.EPATH_REGEX);
        Matcher matcher = pEPath.matcher(location);
        if (matcher.matches()) {
            messageElement = MessageElement.Factory.newInstance();
            String segmentGroups = matcher.group(1);
            SegmentGroup sg = null;
            if (!segmentGroups.equals("")) {
                for (String segmentGroup : segmentGroups.split("/")) {
                    Matcher m = segmentGroupsRegex.matcher(segmentGroup);
                    if (m.matches()) {
                        String segmentGroupName = m.group(1);
                        String segmentGroupInstanceNumber = m.group(2);
                        if (sg == null) {
                            sg = messageElement.addNewSegmentGroup();
                            sg.setName(segmentGroupName);
                            sg.setInstanceNumber(Integer.parseInt(segmentGroupInstanceNumber));
                        } else {
                            sg = sg.addNewSegmentGroup();
                            sg.setName(segmentGroupName);
                            sg.setInstanceNumber(Integer.parseInt(segmentGroupInstanceNumber));
                        }
                    }
                }
            }
            // Segment
            Segment segment = null;
            if (sg == null) {
                segment = messageElement.addNewSegment();
            } else {
                segment = sg.addNewSegment();
            }
            segment.setName(matcher.group(2));
            if ("*".equals(matcher.group(3))) {
                segment.setAnyInstanceNumber(true);
            } else {
                segment.setInstanceNumber(Integer.parseInt(matcher.group(3)));
            }
            // Field
            if (matcher.group(4) != null) {
                Field field = segment.addNewField();
                field.setPosition(Integer.parseInt(matcher.group(4)));
                if ("*".equals(matcher.group(5))) {
                    field.setAnyInstanceNumber(true);
                } else {
                    field.setInstanceNumber(Integer.parseInt(matcher.group(5)));
                }
                // Component
                if (matcher.group(6) != null) {
                    Component component = field.addNewComponent();
                    component.setPosition(Integer.parseInt(matcher.group(6)));
                    // SubComponent
                    if (matcher.group(7) != null) {
                        SubComponent subComponent = component.addNewSubComponent();
                        subComponent.setPosition(Integer.parseInt(matcher.group(7)));
                    }
                }
            }
        }
        return messageElement;
    }

    /**
     * Return all the message locations based on the message. It will replace
     * the AnyInstanceNumber by the actual instance numbers.
     * 
     * @param location
     * @param message
     * @return a list of MessageLocation
     */
    public static List<MessageLocation> getMessageLocations(
            MessageElement location, HL7V2Message message) {
        List<MessageLocation> messageLocations = new ArrayList<MessageLocation>();
        if (location != null) {
            boolean isSegmentAnyInstanceNumber = MessageElementUtil.isSegmentAnyInstanceNumberSet(location);
            boolean isFieldAnyInstanceNumber = MessageElementUtil.isFieldAnyInstanceNumberSet(location);
            Segment segment = getSegment(location);
            if (!isSegmentAnyInstanceNumber && !isFieldAnyInstanceNumber) {
                // No any instance number
                messageLocations.add(new MessageLocation(location));
            } else if (isSegmentAnyInstanceNumber && !isFieldAnyInstanceNumber) {
                // segment any instance number only
                int segmentCount = message.getSegmentCount(segment.getName());
                for (int s = 1; s <= segmentCount; s++) {
                    segment.setInstanceNumber(s);
                    messageLocations.add(new MessageLocation(location));
                }
            } else if (!isSegmentAnyInstanceNumber && isFieldAnyInstanceNumber) {
                // field any instance number only
                int fieldCount = message.getFieldCount(
                        segment.getName(),
                        segment.getInstanceNumber() == 0 ? 1
                                : segment.getInstanceNumber(),
                        segment.getField().getPosition());
                for (int f = 1; f <= fieldCount; f++) {
                    segment.getField().setInstanceNumber(f);
                    messageLocations.add(new MessageLocation(location));
                }
            } else if (isSegmentAnyInstanceNumber && isFieldAnyInstanceNumber) {
                // both segment and field any instance number
                int segmentCount = message.getSegmentCount(segment.getName());
                for (int s = 1; s <= segmentCount; s++) {
                    segment.setInstanceNumber(s);
                    int fieldCount = message.getFieldCount(segment.getName(),
                            segment.getInstanceNumber(),
                            segment.getField().getPosition());
                    for (int f = 1; f <= fieldCount; f++) {
                        segment.getField().setInstanceNumber(f);
                        messageLocations.add(new MessageLocation(location));
                    }
                }
            }
        }
        return messageLocations;
    }

}
