package rnd;

import se.purplescout.pong.game.collision.Vector;
import se.purplescout.pong.game.GameRound;
import se.purplescout.pong.game.Paddle;


public class NewClass extends Paddle {

    @Override
    public void decideWhatToDoThisTick(GameRound context) {
        
        double them = context.getOtherPaddlePosition().getX();
        Vector v = context.getBallPosition();
        if(v.getX()>2)
        
        
        ploxxMoveMyCenterTo(v.getY()-3);
    }

    @Override
    public String getTeamName() {
        return "Best paddel NA";
    }
}
