package model.ServerModule.serverMessages;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

public class Userlogin extends ServerXMLmessage
{
    public Userlogin() throws ParserConfigurationException
    {}

    @Override
    public Document packMessage(String data)
    {
        Document result = documentBuilder_.newDocument();
        Element rootElement = result.createElement("event");
        rootElement.setAttribute("event", "userlogin");
        Node name = createChild(result, "name", data);
        rootElement.appendChild(name);
        result.appendChild(rootElement);
        return result;
    }
}
