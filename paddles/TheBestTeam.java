package rnd;

import java.util.Random;
import se.purplescout.pong.game.collision.Rectangle;
import se.purplescout.pong.game.collision.Vector;
import se.purplescout.pong.game.GameRound;
import se.purplescout.pong.game.Paddle;

public class MyPaddle extends Paddle {

    @Override
    public void decideWhatToDoThisTick(GameRound context) {
        Vector ballPosition = context.getBallPosition();
        Vector ballDirection = context.getBallVelocity();
        Random rnd = new Random(System.currentTimeMillis());
        
        Rectangle myBound = this.getBoundingBox();
       double otherVel = context.getOtherPaddleVelocity().getY();
       
        double myX = myBound.getCenterX();
        double xDiff = ballPosition.getX()-myX;
        double yPerX = ballDirection.getY()/ballDirection.getX();
        double yDiff = xDiff*yPerX + (otherVel * -1);
        double finalY = ballPosition.getY()+yDiff;
        ploxxMoveMyCenterTo(finalY);
        
    }

    @Override
    public String getTeamName() {
        return "TheBestTeam";
    }
}
