package network;

import java.io.*;
import java.net.*;

public class Client {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public Client(String host, int port) throws IOException {
        try {
            System.out.println("=== CLIENT ===");
            System.out.println("1. Tentative de connexion à " + host + ":" + port + "...");
            
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 10000); // timeout 10 secondes
            
            System.out.println("2. ✓ Connecté au serveur !");
            System.out.println("3. IP locale : " + socket.getLocalAddress().getHostAddress());
            System.out.println("4. Port local : " + socket.getLocalPort());
            
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            
            System.out.println("5. ✓ Streams configurés, prêt à communiquer");
        } catch (IOException e) {
            System.err.println("❌ ERREUR lors de la connexion au serveur :");
            System.err.println("   Hôte : " + host);
            System.err.println("   Port : " + port);
            System.err.println("   Message : " + e.getMessage());
            System.err.println("   Type : " + e.getClass().getName());
            e.printStackTrace();
            throw e;
        }
    }

    public void send(String msg) throws IOException {
        out.writeUTF(msg);
        out.flush();
    }

    public String read() throws IOException {
        return in.readUTF();
    }
}
