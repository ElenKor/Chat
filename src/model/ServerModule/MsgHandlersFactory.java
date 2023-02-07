package model.ServerModule;

import model.consts.MessagesHandlerTypes;

import java.util.HashMap;
import java.util.Map;

public class MsgHandlersFactory
{
    private Map<MessagesHandlerTypes, MessagesHandler> handlersMap_;

    public MsgHandlersFactory()
    {
        handlersMap_ = new HashMap<>();
        handlersMap_.put(MessagesHandlerTypes.SERIALISATION, new SerialisationHandler());
        handlersMap_.put(MessagesHandlerTypes.XML, new XMLhandler());
    }

    public  MessagesHandler createHandler(MessagesHandlerTypes type) throws CloneNotSupportedException
    {
        MessagesHandler handler;
        handler = handlersMap_.get(type);
        MessagesHandler clone;
        clone = handler.clone();
        return clone;
    }
}
