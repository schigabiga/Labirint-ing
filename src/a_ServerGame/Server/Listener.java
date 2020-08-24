package a_ServerGame.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("Duplicates")
public class Listener extends Thread {
    @Override
    public void run() {
        try {

            ServerSocket server = new ServerSocket(Server.port);
            while (!interrupted()) {
                Socket clientSocket = server.accept();
                new ServerClient(clientSocket).start();
            }
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}