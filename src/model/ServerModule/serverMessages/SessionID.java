package model.ServerModule.serverMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

public class SessionID extends ServerXMLmessage {
    public SessionID() throws ParserConfigurationException
    {}

    @Override
    public Document packMessage(String data)
    {
        Document result = documentBuilder_.newDocument();
        Element rootElement = result.createElement("success");
        Node childElem = createChild(result, "session", data);
        rootElement.appendChild(childElem);
        result.appendChild(rootElement);
        return result;
    }
}
