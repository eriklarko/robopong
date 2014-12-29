package se.purplescout.pong.competition.headless;

import se.purplescout.pong.game.Paddle;

import java.util.Map;

public interface NewScoreListener {

    void newScores(Map<Paddle, Integer> scores);
}
