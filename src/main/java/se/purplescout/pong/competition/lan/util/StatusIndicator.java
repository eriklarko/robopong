package se.purplescout.pong.competition.lan.util;

import java.io.OutputStream;

public interface StatusIndicator {

    void startWorking(Object key, String description, OutputStream output);

    void setStatus(Object key, String description);

    void stopWorking(Object key, String endMessage);
}
