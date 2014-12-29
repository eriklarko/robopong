package se.purplescout.pong.competition.lan.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.purplescout.pong.competition.lan.client.ClientMain;

public class StatusIndicatorExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(StatusIndicatorExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof ToDisplay) {
                ClientMain.mainStatusIndicator.setStatus(null, cause.getMessage());
                return;
            }
            cause = cause.getCause();
        }


        LOG.warn("Unknown exception", ex);

        Object key = new Object();
        String s = "Unknown exception occured";
        if (ex.getMessage() != null) {
            s += ", " + ex.getMessage();
        }
        ClientMain.mainStatusIndicator.setStatus(key, s);
        ClientMain.mainStatusIndicator.setStatus(key, "Exception: " + ex);
        //System.exit(1337);
    }
}
