package rnd;

import se.purplescout.pong.game.collision.Vector;
import se.purplescout.pong.game.GameRound;
import se.purplescout.pong.game.Paddle;

public class HJKLÖ extends Paddle {

    Vector previousVec;
    @Override
    public void decideWhatToDoThisTick(GameRound context) {
        Vector v = context.getBallPosition();
        boolean movingRight;
        if(previousVec != null) {
            if(previousVec.getX() - v.getX() > 0) {
                movingRight = false;
                ploxxMoveMyCenterTo(context.getBoardSize().getHeight()/2);
            }
            else {
                ploxxMoveMyCenterTo(context.getBallPosition().getY());
            }
            previousVec = v;
        }
        else {
            previousVec = v;
        }
        
        
    }

    @Override
    public String getTeamName() {
        return "Roberts Kanoter";
    }
}
