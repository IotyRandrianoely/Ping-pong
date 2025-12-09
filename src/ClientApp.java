import javafx.application.Application;
import javafx.stage.Stage;

public class ClientApp extends Application {
    @Override
    public void start(Stage stage) {
        new game.GameEngine(false, 350, 45); // false=client, racket X=350, ball angle=45°
    }
}
