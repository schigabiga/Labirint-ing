package a_ServerGame.Server;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("ALL")
public class Server extends Application {

    /*A pontszerző objektumokat jelezzük körrel, legyenek megkülönböztethetők a
    játékosoktól. A több pontot adók nagyobbak legyenek. A pontszerzőket a
    szerver generálja véletlenszerűen időközönként, mennyiségben és pozícióra, de
    nem egymásra vagy olyan cellára, amelyen egy játékos van.*/


    //kliens addig próbálkozzon belépni amíg nem siküler kapcoslatot teremtenie

    public static final Integer port=11223;
    static Manager manager;
    static Integer N=0; //max players
    static Integer n=2; //ennyi csatlakozott játékos után már elindulhat a játék

    static Integer trackSize=30;
    static Integer[][] track=new Integer[trackSize][trackSize];
    static List<XY> notwall = new LinkedList<>(); //azok a poziciók ahol nincsne fal

    static boolean pointgenerator=false;

    static List<Colors> colors= new LinkedList<>();

    char[] nums = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

    public static Object startwait=new Object();

    public Label l;

    private String filename="track1.txt"; //default pálya

    public static void main(String[] args) throws IOException {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {

        Group root=new Group();
        Scene scene=new Scene(root,300,300);

        Image im=new Image("a_ServerGame/Server/track1.png");
        Button track1=new Button("",new ImageView(im));
        track1.setStyle("-fx-background-color: gray;");
        track1.setPrefSize(30,30);
        track1.setLayoutX(scene.getWidth()/2-80);
        track1.setLayoutY(scene.getHeight()/2-15);

        Image im2=new Image("a_ServerGame/Server/track2.png");
        Button track2=new Button("",new ImageView(im2));
        track2.setStyle("-fx-background-color: white;");
        track2.setPrefSize(30,30);
        track2.setLayoutX(scene.getWidth()/2-30);
        track2.setLayoutY(scene.getHeight()/2-15);

        Image im3=new Image("a_ServerGame/Server/track3.png");
        Button track3=new Button("",new ImageView(im3));
        track3.setStyle("-fx-background-color: white;");
        track3.setPrefSize(30,30);
        track3.setLayoutX(scene.getWidth()/2+20);
        track3.setLayoutY(scene.getHeight()/2-15);
        root.getChildren().addAll(track1,track2,track3);

        track1.setOnAction(event -> {
            track1.setStyle("-fx-background-color: gray;");
            filename="track1.txt";
            track2.setStyle("-fx-background-color: white;");
            track3.setStyle("-fx-background-color: white;");
        });

        track2.setOnAction(event -> {
            track2.setStyle("-fx-background-color: gray;");
            filename="track2.txt";
            track1.setStyle("-fx-background-color: white;");
            track3.setStyle("-fx-background-color: white;");
        });

        track3.setOnAction(event -> {
            track3.setStyle("-fx-background-color: gray;");
            filename="track3.txt";
            track1.setStyle("-fx-background-color: white;");
            track2.setStyle("-fx-background-color: white;");
        });


        Label lb=new Label("Labirint-ing");
        lb.setFont(Font.font(25));
        lb.setPrefSize(130,30);
        lb.setLayoutX(scene.getWidth()/2-65);
        lb.setLayoutY(scene.getHeight()/2-110);

        TextField tf=new TextField();
        tf.setPromptText("max player...");
        tf.setPrefSize(100,30);
        tf.setLayoutX(scene.getWidth()/2-50);
        tf.setLayoutY(scene.getHeight()/2-60);

        Button btn=new Button("Create a Server");
        btn.setPrefSize(100,30);
        btn.setLayoutX(scene.getWidth()/2-50);
        btn.setLayoutY(scene.getHeight()/2+30);


        btn.setOnAction(event -> {

            boolean csakbetuk=true; //megvizsgálom, hogy csak számokat ír e be
            String t=tf.getText();
            for (int i = 0; i < t.length(); i++) {
                boolean f=false;
                for (int j = 0; j < nums.length; j++) {
                    if (nums[j] == t.charAt(i)) {
                        f = true;
                        break;
                    }
                }
                if(f==true){
                    csakbetuk=true;
                }else {
                    csakbetuk=false;
                    break;
                }
            }
            if(!csakbetuk){
                l=new Label("Wrong characters!");
                l.setPrefSize(100,30);
                l.setLayoutX(scene.getWidth()/2-40);
                l.setLayoutY(scene.getHeight()/2+60);
                root.getChildren().add(l);
            }
            if(csakbetuk){

                N=Integer.parseInt(tf.getText()); //játékosok száma

                if(root.getChildren().contains(l)){
                    root.getChildren().remove(l);
                }

                root.getChildren().remove(0,6);
                if(root.getChildren().size()==1){
                    root.getChildren().remove(0);
                }
                Label label=new Label("Waiting for players...");
                label.setFont(Font.font(25));
                label.setPrefSize(250,30);
                label.setLayoutX(scene.getWidth()/2-125);
                label.setLayoutY(scene.getHeight()/2-110);
                root.getChildren().add(label);

                BufferedReader fw = null;
                try {
                    fw = new BufferedReader(new FileReader(new File(filename)));
                    for (int i = 0; i <track.length ; i++) {
                        String[] datas =fw.readLine().split(" ");
                        for (int j = 0; j < track.length ; j++) {
                            track[i][j]=Integer.parseInt(datas[j]);

                            if (Server.track[i][j] == 0) {
                                notwall.add(new XY(j * 10, i * 10));
                            }
                        }
                    }

                    colorsAdder();
                    Thread listener=new Listener(); //0. lépés
                    listener.start();
                    manager=new Manager();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        root.getChildren().addAll(lb,tf,btn);

        tf.getParent().requestFocus();

        scene.setFill(Color.rgb(244,255,139));
        primaryStage.setTitle("LabirintServer");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    void colorsAdder(){
        Server.colors.add(new Colors( 0, 0, 255));
        Server.colors.add(new Colors(210, 105, 30));
        Server.colors.add(new Colors(139, 0, 139));
        Server.colors.add(new Colors(255, 140, 10));
        Server.colors.add(new Colors(255, 20, 147));
        Server.colors.add(new Colors(20, 90, 50));
        Server.colors.add(new Colors(240, 128, 128));
        Server.colors.add(new Colors(147,112,219));
        Server.colors.add(new Colors(199,21,133));
        Server.colors.add(new Colors(107,142,35));
    }
}

