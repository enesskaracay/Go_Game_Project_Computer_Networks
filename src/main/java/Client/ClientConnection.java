package Client;

import java.io.*;
import java.net.*;

public class ClientConnection {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    public interface MessageListener {
        void onMessageReceived(String message);
    }

    public ClientConnection(String host, int port, MessageListener listener) {
        try {
            socket = new Socket(host, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            Thread receiveThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = input.readLine()) != null) {
                        listener.onMessageReceived(message);
                    }
                } catch (IOException e) {
                    System.out.println("Bağlantı koptu.");
                    output = null;
                    input = null;
                    socket = null;
                }
            });
            receiveThread.start();

        } catch (IOException e) {
            System.out.println("Sunucuya bağlanılamadı: " + e.getMessage());
            output = null;
            input = null;
            socket = null;
        }
    }

    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        } else {
            System.out.println("Mesaj gönderilemedi: Bağlantı yok veya output null.");
        }
    }
}