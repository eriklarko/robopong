package se.purplescout.pong.server;

import javafx.scene.control.Alert;
import se.purplescout.pong.codetransfer.NewPaddleListener;
import se.purplescout.pong.codetransfer.lan.BroadcastAnnouncer;
import se.purplescout.pong.codetransfer.lan.CodeReceiver;
import se.purplescout.pong.compiler.JDKNotFoundException;
import se.purplescout.pong.game.Paddle;
import se.purplescout.pong.gui.client.paddle.classselector.ClassSelector;
import se.purplescout.pong.gui.client.paddle.classselector.PaddleCache;
import se.purplescout.pong.server.autofight.FightQueue;
import se.purplescout.pong.server.autofight.FightRoundDoneListener;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class AutoFightHandler implements NewPaddleListener, FightRoundDoneListener, OnPaddleRemovedListener {

    private static final File CODE_LOG_DIRECTORY = new File("paddles");

    private final FightQueue fightQueue = new FightQueue(this);

    public AutoFightHandler() {
        if (!CODE_LOG_DIRECTORY.exists()) {
            if (!CODE_LOG_DIRECTORY.mkdirs()) {
                System.out.println("Code log directory bårken");
            }
        }
    }

    protected abstract void enableReceivingPaddles() throws IOException;

    public void initialize() {
        try {
            addStoredPaddles();
        } catch (Exception e) {
            e.printStackTrace();

            alert(Alert.AlertType.WARNING, "Warning", "Could not restore previous paddles, " + e.getMessage());
        }

        try {
            enableReceivingPaddles();
        } catch (IOException e) {
            e.printStackTrace();

            alert(Alert.AlertType.ERROR, "Fatal exception", "Unable to start listening for paddles, " + e.getMessage());
            System.exit(13);
        }
    }

    private void addStoredPaddles() throws IOException, JDKNotFoundException {
        List<Path> files = Files.walk(CODE_LOG_DIRECTORY.toPath())
                .filter(p -> p.toFile().isFile() && !p.toFile().getName().startsWith("ignore_"))
                .collect(Collectors.toList());

        for (Path file : files) {
            compileAndAddPaddle(file);
        }
    }

    private void compileAndAddPaddle(Path p) throws JDKNotFoundException {
        Class<Paddle> clazz = ClassSelector.compile(p, null);
        addNewPaddle(clazz);
    }

    @Override
    public void newPaddle(Class<? extends Paddle> paddleClass, String code) {
        addNewPaddle(paddleClass);
        logCode(code, paddleClass);
    }

    private void addNewPaddle(Class<? extends Paddle> paddleClass) {
        System.out.println("New paddle!");
        try {
            Class<Paddle> clazz = (Class<Paddle>) paddleClass;
            PaddleCache.registerNewPaddle(clazz);
            fightQueue.addPaddle(clazz);
        } catch (Exception e) {
            e.printStackTrace();
            alert(Alert.AlertType.WARNING, "", "Unable to register new paddle, " + e.getMessage());
        }
    }

    protected abstract void alert(Alert.AlertType type, String title, String body);

    private void logCode(String code, Class<? extends Paddle> clazz) {
        if (!CODE_LOG_DIRECTORY.canWrite()) {
            return;
        }

        try {
            Writer bw = new BufferedWriter(new FileWriter(new File(CODE_LOG_DIRECTORY, PaddleCache.getTeamName(clazz))));
            try {
                bw.append(code);
            } catch(IOException ex1) {
                System.out.println(code);
            }
            bw.flush();
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void paddleRemoved(Paddle paddle) {
        fightQueue.removePaddle(paddle.getClass());

        try {
            markPaddleClassFileIgnored(paddle.getClass());
        } catch (IOException e) {
            System.out.println("Unable to ignore paddle class file");
            e.printStackTrace();
        }
    }

    private void markPaddleClassFileIgnored(Class<? extends Paddle> clazz) throws IOException {
        Optional<Path> file = Files.walk(CODE_LOG_DIRECTORY.toPath())
                .filter((p) -> p.toFile().getName().equals(PaddleCache.getTeamName(clazz)))
                .findFirst();

        Path newFileName = file.get().resolveSibling("ignore_" + file.get().toFile().getName());
        try {
            if (file.isPresent()) {
                Files.move(file.get(), newFileName);
            }
        } catch (FileAlreadyExistsException ex) {
            if (newFileName.toFile().delete()) {
                Files.move(file.get(), newFileName);
            } else {
                System.out.println("Unable to ignore " + clazz);
            }
        }
    }
}
