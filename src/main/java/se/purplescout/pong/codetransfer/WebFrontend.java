package se.purplescout.pong.codetransfer;

import com.google.gson.Gson;
import se.purplescout.pong.compiler.DynaCompTest;
import se.purplescout.pong.compiler.InvalidSourceStringException;
import se.purplescout.pong.compiler.JDKNotFoundException;
import se.purplescout.pong.game.Paddle;
import se.purplescout.pong.webserver.WebBasedServer;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Scanner;

import static spark.Spark.*;

public class WebFrontend {

    private final NewPaddleListener newPaddleListener;
    private final WebBasedServer server;

    public WebFrontend(NewPaddleListener newPaddleListener, WebBasedServer server) {
        this.newPaddleListener = newPaddleListener;
        this.server = server;
    }

    public void start() {
        port(1337);
        get("/", (req, res) -> "GET /highscore\nPOST /upload");
        get("/highscore", "application/json", server::sendHighScore, new JsonTransformer());
        post("/upload", this::handleIncomingFile);
    }

    private String handleIncomingFile(Request request, Response response) throws IOException, ServletException, ClassNotFoundException, JDKNotFoundException, InstantiationException, InvalidSourceStringException, IllegalAccessException {
        Part filePart = getUploadedFilePart(request, "file");
        String code = convertStreamToString(filePart.getInputStream());

        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(response.raw().getOutputStream()))) {
            Class<Paddle> p = (Class<Paddle>) DynaCompTest.compile(code, printWriter);
            if (newPaddleListener != null) {
                newPaddleListener.newPaddle(p, code);
            }
        }
        return "All ok";
    }

    private Part getUploadedFilePart(Request request, String name) throws IOException, ServletException {
        MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/tmp");
        request.raw().setAttribute("org.eclipse.multipartConfig", multipartConfigElement);

        return request.raw().getPart(name);
    }

    private String convertStreamToString(java.io.InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static class JsonTransformer implements ResponseTransformer {

        Gson gson = new Gson();

        @Override
        public String render(Object model) {
            return gson.toJson(model);
        }
    }
}
