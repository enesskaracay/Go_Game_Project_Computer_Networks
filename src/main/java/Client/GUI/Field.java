package Client.GUI;

import javax.swing.*;
import java.awt.*;

public class Field extends JButton {
    private int row;
    private int col;
    private Color stoneColor = null;

    public Field(int row, int col) {
        this.row = row;
        this.col = col;
        setContentAreaFilled(false);
        setFocusPainted(false);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Color getStoneColor() {
        return stoneColor; // Bu metodu ekleyin
    }

    public void setStoneColor(Color color) {
        this.stoneColor = color;
        repaint(); // Taş rengini değiştirdiğimizde ekranı yeniliyoruz
    }

    public boolean isOccupied() {
        return stoneColor != null; // Taş var mı?
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (stoneColor != null) {  // Eğer taş rengi null değilse, taş çizilecek
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(stoneColor); // Taş rengi belirleniyor
            int diameter = Math.min(getWidth(), getHeight()) - 10; // Taşın çapı
            g2.fillOval((getWidth() - diameter) / 2, (getHeight() - diameter) / 2, diameter, diameter); // Taş çizimi
        }
    }
}