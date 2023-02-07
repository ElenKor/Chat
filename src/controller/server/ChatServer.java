package controller.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.LogManager;

import model.consts.ServerMessagesDataTypes;
import model.messages.ServerMessage;

public class ChatServer
{
    private ServerAssistant serverAssistant_;
    protected ServerSocket serverSocket;
    private HashMap<Integer, ServerConnection> connections_;
    private Properties settings_;
    private int serverPort_;
    private Logger logger_;
    private ArrayList<String> history_;
    private ScheduledThreadPoolExecutor executor_;

    private boolean log;

    public class ServerAssistant
    {
        void informOthers(ServerConnection connection,  ServerMessage message)
        {
            saveMessage(message.getData());
            connections_.forEach((key, value) -> {
                if (value != connection) {
                    value.sendMessage(message);
                }
            });
        }
        void logInfo(String event)
        {
            if(log) logger_.info(event);
        }
        void addClient(ServerConnection newConnection)
        {
            int ID = ChatServer.this.newSessionID();
            newConnection.setSessionID(ID);
            connections_.put(ID, newConnection);
            String participantsList  =  getParticipantslist();
            sendToAllConnections(new ServerMessage(ServerMessagesDataTypes.LISTUSERS, participantsList));
        }
        void sendToEveryone(ServerMessage message)
        {
            saveMessage(message.getData());
            ChatServer.this.sendToAllConnections(message);
        }
        void disconnectClient(int sessionID)
        {
            executor_.remove(connections_.get(sessionID));
            connections_.remove(sessionID);
            String participantsList  =  getParticipantslist();
            sendToAllConnections(new ServerMessage(ServerMessagesDataTypes.LISTUSERS, participantsList));
        }
        String getHistory()
        {
            StringBuilder result = new StringBuilder();
            if(history_.size() == 0)
            {
                return " ";
            }
            for (String message : history_) {
                result.append(message).append("\n");
            }
            result = new StringBuilder(result.substring(0, result.length() - "\n".length()));
            return result.toString();
        }

        public void sendToClient(ServerConnection connection, ServerMessage message)
        {
            connection.sendMessage(message);
        }

        public void logError(String error)
        {
            if(log) logger_.warning(error);
        }

        public String getParticipantslist()
        {
            return  ChatServer.this.getParticipantslist();
        }
    }

    private void loadSettings() {
        try {
            LogManager.getLogManager().readConfiguration(ChatServer.class.getResourceAsStream("log.properties"));
            logger_ = Logger.getLogger(ChatServer.class.getName());
            FileInputStream settingsFile = new FileInputStream("config");
            settings_ = new Properties();
            settings_.load(settingsFile);
            log=Boolean.parseBoolean(settings_.getProperty("log"));
        } catch (IOException ex) {
            if(log)
                logger_.warning("Config file is not found!");
        }
    }
    public ChatServer()
    {
        loadSettings();
    }

    public void startWorking()
    {
        serverPort_ =  Integer.parseInt(settings_.getProperty("serverPort"));
        connections_ = new HashMap<>();
        history_ = new ArrayList<>();
        serverAssistant_ = new ServerAssistant();
        int maxThreadsNumber = Integer.parseInt(settings_.getProperty("maxClients"));
        executor_ = new ScheduledThreadPoolExecutor(maxThreadsNumber);
        if(log) logger_.info("Server is working.");
        try
        {
            serverSocket = new ServerSocket(serverPort_);
            int timeout =  Integer.parseInt(settings_.getProperty("timeout"));
            while(!serverSocket.isClosed())
            {
                //метод ожидает, пока клиент не подключится к серверу по указанному порту.
                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout(timeout*1000);
                ServerConnection newConn = new ServerConnection(serverAssistant_, clientSocket);
                executor_.scheduleAtFixedRate(newConn, 0, 500, TimeUnit.MILLISECONDS );
            }
        } catch (SocketException e) {if(log) logger_.info("Server is closed"); }
        catch (IOException e)     { e.printStackTrace(); }
        catch(Exception e)
        {
            if(log) logger_.warning(e.toString());
        }
    }
    public void stopServer() throws IOException
    {
        serverSocket.close();
    }

    private void sendToAllConnections(ServerMessage message)
    {
        connections_.forEach((key, value) -> value.sendMessage(message));
    }

    public void saveMessage(String message)
    {
        int historySize =  Integer.parseInt(settings_.getProperty("histroryMessages"));
        if (history_.size() >= historySize)
        {
            history_.remove(0);
            history_.add(message);
        }
        else
        {
            history_.add(message);
        }
    }
    private String getParticipantslist()
    {
        StringBuilder result = new StringBuilder();
        StringBuilder finalResult = result;
        connections_.forEach((key, value) ->
                finalResult.append(value.getName()).append("\n"));
        if(result.length() > 0)
        {
            result = new StringBuilder(result.substring(0, result.length() - "\n".length()));
        }
        return result.toString();
    }
    int newSessionID()
    {
        Object[] keys = connections_.keySet().toArray();
        Arrays.sort(keys);
        if(keys.length == 0)
        {
            return 0;
        }
        int maxSessionID = (int)keys[keys.length - 1];
        return maxSessionID+1;
    }
}
