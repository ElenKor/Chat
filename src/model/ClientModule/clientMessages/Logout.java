package model.ClientModule.clientMessages;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

public class Logout extends ClientXMLmessage {
    public Logout() throws ParserConfigurationException {
    }

    @Override
    public Document packMessage(String data) {
        Document result = documentBuilder_.newDocument();
        Element rootElement = result.createElement("command");
        rootElement.setAttribute("command", "logout");
        Node session = createChild(result, "name", data);
        rootElement.appendChild(session);
        result.appendChild(rootElement);
        return result;
    }
}
