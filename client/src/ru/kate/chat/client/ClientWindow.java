package ru.kate.chat.client;

import ru.kate.chat.network.TCPConnection;
import ru.kate.chat.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {

    private static final String IP_ADDR = "172.16.63.97";
    private static final int PORT = 8189;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private TCPConnection tcpConnection;
    public static void main(String[] args) {

        // со свингом можно работать только из потока едт
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientWindow();
            }
        });
    }
    private final JTextArea log = new JTextArea();
    private final JTextField userName = new JTextField("Vvedite Imya");
    private final JTextField message = new JTextField();

    private ClientWindow() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);  // по центру окна
        setAlwaysOnTop(true); // всегда показывается сверху других окон

        log.setEditable(false); // поле чата изменять нельзя
        log.setLineWrap(true); // автоматический перенос словa
        add(log, BorderLayout.CENTER); //поместить в центр окна

        message.addActionListener(this); // действие по нажатию
        add(userName, BorderLayout.NORTH);
        add(message, BorderLayout.SOUTH);

        setVisible(true);
        try {
            tcpConnection = new TCPConnection(this, IP_ADDR, PORT);
        } catch (IOException e) {
            this.onException(tcpConnection, e);
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = message.getText();
        if( msg.equals(null))return;
        message.setText(null);
        tcpConnection.sendMessage(userName.getText()+ ": " + msg);
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMsg("Connection ready...");
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection closed...");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMsg("Connection exception: " + e);
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMsg(value);
    }

    public synchronized void printMsg(String msg){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getRows());
            }
        });
    }
}
