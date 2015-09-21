package se.purplescout.pong.competition.headless.fightinotherjvm;

import se.purplescout.pong.competition.headless.AutoFight;
import se.purplescout.pong.competition.paddlecache.GetTeamNameException;
import se.purplescout.pong.competition.paddlecache.NewInstanceException;
import se.purplescout.pong.competition.paddlecache.PaddleCache;
import se.purplescout.pong.competition.paddlecache.RegisterTimeoutException;
import se.purplescout.pong.game.Paddle;

import java.io.IOException;

class ClassThatExecutesFightInOtherJvm {

    public static void main(String[] args) throws ClassNotFoundException, RegisterTimeoutException, GetTeamNameException, NewInstanceException, IOException {
        String leftPaddleClassName = args[0];
        String rightPaddleClassName = args[1];

        Class<Paddle> leftPaddle = (Class<Paddle>) Class.forName(leftPaddleClassName);
        Class<Paddle> rightPaddle = (Class<Paddle>) Class.forName(rightPaddleClassName);

        PaddleCache.registerNewPaddle(leftPaddle);
        PaddleCache.registerNewPaddle(rightPaddle);

        Paddle left = PaddleCache.getInstance(leftPaddle);
        Paddle right = PaddleCache.getInstance(rightPaddle);
        if (left == null || right == null) {
            System.err.println("Some fight pair contained nulls :/");
            System.exit(1);
        }

        AutoFight autoFight = new AutoFight(left, right);
        autoFight.run();

        System.out.println(autoFight.getResult());
        System.out.println(autoFight.getLeftScore());
        System.out.println(autoFight.getRightScore());
        System.exit(0);
    }
}
