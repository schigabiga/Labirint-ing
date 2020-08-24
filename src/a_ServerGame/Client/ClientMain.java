package a_ServerGame.Client;

import a_ServerGame.Server.ClientData;
import a_ServerGame.Server.ScoreObject;
import a_ServerGame.Server.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("ALL")
public class ClientMain extends Application {

    public Group pane=new Group();
    public Scene scene=new Scene(pane,300,400);

    Integer trackSize=30;
    static Integer wallSize =10; //játékosok mérete
    public Integer[][] track=new Integer[trackSize][trackSize]; //pálya

    protected Socket client; //client sockete

    public ClientData ctd=new ClientData(); //Játékos adatai
    public List<ClientData> otherPlayers=new LinkedList<>(); //Többi játékos adatai
    public Rectangle player=new Rectangle(); //Játékos
    public List<ScoreObject> scoreObjects=new LinkedList<>(); //Pontszerző objektumok

    public Label waitLabel; //Waiting for otherplayers label
    public Label lb;

    public boolean lephet=false; // játék kezdete ,és, egy kis idő eltelvével léphet újra

    public ScreenPosition[] screenPositions ={ //a többi játékos sávját jelző kis téglalapok helyzetei
            new ScreenPosition(0,301),
            new ScreenPosition(50,301),
            new ScreenPosition(100,301),
            new ScreenPosition(150,301),
            new ScreenPosition(200,301),
            new ScreenPosition(250,301),
            new ScreenPosition(0, 351),
            new ScreenPosition(50,351),
            new ScreenPosition(100,351),
            new ScreenPosition(150,351),
            new ScreenPosition(200,351),
            new ScreenPosition(250,351),
    };


    public List<PlayerDataOnScreen> playersOnScreen=new LinkedList<>();

    public boolean nevok_e=false;

    @Override
    public void start(Stage primaryStage) throws Exception {

        lb=new Label("Labirint-ing");
        lb.setFont(Font.font(25));
        lb.setPrefSize(130,30);
        lb.setLayoutX(scene.getWidth()/2-65);
        lb.setLayoutY(scene.getHeight()/2-110);


        Button conBtn=new Button("Connect");
        conBtn.setPrefSize(100,30);
        conBtn.setLayoutX(scene.getWidth()/2-50);
        conBtn.setLayoutY(scene.getHeight()/2+30);

        TextField tf=new TextField();
        tf.setPromptText("name...");
        tf.setPrefSize(100,30);
        tf.setLayoutX(scene.getWidth()/2-50);
        tf.setLayoutY(scene.getHeight()/2-60);

        Label l=new Label("Error(Server is full,or name is already use!)");
        l.setPrefSize(230,30);
        l.setLayoutX(scene.getWidth()/2-120);
        l.setLayoutY(scene.getHeight()/2-20);

        pane.getChildren().addAll(conBtn,tf,lb);
        tf.getParent().requestFocus();


        waitLabel =new Label("Waiting for other players..."); //kapcsolodás után
        waitLabel.setLayoutX(100);
        waitLabel.setLayoutY(130);

        //kapcsolodás a szervehez
        conBtn.setOnAction(event -> {
            try {
                conBtn.setDisable(true);////////
                client = new Socket("localhost", Server.port);


                if(!nevok_e) {
                    PrintWriter serverOutput = new PrintWriter(client.getOutputStream());
                    serverOutput.write(tf.getText() + "\r\n");
                    serverOutput.flush();

                    BufferedReader inp = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String str;
                    while ((str = inp.readLine()) != null) {
                        break;
                    }
                    if (str.equals("ok")) {
                        nevok_e = true;

                    } else {
                        pane.getChildren().add(l);
                        conBtn.setDisable(false);
                        client.close();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            if(nevok_e) {

                new Thread(new Receiver(client, this)).start();

                if(pane.getChildren().contains(l))
                    pane.getChildren().remove(l);

                pane.getChildren().remove(tf);
                pane.getChildren().remove(conBtn);
                pane.getChildren().add(waitLabel);
            }
        });

        //kilépés a játékből
        primaryStage.setOnCloseRequest(event -> {
            if(lephet==false){ //ha még nem csatlakozott
                Platform.exit();
            }else{
                exitGame();
            } //client.close()?
        });

        //játékos lépései ,és, kilépés a játékból
        scene.setOnKeyReleased(event -> {

            if(event.getCode()== KeyCode.ESCAPE){
                exitGame();
            }

            if(lephet==true) {
                player.setFill(Color.WHITE);

                if (event.getCode().equals(KeyCode.UP) && canStep(0,-10)) {
                    player.setY(player.getY() - 10);
                }
                if (event.getCode().equals(KeyCode.DOWN) && canStep(0,10)) {
                    player.setY(player.getY() + 10);
                }
                if (event.getCode().equals(KeyCode.LEFT) && canStep(-10,0)) {
                    player.setX(player.getX() - 10);
                }
                if (event.getCode().equals(KeyCode.RIGHT) && canStep(10,0)) {
                    player.setX(player.getX() + 10);
                }
                ctd.x = (int) player.getX();
                ctd.y = (int) player.getY();
                player.setFill(Color.rgb(ctd.color.r, ctd.color.g, ctd.color.b));

                //pontszerző objektumra lépett-e?
                boolean score=false;
                for (int i = 0; i <scoreObjects.size() ; i++) {

                    Integer ix=scoreObjects.get(i).centerX- ScoreObject.radius2;
                    Integer iy=scoreObjects.get(i).centerY- ScoreObject.radius2;


                    if(ctd.x.equals((ix)) &&
                            ctd.y.equals(iy)){
                        ctd.score+=scoreObjects.get(i).score;
                        playersOnScreen.get(0).score_int+=scoreObjects.get(i).score;
                        playersOnScreen.get(0).score.setText("Score: "+Integer.toString(playersOnScreen.get(0).score_int));

                        ctd.scoreObject=scoreObjects.get(i);

                        for (int j = 0; j <pane.getChildren().size() ; j++) {
                            if(pane.getChildren().get(j).getLayoutX()==(ix+ScoreObject.radius2)
                                    && pane.getChildren().get(j).getLayoutY()==(iy+ScoreObject.radius2)){

                                pane.getChildren().remove(j);
                                score=true;
                                break;
                            }
                        }
                        scoreObjects.remove(scoreObjects.get(i));
                        break;
                    }
                }
                try {
                    sendPos();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(score){
                    ctd.scoreObject=null;
                }

                lephet=false;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        lephet=true;
                    }
                }).start();
            }
        });

        primaryStage.setTitle("Labirint-ing");
        scene.setFill(Color.rgb(234, 206, 0));
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    void sendPos() throws IOException {
            ObjectOutputStream out=new ObjectOutputStream(client.getOutputStream());
            out.writeObject(ctd);
            out.flush();
    }

    boolean canStep(Integer plusx,Integer plusy){

        if(track[(int) (ctd.y+plusy)/10][(int) (ctd.x+plusx)/10]==1){
            return false;
        }
        for (int i = 0; i < otherPlayers.size() ; i++) {
            if((otherPlayers.get(i).x)==(ctd.x+plusx) && (otherPlayers.get(i).y)==(ctd.y+plusy)){
                return false;
            }
        }
        return true;
    }

    void exitGame(){
        ctd.jatszik=false;
        try {
            sendPos();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Platform.exit();
    }

    public static void main(String[] args) {
        //
        //amikor létrje jön elküldi a koordinátáit majd a többi klient kiolvassa
        // és hozzáadja mint mások játékos a listájához
        //
        //lehetne azt hogy hozzá adja a dolgait de van egy gomb amire elkezdi majd kirajzolni a többi playert
        //
        //1. megvárjuk a playereket és a Start gombot nem lehet addig megnyomni amíg a server azt nem mondja hogy mehet
        //2. kéne egy olyan amikor a pályát elküldi a server a clienseknek (objektetkéne küldeni)
        //
        //!!!TERV!!!
        // -> BUTTON (Connect to Server) kattintására csatalakozik ha sikerült LABEL (waiting for the other players)
        // -> servertől kap infót hogy több már nem csatlakozik ekkor pálya,többi elküldése (RUNLATER) !!!
        // -> cliensek várnak nem történik semmi
        // -> server megvárja míg N en csatlakoznak hozzá
        // -> server kiküldi a pályát mindenkinek
        // -> server kiküldi a játékos helyét és a többi játkos helyét
        // -> server feloldja a START gombot
        //

        launch(args);
        //1. lépés
        //itt lesz a client modosítasa;

    }
}

