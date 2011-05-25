/*
 * NIST Healthcare Core
 * SkeletonURIResolver.java Mar 03, 2010
 *
 * This code was produced by the National Institute of Standards and
 * Technology (NIST). See the "nist.disclaimer" file given in the distribution
 * for information on the use and redistribution of this software.
 */
package gov.nist.healthcare.core.validation.message.structure.v3;

import gov.nist.healthcare.core.validation.message.MessageValidationConstants;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

public class SkeletonURIResolver implements URIResolver {

    public Source resolve(String href, String base) throws TransformerException {
        if ("skeleton1-5.xsl".equals(href)) {
            StreamSource skeleton = new StreamSource(
                    SkeletonURIResolver.class.getClassLoader().getResourceAsStream(
                            MessageValidationConstants.XSLT_SKELETON_1_5));
            return skeleton;
        }
        return null;
    }

}
