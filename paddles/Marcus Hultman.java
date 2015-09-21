package rnd;

import se.purplescout.pong.game.collision.Vector;
import se.purplescout.pong.game.GameRound;
import se.purplescout.pong.game.Paddle;

public class NewClass1 extends Paddle {
    
    @Override
    public void decideWhatToDoThisTick(GameRound context) {
        Vector ballPosition = context.getBallPosition();
        Vector ballVelocity = context.getBallVelocity();
        double b0pos = ballPosition.getY() + ballVelocity.getY() * ballPosition.getX() / ballVelocity.getX();
        
        double targetAngle = Math.tan(b0pos  / context.getBoardSize().getWidth());
        double targetOffset = this.getBoundingBox().getHeight() / 2.0 *  targetAngle / 60.0;
        
        double playerTarget = b0pos + targetOffset;
            
        ploxxMoveMyCenterTo(playerTarget);
    }
    
    
    @Override
    public String getTeamName() {
        return "Marcus Hultman";
    }
}
