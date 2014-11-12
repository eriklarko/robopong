package se.purplescout.pong.game;

public interface SomeoneScoredListener {

    enum PLAYER {
        LEFT, RIGHT
    }

    void someoneScored(PLAYER player, String name, Paddle paddle);
}
