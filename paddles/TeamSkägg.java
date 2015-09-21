package edu.chl.wmax;


import se.purplescout.pong.game.GameRound;
import se.purplescout.pong.game.Paddle;
import se.purplescout.pong.game.collision.Vector;


import java.awt.*;
import java.awt.geom.Point2D;

public class Main extends Paddle {

    double ballMaxY = 394;
    double ballMaxX = 854;
    double ballMinX = 45;

    public static void main(String[] args) {
	// write your code here
    }

    Point2D.Double lastPos;
    Point2D.Double currentPos;

    @Override
    public void decideWhatToDoThisTick(GameRound gameRound) {
        Vector ballPosition = gameRound.getBallPosition();
        Vector ballVelocity = gameRound.getBallVelocity();
        double myX;
        if(gameRound.getOtherPaddlePosition().getX() > 450) {
            myX = ballMinX;
            if(gameRound.getBallVelocity().getX() < 0)
                ploxxMoveMyCenterTo(getHitPoint(gameRound.getBallPosition(),
                    gameRound.getBallVelocity(), myX) + (10 * Math.random() - 5));
        } else {
            myX = ballMaxX;
            if(gameRound.getBallVelocity().getX() > 0)
                ploxxMoveMyCenterTo(getHitPoint(gameRound.getBallPosition(),
                    gameRound.getBallVelocity(), myX) + (10 * Math.random() - 5));
        }

        // System.out.println(getHitPoint(gameRound.getBallPosition(), gameRound.getBallVelocity(), myX));

    }

    public double getAngle(Vector velocity) {
        return (velocity.getY()/Math.abs(velocity.getX()));
    }

    public double getHitPoint(Vector bollPos, Vector velocity, double myPos) {

        double dist = Math.abs(bollPos.getX() - myPos);

        double hitPos = bollPos.getY() + (dist*getAngle(velocity));

        while(hitPos < - ballMaxY) {
            hitPos += 2* ballMaxY;
        }

        while(hitPos > 2* ballMaxY) {
            hitPos -= 2* ballMaxY;
        }

        if(hitPos < 0) {
            hitPos = -hitPos;
        }

        if(hitPos > ballMaxY) {
            hitPos = ballMaxY - (hitPos - ballMaxY);
        }

        return hitPos;
    }


    @Override
    public String getTeamName() {
        return "TeamSkägg";
    }
}
