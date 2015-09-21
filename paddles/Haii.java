package example;

import se.purplescout.pong.game.GameRound;
import se.purplescout.pong.game.Paddle;

public class TopLoader extends Paddle {

    @Override
    public void decideWhatToDoThisTick(GameRound context) {
        if (!this.willMoveThisTick())
        ploxxMoveMyCenterTo((Math.random() * context.getBoardSize().getHeight()) / 2);
    }

    @Override
    public String getTeamName() {
        return "Haii";
    }
}
