package view;

import model.consts.ClientMessagesDataTypes;
import model.consts.ServerMessagesDataTypes;
import model.messages.ClientMessage;
import model.messages.ServerMessage;
import controller.client.ClientConnection;


import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
public class ClientWindow extends JFrame {

    private Properties settings_;
    public static final int TEXT_SIZE = 20;
    private JTextArea chatField_;
    private JScrollPane chatScrollPane_;
    private JTextArea participantsList_;
    private JScrollPane participantsScrollPane_;
    private JTextField name_;
    private JTextField messageInput_;
    private ClientListener clientListener_;
    private ClientConnection connection_;
    private int serverPort_;
    private Color mainColor_;
    private Color bordersColor_;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientWindow::new);
    }

    public class ClientListener {
        public void acceptMessage(ServerMessage message)
        {
            SwingUtilities.invokeLater(() -> {
                if(message.getType().equals(ServerMessagesDataTypes.LISTUSERS))
                {
                    participantsList_.setText("Участники" + ": " + "\n");
                    String[] participants = message.getData().split("\n");
                    int i = 1;
                    for(String participant : participants)
                    {
                        participantsList_.append(i + "." + participant + "\n");
                        i++;
                    }
                    participantsList_.setCaretPosition(participantsList_.getDocument().getLength());
                    if (participantsList_ != null)
                    {
                        participantsList_.revalidate();
                    }
                }
                else if(message.getType().equals(ServerMessagesDataTypes.NO_DATA_SUCCESS))
                {
                    return;
                }
                else if(message.getType().equals(ServerMessagesDataTypes.HISTORY))
                {
                    if( message.getData().equals(" "))
                    {
                        chatField_.append("Добро пожаловать в чат, " +name_.getText()+". Отправьте первое сообщение"+"\n");
                        chatField_.setCaretPosition(chatField_.getDocument().getLength());
                        if (chatScrollPane_ != null) {
                            chatScrollPane_.revalidate();
                        }
                        return;
                    }
                    String[] events = message.getData().split("\n");
                    for(String event : events)
                    {
                        chatField_.append(event + "\n");
                    }
                    chatField_.setCaretPosition(chatField_.getDocument().getLength());
                    if (chatScrollPane_ != null) {
                        chatScrollPane_.revalidate();
                    }
                }
                else {
                    String data = message.getData();
                    chatField_.append(data + "\n");
                    chatField_.setCaretPosition(chatField_.getDocument().getLength());
                    if (chatScrollPane_ != null) {
                        chatScrollPane_.revalidate();
                    }
                }
            });
        }
        public void printClientError(String error)
        {

            chatField_.append("Ошибка отправки сообщения" + ": " + "\n");
            chatField_.append(error + "\n");

            chatField_.setCaretPosition(chatField_.getDocument().getLength());
            if (chatScrollPane_ != null) {
                chatScrollPane_.revalidate();
            }
        }
    }

    private void loadSettings() {
        try {
            FileInputStream fis = new FileInputStream("config");
            settings_ = new Properties();
            settings_.load(fis);
        } catch (IOException ex) {
            System.out.println("Config не найден");
        }
    }

    void setConstraints(GridBagConstraints constraints) {
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridheight = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.ipadx = 0;
        constraints.ipady = 0;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
    }

    private void setNameThenStartChat() {
        setSize(500, 100);

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        setConstraints(constraints);

        JTextField suggestionToEnterName = new JTextField("Как вас зовут?");
        suggestionToEnterName.setFont(new Font("Arial", Font.BOLD, TEXT_SIZE));
        suggestionToEnterName.setEditable(false);
        gbl.setConstraints(suggestionToEnterName, constraints);
        suggestionToEnterName.setBorder(new LineBorder(bordersColor_, 1));

        suggestionToEnterName.setBackground(Color.CYAN);
        add(suggestionToEnterName, BorderLayout.NORTH);

        name_ = new JTextField(30);
        name_.setFont(new Font("Arial", Font.BOLD, TEXT_SIZE));
        name_.setEditable(true);
        constraints.insets = new Insets(10, 0, 0, 0);
        constraints.gridy = GridBagConstraints.RELATIVE;
        gbl.setConstraints(name_, constraints);
        name_.setBorder(new LineBorder(bordersColor_, 1));
        name_.setBackground(Color.pink);
        add(name_);

        ActionListener listener = actionEvent -> {
            remove(suggestionToEnterName);
            remove(name_);
            gbl.setConstraints(name_, constraints);
            name_.setBackground(mainColor_);
            name_.setBorder(new LineBorder(bordersColor_, 1));
            name_.setEditable(false);
            setSize(700, 600);
            add(name_, BorderLayout.NORTH);
            serverPort_ = Integer.parseInt(settings_.getProperty("serverPort"));
            try {
                connection_ = new ClientConnection(clientListener_, "127.0.0.1", serverPort_);
            }
            catch (IOException |CloneNotSupportedException e)
            {
                connectionRefused();
                return;
            }
            connection_.startConversation();
            setChatWindow();
            repaint();
        };
        name_.addActionListener(listener);
    }

    private void connectionRefused()
    {
        chatField_.setEditable(false);
        chatField_.setLineWrap(true);
        chatField_.setFont(new Font("Arial", Font.PLAIN, TEXT_SIZE));
        chatScrollPane_ = new JScrollPane(chatField_);
        add(chatScrollPane_, BorderLayout.CENTER);
        clientListener_.acceptMessage(new ServerMessage(ServerMessagesDataTypes.ERROR, "Сервер отключен. Повторите попытку подключения."));
    }

    private void setParticipantslist()
    {
        participantsList_.setEditable(false);
        participantsList_.setLineWrap(true);
        participantsList_.setFont(new Font("Arial", Font.PLAIN, TEXT_SIZE-3));
        participantsList_.setBackground(mainColor_);
        participantsScrollPane_ = new JScrollPane(participantsList_);
        participantsScrollPane_.setBorder(new LineBorder(bordersColor_, 1));
        participantsScrollPane_.setBackground(mainColor_);
        add(participantsScrollPane_, BorderLayout.WEST);
    }
    private void setChatWindow() {
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        chatField_.setEditable(false);
        chatField_.setLineWrap(true);
        setParticipantslist();

        connection_.sendMessage(new ClientMessage(ClientMessagesDataTypes.LOGIN, name_.getText()));
        ActionListener listener = actionEvent -> {
            String msg = messageInput_.getText();
            if (msg.equals("")) {
                return;
            } else {
                messageInput_.setText(null);
                connection_.sendMessage(new ClientMessage(ClientMessagesDataTypes.MESSAGE, msg));
            }
        };
        messageInput_.addActionListener(listener);
        messageInput_.setFont(new Font("Arial", Font.PLAIN, TEXT_SIZE));
        chatField_.setFont(new Font("Arial", Font.PLAIN, TEXT_SIZE));
        chatScrollPane_ = new JScrollPane(chatField_);
        chatScrollPane_.setBorder(new LineBorder(bordersColor_, 1));
        add(chatScrollPane_, BorderLayout.CENTER);
        add(messageInput_, BorderLayout.SOUTH);
    }

    private ClientWindow() {
        super("TCP-чат");
        loadSettings();
        messageInput_ = new JTextField();
        chatField_ = new JTextArea();
        participantsList_ = new JTextArea();
        clientListener_ = new ClientListener();
        mainColor_ = new Color(155, 210, 212);
        bordersColor_ = new Color(52, 88, 129);
        setNameThenStartChat();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(JOptionPane.showConfirmDialog(ClientWindow.this, "Подтвердите выход из чата.", "Выход", JOptionPane.YES_NO_OPTION ) == JOptionPane.OK_OPTION)
                {
                    setVisible(false);
                    if(connection_ != null)
                    {
                        connection_.sendMessage(new ClientMessage(ClientMessagesDataTypes.LOGOUT, name_.getText()));
                        connection_.disconnect();
                        ClientWindow.this.dispose();
                    }
                }
            }
        });
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setVisible(true);
    }
}
