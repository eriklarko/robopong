package se.purplescout.pong.competition.lan.util.filesystemlistener;

import java.nio.file.Path;

public abstract class AbstractDirectoryChangeListener implements DirectoryChangeListener {

    @Override
    public void overflowed() {

    }

    @Override
    public void created(Path file) {

    }

    @Override
    public void deleted(Path file) {

    }

    @Override
    public void modified(Path file) {

    }

    @Override
    public void failed() {

    }
}
