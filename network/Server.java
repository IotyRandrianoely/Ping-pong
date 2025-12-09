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
        try {
            System.out.println("=== SERVEUR ===");
            System.out.println("1. Création du ServerSocket sur le port " + port + "...");
            
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("0.0.0.0"));
            
            System.out.println("2. ✓ Serveur démarré avec succès !");
            System.out.println("3. Adresse locale : " + InetAddress.getLocalHost().getHostAddress());
            System.out.println("4. Port : " + serverSocket.getLocalPort());
            System.out.println("5. En attente d'un client... (accept() bloquant)");
            
            client = serverSocket.accept();
            
            System.out.println("6. ✓ Client connecté !");
            System.out.println("7. IP du client : " + client.getInetAddress().getHostAddress());
            System.out.println("8. Port du client : " + client.getPort());
            
            in = new DataInputStream(client.getInputStream());
            out = new DataOutputStream(client.getOutputStream());
            
            System.out.println("9. ✓ Streams configurés, prêt à communiquer");
        } catch (IOException e) {
            System.err.println("❌ ERREUR lors de la création du serveur :");
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
