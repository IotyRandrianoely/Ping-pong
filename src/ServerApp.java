import javafx.application.Application;
import javafx.stage.Stage;

public class ServerApp extends Application {
    @Override
    public void start(Stage stage) {
        new game.GameEngine(true, 350, 45); // true=server, racket X=350, ball angle=45°
    }
}
