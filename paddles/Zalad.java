package rnd;

import se.purplescout.pong.game.collision.Rectangle;
import se.purplescout.pong.game.collision.Vector;
import se.purplescout.pong.game.GameRound;
import se.purplescout.pong.game.Paddle;

public class PaddleController extends Paddle {

    @Override
    public void decideWhatToDoThisTick(GameRound context) {
        
        movePaddleLowerToBallEndPoint(context);
        
    }

    @Override
    public String getTeamName() {
        return "Zalad";
    }

    private void movePaddleLowerToBallEndPoint(GameRound context) {
        Vector ballPosition = context.getBallPosition();
        Vector ballDirection = context.getBallVelocity();
        
        Rectangle myBound = this.getBoundingBox();
        double myX = myBound.getCenterX();
        double xDiff = ballPosition.getX()-myX;
        double yPerX = ballDirection.getY()/ballDirection.getX();
        double yDiff = xDiff*yPerX;
        double finalY = ballPosition.getY()+yDiff;
        ploxxMoveMyCenterTo(finalY);
    }
    
    
}
