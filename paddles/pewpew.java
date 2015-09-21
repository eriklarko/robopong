package rnd;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import se.purplescout.pong.game.GameRound;
import se.purplescout.pong.game.Paddle;

public class FGFG extends Paddle {
    public boolean colliding_sides;
    
    int cycle = 0;
    se.purplescout.pong.game.collision.Vector position1, position2;
    GameRound context;
    @Override
    public void decideWhatToDoThisTick(GameRound context) {
        ploxxMoveMyCenterTo(0);
        this.context = context;
        position1 = context.getBallPosition();
        ploxxMoveMyCenterTo(position1.getY());
    }

/*    public synchronized void thread0(){
        position2 = context.getBallPosition();
        ploxxMoveMyCenterTo(position2.getY());
        
    }
*/    
    @Override
    public String getTeamName() {
        return "pewpew";
    }
}
