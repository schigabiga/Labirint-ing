package a_ServerGame.Server;

import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("ALL")
public class PointGenerator extends Thread {
    @Override
    public void run() {
        try {
            int k=0;
            boolean t=true;
            while (t==true) {
                Thread.sleep((5000)+2500);
                if (Server.pointgenerator==true) {
                    synchronized (ServerClient.Object_sending) {
                        if (ServerClient.numberOfClients_recently > 1 && ServerClient.scoreObjects.size() < ServerClient.scoreObject_limit) {
                            boolean megfelelo_pos = false;
                            XY xy = null;
                            while (megfelelo_pos == false) {
                                xy = Server.notwall.get(new Random().nextInt(Server.notwall.size()));
                                megfelelo_pos = true;
                                synchronized (ServerClient.Object_score) {
                                    for (ScoreObject s : ServerClient.scoreObjects) {
                                        if ((s.centerX - ScoreObject.radius2) == xy.x && (s.centerY - ScoreObject.radius2) == xy.y) {
                                            megfelelo_pos = false;
                                            break;
                                        }
                                    }
                                }
                                if (megfelelo_pos == false) {
                                    break;
                                }
                                synchronized (ServerClient.positions) {
                                    for (int i = 0; i < ServerClient.positions.size(); i++) {
                                        if (ServerClient.positions.get(i).x.equals(xy.x) && ServerClient.positions.get(i).y.equals(xy.y)) { //equales kéne!!
                                            megfelelo_pos = false;
                                            break;
                                        }
                                    }
                                }
                            }

                            ScoreObject scoreobject = new ScoreObject(xy.x, xy.y, new Random().nextInt((4 - 2) + 1) + 2);
                            ClientData score = new ClientData(scoreobject);
                            ServerClient.scoreObjects.add(scoreobject);

                        }
                    }
                }

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
