package model.ClientModule;
import model.consts.*;
import model.messages.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import model.ClientModule.clientMessages.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
public class XMLhandler extends MessagesHandler
{
    private BufferedReader socketInput_;
    private BufferedWriter socketOutput_;
    HashMap<ClientMessagesDataTypes, ClientXMLmessage> clientMessages_;
    public XMLhandler()
    {
        super(MessagesHandlerTypes.XML);
        clientMessages_  =new HashMap<>();
        try {
            clientMessages_.put(ClientMessagesDataTypes.LOGIN, new Login());
            clientMessages_.put(ClientMessagesDataTypes.MESSAGE, new UserSimpleMessage());
            clientMessages_.put(ClientMessagesDataTypes.LOGOUT, new Logout());

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setSocket(Socket socket) {
        socket_ = socket;
    }

    @Override
    public ServerMessage receiveMessage() throws Exception
    {
        String message = null;
        try
        {
            if(socketInput_ == null)
            {
                socketInput_ = new BufferedReader(new InputStreamReader(socket_.getInputStream()));
            }
            if(!socket_.isClosed())
            {
                try {
                    message = socketInput_.readLine();
                }
                catch (EOFException | SocketTimeoutException ex)
                {
                    throw ex;
                }
            }
        } catch (IOException ex)
        {
            throw ex;
        }
        String[] messageContent;
        try {
            messageContent = unpackMessage(message);
        } catch (Exception ex)
        {
           throw ex;
        }
        ServerMessagesDataTypes type;
        try {
            type = ServerMessagesDataTypes.valueOf(messageContent[0]);
        }
        catch(IllegalArgumentException ex)
        {
            throw ex;
        }
        String data = messageContent[1];
        return new ServerMessage(type, data);
    }
    @Override
    public void sendMessage(ClientMessage message) throws Exception
    {
        Document messageDocument = packMessage(message);
        String messageString = documentToString(messageDocument);
        try {
            if(socketOutput_ == null)
            {
                socketOutput_ = new BufferedWriter( new OutputStreamWriter(socket_.getOutputStream()));
            }
            if(!socket_.isClosed())
            {
                socketOutput_.write(messageString + "\n");
                socketOutput_.flush();
            }
        } catch (IOException ex)
        {
            throw ex;
        }
    }

    private Document packMessage(ClientMessage message)
    {
        return clientMessages_.get(message.getType()).packMessage(message.getData());
    }

    private String[] unpackMessage(String message) throws Exception
    {
        Document document;
        try {
                document = stringToDocument(message);
        } catch (Exception ex)
        {
            throw ex;
        }
        document.getDocumentElement().normalize();
        Element rootElement = document.getDocumentElement();
        String nodeName = rootElement.getNodeName();
        String attributes;
        NodeList nodeList = document.getElementsByTagName(rootElement.getNodeName());
        String[] result  = new String[2];
        result[0] = "";
        result[1] = "";
        if(nodeName.equals("event"))
        {
            attributes = rootElement.getAttribute(nodeName);
            switch (attributes) {
                case "message" -> {
                    result[0] = "MESSAGE";
                    Node firstNode = nodeList.item(0);
                    if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) firstNode;
                        result[1] = getTagValue("message", element);
                    }
                }
                case "userlogin" -> {
                    result[0] = String.valueOf(ServerMessagesDataTypes.USERLOGIN);
                    Node firstNode = nodeList.item(0);
                    if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) firstNode;
                        result[1] = getTagValue("name", element);
                    }
                }
                case "userlogout" -> {
                    result[0] = String.valueOf(ServerMessagesDataTypes.USERLOGOUT);
                    Node firstNode = nodeList.item(0);
                    if (firstNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) firstNode;
                        result[1] = getTagValue("name", element);
                    }
                }
            }
        }
        else {
            if(nodeName.equals("success"))
            {
                Node firstNode = nodeList.item(0);
                if (!firstNode.hasChildNodes())
                {
                    result[0] = String.valueOf(ServerMessagesDataTypes.NO_DATA_SUCCESS);
                    result[1] = " ";
                    return result;
                }
                Node first = firstNode.getFirstChild();
                if (first.getNodeType() == Node.ELEMENT_NODE)
                {
                    String firstChildName = first.getNodeName();
                    switch (firstChildName) {
                        case "session" -> {
                            Element element = (Element) firstNode;
                            result[0] = String.valueOf(ServerMessagesDataTypes.SESSION_ID);
                            result[1] = getTagValue(firstChildName, element);
                        }
                        case "listusers" -> {
                            result[0] = String.valueOf(ServerMessagesDataTypes.LISTUSERS);
                            NodeList childList = document.getElementsByTagName(firstNode.getNodeName());
                            NodeList users = childList.item(0).getChildNodes().item(0).getChildNodes();
                            for (int i = 0; i < users.getLength(); i++) {
                                result[1] += (getTagValue("name", (Element) users.item(i)) + "\n");
                            }
                        }
                        case "history" -> {
                            NodeList childList = document.getElementsByTagName(firstNode.getNodeName());
                            NodeList events = childList.item(0).getChildNodes().item(0).getChildNodes();
                            result[0] = String.valueOf(ServerMessagesDataTypes.HISTORY);
                            for (int i = 0; i < events.getLength() - 1; i++) {
                                result[1] += (events.item(i).getTextContent() + "\n");
                            }
                            result[1] += events.item(events.getLength() - 1).getTextContent();
                        }
                    }
                }
            }
            else if(nodeName.equals("error"))
            {
                result[0] =  String.valueOf(ServerMessagesDataTypes.ERROR);
                Node name = nodeList.item(0);
                if (name.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element element = (Element) name;
                    result[1] = getTagValue("message", element);
                }
            }
        }
        return result;
    }
    private Document stringToDocument(String xmlString) throws Exception
    {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        Document result;
        docBuilder = builderFactory.newDocumentBuilder();
        result  = docBuilder.parse(new InputSource(new StringReader(xmlString)));
        return result;
    }
    private  String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }
    private String documentToString(Document doc)
    {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transfObject;
        String result = null;
        try
        {
            transfObject = tFactory.newTransformer();
            transfObject.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transfObject.transform(new DOMSource(doc), new StreamResult(writer));
            result =  writer.getBuffer().toString();
        }
        catch (TransformerException e)
        {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public void finishWork() {
        try {
            if(socketInput_!= null && socketOutput_ != null)
            {
                socketInput_.close();
                socketOutput_.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
