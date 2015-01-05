package se.purplescout.pong.competition.paddlecache;

import se.purplescout.pong.game.Paddle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class PaddleCache {

    private static final Map<Class<Paddle>, Paddle> instances = new HashMap<>();
    private static final Map<Class<Paddle>, String> teamNames = new HashMap<>();
    private static final ExecutorService executor = new ScheduledThreadPoolExecutor(1);

    public static void registerNewPaddle(Class<Paddle> clazz) throws NewInstanceException, GetTeamNameException, RegisterTimeoutException, IOException {
        if (!instances.containsKey(clazz)) {
            instances.put(clazz, createNewInstance(clazz));
        }
    }

    private static Paddle createNewInstance(Class<Paddle> clazz) throws NewInstanceException, GetTeamNameException, RegisterTimeoutException, IOException {
        try {
            Paddle toReturn = executor.submit(() -> {
                Paddle instance = createPaddleInstance(clazz);
                registerPaddleName(clazz, instance);
                return instance;
            }).get(1, TimeUnit.SECONDS);

            return toReturn;
        } catch (InterruptedException | ExecutionException ex) {
            throw new IOException(ex);
        } catch (TimeoutException ex) {
            throw new RegisterTimeoutException();
        }
    }

    private static Paddle createPaddleInstance(Class<Paddle> clazz) throws NewInstanceException {
        Paddle instance;
        try {
            instance = clazz.newInstance();
        } catch (Exception ex) {
            throw new NewInstanceException(ex);
        }
        return instance;
    }

    private static void registerPaddleName(Class<Paddle> clazz, Paddle instance) throws GetTeamNameException {
        try {
            teamNames.put(clazz, instance.getTeamName());
        } catch (Exception ex) {
            throw new GetTeamNameException(ex);
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
