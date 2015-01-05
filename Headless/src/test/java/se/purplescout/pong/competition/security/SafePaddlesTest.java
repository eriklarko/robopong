package se.purplescout.pong.competition.security;

import org.junit.Assert;
import org.junit.Test;
import se.purplescout.pong.competition.compiler.DynaCompTest;
import se.purplescout.pong.competition.paddlecache.PaddleCache;
import se.purplescout.pong.game.Paddle;

import java.io.PrintWriter;

public class SafePaddlesTest {

    @Test
    public void testEmptyPaddle() throws Exception {
        String code = "package hej; " +
                "public class ViolationInConstructor extends se.purplescout.pong.game.Paddle {" +
                "  public void decideWhatToDoThisTick(se.purplescout.pong.game.GameRound context) {}" +
                "  public String getTeamName() { return \"foo\";}" +
                "}";
        Class<Paddle> clazz = (Class<Paddle>) DynaCompTest.compile(code, new PrintWriter(System.err));
        PaddleCache.registerNewPaddle(clazz);
        Assert.assertEquals("foo", PaddleCache.getTeamName(clazz));
        Assert.assertNotNull(PaddleCache.getInstance(clazz));
    }
}
