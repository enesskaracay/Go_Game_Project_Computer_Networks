package Client.GUI;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import Client.ClientConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class GameController {

    private GameBoard gameBoard;
    private boolean blackTurn = true;
    private boolean myTurn = false;
    private JLabel turnLabel;
    private ClientConnection connection;
    private boolean lastMoveWasPass = false;
    private boolean opponentPassed = false;
    private App app;
    private Color[][] previousBoard;
    private Point lastCapturedPosition = null;
    private Point koPoint = null;
    private int blackCapturedStones = 0;
    private int whiteCapturedStones = 0;
    private String myName;
    private String opponentName = "Rakip";
    private boolean newGameRequested = false; // Yeni oyun isteği gönderildi mi?
    private boolean gameStarted = true; // İki oyuncu bağlanmadan oyun başlamaz
    private boolean exitRequested = false;

    public GameController(GameBoard board, JLabel turnLabel, ClientConnection connection, App app, String myName) {
        this.gameBoard = board;
        this.turnLabel = turnLabel;
        this.connection = connection;
        this.app = app;
        this.myName = myName;

        myTurn = blackTurn;
        updateTurnLabel();

        Field[][] fields = gameBoard.getFields();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                Field field = fields[row][col];
                final int r = row;
                final int c = col;
                field.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("gameStarted = " + gameStarted);
        System.out.println("myTurn = " + myTurn);
        System.out.println("field.isOccupied() = " + field.isOccupied());
                        if (!gameStarted) {
                            JOptionPane.showMessageDialog(app, "Lütfen diğer oyuncunun bağlanmasını bekleyin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        if (!field.isOccupied() && myTurn) {
                            Color currentColor = blackTurn ? Color.BLACK : Color.WHITE;
                            Point currentMove = new Point(r, c);
                            if (isSuicideMove(r, c, currentColor)) {
                                if (app != null) {
                                    app.showInvalidMoveMessage("Bu hamle yasaktır (intihar).");
                                }
                                return;
                            }
                            if (koPoint != null && currentMove.equals(koPoint)) {
                                if (app != null) {
                                    app.showInvalidMoveMessage("Bu hamle Ko kuralını ihlal ediyor. Başka bir yere oynamalısınız.");
                                }
                                return;
                            }
                            if (isKoViolation(r, c, currentColor)) {
                                if (app != null) {
                                    app.showInvalidMoveMessage("Bu hamle Ko kuralını ihlal ediyor.");
                                }
                                return;
                            }
                            savePreviousBoard();
                            field.setStoneColor(currentColor);
                            connection.sendMessage(r + "," + c);
                            processCaptures(r, c, currentColor);
                            blackTurn = !blackTurn;
                            myTurn = false;
                            updateTurnLabel();
                            lastMoveWasPass = false;
                            opponentPassed = false;
                        }
                    }
                });
            }
        }
    }

    public void setConnection(ClientConnection connection) {
        this.connection = connection;

    }

    private void resetGameStatus() {
        blackTurn = true; // Yeni oyunda ilk siyah başlar
        lastMoveWasPass = false;
        opponentPassed = false;
        blackCapturedStones = 0;
        whiteCapturedStones = 0;
        koPoint = null;
        lastCapturedPosition = null;
        previousBoard = null;
        myTurn = isBlackPlayer(); // Yeni oyunda kendi sıramı belirle
    }

    private boolean isBlackPlayer() {
        return myName.equals("Oyuncu1"); // Örnek mantık, gerçek uygulamada sunucudan alınmalı
    }

    public void notifyGameStart() {
        this.gameStarted = true;
        if (isBlackPlayer()) {
            myTurn = true;
        }
        updateTurnLabel();
        JOptionPane.showMessageDialog(app, "Oyun başladı! Hamle yapabilirsiniz.");
    }

    public void resetGameForNewRound() {
    Field[][] fields = gameBoard.getFields();

    System.out.println("=== Yeni oyun sıfırlanıyor ===");

    for (int i = 0; i < 9; i++) {
        for (int j = 0; j < 9; j++) {
            fields[i][j].setStoneColor(null);
            boolean occupied = fields[i][j].isOccupied();
            System.out.println("Tahta [" + i + "," + j + "] sıfırlandı. isOccupied: " + occupied);
        }
    }

    gameBoard.repaint();

    blackTurn = true;
    myTurn =blackTurn ; // kendi sıramı belirle
    System.out.println("myTurn = " + myTurn + ", isBlackPlayer = " + isBlackPlayer());

    updateTurnLabel();

    lastMoveWasPass = false;
    opponentPassed = false;

    blackCapturedStones = 0;
    whiteCapturedStones = 0;
    updateScoreDisplay();

    koPoint = null;
    lastCapturedPosition = null;
    previousBoard = null;
    newGameRequested = false;

    this.gameStarted = true;
    System.out.println("gameStarted = " + gameStarted);
    System.out.println("=== Yeni oyun için tahta sıfırlandı ===");
}


    public void clearMyStonesAndResetGame() {
        clearBoard(); // Tahtadaki tüm taşları sil
        resetGameStatus(); // Oyun durumunu sıfırla
        updateScoreDisplay(); // Skoru sıfırla
        updateTurnLabel(); // Sıra etiketini güncelle
        System.out.println(myName + " tarafından tahta temizlendi ve oyun sıfırlandı.");
        // İsteğe bağlı: Sunucuya yeni oyunun başlatıldığını bildirebilirsiniz.
        // connection.sendMessage("NEW_GAME_INITIATED");
    }

    private void clearBoard() {
        Field[][] fields = gameBoard.getFields();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (fields[i][j].isOccupied()) {
                    fields[i][j].setStoneColor(null);
                }
            }
        }
        gameBoard.repaint();
    }

    public void clearMyStones() {
        Field[][] fields = gameBoard.getFields();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (fields[i][j].isOccupied()) {
                    fields[i][j].setStoneColor(null);
                }
            }
        }
        gameBoard.repaint();
        System.out.println(myName + " tarafından tahtadaki tüm taşlar silindi ve yeni oyuna hazır hale geliniyor.");
        resetGameStatus(); // Oyun durumunu sıfırla
        updateScoreDisplay(); // Skoru sıfırla
        updateTurnLabel(); // Sıra etiketini güncelle
        // Sunucuya "NEW_GAME_CLEARED" gibi bir mesaj gönderebilirsiniz
        // eğer diğer istemcinin de bu durumu bilmesi gerekiyorsa.
        // connection.sendMessage("NEW_GAME_CLEARED");
    }

    public void placeStoneFromNetwork(int row, int col) {
        savePreviousBoard();
        Field field = gameBoard.getFields()[row][col];
        Point opponentMove = new Point(row, col);
        if (opponentMove.equals(koPoint)) {
            koPoint = null;
        }
        if (!field.isOccupied()) {
            Color currentColor = blackTurn ? Color.BLACK : Color.WHITE;
            field.setStoneColor(currentColor);
            processCaptures(row, col, currentColor);
            blackTurn = !blackTurn;
            myTurn = true;
            updateTurnLabel();
            lastMoveWasPass = false;
            opponentPassed = false;
        }
    }

    private boolean isSuicideMove(int row, int col, Color color) {
        GameBoard tempBoard = new GameBoard(null);
        Field[][] tempFields = tempBoard.getFields();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (gameBoard.getFields()[i][j].isOccupied()) {
                    tempFields[i][j].setStoneColor(gameBoard.getFields()[i][j].getStoneColor());
                }
            }
        }
        tempFields[row][col].setStoneColor(color);

        if (hasLiberties(row, col, color, tempFields)) {
            return false;
        }

        Color opponentColor = (color == Color.BLACK) ? Color.WHITE : Color.BLACK;
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] dir : directions) {
            int checkRow = row + dir[0];
            int checkCol = col + dir[1];
            if (isValidPosition(checkRow, checkCol)) {
                if (tempFields[checkRow][checkCol].isOccupied()
                        && tempFields[checkRow][checkCol].getStoneColor().equals(opponentColor)) {
                    if (!hasLiberties(checkRow, checkCol, opponentColor, tempFields)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean hasLiberties(int row, int col, Color color, Field[][] board) {
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] dir : directions) {
            int checkRow = row + dir[0];
            int checkCol = col + dir[1];
            if (isValidPosition(checkRow, checkCol) && !board[checkRow][checkCol].isOccupied()) {
                return true;
            }
        }
        return false;
    }

    private void processCaptures(int placedRow, int placedCol, Color placedColor) {
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        Color opponentColor = (placedColor == Color.BLACK) ? Color.WHITE : Color.BLACK;
        int capturedInThisTurn = 0;
        Point capturedPoint = null;

        for (int[] dir : directions) {
            int checkRow = placedRow + dir[0];
            int checkCol = placedCol + dir[1];

            if (isValidPosition(checkRow, checkCol)) {
                Field neighbor = gameBoard.getFields()[checkRow][checkCol];
                if (neighbor.isOccupied() && neighbor.getStoneColor().equals(opponentColor)) {
                    Set<Point> visited = new HashSet<>();
                    List<Point> group = new ArrayList<>();
                    findGroupForCapture(checkRow, checkCol, opponentColor, group, visited);
                    if (hasNoLibertiesForCapture(checkRow, checkCol, opponentColor, group)) {
                        for (Point p : group) {
                            gameBoard.getFields()[p.x][p.y].setStoneColor(null);
                            connection.sendMessage("CAPTURE," + p.x + "," + p.y);
                            capturedInThisTurn++;
                            capturedPoint = p;
                        }
                        if (placedColor == Color.BLACK) {
                            blackCapturedStones += capturedInThisTurn;
                        } else {
                            whiteCapturedStones += capturedInThisTurn;
                        }
                    }
                }
            }
        }
        if (capturedInThisTurn == 1) {
            koPoint = new Point(capturedPoint.x, capturedPoint.y);
            lastCapturedPosition = new Point(capturedPoint.x, capturedPoint.y);
        } else {
            koPoint = null;
            lastCapturedPosition = null;
        }
        gameBoard.repaint();
        updateScoreDisplay();
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 9 && col >= 0 && col < 9;
    }

    private boolean hasNoLibertiesForCapture(int row, int col, Color color, List<Point> group) {
        for (Point p : group) {
            int r = p.x;
            int c = p.y;
            int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] dir : directions) {
                int nr = r + dir[0];
                int nc = c + dir[1];
                if (isValidPosition(nr, nc) && !gameBoard.getFields()[nr][nc].isOccupied()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void findGroupForCapture(int row, int col, Color color, List<Point> group, Set<Point> visited) {
        if (!isValidPosition(row, col) || visited.contains(new Point(row, col))
                || !gameBoard.getFields()[row][col].isOccupied()
                || !gameBoard.getFields()[row][col].getStoneColor().equals(color)) {
            return;
        }
        visited.add(new Point(row, col));
        group.add(new Point(row, col));
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] dir : directions) {
            findGroupForCapture(row + dir[0], col + dir[1], color, group, visited);
        }
    }

    public void captureStoneFromNetwork(int row, int col) {
        if (isValidPosition(row, col)) {
            Color capturedColor = gameBoard.getFields()[row][col].getStoneColor();
            if (capturedColor == Color.BLACK) {
                whiteCapturedStones++;
            } else if (capturedColor == Color.WHITE) {
                blackCapturedStones++;
            }
            gameBoard.getFields()[row][col].setStoneColor(null);
            gameBoard.repaint();
            updateScoreDisplay();
        }
    }

    public void passTurn() {
        if (opponentPassed) {
            System.out.println("İki oyuncu da pas geçti. Oyun bitti! Skor hesaplanıyor...");
            connection.sendMessage("END_GAME");
            calculateFinalScore();
            // gameState = GAME_OVER; // İsteğe bağlı olarak oyun durumunu güncelleyebilirsiniz.
        } else {
            connection.sendMessage("PASS");
            opponentPassed = true;
            myTurn = false;
            blackTurn = !blackTurn;
            updateTurnLabel();
            lastMoveWasPass = true;
            System.out.println(myName + " pas geçti.");
        }
    }

    /* public void resumeGame() {
    blackTurn = true;
    myTurn = blackTurn;
    connection.sendMessage("RESUME");
    updateTurnLabel();
    Field[][] fields = gameBoard.getFields();
    for (int i = 0; i < 9; i++) {
        for (int j = 0; j < 9; j++) {
            fields[i][j].setStoneColor(null);
        }
    }
    gameBoard.repaint();
    lastMoveWasPass = false;
    opponentPassed = false;
    blackCapturedStones = 0;
    whiteCapturedStones = 0;
    updateScoreDisplay();
}*/
    public void passTurnFromNetwork() {
        if (lastMoveWasPass) {
            System.out.println("Sen de pas geçtin. Oyun bitti! Skor hesaplaması bekleniyor...");
            // Rakip zaten END_GAME mesajı göndermiş olabilir veya siz de gönderebilirsiniz.
            // calculateFinalScore(); // Bu istemci skoru hesaplamamalı, diğerinden beklemeli.
            // gameState = GAME_OVER; // İsteğe bağlı olarak oyun durumunu güncelleyebilirsiniz.
        } else {
            opponentPassed = true;
            myTurn = true;
            blackTurn = !blackTurn;
            updateTurnLabel();
            lastMoveWasPass = true;
            System.out.println(opponentName + " pas geçti.");
        }
    }

    public void handleEndGame() {
        System.out.println("Rakip oyunu bitirdi. Skor hesaplanıyor...");
        calculateFinalScore();
        // gameState = GAME_OVER; // İsteğe bağlı olarak oyun durumunu güncelleyebilirsiniz.
    }

    public boolean isMyTurn() {
        return myTurn;
    }


    /*public void resumeGameFromNetwork() {
    blackTurn = true;
    myTurn = false;
    updateTurnLabel();
    Field[][] fields = gameBoard.getFields();
    for (int i = 0; i < 9; i++) {
        for (int j = 0; j < 9; j++) {
            fields[i][j].setStoneColor(null);
        }
    }
    gameBoard.repaint();
    lastMoveWasPass = false;
    opponentPassed = false;
    blackCapturedStones = 0;
    whiteCapturedStones = 0;
    updateScoreDisplay();
}*/
    public void setOpponentName(String name) {
        this.opponentName = name;
        updateTurnLabel(); // İsim değiştiğinde etiketi güncelle
    }

    private void updateTurnLabel() {
        String colorText = blackTurn ? "Siyah" : "Beyaz";
        if (myTurn) {
            turnLabel.setText("Sıra sende (" + myName + " - " + colorText + ")");
        } else {
            turnLabel.setText("Rakip oynuyor (" + opponentName + " - " + colorText + ")");
        }
    }

    private void savePreviousBoard() {
        Field[][] currentFields = gameBoard.getFields();
        if (previousBoard == null) {
            previousBoard = new Color[9][9];
        }
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                previousBoard[i][j] = currentFields[i][j].getStoneColor();
            }
        }
    }

    private boolean isKoViolation(int row, int col, Color currentColor) {
        if (lastCapturedPosition != null && lastCapturedPosition.x == row && lastCapturedPosition.y == col) {
            GameBoard tempBoard = new GameBoard(null);
            Field[][] tempFields = tempBoard.getFields();
            Field[][] currentFields = gameBoard.getFields();

            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    tempFields[i][j].setStoneColor(currentFields[i][j].getStoneColor());
                }
            }
            tempFields[row][col].setStoneColor(currentColor);

            if (previousBoard != null) {
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        if ((previousBoard[i][j] == null && tempFields[i][j].getStoneColor() != null) || (previousBoard[i][j] != null
                                && (tempFields[i][j].getStoneColor() == null || !previousBoard[i][j]
                                .equals(tempFields[i][j].getStoneColor())))) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void updateScoreDisplay() {
        if (app != null) {
            app.updateScore(blackCapturedStones, whiteCapturedStones);
        }
    }

    private void calculateFinalScore() {
        int blackTerritory = getTerritory(Color.BLACK);
        int whiteTerritory = getTerritory(Color.WHITE);
        int blackScore = blackTerritory + blackCapturedStones;
        int whiteScore = whiteTerritory + whiteCapturedStones;
        whiteScore += 6.5;

        String result = "Oyun Bitti!\n\nSiyah Esir Aldı: " + blackCapturedStones + " taş\nBeyaz Esir Aldı: "
                + whiteCapturedStones + " taş\n\nSiyah Alanı: " + blackTerritory + " kare\nBeyaz Alanı: "
                + whiteTerritory + " kare\n\nSiyah Toplam Skor: " + blackScore + "\nBeyaz Toplam Skor: " + whiteScore
                + "\n\n";

        if (blackScore > whiteScore) {
            result += "Siyah Kazandı!";
        } else if (whiteScore > blackScore) {
            result += "Beyaz Kazandı!";
        } else {
            result += "Berabere!";
        }

        int choice = JOptionPane.showOptionDialog(
                app,
                result + "\nYeni oyun başlatmak ister misiniz?",
                "Oyun Sonu",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[]{"New Game", "Exit"},
                "New Game"
        );

        if (choice == 0) {
            connection.sendMessage("NEW_GAME_DECISION:YES");
        } else {
            connection.sendMessage("NEW_GAME_DECISION:NO");
            JOptionPane.showMessageDialog(app, "Oyun kapatılıyor...");
            System.exit(0);
        }

    }

    private int getTerritory(Color color) {
        int territory = 0;
        boolean[][] visited = new boolean[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!visited[i][j] && !gameBoard.getFields()[i][j].isOccupied()) {
                    Set<Point> emptyGroup = new HashSet<>();
                    Stack<Point> stack = new Stack<>();
                    stack.push(new Point(i, j));
                    visited[i][j] = true;
                    emptyGroup.add(new Point(i, j));

                    while (!stack.isEmpty()) {
                        Point current = stack.pop();
                        int r = current.x;
                        int c = current.y;
                        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
                        for (int[] dir : directions) {
                            int nr = r + dir[0];
                            int nc = c + dir[1];
                            if (isValidPosition(nr, nc) && !visited[nr][nc] && !gameBoard.getFields()[nr][nc].isOccupied()) {
                                visited[nr][nc] = true;
                                emptyGroup.add(new Point(nr, nc));
                                stack.push(new Point(nr, nc));
                            }
                        }
                    }

                    Set<Color> surroundingColors = new HashSet<>();
                    for (Point p : emptyGroup) {
                        int r = p.x;
                        int c = p.y;
                        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
                        for (int[] dir : directions) {
                            int nr = r + dir[0];
                            int nc = c + dir[1];
                            if (isValidPosition(nr, nc) && gameBoard.getFields()[nr][nc].isOccupied()) {
                                surroundingColors.add(gameBoard.getFields()[nr][nc].getStoneColor());
                            }
                        }
                    }

                    if (surroundingColors.size() == 1 && surroundingColors.contains(color)) {
                        territory += emptyGroup.size();
                    }
                }
            }
        }
        System.out.println((color == Color.BLACK ? "Siyah" : "Beyaz") + " toplam alanı: " + territory);
        return territory;
    }

    private Set<Color> findSurroundingColors(int row, int col, boolean[][] visited) {
        Stack<Point> stack = new Stack<>();
        stack.push(new Point(row, col));
        visited[row][col] = true;
        Set<Color> surroundingColors = new HashSet<>();
        boolean hasUnoccupiedNeighbor = false;

        while (!stack.isEmpty()) {
            Point p = stack.pop();
            int r = p.x;
            int c = p.y;

            int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] dir : directions) {
                int nr = r + dir[0];
                int nc = c + dir[1];
                if (isValidPosition(nr, nc)) {
                    if (!gameBoard.getFields()[nr][nc].isOccupied()) {
                        if (!visited[nr][nc]) {
                            visited[nr][nc] = true;
                            stack.push(new Point(nr, nc));
                            hasUnoccupiedNeighbor = true;
                        }
                    } else {
                        surroundingColors.add(gameBoard.getFields()[nr][nc].getStoneColor());
                    }
                }
            }
            if (hasUnoccupiedNeighbor && surroundingColors.size() > 1) {
                // Eğer boş alan komşusu var ve birden fazla renk komşusu varsa, bu alan nötrdür.
                return Set.of(Color.GRAY); // Nötrü belirtmek için farklı bir renk kullanabiliriz.
            }
        }
        return surroundingColors;
    }

// Yardımcı metot: Bir alanın çevresindeki boşlukları sayar
    private int countEmptySpaces(int row, int col, boolean[][] visited) {
        if (!isValidPosition(row, col) || visited[row][col] || gameBoard.getFields()[row][col].isOccupied()) {
            return 0;
        }
        visited[row][col] = true;
        int count = 1;
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] dir : directions) {
            count += countEmptySpaces(row + dir[0], col + dir[1], visited);
        }
        return count;
    }

    private boolean isSurroundedBySingleColor(int row, int col, Color color) {
        Set<Color> surroundingColors = getSurroundingColors(row, col);
        return surroundingColors.size() == 1 && surroundingColors.contains(color);
    }

    private Set<Color> getSurroundingColors(int row, int col) {
        Set<Color> surroundingColors = new HashSet<>();
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            if (isValidPosition(newRow, newCol) && gameBoard.getFields()[newRow][newCol].isOccupied()) {
                surroundingColors.add(gameBoard.getFields()[newRow][newCol].getStoneColor());
            }
        }
        return surroundingColors;
    }

    private Set<Color> getSurroundingColors(int row, int col, boolean[][] visited) {
        Stack<Point> stack = new Stack<>();
        stack.push(new Point(row, col));
        visited[row][col] = true;
        Set<Color> surroundingColors = new HashSet<>();

        while (!stack.isEmpty()) {
            Point p = stack.pop();
            int r = p.x;
            int c = p.y;

            int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] dir : directions) {
                int nr = r + dir[0];
                int nc = c + dir[1];
                if (isValidPosition(nr, nc)) {
                    if (!gameBoard.getFields()[nr][nc].isOccupied()) {
                        if (!visited[nr][nc]) {
                            visited[nr][nc] = true;
                            stack.push(new Point(nr, nc));
                        }
                    } else {
                        surroundingColors.add(gameBoard.getFields()[nr][nc].getStoneColor());
                    }
                }
            }
        }
        return surroundingColors;
    }

    private boolean isSurroundedBy(int row, int col, Color color, boolean[][] visited) {
        Stack<Point> stack = new Stack<>();
        stack.push(new Point(row, col));
        visited[row][col] = true;
        boolean surrounded = true;

        while (!stack.isEmpty()) {
            Point p = stack.pop();
            int r = p.x;
            int c = p.y;

            int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] dir : directions) {
                int nr = r + dir[0];
                int nc = c + dir[1];
                if (isValidPosition(nr, nc)) {
                    if (!gameBoard.getFields()[nr][nc].isOccupied()) {
                        if (!visited[nr][nc]) {
                            visited[nr][nc] = true;
                            stack.push(new Point(nr, nc));
                        }
                    } else if (!gameBoard.getFields()[nr][nc].getStoneColor().equals(color)) {
                        surrounded = false;
                    }
                }
            }
        }
        return surrounded;
    }

    

}