package view;

import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import controller.server.ChatServer;
public class MainServer extends JFrame {
    private static ChatServer mainServer;
    public MainServer(){
        JFrame window=new JFrame("Сервер");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                try {
                    mainServer.stopServer();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                e.getWindow().dispose();
            }
        });
        JPanel mainPanel = new JPanel();
        Dimension labelSize = new Dimension(80, 80);
        Border solidBorder = BorderFactory.createLineBorder(Color.CYAN, 1);
        Font font = new Font("Verdana", Font.BOLD, 12);
        mainPanel.setLayout(new BorderLayout());
        JLabel message = new JLabel("Чтобы завершить работу сервера закройте данное окно");
        message.setVerticalAlignment(JLabel.CENTER);
        message.setHorizontalAlignment(JLabel.CENTER);
        message.setPreferredSize(labelSize);
        message.setBorder(solidBorder);
        message.setFont(font);
        mainPanel.add(message,BorderLayout.CENTER);
        window.setSize(300,200);
        window.add(mainPanel);
        window.setLocationByPlatform(true);
        window.setVisible(true);
    }
    public static void main(String[] args) {
        MainServer w=new MainServer();
        mainServer = new ChatServer();
        mainServer.startWorking();
    }
}

