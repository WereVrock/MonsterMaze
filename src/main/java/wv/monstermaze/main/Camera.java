package wv.monstermaze.main;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class Camera {

    private double x;
    private double y;
    private double zoom = 1.0;

    private int viewWidthTiles;
    private int viewHeightTiles;
    private int tileSize;
    private double smoothing = 0.12;

    private Game game;

    public Camera(Game game, int viewWidthTiles, int viewHeightTiles, int tileSize) {
        this.game = game;
        this.viewWidthTiles = viewWidthTiles;
        this.viewHeightTiles = viewHeightTiles;
        this.tileSize = tileSize;

        Player player = game.getPlayer();
        this.x = player.x - viewWidthTiles * tileSize / 2;
        this.y = player.y - viewHeightTiles * tileSize / 2;
    }

    public void update() {
        Player player = game.getPlayer();

        double targetX = player.x - viewWidthTiles * tileSize / 2;
        double targetY = player.y - viewHeightTiles * tileSize / 2;

        this.x += (targetX - x) * smoothing;
        this.y += (targetY - y) * smoothing;

        // Example of zoom adjustments based on speed
        SettingsMenu settings = game.getSettingsMenu();
        double targetZoom = 1.0;
        if (settings.isSpeedVfxEnabled() && player.getSpeedMultiplier() > 1.0) targetZoom = 0.85;
        this.zoom += (targetZoom - zoom) * 0.08;
    }

    public void applyTransform(Graphics2D g2) {
        int screenW = game.getWidth();
        int screenH = game.getHeight();

        g2.translate(screenW / 2, screenH / 2);
        g2.scale(zoom, zoom);
        g2.translate(-screenW / 2, -screenH / 2);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZoom() { return zoom; }

    public int getViewWidthTiles() { return viewWidthTiles; }
    public int getViewHeightTiles() { return viewHeightTiles; }
}