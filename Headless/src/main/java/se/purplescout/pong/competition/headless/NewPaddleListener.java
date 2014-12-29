package se.purplescout.pong.competition.headless;

import se.purplescout.pong.game.Paddle;

public interface NewPaddleListener {

    void newPaddle(Class<? extends Paddle> paddleClass, String code);
}
