package se.purplescout.pong.competition.compiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.purplescout.pong.game.Paddle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class PaddleCompiler {

    private static final Logger LOG = LoggerFactory.getLogger(PaddleCompiler.class);

    public Class<Paddle> compile(Path p, File jdkPath) throws JDKNotFoundException {
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
            LOG.warn("There was something wrong with the source code in " + p, ex);
            throw new ToDisplay("There was something wrong with the source code in " + p + ". " + ex.getMessage(), ex);
        } catch (InstantiationException ex) {
            LOG.warn("Could not instantiate object. Do you have an empty constructor?", ex);
            throw new ToDisplay("Could not instantiate object. Do you have an empty constructor? " + ex + "\n\n" + ex.getMessage(), ex);
        } catch (ClassNotFoundException | IllegalAccessException ex) {
            LOG.warn("Failed compilation " + p, ex);
            throw new ToDisplay("Failed compilation " + p + ". " + ex + "\n\n" + ex.getMessage(), ex);
        } catch (UnableToReadSourceCodeFileException ex) {
            LOG.warn("Unable to read source code file " + p, ex);
            throw new ToDisplay("Unable to read source code file " + p, ex);
        } finally {
            System.setProperty("java.home", previousJavaHome);
            //System.out.println("Java home reset to " + System.getProperty("java.home"));
        }
    }

    private String getFileAsString(Path p) throws UnableToReadSourceCodeFileException {
        try {
            return String.join("\n", Files.readAllLines(p));
        } catch (IOException ex) {
            throw new UnableToReadSourceCodeFileException(p, ex);
        }
    }
}
