package se.purplescout.pong.server.highscore;

import java.util.Map;
import se.purplescout.pong.game.Paddle;

public interface NewScoreListener {

    void newScores(Map<Paddle, Integer> scores);
}
