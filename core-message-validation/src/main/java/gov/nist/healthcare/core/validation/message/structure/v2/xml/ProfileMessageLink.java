/*
 * NIST Healthcare Core
 * ProfileMessageLink.java Oct 9, 2007
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.structure.v2.xml;

/**
 * This class is linking a ProfileContext and a MessageContext
 * 
 * @author Sydney Henrard (NIST)
 */
public class ProfileMessageLink {

    private ProfileContext profileContext;
    private MessageContext messageContext;

    /**
     * Constructor
     * 
     * @param profileContext
     * @param messageContext
     */
    public ProfileMessageLink(ProfileContext profileContext,
            MessageContext messageContext) {
        this.profileContext = profileContext;
        this.messageContext = messageContext;
    }

    public ProfileContext getProfileContext() {
        return profileContext;
    }

    public void setProfileContext(ProfileContext profileContext) {
        this.profileContext = profileContext;
    }

    public MessageContext getMessageContext() {
        return messageContext;
    }

    public void setMessageContext(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

}
