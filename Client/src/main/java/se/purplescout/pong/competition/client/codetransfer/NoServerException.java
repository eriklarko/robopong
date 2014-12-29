package se.purplescout.pong.competition.client.codetransfer;

public class NoServerException extends Exception {

    public NoServerException() {
        super("No server found");
    }
}
