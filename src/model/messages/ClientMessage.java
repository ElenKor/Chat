package model.messages;

import model.consts.ClientMessagesDataTypes;

public class ClientMessage{
    public ClientMessage(ClientMessagesDataTypes type, String data)
    {
        type_ = type;
        data_ = data;
    }
    ClientMessagesDataTypes type_;
    String data_;

    public ClientMessagesDataTypes getType() {
        return type_;
    }

    public String getData() {
        return data_;
    }
}
