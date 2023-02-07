package model.ServerModule.clientMessages;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UserSimpleMessage extends ClientXMLmessage {
    @Override
    public String[] unpackMessage(NodeList nodeList) {
        String[] result  = new String[2];
        result[1] = "";
        result[0] = "MESSAGE";
        Node msg = nodeList.item(0);
        if (msg.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) msg;
            result[1] = getTagValue("message", element);
        }
        return result;
    }
}

