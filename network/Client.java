package network;

import java.io.*;
import java.net.*;

import javafx.stage.Stage;

public class Client {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public Client(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    public void send(String msg) throws IOException {
        out.writeUTF(msg);
        out.flush();
    }

    public String read() throws IOException {
        return in.readUTF();
    }

}
