package model.ClientModule.clientMessages;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public abstract class ClientXMLmessage {
    DocumentBuilder documentBuilder_;
    public ClientXMLmessage() throws ParserConfigurationException {
        documentBuilder_ = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }
    public abstract Document packMessage(String data);
    protected Node createChild(Document document, String name, String value)
    {
        Node childElem = document.createElement(name);
        Node textNode = document.createTextNode(value);
        childElem.appendChild(textNode);
        return childElem;
    }
}
