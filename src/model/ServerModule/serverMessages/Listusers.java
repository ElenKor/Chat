package model.ServerModule.serverMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

public class Listusers extends ServerXMLmessage {
    public Listusers() throws ParserConfigurationException
    {}

    @Override
    public Document packMessage(String data)
    {
        Document result = documentBuilder_.newDocument();
        Element rootElement = result.createElement("success");
        Element listUsers = result.createElement("listusers");
        String[] users = data.split("\n");
        for(int i = 0 ; i < users.length; i++)
        {
            Node user_i = result.createElement("user");
            Node name = createChild(result, "name", users[i]);
            Node type = createChild(result, "type", "CHAT_CLIENT_" + i + 1);
            user_i.appendChild(name);
            user_i.appendChild(type);
            listUsers.appendChild(user_i);
        }
        rootElement.appendChild(listUsers);
        result.appendChild(rootElement);
        return result;
    }
}
