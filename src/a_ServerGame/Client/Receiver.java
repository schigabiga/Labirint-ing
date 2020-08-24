package a_ServerGame.Client;

import a_ServerGame.Server.ClientData;
import a_ServerGame.Server.ScoreObject;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.List;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

@SuppressWarnings("Duplicates")
public class Receiver implements Runnable {

    private Socket client;
    private ClientMain s;
    public Receiver(Socket client, ClientMain s) {
        this.client=client;
        this.s=s;
    }

    @Override
    public void run(){
        try{
            //Pálya kiolvasása
            ObjectInputStream inp=new ObjectInputStream(client.getInputStream());
            Integer[][] track;
            while ( (track= (Integer[][]) inp.readObject()) !=null){
                break;
            }
            // Pálya betöltése
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                   s.pane.getChildren().remove(s.waitLabel);
                   s.pane.getChildren().remove(s.lb);
                   s.track=track;
                   int y=0;
                    for (int i = 0; i < s.track.length; i++) {
                        int x=0;
                        for (int j = 0; j <s.track.length ; j++) {
                            if(s.track[i][j]==1){
                                Rectangle wall=new Rectangle(x,y,ClientMain.wallSize,ClientMain.wallSize);
                                wall.setFill(Color.RED);
                                s.pane.getChildren().add(wall);
                            }
                            x+=10;
                        }
                        y+=10;
                    }
                }
            });

            //Játékos pozició kiolvasása
            ObjectInputStream inp2=new ObjectInputStream(client.getInputStream());
            ClientData ctd;
            while ( (ctd= (ClientData) inp2.readObject()) !=null){
                break;
            }
            //Játékos pozició és egyébek beállítása
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    //Játékos pozició beállítása
                    s.ctd=ctd;
                    Rectangle rect=new Rectangle(ctd.x,ctd.y,ClientMain.wallSize,ClientMain.wallSize);
                    rect.setFill(Color.rgb(ctd.color.r,ctd.color.g,ctd.color.b));
                    s.player=rect;
                    s.pane.getChildren().add(rect);

                    //Játékos adatai a képernyőn
                    int i=0;
                    while (s.screenPositions[i].ures!=true){
                        i++;
                    }

                    s.screenPositions[i].ures=false;
                    for (int j = 0; j < s.screenPositions.length; j++) {
                    }

                    PlayerDataOnScreen pons=new PlayerDataOnScreen(s.ctd,s.screenPositions[i]);
                    s.playersOnScreen.add(pons);
                    s.pane.getChildren().addAll(pons.name,pons.rect,pons.score);


                    //Scoreobjektek kiírása
                    for(ScoreObject sc : ctd.scoreObjects){
                        s.scoreObjects.add(sc);
                        Circle c=new Circle(sc.radius);
                        c.setLayoutX(sc.centerX);
                        c.setLayoutY(sc.centerY);
                        c.setFill(Color.rgb(sc.color.r,sc.color.g,sc.color.b));
                        s.pane.getChildren().add(c);
                    }

                }
            });

            //TöbbiJátékos pozició kiolvasása
            ObjectInputStream inp3=new ObjectInputStream(client.getInputStream());
            List<ClientData> others;
            while ( (others= (List) inp3.readObject()) !=null){
                break;
            }
            //TöbbiJátékos pozició és egyéb beállítása
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    for(ClientData c : others){
                        //TöbbiJátékos pozició beállítása
                        Rectangle rect=new Rectangle(c.x,c.y,ClientMain.wallSize,ClientMain.wallSize);
                        rect.setFill(Color.rgb(c.color.r,c.color.g,c.color.b));
                        c.addRect(rect);
                        s.pane.getChildren().add(c.rect);

                        //TöbbiJátékos adatai a képernyőn
                        int i=0;
                        while (s.screenPositions[i].ures!=true){
                            i++;
                        }
                        s.screenPositions[i].ures=false;
                        PlayerDataOnScreen pons=new PlayerDataOnScreen(c,s.screenPositions[i]);
                        s.playersOnScreen.add(pons);
                        s.pane.getChildren().addAll(pons.name,pons.rect,pons.score);

                    }
                    for (int j = 0; j < s.screenPositions.length; j++) {
                    }
                    s.otherPlayers=others;
                }
            });

            //Játékos léphet
            s.lephet = true;


            //Többi játékos lépései ,és, pontszerző objektum
            boolean fogad=true;
            while (fogad==true){
                ObjectInputStream inp4=new ObjectInputStream(client.getInputStream());
                ClientData other=null;
                while( (other= (ClientData) inp4.readObject())!=null){
                    ClientData finalOther = other;

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                            for (int j = 0; j < finalOther.scoreObjects.size(); j++) {
                                boolean vane=false;
                                for (int i = 0; i < s.scoreObjects.size(); i++) {

                                    if (finalOther.scoreObjects.get(j).centerX.equals(s.scoreObjects.get(i).centerX)
                                            && finalOther.scoreObjects.get(j).centerY.equals(s.scoreObjects.get(i).centerY)) {
                                        vane=true;
                                        break;
                                    }
                                }
                                if(!vane){
                                    s.scoreObjects.add(finalOther.scoreObjects.get(j));
                                    Circle c=new Circle(finalOther.scoreObjects.get(j).radius);
                                    c.setLayoutX(finalOther.scoreObjects.get(j).centerX);
                                    c.setLayoutY(finalOther.scoreObjects.get(j).centerY);
                                    c.setFill(Color.rgb(finalOther.scoreObjects.get(j).color.r,finalOther.scoreObjects.get(j).color.g,finalOther.scoreObjects.get(j).color.b));
                                    s.pane.getChildren().add(c);
                                }
                            }
                        }
                    });

        //új játékos adatainak beállítása
                    if(finalOther.uj==true){ //scoreobjekteket küldje át
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if(!finalOther.id.equals(s.ctd.id)) {
                                    //TöbbiJátékos pozició beállítása
                                    Rectangle rect = new Rectangle(finalOther.x, finalOther.y, ClientMain.wallSize, ClientMain.wallSize);
                                    rect.setFill(Color.rgb(finalOther.color.r, finalOther.color.g, finalOther.color.b));
                                    finalOther.addRect(rect);
                                    s.pane.getChildren().add(finalOther.rect);
                                    s.otherPlayers.add(finalOther);

                                    //TöbbiJátékos adatai a képernyőn
                                    int i=0;
                                    while (s.screenPositions[i].ures!=true){
                                        i++;
                                    }

                                    s.screenPositions[i].ures=false;
                                    for (int j = 0; j < s.screenPositions.length; j++) {
                                    }
                                    PlayerDataOnScreen pons=new PlayerDataOnScreen(finalOther,s.screenPositions[i]);
                                    s.playersOnScreen.add(pons);
                                    s.pane.getChildren().addAll(pons.name,pons.rect,pons.score);
                                }
                            }
                        });
                    }

        //Játékos kilépése -> finalOther maga a kapott csomag (aktuális)
                    if(s.ctd.id!=null) {
                        if (finalOther.id.equals(s.ctd.id)) { //kliens véget ér
                            if (finalOther.jatszik == false) {
                                fogad = false;
                            }
                            break;
                        }
                    }



        //Játékos pozició ,és, képernyő beállítás(ellenfél)
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                    //Pocozió firssités(ellenfél)
                            for (ClientData c : s.otherPlayers) {
                                if (c.id.equals(finalOther.id)) {
                                    //aktuális játékos kilépett eltűntetni a játékmezőről
                                    if(finalOther.jatszik==false){
                                        s.pane.getChildren().remove(c.rect);
                                    } else{
                                        //akutális játékos pozició frissítése
                                        c.x = finalOther.x;
                                        c.y = finalOther.y;
                                        c.rect.setX(finalOther.x);
                                        c.rect.setY(finalOther.y);
                                    }
                                   // break;
                                }
                            }



                     //Pontszerző objektum elérése esetén(ellenfél)
                            //Pont szerző objeekt kitrörlése
                            if(finalOther.scoreObject!=null){
                                if(!finalOther.id.equals(s.ctd.id)){
                                        for (int j = 0; j < s.pane.getChildren().size(); j++) {
                                            if (s.pane.getChildren().get(j).getLayoutX() == finalOther.scoreObject.centerX
                                                    && s.pane.getChildren().get(j).getLayoutY() == finalOther.scoreObject.centerY) {
                                                s.pane.getChildren().remove(j);
                                            }
                                        }
                                    }
                            }

                            //Firssíteni a pontját a képernyőn
                            for(PlayerDataOnScreen p : s.playersOnScreen){
                                if(p.id_int.equals(finalOther.id) && finalOther.scoreObject!=null){
                                    p.score_int+=finalOther.scoreObject.score;
                                    p.score.setText("Score: "+Integer.toString(p.score_int));
                                }
                            }

                    //Játékos kilépése(ellenfél)
                            if(finalOther.jatszik==false){
                                boolean mehet=false;
                                int hanyadik=0;
                                int k=0;
                                for (int i = 0; i < s.playersOnScreen.size(); i++) {
                                    if(mehet) {
                                        s.pane.getChildren().remove(s.playersOnScreen.get(i).rect);
                                        s.pane.getChildren().remove(s.playersOnScreen.get(i).score);
                                        s.pane.getChildren().remove(s.playersOnScreen.get(i).name);
                                        s.playersOnScreen.get(i).pos = s.screenPositions[k];
                                        s.playersOnScreen.get(i).set(s.playersOnScreen.get(i).pos);
                                        s.pane.getChildren().addAll(s.playersOnScreen.get(i).name, s.playersOnScreen.get(i).rect, s.playersOnScreen.get(i).score);
                                        k++;
                                        if(i==s.playersOnScreen.size()-1){
                                            s.screenPositions[k].ures=true;
                                            for (int j = 0; j < s.screenPositions.length; j++) {
                                            }
                                        }
                                    }

                                    if(s.playersOnScreen.get(i).ctd.id.equals(finalOther.id)){
                                        for (int j = 0; j < s.screenPositions.length; j++) {
                                            if(s.playersOnScreen.get(i).pos.equals(s.screenPositions[j])) {
                                                k=j;
                                            }
                                        }
                                        if(i==s.playersOnScreen.size()-1){
                                            s.screenPositions[k].ures=true;
                                            for (int j = 0; j < s.screenPositions.length; j++) {
                                            }
                                        }
                                        s.pane.getChildren().remove(s.playersOnScreen.get(i).rect);
                                        s.pane.getChildren().remove(s.playersOnScreen.get(i).score);
                                        s.pane.getChildren().remove(s.playersOnScreen.get(i).name);
                                        s.pane.getChildren().remove(finalOther);
                                        hanyadik=i;
                                        mehet=true;
                                    }
                                }
                                s.playersOnScreen.remove(hanyadik);
                            }


                        }
                    });
                    break;
                }
            } //megnézni a száll végét

        }catch (IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            try{
                client.close();
            }catch (IOException r){

            }
        }
    }
}


