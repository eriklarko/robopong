package se.purplescout.pong.server;

import se.purplescout.pong.game.Paddle;

/**
* Created by eriklark on 12/22/14.
*/
public interface OnPaddleRemovedListener {

    void paddleRemoved(Paddle paddle);
}
