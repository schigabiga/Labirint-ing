package a_ServerGame.Client;

import a_ServerGame.Server.ScoreObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class Score implements Runnable {

    Socket soc;
    public Score(Socket soc) {
        this.soc=soc;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream in=new ObjectInputStream(soc.getInputStream());
            ScoreObject s;
            while ((s=(ScoreObject) in.readObject()) !=null){
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
