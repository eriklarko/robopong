package se.purplescout.pong.gui.client.paddle.classselector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import se.purplescout.pong.game.Paddle;

public class PaddleCache {

    private static final Map<Class<Paddle>, Paddle> instances = new HashMap<>();
    private static final Map<Class<Paddle>, String> teamNames = new HashMap<>();
    private static final ExecutorService executor = new ScheduledThreadPoolExecutor(1);

    public static void registerNewPaddle(Class<Paddle> clazz) throws NewInstanceException, GetTeamNameException, RegisterTimeoutException, IOException {
        // TODO: Test
        if (!instances.containsKey(clazz)) {
            instances.put(clazz, createNewInstance(clazz));
        }
    }

    private static Paddle createNewInstance(Class<Paddle> clazz) throws NewInstanceException, GetTeamNameException, RegisterTimeoutException, IOException {
        try {
            Paddle toReturn = executor.submit(() -> {
                Paddle instance;
                try {
                    instance = clazz.newInstance();
                } catch (Exception ex) {
                    throw new NewInstanceException(ex);
                }

                try {
                    teamNames.put(clazz, instance.getTeamName());
                } catch (Exception ex) {
                    throw new GetTeamNameException(ex);
                }

                return instance;
            }).get(1, TimeUnit.SECONDS);

            return toReturn;
        } catch (InterruptedException | ExecutionException ex) {
            throw new IOException(ex);
        } catch (TimeoutException ex) {
            throw new RegisterTimeoutException();
        }
    }

    public static String getTeamName(Class<? extends Paddle> clazz) {
        return teamNames.get(clazz);
    }

    public static Paddle getInstance(Class<Paddle> clazz) {
        return instances.get(clazz);
    }

    private PaddleCache() {
    }
}
