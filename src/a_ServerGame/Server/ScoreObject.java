package a_ServerGame.Server;

import javafx.scene.paint.Color;

import java.io.Serializable;

public class ScoreObject implements Serializable {

    public Integer score=0;
    public Integer centerX, centerY, radius;
    public static Integer radius2=5;//radius2vel állítom be hogy a 10x10kocka közepére rajzolja majd ki
    public Colors color=null;

    public ScoreObject(Integer centerX, Integer centerY,Integer score) {

        this.score=score;

        switch (score){
            case 2:
                color=new Colors(0,0,0);
                this.radius=3;
                break;
            case 3:
                color=new Colors(128,128,128);
                this.radius=4;
                break;
            case 4:
                color=new Colors(100,98,98);
                this.radius=5;
                break;
        }
        this.centerX=centerX+radius2;
        this.centerY=centerY+radius2;
    }
}
