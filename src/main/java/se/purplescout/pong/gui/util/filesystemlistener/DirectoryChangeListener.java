package se.purplescout.pong.gui.util.filesystemlistener;

import java.nio.file.Path;

public interface DirectoryChangeListener {

    void overflowed();

    void created(Path file);

    void deleted(Path file);

    void modified(Path file);

    void failed();
}
