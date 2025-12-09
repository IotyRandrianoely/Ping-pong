package game;

import javafx.scene.canvas.GraphicsContext;

public class Racket {
    public double x;

    public Racket(double x) {
        this.x = x;
    }

    public void moveLeft() {
        x -= 15;
    }

    public void moveRight() {
        x += 15;
    }

    public void draw(GraphicsContext gc, double y) {
        gc.fillRect(x, y, 100, 20);
    }
}