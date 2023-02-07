package model.ServerModule;

import model.consts.MessagesHandlerTypes;
import model.messages.ClientMessage;
import model.messages.ServerMessage;

import java.net.Socket;

public abstract class MessagesHandler implements Cloneable
{
    private MessagesHandlerTypes type_;
    protected Socket socket_;

    public MessagesHandler(MessagesHandlerTypes type)
    {
        type_ = type;
    }
    public abstract void setSocket(Socket socket);
    public abstract ClientMessage receiveMessage() throws Exception;
    public abstract void sendMessage(ServerMessage message) throws Exception;
    public abstract void finishWork();

    public MessagesHandler clone() throws CloneNotSupportedException
    {
        return (MessagesHandler)super.clone();
    }
}
