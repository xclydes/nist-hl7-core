/*
 * NIST Healthcare Core
 * AnnotatedMessage.java May 20, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.generation;

import gov.nist.healthcare.core.MalformedMessageException;
import gov.nist.healthcare.core.message.v2.xml.XmlMessage;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlCursor;

/**
 * This class represents an XML message but with annotations. The annotations
 * will be used to prune the XML message.
 * 
 * @author Sydney Henrard (NIST)
 */
public class AnnotatedMessage extends XmlMessage {

    /**
     * Constructor
     * 
     * @param message
     *        the message as a String
     * @throws MalformedMessageException
     */
    public AnnotatedMessage(String message) throws MalformedMessageException {
        super(message);
    }

    /**
     * Prune the annotated message.
     */
    public void prune() {
        XmlCursor cursor = messageDoc.newCursor();
        cursor.toNextToken();
        boolean remove = false;
        boolean end = false;
        do {
            if (remove) {
                cursor.removeXml();
            }
            if (!remove && cursor.toFirstChild()) {
                // Do Nothing
            } else {
                while (!end && !cursor.toNextSibling()) {
                    end = !cursor.toParent();
                }
            }
            remove = false;
            if (cursor.getName() != null) {
                boolean marked = isMarked(cursor)
                        || countMarkedNextSiblings(cursor) > 0;
                if (marked) {
                    // Do Nothing
                } else {
                    remove = true;
                    // Keep the R element with position <= the Min
                    String usage = cursor.getAttributeText(QName.valueOf("Usage"));
                    String min = cursor.getAttributeText(QName.valueOf("Min"));
                    int position = getPosition(cursor);
                    if ("R".equals(usage) && position <= Integer.parseInt(min)) {
                        remove = false;
                    } else if ("RE".equals(usage)) {
                        if (position > 1 && position <= Integer.parseInt(min)) {
                            remove = false;
                        }
                    }
                }
                if (!remove) {
                    markFirstChild(cursor);
                    removeAnnotation(cursor);
                }
            }
        } while (!end);
    }

    /**
     * Get the position of the element.
     * 
     * @param cursor
     * @return the position
     */
    private int getPosition(XmlCursor cursor) {
        cursor.push();
        int position = 1;
        QName name = cursor.getName();
        while (cursor.toPrevSibling()) {
            if (cursor.getName().equals(name)) {
                position++;
            }
        }
        cursor.pop();
        return position;
    }

    /**
     * Remove all the annotations
     * 
     * @param cursor
     */
    private void removeAnnotation(XmlCursor cursor) {
        cursor.removeAttribute(QName.valueOf("Marked"));
        cursor.removeAttribute(QName.valueOf("Usage"));
        cursor.removeAttribute(QName.valueOf("Min"));
    }

    /**
     * Mark the first child if none of its siblings is a requirement element.
     * 
     * @param cursor
     */
    private void markFirstChild(XmlCursor cursor) {
        cursor.push();
        if (cursor.toFirstChild()) {
            if (!"X".equals(cursor.getAttributeText(QName.valueOf("Usage")))) {
                cursor.push();
            }
            boolean siblingRequired = false;
            do {
                if ("R".equals(cursor.getAttributeText(QName.valueOf("Usage")))) {
                    siblingRequired = true;
                    break;
                }
            } while (cursor.toNextSibling());
            cursor.pop();
            if (!siblingRequired) {
                cursor.setAttributeText(QName.valueOf("Marked"), "");
            }
        }
        cursor.pop();
    }

    /**
     * Return if the element is marked
     * 
     * @param cursor
     * @return true if marked; false otherwise
     */
    private boolean isMarked(XmlCursor cursor) {
        return cursor.getAttributeText(QName.valueOf("Marked")) != null;
    }

    /**
     * Count the number of marked next siblings
     * 
     * @param aCursor
     * @return the number of marked next siblings
     */
    private int countMarkedNextSiblings(XmlCursor aCursor) {
        aCursor.push();
        QName current = aCursor.getName();
        boolean end = false;
        int marked = 0;
        while (aCursor.toNextSibling() && !end) {
            if (aCursor.getName().equals(current)) {
                if (aCursor.getAttributeText(QName.valueOf("Marked")) != null) {
                    marked++;
                }
            }
        }
        aCursor.pop();
        return marked;
    }

}
