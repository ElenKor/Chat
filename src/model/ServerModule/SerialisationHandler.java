package model.ServerModule;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import model.consts.*;
import model.messages.*;

public class SerialisationHandler extends  MessagesHandler {
    private ObjectInputStream socketInput_;
    private ObjectOutputStream socketOutput_;

    public SerialisationHandler() {
        super(MessagesHandlerTypes.SERIALISATION);
    }

    @Override
    public ClientMessage receiveMessage() throws Exception {
        String message="";
        try
        {
            if(socketInput_ == null)
            {
                try {
                    socketInput_ = new ObjectInputStream(socket_.getInputStream());
                }
                catch(java.net.SocketTimeoutException ex)
                {
                    throw ex;
                }
            }
            if(!socket_.isClosed())
            {
               try {
                    message = (String) socketInput_.readObject();
                }
                catch (EOFException | SocketTimeoutException ex)
                {
                    throw ex;
                }
            }
        } catch (IOException | ClassNotFoundException ex)
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
        return message.split("-");
    }

    @Override
    public  void sendMessage(ServerMessage message) throws Exception {
        String messageString = packMessage(message);
        try {
            if(socketOutput_ == null)
            {
                socketOutput_ = new ObjectOutputStream(socket_.getOutputStream());
            }
            if(!socket_.isClosed())
            {
                socketOutput_.writeObject(messageString);
                socketOutput_.flush();
            }
        } catch (IOException ex)
        {
            throw ex;
        }
    }
    private String packMessage(ServerMessage message)
    {
        return message.getType().toString() + "-" + message.getData();
    }
    @Override
    public void finishWork()
    {
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

    @Override
    public void setSocket(Socket socket) {
        socket_ = socket;
    }
}
