package se.purplescout.pong.game.examplepaddles;

import se.purplescout.pong.game.GameRound;
import se.purplescout.pong.game.Paddle;

public class NopPaddle extends Paddle {

    private final String name;

    public NopPaddle() {
        this("asd");
    }

    public NopPaddle(String name) {
        this.name = name;
    }

    @Override
    public void decideWhatToDoThisTick(GameRound context) {
        ploxxMoveMyCenterTo(context.getBallPosition().getY());
    }

    @Override
    public String getTeamName() {
        return name;
    }
}
