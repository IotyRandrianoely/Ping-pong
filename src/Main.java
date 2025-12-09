public class Main {
    public static void main(String[] args) {
        // Choisir quel mode lancer
        // Pour lancer le serveur :
        javafx.application.Application.launch(ServerApp.class, args);
        // Pour lancer le client, décommentez la ligne suivante :
        // javafx.application.Application.launch(ClientApp.class, args);
    }
}