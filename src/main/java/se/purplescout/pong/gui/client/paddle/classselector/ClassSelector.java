package se.purplescout.pong.gui.client.paddle.classselector;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import se.purplescout.pong.compiler.DynaCompTest;
import se.purplescout.pong.compiler.InvalidSourceStringException;
import se.purplescout.pong.compiler.JDKNotFoundException;
import se.purplescout.pong.game.Paddle;
import se.purplescout.pong.gui.util.*;
import se.purplescout.pong.gui.util.filesystemlistener.AbstractDirectoryChangeListener;
import se.purplescout.pong.gui.util.filesystemlistener.DirectoryChangeListener;
import se.purplescout.pong.gui.util.filesystemlistener.DirectoryWatcher;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import se.purplescout.pong.gui.client.paddle.PaddleController;

public class ClassSelector extends ChoiceBox<Class<Paddle>> {

    private final ObjectProperty<File> paddleClassFolder = new SimpleObjectProperty<>();
    private final ObjectProperty<File> jdkPath = new SimpleObjectProperty<>();
    private final ObjectProperty<Class<Paddle>> selectedPaddle = new SimpleObjectProperty<>();
    private final Map<Class<Paddle>, PathAndUpdatedStatus> paddleToFile = new HashMap<>();
    private StatusIndicator statusIndicator;
    private DirectoryWatcher directoryWatcher;
    private final DirectoryChangeListener directoryChangeListener = new AbstractDirectoryChangeListener() {
        @Override
        public void created(Path file) {
            updateIfJavaFile(file);
        }

        @Override
        public void deleted(Path file) {
            updateIfJavaFile(file);
        }

        @Override
        public void modified(Path file) {
            updateIfJavaFile(file);
        }

        private void updateIfJavaFile(Path file) {
            if (file.toFile().getName().endsWith(".java")) {
                Platform.runLater(ClassSelector.this::updatePaddleClassList);
            }
        }
    };

    public ClassSelector() {
        super();
        paddleClassFolder.addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
                registerFileSystemListener();
                updatePaddleClassList();
            }
        });

        this.valueProperty().bindBidirectional(selectedPaddle);
        this.setConverter(new StringConverter<Class<Paddle>>() {
            Map<String, Class<Paddle>> convertHistory = new HashMap<>();

            @Override
            public String toString(Class<Paddle> clazz) {
                String asString = PaddleCache.getTeamName(clazz);
                convertHistory.put(asString, clazz);
                return asString;
            }

            @Override
            public Class<Paddle> fromString(String string) {
                Class<Paddle> paddle = convertHistory.get(string);
                if (paddle == null) {
                    throw new NullPointerException("Tried to turn " + string + " into a paddle, but didn't know how");
                }
                return paddle;
            }
        });
    }

    public void setStatusIndicator(StatusIndicator statusIndicator) {
        this.statusIndicator = statusIndicator;
    }

    public ObjectProperty<File> jdkFolderProperty() {
        return jdkPath;
    }

    public ObjectProperty<Class<Paddle>> selectedPaddleProperty() {
        return selectedPaddle;
    }

    private void registerFileSystemListener() {
        if (directoryWatcher != null) {
            directoryWatcher.requestStop();
        }

        try {
            directoryWatcher = new DirectoryWatcher(paddleClassFolder.get().toPath(), directoryChangeListener);
            directoryWatcher.startWatching();
        } catch (IOException e) {
            throw new ToDisplay("Unable to register file system listener on the paddle directory", e);
        }
    }

    private void populatePaddleClassChoices(Collection<Class<Paddle>> newPaddles) {
        Class<Paddle> currentPaddle = this.getValue();

        // Disconnect properties to prevent change events from being fired.
        this.valueProperty().unbindBidirectional(selectedPaddle);
        // Change available paddles. No event is fired here
        this.setItems(FXCollections.observableArrayList(newPaddles));

        // Check if the currently selected paddle is still a valid choice
        if (currentPaddle != null && containsPaddle(currentPaddle, newPaddles)) {
            currentPaddle = getPaddleFromSameTeam(currentPaddle, newPaddles);
            PathAndUpdatedStatus pathAndUpdatedStatus = paddleToFile.get(currentPaddle);
            boolean classHasChanged = pathAndUpdatedStatus == null ? true : pathAndUpdatedStatus.isUpdated();

            if (classHasChanged) { // If the current paddle is a valid choice and it has changed, fire a change event
                this.valueProperty().bindBidirectional(selectedPaddle);
                this.setValue(currentPaddle);
            } else { // If the current paddle is valid but hasn't changed, don't fire a change event
                this.setValue(currentPaddle);
                this.valueProperty().bindBidirectional(selectedPaddle);
            }
        } else {
            // The currently selected paddle is no longer valid, select an arbitrary paddle and fire a change event
            this.valueProperty().bindBidirectional(selectedPaddle);
            this.setValue(this.getItems().get(0));
        }
    }

    private boolean containsPaddle(Class<Paddle> paddle, Iterable<Class<Paddle>> paddles) {
        return getPaddleFromSameTeam(paddle, paddles) != null;
    }

    private Class<Paddle> getPaddleFromSameTeam(Class<Paddle> template, Iterable<Class<Paddle>> paddles) {
        for (Class<Paddle> paddle : paddles) {
            if (PaddleCache.getTeamName(template).equals(PaddleCache.getTeamName(paddle))) {
                return paddle;
            }
        }

        return null;
    }

    public ObjectProperty<File> paddleClassFolderProperty() {
        return paddleClassFolder;
    }

    public void updatePaddleClassList() {
        if (paddleClassFolder.get() != null) {
            Collection<Class<Paddle>> classes = getPaddlesInFolder(paddleClassFolder.get());
            populatePaddleClassChoices(classes);
        }
    }

    private Collection<Class<Paddle>> getPaddlesInFolder(File root) {
        final Object key = new Object();
        try {
            if (statusIndicator != null) {
                statusIndicator.startWorking(key, "Looking for files in " + root, null);
            }
            List<Path> files = getJavaFilesInFolder(root);
            log(key, "Found " + files.size() + " file(s)");

            Collection<Class<Paddle>> paddles = new LinkedList<>();
            for (Path file : files) {
                try {
                    Class<Paddle> paddle = compile(file, jdkPath.get());
                    if (paddle != null) {
                        String oldTeamName = PaddleCache.getTeamName(paddle);
                        PaddleCache.registerNewPaddle(paddle);
                        paddles.add(paddle);

                        String previousHash = null;
                        for (Map.Entry<Class<Paddle>, PathAndUpdatedStatus> entry : paddleToFile.entrySet()) {
                            if (PaddleCache.getTeamName(entry.getKey()).equals(oldTeamName)) {
                                previousHash = entry.getValue().getHash();

                                System.out.println("Removed " + oldTeamName + " from paddle to file mapping");
                                paddleToFile.remove(entry.getKey());
                                break;
                            }
                        }
                        System.out.println(PaddleCache.getTeamName(paddle) + " came from " + file.toFile().getAbsolutePath());
                        paddleToFile.put(paddle, new PathAndUpdatedStatus(file, previousHash));
                    }
                } catch (JDKNotFoundException ex) {
                    throw new ToDisplay("I can't seem to find your JDK, please tell me where it is", ex);
                } catch (NewInstanceException ex) {
                    throw new ToDisplay("Unable to instatiate paddle " + file + ", try to make the class public and add a public constructor with no arguments if you have declared your own constructor. ", ex);
                } catch (GetTeamNameException ex) {
                    throw new ToDisplay("Couldn't get the team name from " + file + ", it either took too long or throwed an exception", ex);
                } catch (RegisterTimeoutException ex) {
                    throw new ToDisplay("Unable to instatiate paddle " + file + ", it took too long..", ex);
                } catch (IOException ex) {
                    throw new ToDisplay("Unable to instatiate paddle " + file + ", " + ex.getMessage(), ex);
                }
            }

            log(key, "Compiled " + files.size() + " file(s)");
            return paddles;
        } finally {
            if (statusIndicator != null) {
                statusIndicator.stopWorking(key, null);
            }
        }
    }

    private void log(Object key, String msg) {
        if (statusIndicator != null) {
            statusIndicator.setStatus(key, msg);
        }
    }

    private List<Path> getJavaFilesInFolder(File root) {
        try {
            List<Path> files = Files.walk(root.toPath()).filter(p -> p.toFile().getName().endsWith(".java")).collect(Collectors.toList());
            if (files.isEmpty()) {
                throw new ToDisplay("Found no Java files in " + root.getAbsolutePath());
            }
            if (files.size() > 100) {
                // TODO: Allow the user to override this decision
                throw new ToDisplay("Found too many Java files in " + root.getAbsolutePath());
            }
            return files;
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            throw new ToDisplay("Unable to locate classes in the selected folder: " + ex.getMessage() + ".\nPlease try different folder, it is probably named 'src'.");
        }
    }

    public static Class<Paddle> compile(Path p, File jdkPath) throws JDKNotFoundException {
        String previousJavaHome = System.getProperty("java.home");
        try {
            if (jdkPath != null) {
                System.setProperty("java.home", jdkPath.getAbsolutePath());
                //System.out.println("Java home set to " + System.getProperty("java.home"));
            }
            String code = getFileAsString(p);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter errorStream = new PrintWriter(baos);
            Class<?> clazz = DynaCompTest.compile(code.toString(), errorStream);
            String errors = new String(baos.toByteArray());
            if (!errors.trim().isEmpty()) {
                throw new ToDisplay("Unable to compile " + p + ".\n\n" + errors);
            }

            if (clazz == null) {
                throw new ToDisplay("Got null instance when compiling " + p + ". This is weird and unexpected.");
            }
            try {
                return (Class<Paddle>) clazz;
            } catch (ClassCastException ex) {
                throw new ToDisplay(p + " does not seem to be a paddle..");
            }
        } catch (InvalidSourceStringException ex) {
            Logger.getLogger(PaddleController.class.getName()).log(Level.SEVERE, null, ex);
            throw new ToDisplay("There was something wrong with the source code in " + p + ". " + ex.getMessage(), ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(PaddleController.class.getName()).log(Level.SEVERE, null, ex);
            throw new ToDisplay("Could not instantiate object. Do you have an empty constructor? " + ex + "\n\n" + ex.getMessage(), ex);
        } catch (ClassNotFoundException | IllegalAccessException ex) {
            Logger.getLogger(PaddleController.class.getName()).log(Level.SEVERE, null, ex);
            throw new ToDisplay("Failed compilation " + p + ". " + ex + "\n\n" + ex.getMessage(), ex);
        } catch (UnableToReadSourceCodeFileException ex) {
            Logger.getLogger(PaddleController.class.getName()).log(Level.SEVERE, null, ex);
            throw new ToDisplay("Unable to read source code file " + p, ex);
        } finally {
            System.setProperty("java.home", previousJavaHome);
            //System.out.println("Java home reset to " + System.getProperty("java.home"));
        }
    }

    private static String getFileAsString(Path p) throws UnableToReadSourceCodeFileException {
        try {
            return String.join("\n", Files.readAllLines(p));
        } catch (IOException ex) {
            throw new UnableToReadSourceCodeFileException(p, ex);
        }
    }

    public Path whichFileCompiledTo(Class<Paddle> paddle) {
        PathAndUpdatedStatus toReturn = paddleToFile.get(paddle);
        if (toReturn == null) {
            System.out.println("Did not find file that compiled to " + PaddleCache.getTeamName(paddle));
            return null;
        } else {
            return toReturn.getPath();
        }
    }
}
