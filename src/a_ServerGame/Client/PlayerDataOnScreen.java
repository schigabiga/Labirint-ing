package a_ServerGame.Client;

import a_ServerGame.Server.ClientData;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class PlayerDataOnScreen {
    static Integer x=0;
    static Integer y=301;

    public ScreenPosition pos;
    public ClientData ctd;

    public Rectangle rect;
    public Label name;
    public Integer id_int=0;
    public Label score;
    public Integer score_int=0;
    public PlayerDataOnScreen(ClientData ctd, ScreenPosition pos){
        this.ctd=ctd;
        this.pos=pos;
        id_int=ctd.id;
        this.name= new Label(ctd.name);
        this.rect=new Rectangle(ClientMain.wallSize,ClientMain.wallSize);
        score_int=ctd.score;
        this.score=new Label("Score: "+Integer.toString(score_int));

        pos.ures=false;

        set(pos);
    }

    public void set(ScreenPosition pos){
        this.name.setLayoutX(pos.x+25-(this.name.getFont().getSize()/2*this.name.getText().length()/2));
        this.name.setLayoutY(pos.y+10);
        this.rect.setLayoutX(pos.x+25-ClientMain.wallSize/2);
        this.rect.setLayoutY(pos.y+25);
        this.rect.setFill(Color.rgb(ctd.color.r,ctd.color.g,ctd.color.b));
        this.score.setLayoutX(pos.x+25-(this.score.getFont().getSize()/2*this.score.getText().length()/2));
        this.score.setLayoutY(pos.y+35);
    }
}
