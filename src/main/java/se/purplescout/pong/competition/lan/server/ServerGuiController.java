package se.purplescout.pong.competition.lan.server;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import se.purplescout.pong.competition.headless.NoLogDirectoryException;
import se.purplescout.pong.competition.lan.codetransfer.BroadcastAnnouncer;
import se.purplescout.pong.competition.lan.codetransfer.CodeReceiver;
import se.purplescout.pong.competition.headless.AutoFightHandler;
import se.purplescout.pong.competition.headless.AutoFight;

import java.io.IOException;
import java.util.List;

public class ServerGuiController extends AutoFightHandler {

    @FXML private ScrollPane highScoreScroll;
    @FXML private HighScore highScore;


    @Override
    public void initialize() {
        highScore.minWidthProperty().bind(highScoreScroll.widthProperty().subtract(20));
        highScore.minHeightProperty().bind(highScoreScroll.heightProperty());

        highScore.setOnPaddleRemovedListener(this);

        try {
            super.initialize();
        } catch (NoLogDirectoryException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void enableReceivingPaddles() throws IOException {
        new CodeReceiver(this).startServer();
        new BroadcastAnnouncer().start();
    }

    @Override
    public void fightRoundDone(List<AutoFight> fights) {
        Platform.runLater(() -> {
            highScore.setFights(fights);
            highScoreScroll.setHvalue(0.5);
        });
    }
}
