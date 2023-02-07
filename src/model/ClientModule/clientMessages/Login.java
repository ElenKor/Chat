package model.ClientModule.clientMessages;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

public class Login extends ClientXMLmessage {
    public Login() throws ParserConfigurationException {
    }

    @Override
    public Document packMessage(String data) {
        Document result = documentBuilder_.newDocument();
        Element rootElement = result.createElement("command");
        rootElement.setAttribute("command", "login");
        Node name = createChild(result, "name", data);
        rootElement.appendChild(name);
        Node type = createChild(result, "type", "CHAT_CLIENT_NAME");
        rootElement.appendChild(type);
        result.appendChild(rootElement);
        return result;
    }
}
