package rnd;

import se.purplescout.pong.game.GameRound;
import se.purplescout.pong.game.Paddle;

public class HJK extends Paddle {

    @Override
    public void decideWhatToDoThisTick(GameRound context) {
        
        if (context.getBallVelocity().getX() > 0) {
            
            ploxxMoveMyCenterTo(context.getBoardSize().getHeight()/2);
        } else {                     
                       
            ploxxMoveMyCenterTo(context.getBallPosition().getY());
        }
        
        
           
        
        
    }

    @Override
    public String getTeamName() {
        return "Vitalii Iarko";
    }
}
