package a_ServerGame.Server;


import java.io.Serializable;

public class Colors implements Serializable {
    public Integer r,g,b;
    public Colors(Integer r,Integer g,Integer b){
        this.r=r;
        this.g=g;
        this.b=b;
    }
}
