package se.purplescout.pong.gui.util;


public class ToDisplay extends RuntimeException {

    public ToDisplay(String message) {
        super(message);
    }

    public ToDisplay(String message, Throwable cause) {
        super(message, cause);
    }
}
