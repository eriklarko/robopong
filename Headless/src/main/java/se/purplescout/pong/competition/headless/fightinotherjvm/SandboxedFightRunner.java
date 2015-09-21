package se.purplescout.pong.competition.headless.fightinotherjvm;

import se.purplescout.pong.competition.headless.AutoFight;
import se.purplescout.pong.competition.jvm.ExecutionResult;
import se.purplescout.pong.competition.jvm.Jvm;
import se.purplescout.pong.competition.jvm.NewJvmListener;
import se.purplescout.pong.game.Paddle;
import se.purplescout.pong.game.examplepaddles.MoveToTopPaddle;
import se.purplescout.pong.game.examplepaddles.NopPaddle;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class SandboxedFightRunner {

    public static void main(String[] args) throws IOException, InterruptedException {
        FightResult fightResult = new SandboxedFightRunner().fightInOtherJvm(MoveToTopPaddle.class, NopPaddle.class, 10000);
        System.out.println(fightResult);
    }

    public static FightResult fightInOtherJvm(Class<? extends Paddle> left, Class<? extends Paddle> right, long millisToWait) throws IOException, InterruptedException {
        Semaphore sem = new Semaphore(0);
        ThreadLocal<ExecutionResult> result = new ThreadLocal<>();

        new Jvm(ClassThatExecutesFightInOtherJvm.class, millisToWait, new NewJvmListener() {
            @Override
            public void acceptOutputFromOtherJvm(ExecutionResult asd) {
                result.set(asd);
                sem.release();
            }

            @Override
            public void otherJvmTimedOut() {
                sem.release();
            }
        }, left.getCanonicalName(), right.getCanonicalName()).run();

        sem.acquireUninterruptibly();
        return parseOutput(result.get());
    }

    private static FightResult parseOutput(ExecutionResult result) {
        if (result == null) {
            return new FightResult(AutoFight.RESULT.FIGHT_TOOK_TOO_LONG, 0, 0);
        }
        if (result.getExitCode() != 0) {
            return new FightResult(AutoFight.RESULT.UNKNOWN_ERROR, 0, 0);
        }
        // TODO: Possibly check stderr here

        String[] parts = result.getStdout().split(System.lineSeparator());
        AutoFight.RESULT fightResult = AutoFight.RESULT.valueOf(parts[0]);
        int leftScore = Integer.parseInt(parts[1]);
        int rightScore = Integer.parseInt(parts[2]);

        return new FightResult(fightResult, leftScore, rightScore);
    }

    public static class FightResult {

        private final AutoFight.RESULT result;
        private final int leftScore, rightScore;

        public FightResult(AutoFight.RESULT result, int leftScore, int rightScore) {
            this.result = result;
            this.leftScore = leftScore;
            this.rightScore = rightScore;
        }

        public AutoFight.RESULT getResult() {
            return result;
        }

        public int getLeftScore() {
            return leftScore;
        }

        public int getRightScore() {
            return rightScore;
        }

        @Override
        public String toString() {
            return "FightResult{" +
                    "result=" + result +
                    ", leftScore=" + leftScore +
                    ", rightScore=" + rightScore +
                    '}';
        }
    }
}
