package se.purplescout.pong.competition.server;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import se.purplescout.pong.competition.security.PongPolicy;

import java.security.Policy;

public class ServerMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Policy.setPolicy(new PongPolicy());
        System.setSecurityManager(new SecurityManager());

        Parent root = FXMLLoader.load(getClass().getResource("ServerGui.fxml"));
        primaryStage.setTitle("Pong HighScore");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.setFullScreen(true);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
