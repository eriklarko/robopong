package se.purplescout.pong.competition.lan.util;

import java.io.IOException;
import java.nio.file.Path;

public class UnableToReadSourceCodeFileException extends Throwable {

    public UnableToReadSourceCodeFileException(Path p, IOException ex) {
        super(p.toString(), ex);
    }
}
