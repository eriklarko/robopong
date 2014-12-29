package se.purplescout.pong.competition.headless;

import java.util.Map;
import se.purplescout.pong.game.Paddle;

public interface NewScoreListener {

    void newScores(Map<Paddle, Integer> scores);
}
