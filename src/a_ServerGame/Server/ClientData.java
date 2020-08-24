package a_ServerGame.Server;


import javafx.scene.shape.Rectangle;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class ClientData implements Serializable {

    //Játékos adatai
    public Integer x,y,id=-1;
    public Integer score=null;
    public String name=null;
    public Rectangle rect=null; //lehet hogy evvel is kéne kezdeni valamit a serimiatt
    public Colors color=null;

    public boolean jatszik=true; //játszik e még
    public boolean uj=false; //újonnan becsatlakozott kliens

    //pontszerző objektum
    public boolean isscore=false;
    //Funkciói
    //1. maga a pontszerző obejktum
    //2. kliens melyik scoreObjektet szerezte meg
    public ScoreObject scoreObject=null;

    public List<ScoreObject> scoreObjects=new LinkedList<>();

    public ClientData(Integer id, Integer x, Integer y, Colors color){
        this.x=x;
        this.y=y;
        this.id=id;
        this.color=color;
        this.score=0;
    }

    public ClientData(ScoreObject scoreObject){
        this.scoreObject=scoreObject;
        this.isscore=true;
    }

    public ClientData(){
    }

    public void addRect(Rectangle rect){
        this.rect=rect;
    }
}
