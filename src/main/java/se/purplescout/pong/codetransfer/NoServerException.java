package se.purplescout.pong.codetransfer;

public class NoServerException extends Exception {

    public NoServerException() {
        super("No server found");
    }
}
