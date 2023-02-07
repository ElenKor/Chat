package controller.server;

import model.consts.ClientMessagesDataTypes;
import model.consts.MessagesHandlerTypes;
import model.consts.ServerMessagesDataTypes;
import model.messages.ClientMessage;
import model.messages.ServerMessage;
import model.ServerModule.MessagesHandler;
import model.ServerModule.MsgHandlersFactory;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;


public class ServerConnection implements Runnable
{
    private Properties settings_;
    private Socket clientSocket_;
    public static final String  SHOW_TEXT =  ": ";
    private String connectionName_;
    private ChatServer.ServerAssistant serverAssistant_;
    private MessagesHandler msgHandler_;
    private int sessionID_;
    private void loadSettings() {
        try {
            FileInputStream fis = new FileInputStream("config");
            settings_ = new Properties();
            settings_.load(fis);
        } catch (IOException ex) {
            System.out.println("Config file is not found!");
        }
    }
    public ServerConnection(ChatServer.ServerAssistant serverAssistant, Socket clientSocket) throws CloneNotSupportedException {
        clientSocket_ = clientSocket;
        serverAssistant_ = serverAssistant;
        loadSettings();
        try {
            MessagesHandlerTypes msgHandlerType = MessagesHandlerTypes.valueOf(settings_.getProperty("messagesHandlerType"));
            MsgHandlersFactory handlersFactory = new MsgHandlersFactory();
            msgHandler_ = handlersFactory.createHandler(msgHandlerType);
            msgHandler_.setSocket(clientSocket_);
        } catch (CloneNotSupportedException ex) {
            throw ex;
        }
    }
    public void sendMessage(ServerMessage message)
    {
        try {
            msgHandler_.sendMessage(message);
        }
        catch (Exception e) {
            serverAssistant_.logError(e.toString());
        }
    }
    public void disconnect() {
        serverAssistant_.disconnectClient(ServerConnection.this.sessionID_);
        try
        {
            msgHandler_.finishWork();
            clientSocket_.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public String getName()
    {
        return  connectionName_;
    }
    void setSessionID(int ID)
    {
        sessionID_ = ID;
    }

    public int getSessionID() {
        return sessionID_;
    }

    @Override
    public void run() {
        if(!clientSocket_.isClosed())
        {
            ServerMessage serverMessage;
            ClientMessage messageFromClient;
            try {
                messageFromClient = msgHandler_.receiveMessage();
            }
            catch(java.net.SocketTimeoutException ex)
            {
                serverAssistant_.logInfo("Server disconnected client" + " " + connectionName_ + " " + "because of timeout.");
                serverMessage = new ServerMessage(ServerMessagesDataTypes.ERROR, "Server disconnected you because of timeout.");
                try {
                    msgHandler_.sendMessage(serverMessage);
                }catch (Exception e) {
                    serverAssistant_.logError(e.toString());
                }
                this.disconnect();
                serverAssistant_.informOthers(ServerConnection.this, new ServerMessage(ServerMessagesDataTypes.MESSAGE, connectionName_ + SHOW_TEXT + "server disconnected the client because of timeout."));
                return;
            }
            catch(EOFException | SocketException ex)
            {
                serverAssistant_.logInfo("Client disconnected" + SHOW_TEXT + connectionName_);
                serverMessage = new ServerMessage(ServerMessagesDataTypes.USERLOGOUT, connectionName_ + " " + "disconnected.");
                serverAssistant_.informOthers(ServerConnection.this, serverMessage);
                this.disconnect();
                return;
            }
            catch(Exception ex)
            {
                serverMessage = new ServerMessage(ServerMessagesDataTypes.ERROR, ex.toString());
                try {
                    msgHandler_.sendMessage(serverMessage);
                } catch (Exception e) {
                    serverAssistant_.logError(e.toString());
                }
                return;
            }
            if(messageFromClient.getType().equals(ClientMessagesDataTypes.LOGIN))
            {
                connectionName_ = messageFromClient.getData();
                try {
                    msgHandler_.sendMessage(new ServerMessage(ServerMessagesDataTypes.SESSION_ID, String.valueOf(ServerConnection.this.sessionID_)));
                    msgHandler_.sendMessage(new ServerMessage(ServerMessagesDataTypes.HISTORY, serverAssistant_.getHistory()));
                }
                catch (Exception e) {
                    serverAssistant_.logError(e.toString());
                }
                serverAssistant_.logInfo("New client connected" + SHOW_TEXT + connectionName_);
                serverAssistant_.addClient(ServerConnection.this);
                serverMessage = new ServerMessage(ServerMessagesDataTypes.USERLOGIN, messageFromClient.getData() + " "+ "connected.");
                serverAssistant_.informOthers(ServerConnection.this, serverMessage);
            }
            else if(messageFromClient.getType().equals(ClientMessagesDataTypes.LOGOUT))
            {
                this.disconnect();
                serverAssistant_.logInfo("Client disconnected" + SHOW_TEXT + connectionName_);
                serverAssistant_.informOthers(ServerConnection.this, new ServerMessage(ServerMessagesDataTypes.USERLOGOUT, connectionName_ + " " + "disconnected."));
            }
            else if(messageFromClient.getType().equals(ClientMessagesDataTypes.MESSAGE))
            {
                serverAssistant_.logInfo("Message from client" + " " + connectionName_ + SHOW_TEXT + messageFromClient.getData());
                serverAssistant_.sendToClient(ServerConnection.this, new ServerMessage(ServerMessagesDataTypes.NO_DATA_SUCCESS, null));
                serverAssistant_.sendToEveryone(new ServerMessage(ServerMessagesDataTypes.MESSAGE, connectionName_+ SHOW_TEXT + messageFromClient.getData()));
            }
        }
    }
}
