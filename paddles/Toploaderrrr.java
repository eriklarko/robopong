package example;

import se.purplescout.pong.game.GameRound;
import se.purplescout.pong.game.Paddle;

public class TopLoader extends Paddle {

    @Override
    public void decideWhatToDoThisTick(GameRound context) {
        ploxxMoveMyCenterTo(190);
    }

    @Override
    public String getTeamName() {
        return "Toploaderrrr";
    }
}
