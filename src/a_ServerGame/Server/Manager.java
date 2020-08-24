package a_ServerGame.Server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Manager {

    synchronized void step(ClientData ctd) throws IOException {
        for (ServerClient s : ServerClient.clientSockets) {
            ObjectOutputStream out=new ObjectOutputStream(s.clientSocket.getOutputStream());
            out.writeObject(ctd);
            out.flush();
        }
    }
}
