package se.purplescout.pong.codetransfer;

import se.purplescout.pong.game.Paddle;

public interface NewPaddleListener {

    void newPaddle(Class<? extends Paddle> paddleClass, String code);
}
