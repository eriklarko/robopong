package se.purplescout.pong.competition.lan.client;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.controlsfx.control.HiddenSidesPane;
import se.purplescout.pong.competition.compiler.JDKNotFoundException;
import se.purplescout.pong.competition.compiler.PaddleCompiler;
import se.purplescout.pong.competition.lan.client.menu.MenuController;
import se.purplescout.pong.competition.lan.client.paddle.PaddleController;
import se.purplescout.pong.competition.paddlecache.PaddleCache;
import se.purplescout.pong.competition.lan.client.workhistory.WorkList;
import se.purplescout.pong.competition.lan.util.StatusIndicator;
import se.purplescout.pong.game.Paddle;
import se.purplescout.pong.game.Pong;
import se.purplescout.pong.game.SomeoneScoredListener;
import se.purplescout.pong.competition.lan.codetransfer.ClientConnection;
import se.purplescout.pong.competition.lan.util.ToDisplay;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.Objects;

public class ClientGuiController implements SomeoneScoredListener, StatusIndicator {

    @FXML
    private HiddenSidesPane hiddenSidesPane;

    @FXML
    private ScrollPane workListScroll;
    @FXML
    private WorkList workList;
    @FXML
    private MenuController menu;

    @FXML
    private PongCanvas gameCanvas;
    @FXML
    private PaddleController leftPaddleControls, rightPaddleControls;
    @FXML
    private Label leftPaddleScoreLabel, rightPaddleScoreLabel;

    @FXML
    private Node spaceStealer;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator workIndicator;

    private Pong pong;
    private int leftPaddleScore, rightPaddleScore;
    private SimulationSpeed simulationSpeed = SimulationSpeed.NORMAL;

    private final ObjectProperty<File> jdkFolder = new SimpleObjectProperty<>();
    private final ObjectProperty<File> javaFilesFolder = new SimpleObjectProperty<>();

    private final ClientConnection clientConnection = new ClientConnection();
    private final PaddleCompiler compiler = new PaddleCompiler();

    public ClientGuiController() {
        ClientMain.mainStatusIndicator = this;

        clientConnection.setServerFoundListener(new ClientConnection.ServerFoundListener() {
            @Override
            public void serverLost() {
                Platform.runLater(() -> leftPaddleControls.disableSendToServerProperty().set(true));
                clientConnection.startListeningForServerAsync();
            }

            @Override
            public void serverFound() {
                Platform.runLater(() -> leftPaddleControls.disableSendToServerProperty().set(false));
            }
        });
    }

    public void initialize() {
        menu.setClientController(this);

        setupGameCanvas();
        setupPaddleControls();
        initializeScoreLabels();

        VBox.setVgrow(spaceStealer, Priority.ALWAYS);
        HBox.setHgrow(gameCanvas, Priority.ALWAYS);
        leftPaddleControls.disableSendToServerProperty().set(true);
        clientConnection.startListeningForServerAsync();

        jdkFolder.bindBidirectional(menu.jdkFolderProperty());
        makeSureThereIsAValidJdk();

        javaFilesFolder.bindBidirectional(menu.paddleSourcesDirectoryProperty());

        workList.minWidthProperty().bind(workListScroll.widthProperty().subtract(18 + 36));
        workList.minHeightProperty().bind(workListScroll.heightProperty());
        workList.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                workListScroll.setVvalue(1.0);
            }
        });

        hiddenSidesPane.setAnimationDelay(Duration.millis(500));
        hiddenSidesPane.setTriggerDistance(45);
    }

    private void makeSureThereIsAValidJdk() {
        try {
            URL resource = getClass().getClassLoader().getResource("se/purplescout/pong/competition/lan/client/JdkTestPaddle.txt");
            if (resource == null) {
                setStatus(null, "Unable to test for the presence of a JDK, couldn't find test paddle class");
                return;
            }

            Path pathToTestPaddle = resourceToPath(resource);
            compiler.compile(pathToTestPaddle, jdkFolder.get());
        } catch (JDKNotFoundException e) {
            menu.setJdkPath();
            makeSureThereIsAValidJdk();
        } catch (Exception e) {
            setStatus(null, "Unable to test for the presence of a JDK. " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Path resourceToPath(URL resource) throws IOException, URISyntaxException {
        Objects.requireNonNull(resource, "Resource URL cannot be null");
        URI uri = resource.toURI();

        String scheme = uri.getScheme();
        if (scheme.equals("file")) {
            return Paths.get(uri);
        }

        if (!scheme.equals("jar")) {
            throw new IllegalArgumentException("Cannot convert to Path: " + uri);
        }

        String s = uri.toString();
        int separator = s.indexOf("!/");
        String entryName = s.substring(separator + 2);
        URI fileURI = URI.create(s.substring(0, separator));

        FileSystem fs;
        try {
            fs = FileSystems.newFileSystem(fileURI, Collections.<String, Object>emptyMap());
        } catch (FileSystemAlreadyExistsException e) {
            System.out.println("File system already existed");
            fs = FileSystems.getFileSystem(fileURI);
        }
        return fs.getPath(entryName);
    }

    private void setupGameCanvas() {
        gameCanvas.setWidth(Pong.AREA_WIDTH);
        gameCanvas.setHeight(Pong.AREA_HEIGHT);
    }

    private void setupPaddleControls() {
        leftPaddleControls.setStatusIndicator(this);
        rightPaddleControls.setStatusIndicator(this);

        leftPaddleControls.setClientConnection(clientConnection);
        rightPaddleControls.setClientConnection(clientConnection);

        leftPaddleControls.jdkFolderProperty().bind(jdkFolder);
        rightPaddleControls.jdkFolderProperty().bind(jdkFolder);

        leftPaddleControls.paddleClassFolderProperty().bind(javaFilesFolder);
        rightPaddleControls.paddleClassFolderProperty().bind(javaFilesFolder);

        leftPaddleControls.selectedPaddleProperty().addListener(new ChangeListener<Class<Paddle>>() {
            @Override
            public void changed(ObservableValue<? extends Class<Paddle>> observable, Class<Paddle> oldValue, Class<Paddle> newValue) {
                System.out.println("Left paddle changed to " + newValue);
                startNewPongGame();
            }
        });
        rightPaddleControls.selectedPaddleProperty().addListener(new ChangeListener<Class<Paddle>>() {
            @Override
            public void changed(ObservableValue<? extends Class<Paddle>> observable, Class<Paddle> oldValue, Class<Paddle> newValue) {
                System.out.println("Right paddle changed to " + newValue);
                startNewPongGame();
            }
        });

        leftPaddleControls.disableSendToServerProperty().bindBidirectional(rightPaddleControls.disableSendToServerProperty());
    }

    private void initializeScoreLabels() {
        leftPaddleScoreLabel.setText("0");
        rightPaddleScoreLabel.setText("0");
    }

    private void startNewPongGame() {
        System.out.println("NEW PONG");
        Class<Paddle> leftPaddle = leftPaddleControls.selectedPaddleProperty().get();
        Class<Paddle> rightPaddle = rightPaddleControls.selectedPaddleProperty().get();

        leftPaddleScore = rightPaddleScore = 0;
        leftPaddleScoreLabel.setText("0");
        rightPaddleScoreLabel.setText("0");

        if (leftPaddle == null || rightPaddle == null) {
            return;
        }

        if (pong != null) {
            pong.requestStop();
        }

        pong = new Pong(simulationSpeed.getDelay(), clone(PaddleCache.getInstance(leftPaddle)), clone(PaddleCache.getInstance(rightPaddle)));
        pong.setSomeoneScoredListener(this);
        gameCanvas.setPong(pong);
        gameCanvas.onSomethingMoved();

        Thread pongThread = new Thread(pong);
        pongThread.setName("Pong - " + PaddleCache.getTeamName(leftPaddle) + " vs " + PaddleCache.getTeamName(rightPaddle));
        pongThread.start();
    }

    private Paddle clone(Paddle paddle) {
        try {
            return paddle.getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new ToDisplay("Unable to clone paddle " + paddle.getTeamName(), ex);
        }
    }

    @FXML
    private void slowSimulationSpeed() {
        setSimulationSpeed(SimulationSpeed.SLOW);
    }

    @FXML
    private void normalSimulationSpeed() {
        setSimulationSpeed(SimulationSpeed.NORMAL);
    }

    @FXML
    private void fastSimulationSpeed() {
        setSimulationSpeed(SimulationSpeed.FAST);
    }

    private void setSimulationSpeed(SimulationSpeed delay) {
        simulationSpeed = delay;
        if (pong != null) {
            pong.setMillisecondsToSleepBetweenFrames(simulationSpeed.getDelay());
        }
    }

    @Override
    public void someoneScored(PLAYER player, String name, Paddle paddle) {
        Platform.runLater(() -> {
            if (player == PLAYER.LEFT) {
                leftPaddleScoreLabel.setText(String.valueOf(++leftPaddleScore));
            } else if (player == PLAYER.RIGHT) {
                rightPaddleScoreLabel.setText(String.valueOf(++rightPaddleScore));
            }
        });
    }

    @Override
    public void startWorking(Object key, String description, OutputStream output) {
        Platform.runLater(() -> {
            statusLabel.setText(description);
            workIndicator.setVisible(true);

            workList.addWorkData(key, description);
        });
    }

    @Override
    public void stopWorking(Object key, String endMessage) {
        Platform.runLater(() -> {
            if (endMessage == null) {
                FadeTransition fade = new FadeTransition(Duration.seconds(1));
                fade.setFromValue(1);
                fade.setToValue(0);
                fade.setOnFinished(event -> {
                    statusLabel.opacityProperty().set(1);
                    statusLabel.setText(null);
                });
                SequentialTransition st = new SequentialTransition(statusLabel, new PauseTransition(Duration.seconds(5)), fade);
                st.play();
            } else {
                statusLabel.setText(endMessage);
                workList.addWorkData(key, endMessage);
                stopWorking(key, null);
            }
            workIndicator.setVisible(false);

        });
    }

    @Override
    public void setStatus(Object key, String description) {
        Platform.runLater(() -> {
            statusLabel.setText(description);

            workList.addWorkData(key, description);
        });

        if (key == null) {
            stopWorking(null, null);
        }
    }

    public void pauseResume() {
        if (pong.isPaused()) {
            pong.resumeThread();
        } else {
            pong.pauseThread();
        }
    }

    public void restartGame() {
        startNewPongGame();
    }

    public void refreshPaddleClasses() {
        leftPaddleControls.updatePaddleClassList();
        rightPaddleControls.updatePaddleClassList();
    }
}
