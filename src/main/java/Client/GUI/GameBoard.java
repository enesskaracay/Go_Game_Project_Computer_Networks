package Client.GUI;

import javax.swing.*;
import java.awt.*;

public class GameBoard extends JPanel {
    private Field[][] fields = new Field[9][9];
    private JLabel turnLabel;

    public GameBoard(JLabel turnLabel) {
        this.turnLabel = turnLabel;
        setLayout(null);

        int fieldSize = 80;
        int offset = 40;

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                fields[row][col] = new Field(row, col);
                fields[row][col].setBounds(offset + col * fieldSize - 20, offset + row * fieldSize - 20, 40, 40);
                add(fields[row][col]);
            }
        }

       
    }

    public Field[][] getFields() {
        return fields;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color woodColor = new Color(193, 154, 107);
        g2.setColor(woodColor);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(Color.BLACK);
        int size = 9;
        int step = 80;
        int margin = 40;

        for (int i = 0; i < size; i++) {
            g2.drawLine(margin, margin + i * step, margin + (size - 1) * step, margin + i * step);
            g2.drawLine(margin + i * step, margin, margin + i * step, margin + (size - 1) * step);
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int x = margin + j * step;
                int y = margin + i * step;
                g2.drawOval(x - 20, y - 20, 40, 40);
            }
        }
    }
}