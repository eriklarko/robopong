package se.purplescout.pong.competition.lan.util;


import java.io.IOException;
import javafx.fxml.FXMLLoader;

/**
 * Boilerplate code for creating custom FX-controls
 */
public final class CustomControl {

    /**
     * Thrown if the FXMLLoader was unable to load the resource. It takes the
     * IOException thrown by FXMLLoader and converts in into an unchecked
     * exception.
     */
    public static class FXMLLoadException extends RuntimeException {

        public FXMLLoadException() {
            super();
        }

        public FXMLLoadException(String message) {
            super(message);
        }

        public FXMLLoadException(String message, Throwable cause) {
            super(message, cause);
        }

        public FXMLLoadException(Throwable cause) {
            super(cause);
        }

        public FXMLLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    /**
     * Sets up the controller to use a fxml-file in the same package named as
     * the controller without the word "Controller".
     *
     * Ex. RecipeDetailsController in se.angstroms.anders.recipe.details looks
     * for an fxml-file named RecipeDetails.fxml in
     * se.angstroms.anders.recipe.details
     *
     * @param instance
     */
    public static void setup(Object instance) {
        Class<?> clazz = instance.getClass();
        String className = clazz.getSimpleName();
        String fxmlFileName = className.replace("Presenter", "") + ".fxml";
        setup(instance, fxmlFileName);
    }

    public static void setup(Object instance, String fxmlPath) {
        FXMLLoader fxmlLoader = new FXMLLoader(ResourceLoader.getResource(instance.getClass(), fxmlPath));
        setup(instance, fxmlLoader);
    }

    public static void setup(Object instance, FXMLLoader fxmlLoader) {
        fxmlLoader.setRoot(instance);
        fxmlLoader.setController(instance);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new FXMLLoadException("Unable to load: " + fxmlLoader.getLocation(), exception);
        }
    }

    private CustomControl() {
    }
}
