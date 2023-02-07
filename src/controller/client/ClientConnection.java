package controller.client;

import model.consts.ServerMessagesDataTypes;
import model.consts.MessagesHandlerTypes;
import model.ClientModule.MessagesHandler;
import model.ClientModule.MsgHandlersFactory;
import model.messages.ClientMessage;
import model.messages.ServerMessage;
import view.ClientWindow;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;


public class ClientConnection
{
    private int sessionID_;
    private Socket clientSocket_;
    public ClientWindow.ClientListener listener_;
    public Thread threadListener_;
    private MessagesHandler msgHandler_;

    public ClientConnection(ClientWindow.ClientListener clientListener,  String IPAddress, int port) throws IOException, CloneNotSupportedException {

        listener_ = clientListener;
        clientSocket_ = new Socket(IPAddress, port);
        FileInputStream fis = new FileInputStream("config");
        Properties settings_;
        settings_ = new Properties();
        settings_.load(fis);
        MessagesHandlerTypes msgHandlerType =  MessagesHandlerTypes.valueOf(settings_.getProperty("messagesHandlerType"));
        MsgHandlersFactory handlersFactory = new MsgHandlersFactory();
        msgHandler_ = handlersFactory.createHandler(msgHandlerType);
        msgHandler_.setSocket(clientSocket_);
        threadListener_ = new Thread(() -> {
            while(!clientSocket_.isClosed() && !threadListener_.isInterrupted())
            {
                ServerMessage messageFromServer;
                try {
                    messageFromServer = msgHandler_.receiveMessage();
                }
                catch(NullPointerException ex)
                {
                   break;
                }
                catch(EOFException | SocketException ex)
                {
                    messageFromServer = new ServerMessage(ServerMessagesDataTypes.ERROR, "Connection was interrupted.");
                }
                catch(Exception ex)
                {
                    messageFromServer = new ServerMessage(ServerMessagesDataTypes.ERROR, ex.toString());
                }

                if(messageFromServer.getType().equals(ServerMessagesDataTypes.ERROR))
                {
                    if(messageFromServer.getData().equals("Server disconnected you because of timeout.") || messageFromServer.getData().equals("Connection was interrupted."))
                    {
                        listener_.acceptMessage(messageFromServer);
                        this.disconnect();
                        return;
                    }
                }
                if(messageFromServer.getType().equals(ServerMessagesDataTypes.SESSION_ID))
                {
                    ClientConnection.this.sessionID_ = Integer.parseInt(messageFromServer.getData());
                    continue;
                }
                listener_.acceptMessage(messageFromServer);
            }
        });
    }

    public void startConversation()
    {
        threadListener_.start();
    }
    public void sendMessage(ClientMessage message)
    {
        try {
            msgHandler_.sendMessage(message);
        } catch (Exception ex) {
            listener_.printClientError(ex.toString());
        }
    }

    public void disconnect()
    {
        try {
            msgHandler_.finishWork();
            clientSocket_.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        threadListener_.interrupt();
    }

    public int getSessionID() {
        return sessionID_;
    }
}
