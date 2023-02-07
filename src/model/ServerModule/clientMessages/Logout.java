package model.ServerModule.clientMessages;
import model.consts.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Logout extends ClientXMLmessage {
    @Override
    public String[] unpackMessage(NodeList nodeList) {
        String[] result  = new String[2];
        result[1] = "";
        result[0] = String.valueOf(ClientMessagesDataTypes.LOGOUT);
        Node name = nodeList.item(0);
        if (name.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) name;
            result[1] = getTagValue("name", element);
        }
        return result;
    }
}
