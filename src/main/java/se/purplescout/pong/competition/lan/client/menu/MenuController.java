package se.purplescout.pong.competition.lan.client.menu;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.purplescout.pong.competition.lan.util.CustomControl;

import java.io.*;
import java.util.Properties;
import se.purplescout.pong.competition.lan.client.ClientGuiController;

public class MenuController extends MenuBar {

    private static final Logger LOG = LoggerFactory.getLogger(MenuController.class);
    private static final File propertiesFile = new File("settings.ini");
    public static final String PADDLE_SOURCES_DIRECTORY_PROPERTY_NAME = "PaddleSourcesDirectory";
    public static final String JDK_PATH_PROPERTY_NAME = "JdkPath";

    @FXML
    private MenuItem pauseResume;
    @FXML
    private MenuItem restart;
    @FXML
    private MenuItem forceRefreshPaddleClasses;

    private ClientGuiController clientController;
    private final Properties properties = new Properties();

    private final ObjectProperty<File> paddleSourcesDirectoryProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<File> jdkProperty = new SimpleObjectProperty<>();
    private final ChangeListener<File> writeToDiskListener = new ChangeListener<File>() {
        @Override
        public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
            String propertyName;
            if (observable == paddleSourcesDirectoryProperty) {
                propertyName = PADDLE_SOURCES_DIRECTORY_PROPERTY_NAME;
            } else if (observable == jdkProperty) {
                propertyName = JDK_PATH_PROPERTY_NAME;
            } else {
                return;
            }
            properties.setProperty(propertyName, newValue.getAbsolutePath());

            try (FileWriter fw = new FileWriter(propertiesFile)) {
                properties.store(fw, null);
            } catch (IOException e) {
                LOG.warn("Unable to persist settings", e);
            }
        }
    };

    public MenuController() {
        CustomControl.setup(this, "Menu.fxml");

        paddleSourcesDirectoryProperty.addListener(writeToDiskListener);
        jdkProperty.addListener(writeToDiskListener);

        readPersistedSettings();
        initPaddleSourcesDirectory();
        initJdkDirectory();
    }

    private void readPersistedSettings() {
        try (InputStream is = new FileInputStream(propertiesFile)) {
            properties.load(is);
        } catch (FileNotFoundException e) {
            // Ignore
            LOG.info("Settings file not found, using defaults.");
        } catch (IOException e) {
            LOG.warn("Unable to open properties file", e);
        }
    }

    private void initPaddleSourcesDirectory() {
        String paddleSourcesDirectory = properties.getProperty(PADDLE_SOURCES_DIRECTORY_PROPERTY_NAME, ".");
        paddleSourcesDirectoryProperty.set(new File(paddleSourcesDirectory));
    }

    private void initJdkDirectory() {
        String jdkPath = properties.getProperty(JDK_PATH_PROPERTY_NAME, "NOT_SET");
        if (!jdkPath.equals("NOT_SET")) {
            jdkProperty.set(new File(jdkPath));
        }
    }

    public void initialize() {
        pauseResume.setAccelerator(new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN, KeyCombination.SHORTCUT_DOWN));
        restart.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN, KeyCombination.SHORTCUT_DOWN));

        forceRefreshPaddleClasses.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
    }

    public ObjectProperty<File> paddleSourcesDirectoryProperty() {
        return paddleSourcesDirectoryProperty;
    }

    public ObjectProperty<File> jdkFolderProperty() {
        return jdkProperty;
    }

    public void setClientController(ClientGuiController clientController) {
        this.clientController = clientController;
    }

    @FXML
    private void setPathToSourceFiles() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        if (paddleSourcesDirectoryProperty.get().exists()) {
            directoryChooser.setInitialDirectory(paddleSourcesDirectoryProperty.get());
        }

        directoryChooser.setTitle("Where are your paddle source code files?");
        File selectedFile = directoryChooser.showDialog(null);
        if (selectedFile != null) {
            paddleSourcesDirectoryProperty.setValue(selectedFile);
        }
    }

    @FXML
    public void setJdkPath() {
        // TODO: Test
        setJdkPathByPointingToJavac();
    }

    private void setJdkPathByPointingToJavaHomeFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(paddleSourcesDirectoryProperty.get());

        directoryChooser.setTitle("Where is your JDK home?");
        File selectedFile = directoryChooser.showDialog(null);
        if (selectedFile != null) {
            jdkProperty.setValue(selectedFile);
        }
    }

    private void setJdkPathByPointingToJavac() {
        // TODO: Ask environment variables where javac is?

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ploxx tellz where yao javac iz");
        File javac = fileChooser.showOpenDialog(null);
        if (javac != null) {
            // javac is located in JAVA_HOME/bin/javac. The parent of javac is JAVA_HOME/bin.
            // Sometimes JAVA_HOME needs to be set to JAVA_HOME/jre. Weird.
            File jdkHome = new File(javac.getParentFile().getParentFile(), "jre");
            jdkProperty.set(jdkHome);
        }
    }

    @FXML
    private void exit() {
        System.exit(0);
    }

    @FXML
    private void pauseResume() {
        if (clientController != null) {
            clientController.pauseResume();
        }
    }

    @FXML
    private void restart() {
        if (clientController != null) {
            clientController.restartGame();
        }
    }

    @FXML
    private void forceRefresh() {
        if (clientController != null) {
            clientController.refreshPaddleClasses();
        }
    }
}
