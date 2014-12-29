package se.purplescout.pong.competition.client;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import se.purplescout.pong.competition.client.util.StatusIndicator;
import se.purplescout.pong.competition.client.util.StatusIndicatorExceptionHandler;

public class ClientMain extends Application {

    public static StatusIndicator mainStatusIndicator;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new StatusIndicatorExceptionHandler());

        Parent root = FXMLLoader.load(getClass().getResource("ClientGui.fxml"));
        primaryStage.setTitle("Hello, I am the pong");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

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
