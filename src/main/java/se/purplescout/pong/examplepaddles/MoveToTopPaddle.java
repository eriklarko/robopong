package se.purplescout.pong.examplepaddles;

import se.purplescout.pong.game.GameRound;
import se.purplescout.pong.game.Paddle;

public class MoveToTopPaddle extends Paddle {

    @Override
    public void decideWhatToDoThisTick(GameRound context) {
        ploxxMoveMyCenterTo(0);
    }

    @Override
    public String getTeamName() {
        return "Move to top";
    }
}
