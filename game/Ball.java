package game;

import javafx.scene.canvas.GraphicsContext;

public class Ball {
    public double x, y, dx = 3, dy = -3;
    private double terrainWidth, terrainHeight;

    public Ball(double x, double y, double terrainWidth, double terrainHeight) {
        this.x = x;
        this.y = y;
        this.terrainWidth = terrainWidth;
        this.terrainHeight = terrainHeight;
    }

    public void update() {
        x += dx;
        y += dy;

        if (x < 0 || x > terrainWidth - 20) dx *= -1;
        if (y < 0 || y > terrainHeight - 20) dy *= -1;
    }

    public void draw(GraphicsContext gc) {
        gc.fillOval(x, y, 20, 20);
    }
}