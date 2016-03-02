package se.purplescout.pong.competition.headless.fightinotherjvm;

import se.purplescout.pong.competition.headless.AutoFight;
import se.purplescout.pong.competition.paddlecache.GetTeamNameException;
import se.purplescout.pong.competition.paddlecache.NewInstanceException;
import se.purplescout.pong.competition.paddlecache.PaddleCache;
import se.purplescout.pong.competition.paddlecache.RegisterTimeoutException;
import se.purplescout.pong.game.Paddle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

class ClassThatExecutesFightInOtherJvm {

    public static void main(String[] args) throws ClassNotFoundException, RegisterTimeoutException, GetTeamNameException, NewInstanceException, IOException {
        PrintStream oldStdOut = System.out;
        PrintStream oldStdErr = System.err;
        redirectStdStreams();

        String leftPaddleClassName = args[0];
        String rightPaddleClassName = args[1];

        Paddle left = loadPaddle(leftPaddleClassName);
        Paddle right = loadPaddle(rightPaddleClassName);
        if (left == null || right == null) {
            System.err.println("Some fight pair contained nulls :/");
            System.exit(1);
        }

        AutoFight autoFight = new AutoFight(left, right);
        autoFight.run();

        // Restore the standard streams again
        System.setOut(oldStdOut);
        System.setErr(oldStdErr);
        System.out.println(autoFight.getResult());
        System.out.println(autoFight.getLeftScore());
        System.out.println(autoFight.getRightScore());
        System.exit(0);
    }

    private static Paddle loadPaddle(String className) throws RegisterTimeoutException, GetTeamNameException, NewInstanceException, IOException, ClassNotFoundException {
        Class<Paddle> paddleClass = (Class<Paddle>) Class.forName(className);
        PaddleCache.registerNewPaddle(paddleClass);

        return PaddleCache.getInstance(paddleClass);
    }

    /**
     * We don't want any pesky System.out.print to mess with our pretty output format.
     */
    private static void redirectStdStreams() {
        PrintStream stream = new PrintStream(new ByteArrayOutputStream());
        System.setOut(stream);
        System.setErr(stream);
    }
}
