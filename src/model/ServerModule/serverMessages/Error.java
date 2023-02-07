package model.ServerModule.serverMessages;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

public class Error extends ServerXMLmessage {
    public Error() throws ParserConfigurationException
    {}

    @Override
    public Document packMessage(String data)
    {
        Document result = documentBuilder_.newDocument();
        Element rootElement = result.createElement("error");
        Node msg = createChild(result, "message", data);
        rootElement.appendChild(msg);
        result.appendChild(rootElement);
        return result;
    }
}
