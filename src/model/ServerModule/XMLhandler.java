package model.ServerModule;

import model.consts.*;
import model.messages.ClientMessage;
import model.messages.ServerMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import model.ServerModule.clientMessages.*;
import model.ServerModule.serverMessages.Error;
import model.ServerModule.serverMessages.ServerSimpleMessage;
import model.ServerModule.serverMessages.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
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
    HashMap<ServerMessagesDataTypes, ServerXMLmessage> serverMessages_;
    HashMap<String, ClientXMLmessage> clientMessages_;
    public XMLhandler()
    {
        super(MessagesHandlerTypes.XML);
        serverMessages_ = new HashMap<>();
        clientMessages_ = new HashMap<>();
        try {
            serverMessages_.put(ServerMessagesDataTypes.NO_DATA_SUCCESS, new EmptySuccess());
            serverMessages_.put(ServerMessagesDataTypes.ERROR, new Error());
            serverMessages_.put(ServerMessagesDataTypes.HISTORY, new History());
            serverMessages_.put(ServerMessagesDataTypes.LISTUSERS, new Listusers());
            serverMessages_.put(ServerMessagesDataTypes.MESSAGE, new ServerSimpleMessage());
            serverMessages_.put(ServerMessagesDataTypes.SESSION_ID, new SessionID());
            serverMessages_.put(ServerMessagesDataTypes.USERLOGIN, new Userlogin());
            serverMessages_.put(ServerMessagesDataTypes.USERLOGOUT, new Userlogout());
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        clientMessages_.put("login", new Login());
        clientMessages_.put("logout", new Logout());
        clientMessages_.put("message", new UserSimpleMessage());
    }

    @Override
    public void setSocket(Socket socket) {
        socket_ = socket;
    }

    @Override
    public ClientMessage receiveMessage() throws Exception {

        String message = null;
        try
        {
            if(socketInput_ == null)
            {
                try {
                    socketInput_ = new BufferedReader(new InputStreamReader(socket_.getInputStream()));
                }
                catch(java.net.SocketTimeoutException ex)
                {
                    throw ex;
                }
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
        String[] messageContent = unpackMessage(message);
        ClientMessagesDataTypes type;
        try {
            type = ClientMessagesDataTypes.valueOf(messageContent[0]);
        }
        catch(IllegalArgumentException ex)
        {
            throw ex;
        }
        String data = messageContent[1];
        return new ClientMessage(type, data);
    }

    private String[] unpackMessage(String message)
    {
        Document document;
        document = stringToDocument(message);
        document.getDocumentElement().normalize();
        Element rootElement = document.getDocumentElement();
        String attributes = rootElement.getAttribute(rootElement.getNodeName());
        NodeList nodeList = document.getElementsByTagName(rootElement.getNodeName());
        return clientMessages_.get(attributes).unpackMessage(nodeList);
    }
    private Document stringToDocument(String xmlSource)
    {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        Document result = null;
        try
        {
            docBuilder = builderFactory.newDocumentBuilder();
            result  = docBuilder.parse(new InputSource(new StringReader(xmlSource)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }
    @Override
    public void sendMessage(ServerMessage message) throws Exception
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
    private Document packMessage(ServerMessage message)
    {
        return serverMessages_.get(message.getType()).packMessage(message.getData());
    }

    private String documentToString(Document doc) {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transfObject;
        String result = null;
        try {
            transfObject = tFactory.newTransformer();
            transfObject.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transfObject.transform(new DOMSource(doc), new StreamResult(writer));
            result = writer.getBuffer().toString();
        } catch (TransformerException e) {
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
