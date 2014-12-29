package se.purplescout.pong.competition.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import se.purplescout.pong.competition.headless.NoLogDirectoryException;
import se.purplescout.pong.game.Paddle;
import se.purplescout.pong.competition.paddlecache.PaddleCache;
import se.purplescout.pong.competition.lan.server.HighScore;
import se.purplescout.pong.competition.headless.AutoFightHandler;
import se.purplescout.pong.competition.headless.AutoFight;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class WebBasedServer extends AutoFightHandler {

    private final WebFrontend webFrontend;
    private final AtomicReference<SortedMap<String, Integer>> lastResults;

    public static void main(String[] args) {
        new WebBasedServer().initialize();
    }

    public WebBasedServer() {
        webFrontend = new WebFrontend(this, this);
        lastResults = new AtomicReference<>();
    }

    @Override
    protected void enableReceivingPaddles() throws IOException {
        webFrontend.start();
    }

    @Override
    public void fightRoundDone(List<AutoFight> fights) {
        SortedMap<Paddle, Integer> highScore = HighScore.calculateAndSortScoresForEachPaddle(fights);
        SortedMap<String, Integer> highScoreWithPaddleNames = convertKeys((Paddle paddle) -> PaddleCache.getTeamName(paddle.getClass()), highScore);
        lastResults.set(highScoreWithPaddleNames);
    }

    private SortedMap<String, Integer> convertKeys(Function<Paddle, String> toString, SortedMap<Paddle, Integer> highScore) {
        SortedMap<String, Integer> converted = new TreeMap<>();
        for (Map.Entry<Paddle, Integer> entry : highScore.entrySet()) {
            String key = toString.apply(entry.getKey());
            converted.put(key, entry.getValue());
        }

        return converted;
    }

    public JsonObject sendHighScore(Request request, Response response) {
        JsonObject json = new JsonObject();
        json.addProperty("retry-soon", newResultsShouldComeSoon());
        json.add("highscore", new Gson().toJsonTree(lastResults.get()));

        return json;
    }

    private boolean newResultsShouldComeSoon() {
        return getFightQueue().getNumberOfQueuedRounds() > 0 || getFightQueue().roundIsPlayingOut();
    }
}
