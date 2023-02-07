package model.ServerModule.serverMessages;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

public class Userlogout extends ServerXMLmessage
{
    public Userlogout() throws ParserConfigurationException
    {}

    @Override
    public Document packMessage(String data)
    {
        Document result = documentBuilder_.newDocument();
        Element rootElement = result.createElement("event");
        rootElement.setAttribute("event",  "userlogout");
        Node name = createChild(result, "name", data);
        rootElement.appendChild(name);
        result.appendChild(rootElement);
        return result;
    }
}
