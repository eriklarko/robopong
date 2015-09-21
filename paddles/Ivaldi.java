package example;

import se.purplescout.pong.game.collision.Vector;
import se.purplescout.pong.game.GameRound;
import se.purplescout.pong.game.Paddle;

public class IvaldiPaddle extends Paddle {
    private Double lastPos = 0.0;
    
    @Override
    public void decideWhatToDoThisTick(GameRound context) {
        Vector ballpos = context.getBallPosition();
        Vector velocity = context.getBallVelocity();
        Double newPos = ballpos.getY() + (ballpos.getY() - lastPos) * velocity.getY();
        ploxxMoveMyCenterTo(newPos);
        lastPos = newPos;
    }

    @Override
    public String getTeamName() {
        return "Ivaldi";
    }
}
