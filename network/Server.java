package network;

import java.io.*;
import java.net.*;

import javafx.stage.Stage;

public class Server {
    private ServerSocket serverSocket;
    private Socket client;
    private DataInputStream in;
    private DataOutputStream out;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        client = serverSocket.accept();
        in = new DataInputStream(client.getInputStream());
        out = new DataOutputStream(client.getOutputStream());
    }

    public void send(String msg) throws IOException {
        out.writeUTF(msg);
        out.flush();
    }

    public String read() throws IOException {
        return in.readUTF();
    }
  
}
