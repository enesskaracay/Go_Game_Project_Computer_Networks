package Client;

import java.io.*;
import java.net.*;
import java.util.Scanner;


public class ClientMain {
    public static void main(String[] args) {
        // İsim alma işlemini kaldırıyoruz.
        // String playerName = JOptionPane.showInputDialog(
        //         null,
        //         "Lütfen isminizi girin:",
        //         "Oyuncu İsmi Girişi",
        //         JOptionPane.QUESTION_MESSAGE);

        // if (playerName != null && !playerName.trim().isEmpty()) {
            // İsim alındı, sunucuya bağlan ve oyunu başlat
            try {
                Socket socket = new Socket("13.48.56.249", 5000);
                System.out.println("Sunucuya bağlanıldı.");

                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                Scanner scanner = new Scanner(System.in);

                // Server'dan gelen mesajı dinlemek için thread oluşturuluyor
                Thread receiveThread = new Thread(() -> {
                    try {
                        String message;
                        while ((message = input.readLine()) != null) {
                            System.out.println("Rakip: " + message);
                        }
                    } catch (IOException e) {
                        System.out.println("Sunucuyla bağlantı kesildi.");
                    }
                });

                receiveThread.start();

                // Oyunu başlat
                while (true) {
                    System.out.print("Hamleni yaz (örnek: 2,3): ");
                    String message = scanner.nextLine();
                    output.println(message);
                }

            } catch (IOException e) {
                System.out.println("Sunucuya bağlanılamadı: " + e.getMessage());
            } finally {
                // scanner.close();  // While döngüsü içinde olduğu için burada kapatılmamalı.
            }

        // } else {
        //     JOptionPane.showMessageDialog(
        //             null,
        //             "Geçersiz bir isim girdiniz. Uygulama kapanıyor.",
        //             "Uyarı",
        //             JOptionPane.WARNING_MESSAGE);
        //     System.exit(0);
        // }
    }
}