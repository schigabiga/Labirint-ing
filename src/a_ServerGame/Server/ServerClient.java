package a_ServerGame.Server;

import javafx.util.Pair;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("ALL")
public class ServerClient extends Thread {

    static int numberOfClients=0; //id
    static int numberOfClients_recently=0; //jelenlegi kliensek akik csatlakozva vannak
    static List<ServerClient> clientSockets=new LinkedList<>();
    static List<ClientData> positions=new LinkedList<>(); //kliensek poziciói
    static List<ScoreObject> scoreObjects=new LinkedList<>(); //pontszerző obejktumok

    static Integer scoreObject_limit=12; //hány objekt lehet egyszerre a pályán
    static List<String> names=new LinkedList<>(); //kliensek nevei(ne lehessen 2ugyan olyan)

    public Socket clientSocket;
    public Integer id;

    public ClientData playerdata=null;
    public String playername=null;

    static Object Object_score=new Object();//
    static Object Object_sending=new Object();

    static boolean ketto=false;
    static boolean start=false;

    public ServerClient(Socket socket){
        clientSocket=socket;
    }

    @Override
    public void run() {
        try{
            boolean mehet=false;

            //klienstől vár egy névre
            BufferedReader inp2 = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String str;
            while ((str=inp2.readLine())!=null){
                break;
            }

            //megnézni hogy van e ilyen nevű
            boolean vanilyennev=false;
            for (int i = 0; i < names.size(); i++) {
                if(names.get(i).equals(str)){
                    vanilyennev=true;
                }
            }
            //ha van vissza írja a kliensnek
            PrintWriter serverOutput = new PrintWriter(clientSocket.getOutputStream());
            if(vanilyennev || numberOfClients_recently==Server.N) {
                serverOutput.write("nemok"+"\r\n");
            }
            else {
                //ha nincs vissza ír a kliensek hogy rendben a név
                serverOutput.write("ok"+"\r\n");
                names.add(str);
                mehet=true;
                playername=str;

                //sikeres csatlakozás
                id=numberOfClients++;
                numberOfClients_recently++;
            }
            serverOutput.flush();


            if(mehet){ //ha sikeres volt a csatlakozás
                if(!ServerClient.start){
                    if(ServerClient.numberOfClients<=2)
                            clientSockets.add(this);
                    if(ServerClient.numberOfClients==2){ //ha ketten vannak csak akkor indul a játék
                            for(ServerClient s : ServerClient.clientSockets){
                                sendTrack(s);
                            }
                            for(ServerClient s : ServerClient.clientSockets){
                                setupPosition(s);
                            }
                            for(ServerClient s : ServerClient.clientSockets){
                                sendPositionToOthers(s);
                            }
                            ServerClient.start=true; //játék indulhat
                            new PointGenerator().start(); //pontszerző objektumok generálása elindul
                            Server.pointgenerator=true;
                            ketto=true; //ketten már vannak
                    }
                }else {

                    sendTrack(this);
                    setupPosition(this);
                    sendPositionToOthers(this);

                    this.playerdata.uj=true; //jelzi hogy ez egy újjonan becsatlakozott kliens lesz
                    synchronized (ServerClient.Object_score) {
                        Thread.sleep(200);
                        Server.manager.step(this.playerdata);
                    }
                    ServerClient.clientSockets.add(this);
                }

                boolean fogad=true;
                ObjectInputStream inp;
                while (fogad==true){
                    inp=new ObjectInputStream(clientSocket.getInputStream()); //vár hogy a kliens oldalon történjen egy lépés
                    ClientData ctd=null;
                    while( (ctd= (ClientData) inp.readObject())!=null){
                        synchronized (ServerClient.Object_sending){
                            if(ctd.jatszik==false){ //megszűnik a kapcsolat
                                fogad=false;
                            }
                            else{
                                for (int i = 0; i <positions.size(); i++) { //kliens pozicióját megváltoztatja a serverben
                                    if(ctd.id.equals(positions.get(i).id)){
                                        positions.set(i,ctd);
                                        playerdata=ctd;
                                        break;
                                    }
                                }
                            }
                            if(ctd.scoreObject!=null){ //ha a kliens pontszerző objektre lépett
                                for (int i = 0; i < scoreObjects.size() ; i++) {
                                    if(ServerClient.scoreObjects.get(i).centerX.equals(ctd.scoreObject.centerX) &&
                                            ServerClient.scoreObjects.get(i).centerX.equals(ctd.scoreObject.centerX)){
                                        ServerClient.scoreObjects.remove(i);
                                    }
                                }
                            }

                            ctd.scoreObjects=ServerClient.scoreObjects;
                            Server.manager.step(ctd); //elküldi mindnekinek hogy lépett
                        }
                        break;
                    }
                }
                //ha vége megkell szűntetni mindent
                clientSockets.remove(this);
                positions.remove(playerdata);
                numberOfClients_recently--;
                Server.colors.add(new Colors(playerdata.color.r,playerdata.color.g,playerdata.color.b));
            }
        } catch (IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            try{
                clientSocket.close();
            }catch (IOException r){

            }
        }

    }

    void sendTrack(ServerClient serverClient) throws IOException {
        ObjectOutputStream out=new ObjectOutputStream(serverClient.clientSocket.getOutputStream());
        out.writeObject(Server.track);
        out.flush();
    }

    void setupPosition(ServerClient serverClient) throws IOException{

        boolean megfelelo_pos = false;
        XY xy = null;
        while (megfelelo_pos == false) {
            xy = Server.notwall.get(new Random().nextInt(Server.notwall.size()));
            megfelelo_pos = true;
            synchronized (ServerClient.Object_score){
                for(ScoreObject s : ServerClient.scoreObjects){
                    if((s.centerX-ScoreObject.radius2)==xy.x && (s.centerY-ScoreObject.radius2)==xy.y){
                        megfelelo_pos=false;
                        break;
                    }
                }
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

        Integer color = new Random().nextInt(Server.colors.size());
        Colors c = Server.colors.get(color);
        ClientData pos = new ClientData(serverClient.id, xy.x, xy.y, c);
        pos.name=serverClient.playername;

        if(ServerClient.ketto==true){
            pos.scoreObjects=ServerClient.scoreObjects;
        }

        serverClient.playerdata=pos;
        Server.colors.remove(c);
        ServerClient.positions.add(pos);
        ObjectOutputStream out=new ObjectOutputStream(serverClient.clientSocket.getOutputStream());
        out.writeObject(pos);
        out.flush();
    }

    void sendPositionToOthers(ServerClient serverClient) throws IOException{
        List<ClientData> others = new LinkedList<>();
        for (int i = 0; i < ServerClient.positions.size(); i++) {
            if (serverClient.id != ServerClient.positions.get(i).id) {
                others.add(ServerClient.positions.get(i));
            }
        }
        ObjectOutputStream out=new ObjectOutputStream(serverClient.clientSocket.getOutputStream());
        out.writeObject(others);
        out.flush();
    }

}
