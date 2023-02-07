package model.ClientModule;

import model.consts.MessagesHandlerTypes;
import model.consts.ServerMessagesDataTypes;
import model.messages.ClientMessage;
import model.messages.ServerMessage;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SerialisationHandler extends MessagesHandler {
    private ObjectInputStream socketInput_;
    private ObjectOutputStream socketOutput_;

    public SerialisationHandler() {
        super(MessagesHandlerTypes.SERIALISATION);
    }

    @Override
    public ServerMessage receiveMessage() throws Exception
    {
        String message = null;
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
        assert message != null;
        String[] messageContent = message.split("-");
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
    public  void sendMessage(ClientMessage message) throws Exception
    {
        String messageString = packMessage(message);
        try {
            if (socketOutput_ == null)
            {
                socketOutput_ = new ObjectOutputStream(socket_.getOutputStream());
            }
            if (!socket_.isClosed()) {
                socketOutput_.writeObject(messageString);
                socketOutput_.flush();
            }
        }
        catch (IOException ex)
        {
            throw ex;
        }
    }
    private String packMessage(ClientMessage message)
    {
        return message.getType().toString() + "-" + message.getData();
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

    @Override
    public void setSocket(Socket socket) {
        socket_ = socket;
    }
}
