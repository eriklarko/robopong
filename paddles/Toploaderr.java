package example;

import se.purplescout.pong.game.GameRound;
import se.purplescout.pong.game.Paddle;

public class TopLoader extends Paddle {

    @Override
    public void decideWhatToDoThisTick(GameRound context) {
        ploxxMoveMyCenterTo(60);
    }

    @Override
    public String getTeamName() {
        return "Toploaderr";
    }
}
