package nl.knaw.huygens.oai;

import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;


public class OAINamespaceContext implements NamespaceContext {
    @Override
    public String getNamespaceURI(String prefix) {
        switch (prefix) {
            case "xsi": return "http://www.w3.org/2001/XMLSchema-instance";
            case "oai": return "http://www.openarchives.org/OAI/2.0/";
            case "didl": return "urn:mpeg:mpeg21:2002:02-DIDL-NS";
            case "dc": return "http://purl.org/dc/elements/1.1/";
            case "dcterms": return "http://purl.org/dc/terms/";
            case "dcx": return "http://krait.kb.nl/coop/tel/handbook/telterms.html";
            case "srw": return "http://www.loc.gov/zing/srw/";
            case "srw_dc": return "info:srw/schema/1/dc-v1.1";
            case "ddd": return "http://www.kb.nl/namespaces/ddd";
            default:
        }
        return null;
    }

    @Override
    public String getPrefix(String s) { return null; }
    @Override
    public Iterator getPrefixes(String s) { return null; }
}
