package se.purplescout.pong.competition.client.paddle;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.TilePane;
import se.purplescout.pong.competition.client.codetransfer.ClientConnection;
import se.purplescout.pong.competition.client.paddle.classselector.ClassSelector;
import se.purplescout.pong.competition.client.util.CustomControl;
import se.purplescout.pong.competition.client.util.StatusIndicator;
import se.purplescout.pong.competition.paddlecache.PaddleCache;
import se.purplescout.pong.game.Paddle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaddleController extends TilePane {

    private static final ExecutorService sendToServerPool = Executors.newSingleThreadExecutor();

    @FXML private ClassSelector paddleClassChoice;
    @FXML private Button sendToServerButton;
    @FXML private Label fuckingUglyFxShit;

    private AtomicBoolean currentlySendingClassToServer = new AtomicBoolean(false);
    private StatusIndicator statusIndicator;
    private ClientConnection clientConnection;

    public PaddleController() {
        CustomControl.setup(this, "Paddle.fxml");
        this.setPrefColumns(1);
        this.setVgap(5.0);
    }

    public void initialize() {
        sendToServerButton.disabledProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    fuckingUglyFxShit.setTooltip(new Tooltip("Not connected to a server"));
                } else {
                    fuckingUglyFxShit.setTooltip(null);
                }
            }
        });
        sendToServerButton.setDisable(true);
        sendToServerButton.prefWidthProperty().bind(paddleClassChoice.widthProperty());
    }

    public void setClientConnection(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    public void setStatusIndicator(StatusIndicator statusIndicator) {
        this.statusIndicator = statusIndicator;
    }

    public ObjectProperty<File> jdkFolderProperty() {
        return paddleClassChoice.jdkFolderProperty();
    }

    public ObjectProperty<Class<Paddle>> selectedPaddleProperty() {
        return paddleClassChoice.selectedPaddleProperty();
    }

    public BooleanProperty disableSendToServerProperty() {
        return sendToServerButton.disableProperty();
    }

    @FXML
    private void sendSelectedPaddleToServer() {
        sendToServerPool.submit(new SendToServerTask());
    }

    public ObjectProperty<File> paddleClassFolderProperty() {
        return paddleClassChoice.paddleClassFolderProperty();
    }

    public void updatePaddleClassList() {
        paddleClassChoice.updatePaddleClassList();
    }


    private class SendToServerTask implements Runnable {

        @Override
        public void run() {
            if (clientConnection == null) {
                statusIndicator.setStatus(null, "Unable to send file to the server, I've got no handle to the socket!");
            }

            if (currentlySendingClassToServer.get()) {
                return;
            }

            String teamName = PaddleCache.getTeamName(selectedPaddleProperty().get());
            final Object key = new Object();
            String endMessage = "Failed to send " + teamName + " to the server";
            try {
                currentlySendingClassToServer.set(true);
                if (statusIndicator != null) {
                    statusIndicator.startWorking(key, "Sending " + teamName + " to the server", null);
                }

                Class<Paddle> paddle = selectedPaddleProperty().get();
                Path f = paddleClassChoice.whichFileCompiledTo(paddle);
                if (f == null) {
                    statusIndicator.setStatus(key, "Whoa, this is weird. I couldn't find the file used to create the paddle!");
                }

                try {
                    String output = clientConnection.sendCodeToServer(f.toFile());
                    endMessage = "Sent " + teamName + " to the server, " + output;
                } catch (ClientConnection.NoServerException | IOException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    statusIndicator.setStatus(key, "Unable to send the code to the server: " + ex.getMessage());
                }
            } finally {
                currentlySendingClassToServer.set(false);
                if (statusIndicator != null) {
                    statusIndicator.stopWorking(key, endMessage);
                }
            }
        }
    }
}
