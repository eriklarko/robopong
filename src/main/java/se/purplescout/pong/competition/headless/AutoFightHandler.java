package se.purplescout.pong.competition.headless;

import se.purplescout.pong.competition.compiler.JDKNotFoundException;
import se.purplescout.pong.competition.compiler.PaddleCompiler;
import se.purplescout.pong.game.Paddle;
import se.purplescout.pong.competition.paddlecache.PaddleCache;

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
    private final PaddleCompiler compiler = new PaddleCompiler();

    protected final FightQueue getFightQueue() {
        return fightQueue;
    }

    public void initialize() throws NoLogDirectoryException {
        if (!CODE_LOG_DIRECTORY.exists()) {
            if (!CODE_LOG_DIRECTORY.mkdirs()) {
                throw new NoLogDirectoryException();
            }
        }

        try {
            addStoredPaddles();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            enableReceivingPaddles();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(13);
        }
    }

    protected abstract void enableReceivingPaddles() throws IOException;

    private void addStoredPaddles() throws IOException, JDKNotFoundException {
        List<Path> files = Files.walk(CODE_LOG_DIRECTORY.toPath())
                .filter(p -> p.toFile().isFile() && !p.toFile().getName().startsWith("ignore_"))
                .collect(Collectors.toList());

        for (Path file : files) {
            compileAndAddPaddle(file);
        }
    }

    private void compileAndAddPaddle(Path p) throws JDKNotFoundException {
        Class<Paddle> clazz = compiler.compile(p, null);
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
        }
    }

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
