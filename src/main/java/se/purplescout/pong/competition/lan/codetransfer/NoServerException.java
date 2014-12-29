package se.purplescout.pong.competition.lan.codetransfer;

public class NoServerException extends Exception {

    public NoServerException() {
        super("No server found");
    }
}
