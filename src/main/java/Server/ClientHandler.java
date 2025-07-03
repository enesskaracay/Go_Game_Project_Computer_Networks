package Server;

import java.io.*;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String name;
    private String color; // "BLACK" veya "WHITE"

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }
    
    public void listenForName() {
    try {
        String nameMsg = in.readLine();
        if (nameMsg != null && nameMsg.startsWith("NAME:")) {
            this.name = nameMsg.substring(5);
        }
    } catch (IOException e) {
        System.out.println("İsim alınırken hata: " + e.getMessage());
    }
}

    

    public Socket getSocket() {
        return socket;
    }

    public BufferedReader getIn() {
        return in;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setColor(String color) {
        this.color = color;
        sendMessage("COLOR:" + color);
    }

    public String getColor() {
        return color;
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

  public boolean isAlive() {
    return socket != null && !socket.isClosed();
}



    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}