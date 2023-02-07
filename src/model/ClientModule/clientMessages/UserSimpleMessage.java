package model.ClientModule.clientMessages;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

public class UserSimpleMessage extends ClientXMLmessage {
    public UserSimpleMessage() throws ParserConfigurationException {
    }
    @Override
    public Document packMessage(String data) {
        Document result = documentBuilder_.newDocument();
        Element rootElement = result.createElement("command");
        rootElement.setAttribute("command", "message");
        Node msg = createChild(result, "message", data);
        rootElement.appendChild(msg);
        result.appendChild(rootElement);
        return result;
    }
}

