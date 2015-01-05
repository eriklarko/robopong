package se.purplescout.pong.competition.security;

import org.junit.Assert;
import org.junit.BeforeClass;
import se.purplescout.pong.competition.compiler.DynaCompTest;
import se.purplescout.pong.competition.paddlecache.PaddleCache;
import se.purplescout.pong.game.Paddle;

import java.io.PrintWriter;
import java.security.Policy;
import java.util.function.Function;

public class SecurityTest {

    public static final SecurityManager SECURITY_MANAGER = new SecurityManager();

    @BeforeClass
    public static void enableSecurity() {
        Policy.setPolicy(new PongPolicy());
    }

    public void assertThrowables(SecureTestResult result, Class<? extends Throwable> throwable) {
        assertThrowables(result, throwable, throwable, throwable);
    }

    public void assertThrowables(SecureTestResult result, Class<? extends Throwable> constructor, Class<? extends Throwable> getName, Class<? extends Throwable> tick) {
        assertThrownException("constructor", result.thrownInConstructor(), constructor);
        assertThrownException("getName", result.thrownInGetName(), getName);
        assertThrownException("tick", result.thrownInTick(), tick);
    }

    private void assertThrownException(String place, Throwable thrown, Class<? extends Throwable> expected) {
        if (expected == null) {
            Assert.assertNull(place + " failed", thrown);
        } else {
            Assert.assertNotNull(place + " did not throw exception", thrown);
            Assert.assertEquals(place + " failed", expected, thrown.getClass());
        }
    }

    public SecureTestResult doSecurityTest(String violation) {
        Throwable constructor = runAndGetFirstThrowable(() -> testViolationInConstructor(violation));
        Throwable getName = runAndGetFirstThrowable(() -> testViolationInGetName(violation));
        Throwable tick = runAndGetFirstThrowable(() -> testViolationInTick(violation));

        return new SecureTestResult(constructor, getName, tick);
    }

    private Throwable runAndGetFirstThrowable(Lol callable) {
        try {
            callable.run();
        } catch (Throwable t) {
            return rootCause(t);
        }

        return null;
    }

    private Throwable rootCause(Throwable e) {
        Throwable rootCause = e;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

    private void testViolationInConstructor(String violation) throws Exception {
        try {
            Class<Paddle> cp = compile(this::violationInConstructor, violation);
            System.setSecurityManager(SECURITY_MANAGER);
            PaddleCache.registerNewPaddle(cp);
        } finally {
            System.setSecurityManager(null);
        }
    }

    private void testViolationInGetName(String violation) throws Exception {
        try {
            Class<Paddle> cp = compile(this::violationInGetName, violation);
            System.setSecurityManager(SECURITY_MANAGER);
            PaddleCache.registerNewPaddle(cp);
        } finally {
            System.setSecurityManager(null);
        }
    }

    private void testViolationInTick(String violation) throws Exception {
        try {
            Class<Paddle> cp = compile(this::violationInTick, violation);
            System.setSecurityManager(SECURITY_MANAGER);
            PaddleCache.registerNewPaddle(cp);
            PaddleCache.getInstance(cp).decideWhatToDoThisTick(null);
        } finally {
            System.setSecurityManager(null);
        }
    }

    private Class<Paddle> compile(Function<String, String> f, String violation) {
        String code = f.apply(violation);
        try {
            Class<?> clazz = DynaCompTest.compile(code, new PrintWriter(System.err));
            return (Class<Paddle>) clazz;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String violationInConstructor(String violation) {
        return "package hej; " +
                "public class ViolationInConstructor extends se.purplescout.pong.game.Paddle {" +
                "  public ViolationInConstructor() { " + violation + "}" +
                "  public void decideWhatToDoThisTick(se.purplescout.pong.game.GameRound context) {}" +
                "  public String getTeamName() { return \"\";}" +
                "}";
    }

    private String violationInGetName(String violation) {
        return "package hej; " +
                "public class ViolationInGetName extends se.purplescout.pong.game.Paddle {" +
                "  public void decideWhatToDoThisTick(se.purplescout.pong.game.GameRound context) {}" +
                "  public String getTeamName() { " + violation + "return \"\";}" +
                "}";
    }

    private String violationInTick(String violation) {
        return "package hej; " +
                "public class ViolationInGetName extends se.purplescout.pong.game.Paddle {" +
                "  public void decideWhatToDoThisTick(se.purplescout.pong.game.GameRound context) {" + violation + "}" +
                "  public String getTeamName() { return \"\";}" +
                "}";
    }

    public static class SecureTestResult {
        private final Throwable constructor, getName, tick;

        public SecureTestResult(Throwable constructor, Throwable getName, Throwable tick) {
            this.constructor = constructor;
            this.getName = getName;
            this.tick = tick;
        }

        public Throwable thrownInConstructor() {
            return constructor;
        }

        public Throwable thrownInGetName() {
            return getName;
        }

        public Throwable thrownInTick() {
            return tick;
        }
    }

    private static interface Lol {
         void run() throws Exception;
    }
}
