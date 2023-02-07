package model.messages;

import model.consts.ServerMessagesDataTypes;

public class ServerMessage {
    public ServerMessage(ServerMessagesDataTypes type, String data)
    {
        type_ = type;
        data_ = data;
    }
    ServerMessagesDataTypes type_;
    String data_;

    public ServerMessagesDataTypes getType() {
        return type_;
    }

    public String getData() {
        return data_;
    }
}
