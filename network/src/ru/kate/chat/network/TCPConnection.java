package ru.kate.chat.network;

import sun.rmi.transport.tcp.TCPChannel;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;


public class TCPConnection {
    private final Socket socket;
    TCPConnectionListener eventListener;
    private final Thread rxThread; // поток входящих сообщений
    private final BufferedReader in;
    private final BufferedWriter out;


    public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.socket = socket;
        this.eventListener = eventListener;
        // получаем из сокета входящий и исходящий поток
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));

        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while(!rxThread.isInterrupted()){
                        eventListener.onReceiveString(TCPConnection.this, in.readLine());
                    }
                }catch (IOException e){
                    eventListener.onException(TCPConnection.this, e);
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        rxThread.start();
    }

    public TCPConnection ( TCPConnectionListener eventListener, String ipAddr, int port)throws IOException{
       this(eventListener, new Socket(ipAddr, port));
    }

    public synchronized void sendMessage(String string){
        try {
            out.write(string + "\r\n");  //возврат каретки и перевод строки
            out.flush();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect(){
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }
    @Override
    public String toString(){
        return "TCPConnection" + socket.getInetAddress() + " " + socket.getPort();
    }
}
