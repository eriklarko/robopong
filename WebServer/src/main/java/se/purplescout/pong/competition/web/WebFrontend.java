package se.purplescout.pong.competition.web;

import com.google.gson.Gson;
import se.purplescout.pong.competition.compiler.DynaCompTest;
import se.purplescout.pong.competition.compiler.InvalidSourceStringException;
import se.purplescout.pong.competition.compiler.JDKNotFoundException;
import se.purplescout.pong.competition.headless.NewPaddleListener;
import se.purplescout.pong.game.Paddle;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Scanner;

import static spark.Spark.*;
import static spark.SparkBase.port;

class WebFrontend {

    private final NewPaddleListener newPaddleListener;
    private final WebBasedServer server;

    public WebFrontend(NewPaddleListener newPaddleListener, WebBasedServer server) {
        this.newPaddleListener = newPaddleListener;
        this.server = server;
    }

    public void start() {
        port(1337);
        get("/", (req, res) -> "GET /highscore\nGET /upload\nPOST /upload");
        get("/highscore", "application/json", server::sendHighScore, new JsonTransformer());
        get("/upload", "application/html", this::exampleUploadForm);
        post("/upload", this::handleIncomingFile);
    }

    private String handleIncomingFile(Request request, Response response) throws IOException, ServletException, ClassNotFoundException, JDKNotFoundException, InstantiationException, InvalidSourceStringException, IllegalAccessException {
        Part filePart = getUploadedFilePart(request, "file");
        String code = convertStreamToString(filePart.getInputStream());

        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(response.raw().getOutputStream()))) {
            Class<Paddle> p = null;
            try {
                p = (Class<Paddle>) DynaCompTest.compile(code, printWriter);
            } catch (Exception e) {
                System.out.println("Could not compile the uploaded file. The response headers contains this information too.");
                e.printStackTrace();

                response.status(400);
                response.header("reason", "Could not compile the uploaded file");
                response.header("exception", e.toString());
                return "Could not compile the uploaded file.";
            }

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

    private String exampleUploadForm(Request request, Response response) {
        return "<html>" +
            "<body>" +
                "<form method=\"post\" action=\"/upload\" enctype=\"multipart/form-data\">" +
                    "Select file to upload: <input type=\"file\" name=\"file\" /><br />" +
                    "<br /><input type=\"submit\" value=\"Upload\" />" +
                "</form>" +
            "</body>" +
        "</html>";
    }

    public static class JsonTransformer implements ResponseTransformer {

        Gson gson = new Gson();

        @Override
        public String render(Object model) {
            return gson.toJson(model);
        }
    }
}
