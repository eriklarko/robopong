package se.purplescout.pong.competition.jvm;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class JvmTest {

    public static void main(String[] args) throws Exception {
        new JvmTest().testException();
    }

    @Test
    public void testHappyPath() throws IOException, InterruptedException {

        String firstArg = "a";
        String secondArg = "b";

        NewJvmListener jvmListener = new NewJvmListener() {
            @Override
            public void acceptOutputFromOtherJvm(ExecutionResult result) {
                String[] lines = result.getStdout().split(System.lineSeparator());
                Assert.assertEquals("Returned wrong number of lines", 2, lines.length);
                Assert.assertEquals("First arg was wrong", lines[0], firstArg);
                Assert.assertEquals("Second arg was wrong", lines[1], secondArg);
            }

            @Override
            public void otherJvmTimedOut() {
                Assert.fail("The execution timed out");
            }
        };

        new Jvm(ClassThatGetsRunInOtherJvm_AndExitsNormally.class, 1000, jvmListener, firstArg, secondArg).run();
    }

    @Test
    public void testNonzeroExitCode() throws IOException, InterruptedException {

        NewJvmListener jvmListener = new NewJvmListener() {
            @Override
            public void acceptOutputFromOtherJvm(ExecutionResult result) {
                Assert.assertEquals(1, result.getExitCode());
            }

            @Override
            public void otherJvmTimedOut() {
                Assert.fail("The execution timed out");
            }
        };

        new Jvm(ClassThatGetsRunInOtherJvm_AndExitsAbnormally.class, 1000, jvmListener).run();
    }

    @Test
    public void testException() throws IOException, InterruptedException {

        NewJvmListener jvmListener = new NewJvmListener() {
            @Override
            public void acceptOutputFromOtherJvm(ExecutionResult result) {
                Assert.assertTrue(result.getStderr().contains("Exception"));
                Assert.assertEquals(1, result.getExitCode());
            }

            @Override
            public void otherJvmTimedOut() {
                Assert.fail("The execution timed out");
            }
        };

        new Jvm(ClassThatGetsRunInOtherJvm_AndThrowsError.class, 1000, jvmListener).run();
    }

    @Test
    public void testTimeOut() throws IOException, InterruptedException {

        NewJvmListener jvmListener = new NewJvmListener() {
            @Override
            public void acceptOutputFromOtherJvm(ExecutionResult result) {
                Assert.fail("The execution did not time out");
            }

            @Override
            public void otherJvmTimedOut() {
                System.out.println("Oh yeah");
            }
        };

        new Jvm(ClassThatGetsRunInOtherJvm_AndTimesOut.class, 1000, jvmListener).run();
    }
}
