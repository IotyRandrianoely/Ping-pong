package game;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import network.Server;
import network.Client;

public class GameEngine {
    private boolean isServer;
    private Racket racket;
    private Racket remoteRacket;
    private Ball ball;
    private Server server;
    private Client client;
    private AnimationTimer loop;
    private volatile String lastState; // latest network state
    private volatile double lastClientRacketX = -1; // reçu par le serveur

    public GameEngine(boolean isServer, double racketStartX, double ballStartAngle, Runnable onClientConnected) {
        this.isServer = isServer;

        // Fenêtre
        Stage stage = new Stage();
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Contrôles pour config départ
        Slider angleSlider = new Slider(0, 180, ballStartAngle);
        angleSlider.setShowTickMarks(true);
        angleSlider.setShowTickLabels(true);
        angleSlider.setMajorTickUnit(45);

        Slider racketSlider = new Slider(0, 700, racketStartX);
        racketSlider.setShowTickMarks(true);
        racketSlider.setShowTickLabels(true);
        racketSlider.setMajorTickUnit(100);

        Button startBtn = new Button("Démarrer");
        HBox controls = new HBox(10, new Label("Angle:"), angleSlider, new Label("Raquette X:"), racketSlider, startBtn);
        VBox root = new VBox(10, controls, new Group(canvas));
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle(isServer ? "Serveur" : "Client");
        stage.show();

        // Assurer le focus clavier sur le canvas
        canvas.setFocusTraversable(true);
        stage.setOnShown(ev -> canvas.requestFocus());
        startBtn.setOnAction(ev -> {
            // Config choisie par l'utilisateur
            double chosenAngle = angleSlider.getValue();
            double chosenRacketX = racketSlider.getValue();
            racket.x = chosenRacketX;

            double ballSpeed = 4;
            double dx = ballSpeed * Math.cos(Math.toRadians(chosenAngle));
            double dy = -ballSpeed * Math.sin(Math.toRadians(chosenAngle));
            ball = new Ball(400, 300, canvas.getWidth(), canvas.getHeight());
            ball.dx = dx;
            ball.dy = dy;

            // envoyer la position initiale côté client
            if (!isServer && client != null) {
                try { client.send("INPUT;" + racket.x); } catch (Exception ignore) {}
            }

            // Reprendre le focus après clic
            canvas.requestFocus();
        });

        // Raquettes
        racket = new Racket(racketStartX);       // locale (contrôlée par le clavier)
        remoteRacket = new Racket(racketStartX); // distante (contrôlée par l'autre PC)

        // Réseau
        new Thread(() -> {
            try {
                if (isServer) {
                    server = new Server(5000);
                    // Notifier la connexion du client
                    if (onClientConnected != null) {
                        Platform.runLater(onClientConnected);
                    }
                    // receive client inputs
                    new Thread(() -> {
                        try {
                            while (true) {
                                String msg = server.read();
                                if (msg != null && msg.startsWith("INPUT;")) {
                                    double x = Double.parseDouble(msg.substring(6));
                                    lastClientRacketX = x;
                                    remoteRacket.x = x;
                                }
                            }
                        } catch (Exception ignore) {}
                    }, "Net-Server-Reader").start();
                } else {
                    // TODO: replace with the actual server IP when running on PC2
                    client = new Client("10.252.114.84", 5000); 
                    new Thread(() -> {
                        try {
                            while (true) {
                                lastState = client.read(); // blocking receive
                            }
                        } catch (Exception ignore) {}
                    }, "Net-Client-Reader").start();
                }
            } catch (Exception e) { e.printStackTrace(); }
        }, "Net-Thread").start();

        // Entrée clavier (déplacement local + envoi côté client)
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.LEFT)  racket.moveLeft();
            if (e.getCode() == KeyCode.RIGHT) racket.moveRight();
            // envoyer la position locale si on est client
            if (!isServer && client != null) {
                try { client.send("INPUT;" + racket.x); } catch (Exception ignore) {}
            }
        });

        // Boucle du jeu: démarrer tout de suite (affiche la raquette même avant le lancement de la balle)
        loop = new AnimationTimer() {
            public void handle(long now) {
                update();
                render(gc);
            }
        };
        loop.start();
    }

    private void update() {
        try {
            if (isServer) {
                // server runs physics
                if (ball != null) {
                    ball.update();

                    // collision bas (raquette serveur)
                    if (ball.y + 20 >= 550 && ball.y + 20 <= 570
                        && ball.x + 20 >= racket.x && ball.x <= racket.x + 100) {
                        // point d'impact relatif [-0.5..0.5]
                        double hit = ((ball.x + 10) - (racket.x + 50)) / 100.0;
                        ball.y = 550 - 20;
                        ball.dy = -Math.abs(ball.dy);
                        ball.dx += hit * 3; // léger effet d'angle
                    }

                    // collision haut (raquette client)
                    if (ball.y <= 30 && ball.y >= 10
                        && ball.x + 20 >= remoteRacket.x && ball.x <= remoteRacket.x + 100) {
                        double hit = ((ball.x + 10) - (remoteRacket.x + 50)) / 100.0;
                        ball.y = 30;
                        ball.dy = Math.abs(ball.dy);
                        ball.dx += hit * 3;
                    }

                    // limites haut/bas si raté (sorties)
                    if (ball.y < 0) { ball.y = 0; ball.dy = Math.abs(ball.dy); }
                    if (ball.y > 600 - 20) { ball.y = 600 - 20; ball.dy = -Math.abs(ball.dy); }
                }
                // broadcast state every frame so client animates in sync
                String state = "BALL;" + (ball != null ? ball.x : -1) + ";" + (ball != null ? ball.y : -1)
                             + ";SRACKET;" + racket.x
                             + ";CRACKET;" + remoteRacket.x;
                if (server != null) server.send(state);
            } else {
                // client only consumes last received state (no physics)
                String state = lastState;
                if (state != null) {
                    String[] p = state.split(";");
                    if (p.length >= 3 && "BALL".equals(p[0])) {
                        if (ball == null) ball = new Ball(400, 300, 800, 600);
                        ball.x = Double.parseDouble(p[1]);
                        ball.y = Double.parseDouble(p[2]);
                    }
                    for (int i = 3; i + 1 < p.length; i += 2) {
                        String k = p[i];
                        String v = p[i + 1];
                        if ("SRACKET".equals(k)) {
                            // remote = server racket (bottom)
                            remoteRacket.x = Double.parseDouble(v);
                        } else if ("CRACKET".equals(k)) {
                            // local client racket: do not overwrite
                        }
                    }
                }
            }
        } catch (Exception ignore) {}
    }

    private void render(GraphicsContext gc) {
        // Fond vert
        gc.setFill(javafx.scene.paint.Color.DARKGREEN);
        gc.fillRect(0, 0, 800, 600);

        // Lignes blanches du terrain
        gc.setStroke(javafx.scene.paint.Color.WHITE);
        gc.setLineWidth(4);
        gc.strokeLine(0, 300, 800, 300);
        gc.strokeRect(20, 20, 760, 560);
        gc.strokeOval(350, 250, 100, 100);

        // Raquettes: dessiner selon le rôle
        gc.setFill(javafx.scene.paint.Color.WHITE);
        if (isServer) {
            // serveur: locale en bas, distante (client) en haut
            racket.draw(gc, 550);
            if (remoteRacket != null) {
                gc.setGlobalAlpha(0.7);
                remoteRacket.draw(gc, 30);
                gc.setGlobalAlpha(1.0);
            }
        } else {
            // client: locale en haut, distante (serveur) en bas
            racket.draw(gc, 30);
            if (remoteRacket != null) {
                gc.setGlobalAlpha(0.7);
                remoteRacket.draw(gc, 550);
                gc.setGlobalAlpha(1.0);
            }
        }

        // Balle
        if (ball != null) {
            gc.setFill(javafx.scene.paint.Color.ORANGE);
            ball.draw(gc);
        }
    }
}