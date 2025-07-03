package Server;

import java.io.*;
import java.net.*;

public class GameSession implements Runnable {
    private Socket player1;
    private Socket player2;

    private BufferedReader in1, in2;
    private PrintWriter out1, out2;

    private volatile String decision1 = null;
    private volatile String decision2 = null;

    public GameSession(Socket player1, Socket player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    @Override
    public void run() {
        try {
            in1 = new BufferedReader(new InputStreamReader(player1.getInputStream()));
            out1 = new PrintWriter(player1.getOutputStream(), true);

            in2 = new BufferedReader(new InputStreamReader(player2.getInputStream()));
            out2 = new PrintWriter(player2.getOutputStream(), true);

            // İsim alımı
            String name1 = in1.readLine();
            String name2 = in2.readLine();

            if (name1 != null && name1.startsWith("NAME:") && name2 != null && name2.startsWith("NAME:")) {
                String player1Name = name1.substring(5);
                String player2Name = name2.substring(5);

                out1.println("OPPONENT_NAME:" + player2Name);
                out2.println("OPPONENT_NAME:" + player1Name);

                out1.println("COLOR:BLACK");
out2.println("COLOR:WHITE");

out1.println("START");
out2.println("START");

            } else {
                out1.println("WAIT");
                out2.println("WAIT");
            }

            // Mesaj yönlendirme thread'leri
            Thread t1 = new Thread(() -> handleMessages(in1, out2, out1, true));
            Thread t2 = new Thread(() -> handleMessages(in2, out1, out2, false));
            t1.start();
            t2.start();

            t1.join();
            t2.join();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (player1 != null) player1.close();
                if (player2 != null) player2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessages(BufferedReader in, PrintWriter otherOut, PrintWriter ownOut, boolean isPlayer1) {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                // Karar mesajı mı?
                if (message.startsWith("NEW_GAME_DECISION:")) {
                    String decision = message.split(":")[1];

                    if (isPlayer1) {
                        decision1 = decision;
                    } else {
                        decision2 = decision;
                    }

                    checkAndHandleDecisions(); // Her karar sonrası kontrol et
                } else {
                    otherOut.println(message); // Normal oyun mesajları
                }
            }
        } catch (IOException e) {
            System.out.println((isPlayer1 ? "Client 1" : "Client 2") + " bağlantısı kesildi.");
        }
    }

    private synchronized void checkAndHandleDecisions() {
        if (decision1 != null && decision2 != null) {
            if (decision1.equals("YES") && decision2.equals("YES")) {
                // Her iki oyuncu da yeni oyun istiyor
                out1.println("NEW_GAME_REQUEST");
                out2.println("NEW_GAME_REQUEST");
                decision1 = null;
                decision2 = null;
            } else {
                // En az bir oyuncu yeni oyun istemiyor
                if (decision1.equals("YES")) {
                    out1.println("WAITING_FOR_NEW_PLAYER");
                }
                if (decision2.equals("YES")) {
                    out2.println("WAITING_FOR_NEW_PLAYER");
                }

                try {
                    if (player1 != null) player1.close();
                    if (player2 != null) player2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}