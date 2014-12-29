package se.purplescout.pong.competition.lan.util;

import se.purplescout.pong.competition.lan.client.ClientMain;

public class StatusIndicatorExceptionHandler implements Thread.UncaughtExceptionHandler {

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


        ex.printStackTrace();

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
