package com.suprasync.mirthsync.xml;

import com.suprasync.mirthsync.logging.Logger;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * XML processing utilities using JDOM2.
 * Provides methods for parsing, manipulating, and serializing XML.
 */
public class XmlUtils {

    /**
     * Parse an XML string into a JDOM Document.
     * 
     * @param xmlString The XML string to parse
     * @return Parsed Document
     * @throws IOException if parsing fails
     */
    public static Document parseString(String xmlString) throws IOException {
        try {
            SAXBuilder builder = new SAXBuilder();
            return builder.build(new StringReader(xmlString));
        } catch (JDOMException e) {
            throw new IOException("Failed to parse XML", e);
        }
    }

    /**
     * Convert a JDOM Element to an indented XML string.
     * 
     * @param element The element to serialize
     * @return Indented XML string
     */
    public static String elementToString(Element element) {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        return outputter.outputString(element);
    }

    /**
     * Convert a JDOM Document to an indented XML string.
     * 
     * @param document The document to serialize
     * @return Indented XML string
     */
    public static String documentToString(Document document) {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        return outputter.outputString(document);
    }

    /**
     * Find an element by ID within a document.
     * 
     * @param doc The document to search
     * @param id The ID to find
     * @return The element with the given ID, or null if not found
     */
    public static Element findById(Document doc, String id) {
        return findById(doc.getRootElement(), id);
    }

    /**
     * Find an element by ID within an element.
     * 
     * @param parent The parent element to search
     * @param id The ID to find
     * @return The element with the given ID, or null if not found
     */
    public static Element findById(Element parent, String id) {
        if (parent == null || id == null) {
            return null;
        }
        
        // Check this element
        Element idElement = parent.getChild("id");
        if (idElement != null && id.equals(idElement.getText())) {
            return parent;
        }
        
        // Recursively check children
        for (Element child : parent.getChildren()) {
            Element found = findById(child, id);
            if (found != null) {
                return found;
            }
        }
        
        return null;
    }

    /**
     * Get the text content of a child element.
     * 
     * @param parent The parent element
     * @param childName The name of the child element
     * @return The text content, or null if not found
     */
    public static String getChildText(Element parent, String childName) {
        if (parent == null) {
            return null;
        }
        Element child = parent.getChild(childName);
        return child != null ? child.getText() : null;
    }

    /**
     * Add or update a child element within a parent element by ID.
     * If an element with the same ID exists, it is replaced.
     * 
     * @param parent The parent element
     * @param child The child element to add or update
     */
    public static void addOrUpdateChild(Element parent, Element child) {
        if (parent == null || child == null) {
            return;
        }
        
        String childId = getChildText(child, "id");
        if (childId == null) {
            // No ID, just add it
            parent.addContent(child.clone());
            return;
        }
        
        // Find existing child with same ID
        Element existing = null;
        for (Element elem : parent.getChildren()) {
            String elemId = getChildText(elem, "id");
            if (childId.equals(elemId)) {
                existing = elem;
                break;
            }
        }
        
        if (existing != null) {
            // Replace existing
            int index = parent.indexOf(existing);
            parent.removeContent(existing);
            parent.addContent(index, child.clone());
        } else {
            // Add new
            parent.addContent(child.clone());
        }
    }
}
