package Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerMain {
    private static final int PORT = 5000;
    private static final Queue<Socket> waitingClients = new LinkedList<>();

    public static void main(String[] args) {
        System.out.println("Sunucu başlatıldı. Client bekleniyor...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Yeni client bağlandı: " + client.getInetAddress());

                synchronized (waitingClients) {
                    waitingClients.add(client);
                    if (waitingClients.size() >= 2) {
                        Socket player1 = waitingClients.poll();
                        Socket player2 = waitingClients.poll();

                        // Yeni bir oyun oturumu başlat
                        new Thread(new GameSession(player1, player2)).start();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

}