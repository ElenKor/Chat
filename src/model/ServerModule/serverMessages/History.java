package model.ServerModule.serverMessages;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

public class History extends ServerXMLmessage
{
    public History() throws ParserConfigurationException
    {}

    @Override
    public Document packMessage(String data)
    {
        Document result = documentBuilder_.newDocument();
        Element rootElement =  result.createElement("success");
        Element listEvents = result.createElement("history");
        String[] events = data.split("\n");
        for (String s : events) {
            Node event = createChild(result, "event", s);
            listEvents.appendChild(event);
        }
        rootElement.appendChild(listEvents);
        result.appendChild(rootElement);
        return result;
    }
}
