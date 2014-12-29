package se.purplescout.pong.competition.lan.util;


public class ToDisplay extends RuntimeException {

    public ToDisplay(String message) {
        super(message);
    }

    public ToDisplay(String message, Throwable cause) {
        super(message, cause);
    }
}
