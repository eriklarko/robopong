package se.purplescout.pong.competition.lan.util.filesystemlistener;


import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class DirectoryWatcher implements Runnable {

    private final WatchService watcher;
    private final Path pathToWatch;
    private final DirectoryChangeListener changeListener;
    private boolean stopRequested = false;

    private final Map<WatchKey,Path> keys = new HashMap<>();
    private boolean trace = true;

    public DirectoryWatcher(Path pathToWatch, DirectoryChangeListener changeListener) throws IOException {
        this.pathToWatch = pathToWatch;
        this.changeListener = changeListener;

        watcher = pathToWatch.getFileSystem().newWatchService();
        registerAll(pathToWatch);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    public void requestStop() {
        stopRequested = true;
    }

    public void startWatching() {
        new Thread(this, "Watching for changes in " + pathToWatch.toFile().getAbsolutePath()).start();
    }

    @Override
    public void run() {
        while (!stopRequested) {
            WatchKey key;
            try {
                // wait for a key to be available
                key = watcher.take();
            } catch (InterruptedException ex) {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                // get event type
                WatchEvent.Kind<?> kind = event.kind();

                // get file name
                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path file = ev.context();

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    changeListener.overflowed();
                    continue;
                } else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    changeListener.created(file);
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    changeListener.deleted(file);
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    changeListener.modified(file);
                }
            }

            // IMPORTANT: The key must be reset after processed
            boolean valid = key.reset();
            if (!valid) {
                changeListener.failed();
                break;
            }
        }

        try {
            watcher.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
