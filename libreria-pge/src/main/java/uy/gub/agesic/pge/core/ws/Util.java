package uy.gub.agesic.pge.core.ws;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public class Util {
    public static int count = 0;

    public static String assignWsuId(Element element) {
        String id = element.getAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
        if (id == null || id.length() < 1) {
            id = generateId();
            element.setAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "wsu:Id", id);
            addNamespace(element, "wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
        }
        return id;
    }

    public static Element getFirstChildElement(Node node) {
        Node child = node.getFirstChild();
        while (child != null && child.getNodeType() != 1) {
            child = child.getNextSibling();
        }
        return (Element) child;
    }

    public static Element getNextSiblingElement(Element element) {
        Node sibling = element.getNextSibling();
        while (sibling != null && sibling.getNodeType() != 1) {
            sibling = sibling.getNextSibling();
        }
        return (Element) sibling;
    }

    public static Element getPreviousSiblingElement(Element element) {
        Node sibling = element.getPreviousSibling();
        while (sibling != null && sibling.getNodeType() != 1) {
            sibling = sibling.getPreviousSibling();
        }
        return (Element) sibling;
    }

    public static Element findElement(Element root, String localName, String namespace) {
        return findElement(root, new QName(namespace, localName));
    }

    public static Element findElement(Element root, QName name) {
        if (matchNode(root, name)) {
            return root;
        }

        for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == 1) {
                Element possibleMatch = findElement((Element) child, name);
                if (possibleMatch != null)
                    return possibleMatch;
            }
        }
        return null;
    }

    public static List<Node> findAllElements(Element root, QName name, boolean local) {
        List<Node> list = new ArrayList<>();
        if (matchNode(root, name, local)) {
            list.add(root);
        }

        for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == 1) {
                list.addAll(findAllElements((Element) child, name, local));
            }
        }
        return list;
    }

    public static Element findElementByWsuId(Element root, String id) {
        if (id.equals(getWsuId(root))) {
            return root;
        }

        for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == 1) {
                Element possibleMatch = findElementByWsuId((Element) child, id);
                if (possibleMatch != null)
                    return possibleMatch;
            }
        }
        return null;
    }

    public static Element findOrCreateSoapHeader(Element envelope) {
        String prefix = envelope.getPrefix();
        String uri = envelope.getNamespaceURI();
        QName name = new QName(uri, "Header");
        Element header = findElement(envelope, name);
        if (header == null) {
            header = envelope.getOwnerDocument().createElementNS(uri, prefix + ":Header");
            envelope.insertBefore(header, envelope.getFirstChild());
        }
        return header;
    }

    public static String getWsuId(Element element) {
        if (element.hasAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id")) {
            return element.getAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
        }
        if (element.hasAttribute("Id")) {
            String ns = element.getNamespaceURI();
            if ("http://www.w3.org/2000/09/xmldsig#".equals(ns) || "http://www.w3.org/2001/04/xmlenc#".equals(ns)) {
                return element.getAttribute("Id");
            }
        }
        return null;
    }

    public static boolean equalStrings(String string1, String string2) {
        if (string1 == null && string2 == null) {
            return true;
        }
        return (string1 != null && string1.equals(string2));
    }

    public static boolean matchNode(Node node, QName name) {
        return matchNode(node, name, false);
    }

    public static boolean matchNode(Node node, QName name, boolean local) {
        return (equalStrings(node.getLocalName(), name.getLocalPart()) && (local || equalStrings(node.getNamespaceURI(), name.getNamespaceURI())));
    }

    public static String generateId() {
        return generateId("element");
    }

    public static void addNamespace(Element element, String prefix, String uri) {
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, uri);
    }

    public static String generateId(String prefix) {
        StringBuilder id = new StringBuilder();
        long time = System.currentTimeMillis();
        synchronized (Util.class) {
            count++;
        }
        id.append(prefix).append("-").append(count).append("-").append(time).append("-").append(id.hashCode());
        return id.toString();
    }
}