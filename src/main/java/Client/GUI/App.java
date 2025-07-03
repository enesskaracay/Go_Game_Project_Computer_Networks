package Client.GUI;

import Client.ClientConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class App extends JFrame {
    private GameController controller;
    private JLabel blackScoreLabel;
    private JLabel whiteScoreLabel;
    private JLabel turnLabel;
    private GameBoard gameBoard;
    private String myName;
    private String opponentName = "Rakip";
    private JButton newGameButton; // Yeni oyun butonu

    public App() {
        // Kullanıcıdan isim al
        myName = JOptionPane.showInputDialog(
                this,
                "Lütfen isminizi girin:",
                "Oyuncu İsmi Girişi",
                JOptionPane.QUESTION_MESSAGE);

        if (myName == null || myName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Geçersiz isim. Uygulama kapanıyor.",
                    "Uyarı",
                    JOptionPane.WARNING_MESSAGE);
            System.exit(0);
            return;
        }

        setTitle("Go Game - " + myName);
        setSize(750, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton passButton = new JButton("PAS");
        newGameButton = new JButton("Yeni Oyun"); // Yeni oyun butonu oluşturuluyor

        buttonPanel.add(passButton);
        buttonPanel.add(newGameButton); // Buton panele ekleniyor

        topPanel.add(buttonPanel, BorderLayout.WEST);

        turnLabel = new JLabel();
        turnLabel.setFont(new Font("Arial", Font.BOLD, 18));
        turnLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(turnLabel, BorderLayout.CENTER);

        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel scoreTextLabel = new JLabel("Skor: ");
        scoreTextLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        blackScoreLabel = new JLabel("Siyah: 0");
        whiteScoreLabel = new JLabel("Beyaz: 0");
        blackScoreLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        whiteScoreLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        scorePanel.add(scoreTextLabel);
        scorePanel.add(whiteScoreLabel);
        scorePanel.add(blackScoreLabel);
        topPanel.add(scorePanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        gameBoard = new GameBoard(null);
        add(gameBoard, BorderLayout.CENTER);

        ClientConnection connection = new ClientConnection("13.48.56.249", 5000, message -> {
    switch (message) {
        case "PASS":
            SwingUtilities.invokeLater(() -> controller.passTurnFromNetwork());
            break;
        case "END_GAME":
            SwingUtilities.invokeLater(() -> controller.handleEndGame());
            break;
        case "WAIT":
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Rakip bekleniyor... Lütfen diğer oyuncunun bağlanmasını bekleyin.",
                        "Bekleme",
                        JOptionPane.INFORMATION_MESSAGE);
            });
            break;
        case "WAITING_FOR_NEW_PLAYER":
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Rakibiniz çıkış yaptı. Yeni bir oyuncu bekleniyor...",
                        "Bekleniyor",
                        JOptionPane.INFORMATION_MESSAGE);
            });
            break;
        case "EXIT":
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Her iki oyuncu çıkmak istedi. Oyun kapatılıyor.",
                        "Çıkış",
                        JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            });
            break;
        case "NEW_GAME_REQUEST":
            SwingUtilities.invokeLater(() -> controller.resetGameForNewRound());
            break;
         

        case "START":
            SwingUtilities.invokeLater(() -> controller.notifyGameStart());
            break;
        default:
            if (message.startsWith("CAPTURE")) {
                String[] parts = message.split(",");
                if (parts.length == 3) {
                    try {
                        int row = Integer.parseInt(parts[1]);
                        int col = Integer.parseInt(parts[2]);
                        SwingUtilities.invokeLater(() -> controller.captureStoneFromNetwork(row, col));
                    } catch (NumberFormatException e) {
                        System.err.println("Geçersiz CAPTURE formatı: " + message);
                    }
                } else {
                    System.err.println("Hatalı CAPTURE mesajı: " + message);
                }
            } else if (message.startsWith("OPPONENT_NAME:")) {
                String[] parts = message.split(":");
                if (parts.length == 2) {
                    this.opponentName = parts[1];
                    SwingUtilities.invokeLater(() -> controller.setOpponentName(this.opponentName));
                } else {
                    System.err.println("Hatalı OPPONENT_NAME formatı: " + message);
                }
            } else if (message.startsWith("NEW_GAME_DECISION:")) {
                String[] parts = message.split(":");
                if (parts.length == 2) {
                    if (parts[1].equals("YES")) {
                        SwingUtilities.invokeLater(() -> controller.clearMyStonesAndResetGame());
                    } else if (parts[1].equals("NO")) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this,
                                    "Rakip yeni oyunu reddetti.",
                                    "Yeni Oyun",
                                    JOptionPane.INFORMATION_MESSAGE);
                        });
                    }
                } else {
                    System.err.println("Hatalı NEW_GAME_DECISION formatı: " + message);
                }
            } else {
                // Hamle mesajları
                String[] parts = message.split(",");
                if (parts.length == 2) {
                    try {
                        int row = Integer.parseInt(parts[0]);
                        int col = Integer.parseInt(parts[1]);
                        SwingUtilities.invokeLater(() -> controller.placeStoneFromNetwork(row, col));
                    } catch (NumberFormatException e) {
                        System.err.println("Geçersiz hamle formatı: " + message);
                    }
                } else {
                    System.err.println("Hatalı hamle mesajı: " + message);
                }
            }
            break;
    }
});


        // Oyuncu ismini sunucuya gönder
        connection.sendMessage("NAME:" + myName);

        controller = new GameController(gameBoard, turnLabel, connection, this, myName);
        controller.setOpponentName(this.opponentName);

        passButton.addActionListener(e -> {
            if (controller.isMyTurn()) { // Kontrolü GameController'a devrediyoruz
                connection.sendMessage("PASS");
                controller.passTurn();
            } else {
                JOptionPane.showMessageDialog(
                        App.this,
                        "Sıra sizde değil, pas butonuna basamazsınız.",
                        "Uyarı",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        // Yeni oyun butonu aksiyonu GÜNCELLENDİ
        newGameButton.addActionListener(e -> {
            connection.sendMessage("NEW_GAME_REQUEST");
        });


        setVisible(true);
    }


    public void updateScore(int blackScore, int whiteScore) {
        blackScoreLabel.setText("Siyah: " + blackScore);
        whiteScoreLabel.setText("Beyaz: " + whiteScore);
    }

    public void showInvalidMoveMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Geçersiz Hamle", JOptionPane.WARNING_MESSAGE);
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new App());
    }


    // Variables declaration - do not modify
    // End of variables declaration
}