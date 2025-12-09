import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ServerApp extends Application {
    @Override
    public void start(Stage stage) {
        new game.GameEngine(true, 350, 45, () -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Connexion client");
            alert.setHeaderText(null);
            alert.setContentText("Le client est bien connecté !");
            alert.show();
        });
    }
}